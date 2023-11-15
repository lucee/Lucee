package lucee.runtime.functions.file;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceMetaData;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ModeObjectWrap;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CFTypes;
import lucee.runtime.Component;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.tag.Directory;
import lucee.runtime.type.Collection;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.util.KeyConstants;

public class DirectoryEvery extends BIF {
	private static final long serialVersionUID = 636791970889913461L;

	public static Object call(PageContext pc, String path, Component cfc, boolean recurse) throws PageException {
		invoke(pc, path, cfc, recurse);
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 2 || args.length > 4) throw new FunctionException(pc, "DirectoryEvery", 2, 4, args.length);

		// required
		String path = Caster.toString(args[0]);
		Component cfc = Caster.toComponent(args[1]);

		// optional
		boolean recurse = args.length > 2 && args[2] != null ? Caster.toBooleanValue(args[2]) : false;
		// TODO sorted

		invoke(pc, path, cfc, recurse);
		return null;
	}

	private static void invoke(PageContext pc, String path, Component cfc, boolean recurse) throws PageException {
		Resource directory = ResourceUtil.toResourceNotExisting(pc, path);

		// security
		SecurityManager securityManager = pc.getConfig().getSecurityManager();
		securityManager.checkFileLocation(pc.getConfig(), directory, null);

		// check directory
		if (!directory.exists()) {
			throw new FunctionException(pc, "DirectoryEvery", 1, "path", "Directory [" + directory.toString() + "] doesn't exist");
		}
		if (!directory.isDirectory()) {
			throw new FunctionException(pc, "DirectoryEvery", 1, "path", "File [" + directory.toString() + "] exists, but isn't a directory");
		}
		if (!directory.isReadable()) {
			throw new FunctionException(pc, "DirectoryEvery", 1, "path", "No access to read directory [" + directory.toString() + "]");
		}

		// check component
		ComponentSpecificAccess csa = new ComponentSpecificAccess(Component.ACCESS_PRIVATE, cfc);
		Collection.Key[] keys = csa.keys();
		UDF invoke = Caster.toFunction(csa.get(KeyConstants._invoke), null);
		if (invoke == null)
			throw new FunctionException(pc, "DirectoryEvery", 2, "component", "the listener component does not contain a instance function with name [invoke] that is required");

		// function invoke return type
		if (!(invoke.getReturnType() == CFTypes.TYPE_ANY || invoke.getReturnType() == CFTypes.TYPE_BOOLEAN))
			throw new FunctionException(pc, "DirectoryEvery", 2, "component", "the function invoke of the component listener must have the return type boolean.");

		// function invoke arguments
		FunctionArgument[] udfArgs = invoke.getFunctionArguments();
		if (udfArgs.length < 1 || udfArgs.length > 2) throw new FunctionException(pc, "DirectoryEvery", 2, "component",
				"you need to define one of the 2 possible argument pattern for the function invoke in the listener component:"
						+ " (string path) or (string path,struct metadata). Do not define path if you don't need it, because it is a bit faster without it.");

		boolean withInfo = false;
		FunctionArgument arg = udfArgs[0];
		if (!(arg.getType() == CFTypes.TYPE_ANY || arg.getType() == CFTypes.TYPE_STRING)) throw new FunctionException(pc, "DirectoryEvery", 2, "component",
				"the first argument of the function invoke of the component listener need to be defined as a string or no defintion at all");
		if (udfArgs.length > 1) {
			withInfo = true;
			arg = udfArgs[1];
			if (!(arg.getType() == CFTypes.TYPE_ANY || arg.getType() == CFTypes.TYPE_STRUCT)) throw new FunctionException(pc, "DirectoryEvery", 2, "component",
					"the second argument of the function invoke of the component listener need to be defined as a struct or no defintion at all");
		}

		if (recurse || withInfo) {
			try {
				list(pc, directory, cfc, 1, recurse, withInfo, directory.getResourceProvider().isModeSupported());
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}
		else {
			list(pc, directory, cfc);
		}
	}

	private static void list(PageContext pc, Resource directory, Component cfc) throws PageException {
		if (directory instanceof FileResource) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(((FileResource) ResourceUtil.getCanonicalResourceEL(directory)).toPath())) {
				for (Path entry: stream) {
					if (!Caster.toBooleanValue(cfc.call(pc, KeyConstants._invoke, new Object[] { entry.toString() }))) {
						break;
					}
				}
			}
			catch (IOException ioe) {
				throw Caster.toPageException(ioe);
			}
		}
		else {
			String dir = ResourceUtil.getCanonicalPathEL(directory);
			String[] children = ResourceUtil.getCanonicalResourceEL(directory).list();
			if (children != null) {
				for (String child: children) {
					if (!Caster.toBooleanValue(cfc.call(pc, KeyConstants._invoke, new Object[] { dir + child }))) break;
				}
			}
		}
	}

	private static boolean list(PageContext pc, Resource directory, Component cfc, int level, boolean recurse, boolean withInfo, boolean modeSupported)
			throws PageException, IOException {
		if (directory instanceof FileResource) {
			return _list(pc, ((FileResource) ResourceUtil.getCanonicalResourceEL(directory)).toPath(), cfc, level, recurse, withInfo, modeSupported);
		}
		return _list(pc, ResourceUtil.getCanonicalResourceEL(directory), cfc, level, recurse, withInfo, modeSupported);
	}

	private static boolean _list(PageContext pc, Resource directory, Component cfc, int level, boolean recurse, boolean withInfo, boolean modeSupported)
			throws PageException, IOException {

		Resource[] children = directory.listResources();
		if (children != null) {
			String dir = ResourceUtil.getCanonicalPathEL(directory);
			for (Resource child: children) {
				if (!Caster.toBooleanValue(cfc.call(pc, KeyConstants._invoke,
						withInfo ? new Object[] { child.getAbsolutePath(), info(child, dir, level, modeSupported) } : new Object[] { child.getAbsolutePath() }))) {
					return true;
				}
				if (recurse && child.isDirectory()) {
					if (_list(pc, child, cfc, level + 1, recurse, withInfo, modeSupported)) return true;
				}
			}
		}
		return false;
	}

	private static boolean _list(PageContext pc, Path directory, Component cfc, int level, boolean recurse, boolean withInfo, boolean modeSupported)
			throws PageException, IOException {

		String dir = directory.toString();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (Path entry: stream) {
				if (!Caster.toBooleanValue(cfc.call(pc, KeyConstants._invoke,
						withInfo ? new Object[] { entry.toString(), info(entry, dir, level, modeSupported) } : new Object[] { entry.toString() }))) {
					return true;
				}
				if (recurse && Files.isDirectory(entry)) {
					if (_list(pc, entry, cfc, level + 1, recurse, withInfo, modeSupported)) return true;
				}
			}
		}
		return false;
	}

	private static Object info(Resource res, String dir, int level, boolean modeSupported) throws PageException {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.set(KeyConstants._name, res.getName());
		sct.set(KeyConstants._directory, dir);
		sct.set(KeyConstants._level, level);
		sct.set(KeyConstants._size, Double.valueOf(res.isDirectory() ? 0 : res.length()));
		sct.set(KeyConstants._type, res.isDirectory() ? "Dir" : "File");
		sct.set(KeyConstants._dateLastModified, new DateTimeImpl(res.lastModified(), false));
		if (modeSupported) sct.set(KeyConstants._mode, new ModeObjectWrap(res));
		if (res instanceof ResourceMetaData) sct.set(KeyConstants._meta, ((ResourceMetaData) res).getMetaData());
		sct.set(KeyConstants._attributes, Directory.getFileAttribute(res, true));

		return sct;
	}

	private static Object info(Path path, String dir, int level, boolean modeSupported) throws PageException, IOException {
		boolean isDir = Files.isDirectory(path);
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.set(KeyConstants._name, path.getFileName());
		sct.set(KeyConstants._directory, dir);
		sct.set(KeyConstants._level, level);
		sct.set(KeyConstants._size, Double.valueOf(isDir ? 0 : Files.size(path)));
		sct.set(KeyConstants._type, isDir ? "Dir" : "File");
		sct.set(KeyConstants._dateLastModified, new DateTimeImpl(Files.getLastModifiedTime(path).toMillis(), false));
		if (modeSupported) sct.set(KeyConstants._mode, new ModeObjectWrap(path));
		sct.set(KeyConstants._attributes, Directory.getFileAttribute(path, true));

		return sct;
	}

}
