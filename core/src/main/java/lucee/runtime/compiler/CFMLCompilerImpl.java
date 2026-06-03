/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.objectweb.asm.MethodTooLargeException;

import lucee.commons.digest.RSA;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.compiler.JavaFunction;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.Page;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeFactory;
import lucee.transformer.bytecode.PageImpl;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ClassRenamer;
import lucee.transformer.cfml.script.CFMLScriptTransformer;
import lucee.transformer.cfml.tag.CFMLTransformer;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.AlreadyClassException;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

/**
 * CFML Compiler compiles CFML source templates
 */
public final class CFMLCompilerImpl implements CFMLCompiler {

	private static final boolean IS_WINDOWS = SystemUtil.isWindows();

	private CFMLTransformer cfmlTagTransformer;
	private CFMLScriptTransformer cfmlScriptTransformer;
	private ConcurrentLinkedQueue<WatchEntry> watched = new ConcurrentLinkedQueue<WatchEntry>();

	/**
	 * Constructor of the compiler
	 */
	public CFMLCompilerImpl() {
		cfmlTagTransformer = new CFMLTransformer();
		cfmlScriptTransformer = new CFMLScriptTransformer();
	}

	public Struct ast(ConfigPro config, PageSource ps, boolean ignoreScopes) throws TemplateException, IOException {

		BytecodeFactory factory = BytecodeFactory.getInstance(config);
		// , cwi.getFLDs()
		PageImpl page = ((PageImpl) cfmlTagTransformer.transform(factory, config, ps, config.getTLDs(), config.getFLDs(), false, ignoreScopes));
		Struct root = new StructImpl(Struct.TYPE_LINKED);
		page.dump(root);

		// TODO better solution than simply look at the offset from script
		if (page.getSourceCode().getSourceOffset() == 10) {

			boolean isCFMLCompExt = Constants.isCFMLComponentExtension(ResourceUtil.getExtension(ps.getResource(), ""));
			// in case of a component Lucee moves the component to the root, so at the first position is just an
			// empty script, we simply have to emove this
			// TODO remove the script after moving in the parser
			if (isCFMLCompExt) {
				removeEmptyScriptTag(root);
			}
			else {
				extractScriptTagInRoot(root);
			}
		}
		return root;
	}

	public Struct ast(ConfigPro config, SourceCode sc, boolean ignoreScopes, Boolean script) throws PageException {
		BytecodeFactory factory = BytecodeFactory.getInstance(config);
		// TODO auto when script is null

		if (script != null && script) {
			TagLibTag scriptTag = CFMLTransformer.getTLT(sc, Constants.CFML_SCRIPT_TAG_NAME, config.getIdentification());

			sc.setPos(0);
			// try inside a cfscript
			String text = "<" + scriptTag.getFullName() + ">" + sc.getText() + "\n</" + scriptTag.getFullName() + ">";
			int sourceOffset = ("<" + scriptTag.getFullName() + ">").length();
			sc = new SourceCode(null, text, sc.getWriteLog(), sourceOffset);
		}

		PageImpl page = ((PageImpl) cfmlTagTransformer.transform(factory, config, sc, config.getTLDs(), config.getFLDs(), System.currentTimeMillis(),
				config.getDotNotationUpperCase(), false, ignoreScopes, false, false, false, true));
		Struct root = new StructImpl(Struct.TYPE_LINKED);
		page.dump(root);

		if (script != null && script) {
			extractScriptTagInRoot(root);
		}

		return root;
	}

	// remove script again (a bit complicated, but atm the only way to do it)
	private void extractScriptTagInRoot(Struct root) {
		Array body = Caster.toArray(root.get(KeyConstants._body, null), null);
		if (body != null) {
			Struct first = Caster.toStruct(body.get(1, null), null);
			if (first != null) {
				Struct body2 = Caster.toStruct(first.get(KeyConstants._body, null), null);
				if (body2 != null) {
					Object body3 = body2.get(KeyConstants._body, null);
					if (body3 != null) {
						root.setEL(KeyConstants._body, body3);
					}
				}
			}
		}
	}

	private void removeEmptyScriptTag(Struct root) {
		Array body = Caster.toArray(root.get(KeyConstants._body, null), null);
		if (body != null && body.size() > 1) {
			Struct first = Caster.toStruct(body.get(1, null), null);
			if (first != null) {
				if ("cfscript".equalsIgnoreCase(Caster.toString(first.get("fullname", null), null))) {
					Struct body2 = Caster.toStruct(first.get(KeyConstants._body, null), null);
					Array body3 = Caster.toArray(body2.get(KeyConstants._body, null), null);
					if (body3 != null && body3.size() == 0) {
						body.removeEL(1);
					}
				}
			}
		}
	}

	public Result compile(ConfigPro config, PageSource ps, TagLib[] tld, FunctionLib fld, Resource classRootDir, boolean returnValue, boolean ignoreScopes)
			throws TemplateException, IOException {
		return _compile(config, ps, null, null, tld, fld, classRootDir, returnValue, ignoreScopes);
	}

	public Result compile(ConfigPro config, SourceCode sc, TagLib[] tld, FunctionLib fld, Resource classRootDir, String className, boolean returnValue, boolean ignoreScopes)
			throws TemplateException, IOException {

		// just to be sure
		PageSource ps = (sc instanceof PageSourceCode) ? ((PageSourceCode) sc).getPageSource() : null;

		return _compile(config, ps, sc, className, tld, fld, classRootDir, returnValue, ignoreScopes);
	}

	private Result _compile(ConfigPro config, PageSource ps, SourceCode sc, String className, TagLib[] tld, FunctionLib fld, Resource classRootDir, boolean returnValue,
			boolean ignoreScopes) throws TemplateException, IOException {
		// compilation must not depend on the request that happens to trigger it (the resulting bytecode is
		// cached and shared across applications/requests); disable the ambient PageContext fallback so any
		// indirect lookup resolves deterministic config/server defaults instead of inheriting request state
		boolean prevFallback = ThreadLocalPageContext.fallback(false);
		try {
			return _compile0(config, ps, sc, className, tld, fld, classRootDir, returnValue, ignoreScopes);
		}
		finally {
			ThreadLocalPageContext.fallback(prevFallback);
		}
	}

	private Result _compile0(ConfigPro config, PageSource ps, SourceCode sc, String className, TagLib[] tld, FunctionLib fld, Resource classRootDir, boolean returnValue,
			boolean ignoreScopes) throws TemplateException, IOException {

		String javaName;
		if (className == null) {
			javaName = ListUtil.trim(ps.getJavaName(), "\\/", false);
			className = ps.getClassName();
		}
		else {
			javaName = className.replace('.', '/');
		}

		Result result = null;
		// byte[] barr = null;
		PageImpl page = null;
		BytecodeFactory factory = BytecodeFactory.getInstance(config);
		try {
			page = sc == null ? ((PageImpl) cfmlTagTransformer.transform(factory, config, ps, tld, fld, returnValue, ignoreScopes))
					: ((PageImpl) cfmlTagTransformer.transform(factory, config, sc, tld, fld, System.currentTimeMillis(), config.getDotNotationUpperCase(), returnValue,
							ignoreScopes, false, false, false, false));
			page.setSplitIfNecessary(false);

			// StructImpl sct = new StructImpl(Struct.TYPE_LINKED);
			// page.dump(sct);
			// print.e(sct);

			try {
				byte[] barr = page.execute(className);
				result = new Result(page, barr, page.getJavaFunctions());
			}
			catch (MethodTooLargeException mtle) {
				String source = ps != null ? ps.getDisplayPath() : className;
				String methodName = mtle.getMethodName();
				int codeSize = mtle.getCodeSize();
				throw new RuntimeException(
					"Bytecode too large compiling [" + source + "]: method [" + methodName + "] generated " + codeSize + " bytes (limit 65535)",
					mtle);
			}
			catch (RuntimeException re) {
				/*
				 * String msg = StringUtil.emptyIfNull(re.getMessage()); if (StringUtil.indexOfIgnoreCase(msg,
				 * "Method code too large!") != -1 // org.objectweb.asm.MethodTooLargeException ||
				 * StringUtil.indexOfIgnoreCase(msg, "Method too large:") != -1) { page = sc == null ?
				 * cfmlTransformer.transform(factory, config, ps, tld, fld, returnValue, ignoreScopes) :
				 * cfmlTransformer.transform(factory, config, sc, tld, fld, System.currentTimeMillis(),
				 * config.getDotNotationUpperCase(), returnValue, ignoreScopes);
				 * 
				 * page.setSplitIfNecessary(true); byte[] barr = page.execute(className); result = new Result(page,
				 * barr, page.getJavaFunctions()); } else
				 */
				throw re;

			}
			catch (ClassFormatError cfe) {
				/*
				 * print.e(cfe); String msg = StringUtil.emptyIfNull(cfe.getMessage()); if
				 * (StringUtil.indexOfIgnoreCase(msg, "Invalid method Code length") != -1) { page = ps != null ?
				 * cfmlTransformer.transform(factory, config, ps, tld, fld, returnValue, ignoreScopes) :
				 * cfmlTransformer.transform(factory, config, sc, tld, fld, System.currentTimeMillis(),
				 * config.getDotNotationUpperCase(), returnValue, ignoreScopes);
				 * 
				 * page.setSplitIfNecessary(true); byte[] barr = page.execute(className); result = new Result(page,
				 * barr, page.getJavaFunctions()); } else
				 */ throw cfe;
			}

			// store
			if (classRootDir != null) {
				final Resource classFile = classRootDir.getRealResource(page.getClassName() + ".class");
				Resource classFileDirectory = classFile.getParentResource();
				if (!classFileDirectory.exists()) classFileDirectory.mkdirs();
				else if (classFile.exists() && !IS_WINDOWS) {
					final String prefix = page.getClassName() + "$";
					classRootDir.list(new ResourceNameFilter() {
						@Override
						public boolean accept(Resource parent, String name) {
							if (name.startsWith(prefix)) parent.getRealResource(name).delete();
							return false;
						}
					});
				}
				try {
					IOUtil.copy(new ByteArrayInputStream(result.barr), classFile, true);
				}
				catch (IOException ioe) {
					Resource p = classFile.getParentResource();
					p.mkdirs();
					IOUtil.copy(new ByteArrayInputStream(result.barr), classFile, true);
				}
				if (result.javaFunctions != null) {
					for (JavaFunction jf: result.javaFunctions) {
						IOUtil.copy(new ByteArrayInputStream(jf.byteCode), classFileDirectory.getRealResource(jf.getName() + ".class"), true);
					}
				}
				/// TODO; //store java functions
			}

			return result;
		}
		catch (AlreadyClassException ace) {
			if (Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.compiler.block.bytecode", null), true)) throw new TemplateException(
					"Bytecode execution is disabled by environment configuration. Lucee is configured to prevent the execution of precompiled bytecode files (.cfm|.cfc|.cfs) directly. "
							+ "To enable bytecode execution, set the system property `-Dlucee.compiler.block.bytecode=false` or the environment variable `LUCEE_COMPILER_BLOCK_BYTECODE=false`.");

			byte[] bytes = ace.getEncrypted() ? readEncrypted(ace) : readPlain(ace);

			result = new Result(null, bytes, null); // TODO handle better Java Functions

			String displayPath = ps != null ? "[" + ps.getDisplayPath() + "] " : "";
			String srcName = ASMUtil.getClassName(result.barr);

			// source is cfm and target cfc
			if (endsWith(srcName, Constants.getCFMLTemplateExtensions()) && className.endsWith("_" + Constants.getCFMLComponentExtension() + (Constants.CFML_CLASS_SUFFIX))) {
				throw new TemplateException("Source file [" + displayPath + "] contains the bytecode for a regular cfm template not for a component");
			}
			// source is cfc and target cfm
			if (srcName.endsWith("_" + Constants.getCFMLComponentExtension() + (Constants.CFML_CLASS_SUFFIX)) && endsWith(className, Constants.getCFMLTemplateExtensions()))
				throw new TemplateException("Source file [" + displayPath + "] contains a component not a regular cfm template");

			// rename class name when needed
			if (!srcName.equals(javaName)) {
				byte[] barr = ClassRenamer.rename(result.barr, javaName);
				if (barr != null) result = new Result(result.page, barr, null); // TODO handle java functions
			}
			// store
			if (classRootDir != null) {
				Resource classFile = classRootDir.getRealResource(javaName + ".class");
				Resource classFileDirectory = classFile.getParentResource();
				if (!classFileDirectory.exists()) classFileDirectory.mkdirs();
				result = new Result(result.page, PageImpl.setSourceLastModified(result.barr, ps != null ? ps.getPhyscalFile().lastModified() : System.currentTimeMillis()), null);// TODO
				// handle
				// java
				// functions
				IOUtil.copy(new ByteArrayInputStream(result.barr), classFile, true);
			}

			return result;
		}
		catch (TransformerException bce) {
			Position pos = bce.getPosition();
			int line = pos == null ? -1 : pos.line;
			int col = pos == null ? -1 : pos.column;
			if (ps != null) bce.addContext(ps, line, col, null);
			throw bce;
		}
	}

	private byte[] readPlain(AlreadyClassException ace) throws IOException {
		return IOUtil.toBytes(ace.getInputStream(), true);
	}

	private byte[] readEncrypted(AlreadyClassException ace) throws IOException {

		String str = System.getenv("PUBLIC_KEY");
		if (str == null) str = System.getProperty("PUBLIC_KEY");
		if (str == null) throw new RuntimeException("To decrypt encrypted bytecode, you need to set PUBLIC_KEY as system property or as an environment variable");

		byte[] bytes = IOUtil.toBytes(ace.getInputStream(), true);
		try {
			PublicKey publicKey = RSA.toPublicKey(str);
			// first 2 bytes are just a mask to detect encrypted code, so we need to set offset 2
			bytes = RSA.decrypt(bytes, publicKey, 2);
		}
		catch (IOException ioe) {
			throw ioe;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return bytes;
	}

	private boolean endsWith(String name, String[] extensions) {
		for (int i = 0; i < extensions.length; i++) {
			if (name.endsWith("_" + extensions[i] + (Constants.CFML_CLASS_SUFFIX))) return true;
		}
		return false;
	}

	public Page transform(ConfigPro config, PageSource source, TagLib[] tld, FunctionLib funcLib, boolean returnValue, boolean ignoreScopes) throws TemplateException, IOException {
		return cfmlTagTransformer.transform(BytecodeFactory.getInstance(config), config, source, tld, funcLib, returnValue, ignoreScopes);
	}

	public final class Result {

		public final Page page;
		public final byte[] barr;
		public final List<JavaFunction> javaFunctions;

		public Result(Page page, byte[] barr, List<JavaFunction> javaFunctions) {
			this.page = page;
			this.barr = barr;
			this.javaFunctions = javaFunctions;
		}
	}

	public void watch(PageSource ps, long now) {
		watched.offer(new WatchEntry(ps, now, ps.getPhyscalFile().length(), ps.getPhyscalFile().lastModified()));
	}

	public void checkWatched() {
		WatchEntry we;
		long now = System.currentTimeMillis();
		Stack<WatchEntry> tmp = new Stack<WatchEntry>();
		while ((we = watched.poll()) != null) {
			// to young
			if (we.now + 1000 > now) {
				tmp.add(we);
				continue;
			}

			if (we.length != we.ps.getPhyscalFile().length() && we.ps.getPhyscalFile().length() > 0) { // TODO this is set to avoid that removed files are removed from pool, remove
				// this line if a UDF still wprks fine when the page is gone
				((PageSourceImpl) we.ps).flush();
			}
		}

		// add again entries that was to young for next round
		Iterator<WatchEntry> it = tmp.iterator();
		while (it.hasNext()) {
			watched.add(we = it.next());
		}
	}

	private class WatchEntry {

		private final PageSource ps;
		private final long now;
		private final long length;
		private final long lastModified;

		public WatchEntry(PageSource ps, long now, long length, long lastModified) {
			this.ps = ps;
			this.now = now;
			this.length = length;
			this.lastModified = lastModified;
		}
	}
}
