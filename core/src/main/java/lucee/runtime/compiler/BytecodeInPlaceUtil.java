package lucee.runtime.compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PrivateKey;

import lucee.commons.digest.RSA;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.compiler.CFMLCompilerImpl.Result;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.transformer.bytecode.PageImpl;

public final class BytecodeInPlaceUtil {

	private static final byte BCF = (byte) 207;
	private static final byte B33 = (byte) 51;

	private static final ExtensionResourceFilter FILTER_CFML = new ExtensionResourceFilter(Constants.getExtensions());

	private BytecodeInPlaceUtil() {}

	public static Struct compile(PageContext pc, Resource path, boolean keepSource, String privateKey, boolean recursive) throws PageException {
		if (!ResourceUtil.exists(path)) throw new ApplicationException("path [" + path + "] does not exist");

		Struct result = new StructImpl(Struct.TYPE_LINKED);
		Array compiled = new ArrayImpl();
		Array skipped = new ArrayImpl();
		Struct errors = new StructImpl(Struct.TYPE_LINKED);
		Array warnings = new ArrayImpl();

		result.set("compiled", compiled);
		result.set("skipped", skipped);
		result.set("errors", errors);
		result.set("warnings", warnings);

		if (path.isDirectory()) processDirectory(pc, path, keepSource, privateKey, recursive, compiled, skipped, errors, warnings);
		else processFile(pc, path, keepSource, privateKey, compiled, skipped, errors, warnings);

		return result;
	}

	private static void processDirectory(PageContext pc, Resource dir, boolean keepSource, String privateKey, boolean recursive, Array compiled, Array skipped, Struct errors,
			Array warnings) throws PageException {
		Resource[] files = dir.listResources();
		if (files == null) return;

		for (int i = 0; i < files.length; i++) {
			Resource file = files[i];
			if (file.isDirectory()) {
				if (recursive) processDirectory(pc, file, keepSource, privateKey, recursive, compiled, skipped, errors, warnings);
			}
			else if (file.isFile()) {
				processFile(pc, file, keepSource, privateKey, compiled, skipped, errors, warnings);
			}
		}
	}

	private static void processFile(PageContext pc, Resource file, boolean keepSource, String privateKey, Array compiled, Array skipped, Struct errors, Array warnings)
			throws PageException {
		String path = file.getAbsolutePath();
		String name = file.getName();

		if (isDotPrefixed(file)) {
			skipped.append(path);
			return;
		}

		if (!FILTER_CFML.accept(file)) {
			skipped.append(path);
			return;
		}

		try {
			if (ClassUtil.isBytecode(IOUtil.toBytes(file))) {
				skipped.append(path);
				return;
			}
		}
		catch (IOException e) {
			errors.setEL(path, Caster.toPageException(e).getCatchBlock(pc.getConfig()));
			return;
		}

		ConfigPro config = (ConfigPro) pc.getConfig();
		PageSourceImpl ps = (PageSourceImpl) ConfigUtil.toPageSource(config, null, file, null);
		if (ps == null) {
			errors.setEL(path, "cannot resolve file to a mapping");
			return;
		}

		Mapping mapping = ps.getMapping();

		try {
			ps.clear();
			ps.flush();

			ConfigWebPro cwi = (ConfigWebPro) config;
			Result compileResult = cwi.getCompiler().compile(cwi, ps, cwi.getTLDs(), cwi.getFLDs(), mapping.getClassRootDirectory(), false, false);

			if (compileResult.javaFunctions != null && !compileResult.javaFunctions.isEmpty()) {
				warnings.append("file [" + path + "] produced auxiliary classes stored in cfclasses; deploy the cfclasses directory with the template");
			}

			long srcLastModified = file.lastModified();
			byte[] barr = PageImpl.setSourceLastModified(compileResult.barr, srcLastModified);

			if (!StringUtil.isEmpty(privateKey, true)) barr = encryptBytecode(barr, privateKey.trim());

			if (keepSource) {
				Resource backup = file.getParentResource().getRealResource("." + name);
				if (backup.exists()) throw new ApplicationException("backup file already exists [" + backup.getAbsolutePath() + "]");

				ResourceUtil.copy(file, backup);
			}

			IOUtil.copy(new ByteArrayInputStream(barr), file, true);
			file.setLastModified(srcLastModified);

			ps.flush();
			compiled.append(path);
		}
		catch (Exception e) {
			PageException pe = Caster.toPageException(e);
			errors.setEL(path, pe.getCatchBlock(config));
			LogUtil.log("compile", e);
		}
	}

	private static boolean isDotPrefixed(Resource file) {
		String name = file.getName();
		if (name.startsWith(".")) return true;
		Resource parent = file.getParentResource();
		return parent != null && parent.getRealResource("." + name).equals(file);
	}

	private static byte[] encryptBytecode(byte[] bytecode, String privateKey) throws Exception {
		PrivateKey key = RSA.toPrivateKey(privateKey);
		byte[] encrypted = RSA.encrypt(bytecode, key);
		byte[] result = new byte[encrypted.length + 2];
		result[0] = BCF;
		result[1] = B33;
		System.arraycopy(encrypted, 0, result, 2, encrypted.length);
		return result;
	}

	public static Struct restore(PageContext pc, Resource path, boolean recursive) throws PageException {
		if (!ResourceUtil.exists(path)) throw new ApplicationException("path [" + path + "] does not exist");

		Struct result = new StructImpl(Struct.TYPE_LINKED);
		Array restored = new ArrayImpl();
		Array skipped = new ArrayImpl();
		Struct errors = new StructImpl(Struct.TYPE_LINKED);

		result.set("restored", restored);
		result.set("skipped", skipped);
		result.set("errors", errors);

		if (path.isDirectory()) restoreDirectory(pc, path, recursive, restored, skipped, errors);
		else restorePath(pc, path, restored, skipped, errors);

		return result;
	}

	private static void restoreDirectory(PageContext pc, Resource dir, boolean recursive, Array restored, Array skipped, Struct errors) throws PageException {
		Resource[] files = dir.listResources();
		if (files == null) return;

		for (int i = 0; i < files.length; i++) {
			Resource file = files[i];
			if (file.isDirectory()) {
				if (recursive) restoreDirectory(pc, file, recursive, restored, skipped, errors);
			}
			else if (file.isFile()) {
				restorePath(pc, file, restored, skipped, errors);
			}
		}
	}

	private static void restorePath(PageContext pc, Resource file, Array restored, Array skipped, Struct errors) throws PageException {
		if (isDotPrefixed(file)) {
			Resource publicFile = getPublicFile(file);
			if (publicFile == null || !publicFile.exists()) {
				skipped.append(file.getAbsolutePath());
				return;
			}
			restorePair(pc, publicFile, restored, skipped, errors);
			return;
		}

		if (!FILTER_CFML.accept(file)) {
			skipped.append(file.getAbsolutePath());
			return;
		}

		restorePair(pc, file, restored, skipped, errors);
	}

	private static void restorePair(PageContext pc, Resource publicFile, Array restored, Array skipped, Struct errors) throws PageException {
		String path = publicFile.getAbsolutePath();
		Resource backup = getBackupFile(publicFile);

		if (!backup.exists()) {
			skipped.append(path);
			return;
		}

		try {
			if (!ClassUtil.isBytecode(IOUtil.toBytes(publicFile))) {
				skipped.append(path);
				return;
			}
			if (ClassUtil.isBytecode(IOUtil.toBytes(backup))) {
				errors.setEL(path, "dot-prefixed file [" + backup.getAbsolutePath() + "] is not a CFML source file");
				return;
			}
		}
		catch (IOException e) {
			errors.setEL(path, Caster.toPageException(e).getCatchBlock(pc.getConfig()));
			return;
		}

		ConfigPro config = (ConfigPro) pc.getConfig();
		PageSourceImpl ps = (PageSourceImpl) ConfigUtil.toPageSource(config, null, publicFile, null);
		if (ps == null) {
			errors.setEL(path, "cannot resolve file to a mapping");
			return;
		}

		try {
			long srcLastModified = backup.lastModified();
			ResourceUtil.copy(backup, publicFile);
			publicFile.setLastModified(srcLastModified);
			backup.delete();
			ps.clear();
			ps.flush();
			restored.append(path);
		}
		catch (Exception e) {
			errors.setEL(path, Caster.toPageException(e).getCatchBlock(config));
		}
	}

	private static Resource getBackupFile(Resource publicFile) {
		return publicFile.getParentResource().getRealResource("." + publicFile.getName());
	}

	private static Resource getPublicFile(Resource backupFile) {
		String name = backupFile.getName();
		Resource parent = backupFile.getParentResource();
		if (parent == null) return null;

		if (name.startsWith(".")) return parent.getRealResource(name.substring(1));
		if (parent.getRealResource("." + name).equals(backupFile)) return parent.getRealResource(name);
		return null;
	}

}
