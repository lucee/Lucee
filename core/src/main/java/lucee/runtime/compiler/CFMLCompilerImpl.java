/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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

import lucee.commons.digest.RSA;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.compiler.JavaFunction;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeFactory;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ClassRenamer;
import lucee.transformer.cfml.tag.CFMLTransformer;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.util.AlreadyClassException;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

/**
 * CFML Compiler compiles CFML source templates
 */
public final class CFMLCompilerImpl implements CFMLCompiler {

	private CFMLTransformer cfmlTransformer;
	private ConcurrentLinkedQueue<WatchEntry> watched = new ConcurrentLinkedQueue<WatchEntry>();

	/**
	 * Constructor of the compiler
	 * 
	 * @param config
	 */
	public CFMLCompilerImpl() {
		cfmlTransformer = new CFMLTransformer();
	}

	public Result compile(ConfigImpl config, PageSource ps, TagLib[] tld, FunctionLib[] fld, Resource classRootDir, boolean returnValue, boolean ignoreScopes)
			throws TemplateException, IOException {
		return _compile(config, ps, null, null, tld, fld, classRootDir, returnValue, ignoreScopes);
	}

	public Result compile(ConfigImpl config, SourceCode sc, TagLib[] tld, FunctionLib[] fld, Resource classRootDir, String className, boolean returnValue, boolean ignoreScopes)
			throws TemplateException, IOException {

		// just to be sure
		PageSource ps = (sc instanceof PageSourceCode) ? ((PageSourceCode) sc).getPageSource() : null;

		return _compile(config, ps, sc, className, tld, fld, classRootDir, returnValue, ignoreScopes);
	}

	/*
	 * private byte[] _compiless(ConfigImpl config,PageSource ps,SourceCode sc,String className,
	 * TagLib[] tld, FunctionLib[] fld, Resource classRootDir,TransfomerSettings settings) throws
	 * TemplateException { Factory factory = BytecodeFactory.getInstance(config);
	 * 
	 * Page page=null;
	 * 
	 * TagLib[][] _tlibs=new TagLib[][]{null,new TagLib[0]}; _tlibs[CFMLTransformer.TAG_LIB_GLOBAL]=tld;
	 * // reset page tlds if(_tlibs[CFMLTransformer.TAG_LIB_PAGE].length>0) {
	 * _tlibs[CFMLTransformer.TAG_LIB_PAGE]=new TagLib[0]; }
	 * 
	 * CFMLScriptTransformer scriptTransformer = new CFMLScriptTransformer();
	 * scriptTransformer.transform( BytecodeFactory.getInstance(config) , page , new EvaluatorPool() ,
	 * _tlibs, fld , null , config.getCoreTagLib(ps.getDialect()).getScriptTags() , sc , settings);
	 * 
	 * //CFMLExprTransformer extr=new CFMLExprTransformer(); //extr.transform(factory, page, ep, tld,
	 * fld, scriptTags, cfml, settings)
	 * 
	 * return null; }
	 */

	private Result _compile(ConfigImpl config, PageSource ps, SourceCode sc, String className, TagLib[] tld, FunctionLib[] fld, Resource classRootDir, boolean returnValue,
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
		Page page = null;
		Factory factory = BytecodeFactory.getInstance(config);
		try {
			page = sc == null ? cfmlTransformer.transform(factory, config, ps, tld, fld, returnValue, ignoreScopes)
					: cfmlTransformer.transform(factory, config, sc, tld, fld, System.currentTimeMillis(),
							sc.getDialect() == CFMLEngine.DIALECT_CFML && config.getDotNotationUpperCase(), returnValue, ignoreScopes);
			page.setSplitIfNecessary(false);
			try {
				byte[] barr = page.execute(className);
				result = new Result(page, barr, page.getJavaFunctions());
			}
			catch (RuntimeException re) {
				String msg = StringUtil.emptyIfNull(re.getMessage());
				if (StringUtil.indexOfIgnoreCase(msg, "Method code too large!") != -1) {
					page = sc == null ? cfmlTransformer.transform(factory, config, ps, tld, fld, returnValue, ignoreScopes)
							: cfmlTransformer.transform(factory, config, sc, tld, fld, System.currentTimeMillis(),
									sc.getDialect() == CFMLEngine.DIALECT_CFML && config.getDotNotationUpperCase(), returnValue, ignoreScopes);

					page.setSplitIfNecessary(true);
					byte[] barr = page.execute(className);
					result = new Result(page, barr, page.getJavaFunctions());
				}
				else throw re;
			}
			catch (ClassFormatError cfe) {
				String msg = StringUtil.emptyIfNull(cfe.getMessage());
				if (StringUtil.indexOfIgnoreCase(msg, "Invalid method Code length") != -1) {
					page = ps != null ? cfmlTransformer.transform(factory, config, ps, tld, fld, returnValue, ignoreScopes)
							: cfmlTransformer.transform(factory, config, sc, tld, fld, System.currentTimeMillis(),
									sc.getDialect() == CFMLEngine.DIALECT_CFML && config.getDotNotationUpperCase(), returnValue, ignoreScopes);

					page.setSplitIfNecessary(true);
					byte[] barr = page.execute(className);
					result = new Result(page, barr, page.getJavaFunctions());
				}
				else throw cfe;
			}

			// store
			if (classRootDir != null) {
				Resource classFile = classRootDir.getRealResource(page.getClassName() + ".class");
				Resource classFileDirectory = classFile.getParentResource();
				if (!classFileDirectory.exists()) classFileDirectory.mkdirs();
				else if (classFile.exists() && !SystemUtil.isWindows()) {
					final String prefix = page.getClassName() + "$";
					classRootDir.list(new ResourceNameFilter() {
						@Override
						public boolean accept(Resource parent, String name) {
							if (name.startsWith(prefix)) parent.getRealResource(name).delete();
							return false;
						}
					});
				}
				IOUtil.copy(new ByteArrayInputStream(result.barr), classFile, true);
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

			byte[] bytes = ace.getEncrypted() ? readEncrypted(ace) : readPlain(ace);

			result = new Result(null, bytes, null); // TODO handle better Java Functions

			String displayPath = ps != null ? "[" + ps.getDisplayPath() + "] " : "";
			String srcName = ASMUtil.getClassName(result.barr);

			int dialect = sc == null ? ps.getDialect() : sc.getDialect();
			// source is cfm and target cfc
			if (dialect == CFMLEngine.DIALECT_CFML && endsWith(srcName, Constants.getCFMLTemplateExtensions(), dialect) && className
					.endsWith("_" + Constants.getCFMLComponentExtension() + (dialect == CFMLEngine.DIALECT_CFML ? Constants.CFML_CLASS_SUFFIX : Constants.LUCEE_CLASS_SUFFIX))) {
				throw new TemplateException("Source file [" + displayPath + "] contains the bytecode for a regular cfm template not for a component");
			}
			// source is cfc and target cfm
			if (dialect == CFMLEngine.DIALECT_CFML
					&& srcName.endsWith(
							"_" + Constants.getCFMLComponentExtension() + (dialect == CFMLEngine.DIALECT_CFML ? Constants.CFML_CLASS_SUFFIX : Constants.LUCEE_CLASS_SUFFIX))
					&& endsWith(className, Constants.getCFMLTemplateExtensions(), dialect))
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
				result = new Result(result.page, Page.setSourceLastModified(result.barr, ps != null ? ps.getPhyscalFile().lastModified() : System.currentTimeMillis()), null);// TODO
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

	private boolean endsWith(String name, String[] extensions, int dialect) {
		for (int i = 0; i < extensions.length; i++) {
			if (name.endsWith("_" + extensions[i] + (dialect == CFMLEngine.DIALECT_CFML ? Constants.CFML_CLASS_SUFFIX : Constants.LUCEE_CLASS_SUFFIX))) return true;
		}
		return false;
	}

	public Page transform(ConfigImpl config, PageSource source, TagLib[] tld, FunctionLib[] fld, boolean returnValue, boolean ignoreScopes) throws TemplateException, IOException {
		return cfmlTransformer.transform(BytecodeFactory.getInstance(config), config, source, tld, fld, returnValue, ignoreScopes);
	}

	public class Result {

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
