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
package lucee.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.compiler.JavaFunction;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.commons.lang.types.RefIntegerSync;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.compiler.CFMLCompilerImpl.Result;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.MissingIncludeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.functions.system.GetDirectoryFromPath;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.util.PageSourceCode;

/**
 * represent a cfml file on the runtime system
 */
public final class PageSourceImpl implements PageSource {

	private static final long serialVersionUID = -7661676586215092539L;
	// public static final byte LOAD_NONE=1;
	public static final byte LOAD_ARCHIVE = 2;
	public static final byte LOAD_PHYSICAL = 3;
	private static final long MAX = 1024 * 1024 * 100;
	public static File logAccessDirectory;

	// private byte load=LOAD_NONE;

	private final MappingImpl mapping;

	private boolean isOutSide;

	private String dspPath;
	private String relPath;
	private String packageName;
	private String javaName;
	private String className;
	private String fileName;

	private Resource physcalSource;
	private Resource archiveSource;
	private Resource archiveClass;
	private String compName;
	private PageAndClassName pcn = new PageAndClassName();
	private long lastAccess;
	private RefIntegerSync accessCount = new RefIntegerSync();
	private boolean flush = false;
	// Negative-lookup cache for NEVER mappings. When loadPhysical confirms the underlying file is
	// missing (srcLastModified == 0), this flag short-circuits future calls so repeated lookups of
	// the same non-existent path skip the lastModified() syscall. Cleared by resetLoaded() and
	// clear() so inspectTemplates() / PagePoolClear / classloader-clear all invalidate it.
	private volatile boolean notFound;

	private static class PageAndClassName {
		private Page _page;
		private String _className;

		public void reset() {
			this._page = null;
			this._className = null;
		}

		public void set(Page page) {
			this._page = page;
			if (page != null) _className = page.getClass().getName();
		}

		public Page getPage() {
			return _page;
		}

		public String getClassName() {
			return _className;
		}
	}

	PageSourceImpl(MappingImpl mapping, String relPath, boolean isOutSide) {
		this.mapping = mapping;
		this.isOutSide = isOutSide;
		this.relPath = relPath;
		// if (logAccessDirectory != null) dump();
	}

	/**
	 * return page when already loaded, otherwise null
	 * 
	 * @return
	 */
	public Page getPage() {
		return pcn.getPage();
	}

	public PageSource getParent() {
		if (relPath.equals("/")) return null;
		if (StringUtil.endsWith(relPath, '/')) return getInstance(mapping, GetDirectoryFromPath.invoke(relPath.substring(0, relPath.length() - 1)));
		return getInstance(mapping, GetDirectoryFromPath.invoke(relPath));
	}

	private static PageSource getInstance(MappingImpl mapping, String realPath) {
		boolean isOutSide = false;
		realPath = realPath.replace('\\', '/');
		if (realPath.indexOf("//") != -1) {
			realPath = StringUtil.replace(realPath, "//", "/", false);
		}
		if (realPath.indexOf('/') != 0) {
			if (realPath.startsWith("../")) {
				isOutSide = true;
			}
			else if (realPath.startsWith("./")) {
				realPath = realPath.substring(1);
			}
			else {
				realPath = "/" + realPath;
			}
		}
		return mapping.getPageSource(realPath, isOutSide);
	}

	@Override
	public Page loadPage(PageContext pc, boolean forceReload) throws PageException {
		if (forceReload) pcn.reset();

		Page page = pcn.getPage();
		if (mapping.isPhysicalFirst()) {
			page = loadPhysical(pc, page);
			if (page == null) page = loadArchive(page);
			if (page != null) return page;
		}
		else {
			page = loadArchive(page);
			if (page == null) page = loadPhysical(pc, page);
			if (page != null) return page;
		}
		throw new MissingIncludeException(this);

	}

	@Override
	public Page loadPageThrowTemplateException(PageContext pc, boolean forceReload, Page defaultValue) throws PageException {
		if (forceReload) pcn.reset();

		Page page = pcn.getPage();
		if (mapping.isPhysicalFirst()) {
			page = loadPhysical(pc, page);
			if (page == null) page = loadArchive(page);
			if (page != null) return page;
		}
		else {
			page = loadArchive(page);
			if (page == null) page = loadPhysical(pc, page);
			if (page != null) return page;
		}
		return defaultValue;
	}

	@Override
	public Page loadPage(PageContext pc, boolean forceReload, Page defaultValue) {
		if (forceReload) pcn.reset();

		Page page = pcn.getPage();
		if (mapping.isPhysicalFirst()) {
			try {
				page = loadPhysical(pc, page);
			}
			catch (TemplateException e) {
				page = null;
			}
			if (page == null) page = loadArchive(page, null);
			if (page != null) return page;
		}
		else {
			page = loadArchive(page, null);
			if (page == null) {
				try {
					page = loadPhysical(pc, page);
				}
				catch (TemplateException e) {}
			}
			if (page != null) return page;
		}
		return defaultValue;
	}

	private Page loadArchive(Page page) throws PageException {
		if (!mapping.hasArchive()) return null;
		if (page != null && page.getLoadType() == LOAD_ARCHIVE) return page;
		if (!getArchiveClass().isFile()) {
			return null;
		}
		try {
			Class clazz = mapping.getArchiveClass(getClassName());
			page = newInstance(clazz);
			page.setPageSource(this);
			page.setLoadType(LOAD_ARCHIVE);
			pcn.set(page);
			return page;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private Page loadArchive(Page page, Page defaultValue) {
		if (!mapping.hasArchive()) return defaultValue;
		if (page != null && page.getLoadType() == LOAD_ARCHIVE) return page;
		if (!getArchiveClass().isFile()) {
			return defaultValue;
		}
		try {
			Class clazz = mapping.getArchiveClass(getClassName());
			page = newInstance(clazz);
			page.setPageSource(this);
			page.setLoadType(LOAD_ARCHIVE);
			pcn.set(page);
			return page;
		}
		catch (Exception e) {
			if (mapping.getLog() != null) {
				mapping.getLog().error("page-source", "Failed to load [" + getDisplayPath() + "] from archive [" + getArchiveClass() + "]", e);
			}
			return defaultValue;
		}
	}

	/**
	 * throws only an exception when compilation fails
	 * 
	 * @param pc
	 * @param page
	 * @return
	 * @throws PageException
	 */
	private Page loadPhysical(PageContext pcMayNull, Page page) throws TemplateException {
		if (!mapping.hasPhysical()) return null;

		pcMayNull = ThreadLocalPageContext.get(pcMayNull);
		Config config;
		PageContextImpl pci = null;
		if (pcMayNull != null) {
			config = pcMayNull.getConfig();
			pci = (PageContextImpl) pcMayNull;

		}
		else {
			config = ThreadLocalPageContext.getConfig();
		}

		if (notFound && mapping.getInspectTemplate() == Config.INSPECT_NEVER) return null;

		if ((mapping.getInspectTemplate() == Config.INSPECT_NEVER || mapping.getInspectTemplate() == ConfigPro.INSPECT_AUTO || (pci != null && pci.isTrusted(page)))
				&& isLoad(LOAD_PHYSICAL))
			return page;
		Resource srcFile = getPhyscalFile();

		long srcLastModified = srcFile.lastModified();
		if (srcLastModified == 0L) {
			if (mapping.getInspectTemplate() == Config.INSPECT_NEVER) notFound = true;
			return null;
		}
		// Page exists
		if (page != null) {
			// if(page!=null && !recompileAlways) {
			if (srcLastModified != page.getSourceLastModified() || (page.getSourceLength() != srcFile.length())) {
				synchronized (this) {
					// synchronized (SystemUtil.createToken("PageSource", getRealpathWithVirtual())) {
					if (srcLastModified != page.getSourceLastModified() || (page.getSourceLength() != srcFile.length())) {
						// same size, maybe the content has not changed?
						boolean same = false;
						if (page.getSourceLength() == srcFile.length()) {
							try {
								same = page.getHash() == PageSourceCode.toString(this, config.getTemplateCharset()).hashCode();
							}
							catch (IOException e) {}

						}
						if (!same) {
							LogUtil.log(config, Log.LEVEL_DEBUG, "compile", "recompile [" + getDisplayPath() + "] because loaded page has changed");
							pcn.set(page = compile(config, mapping.getClassRootDirectory(), page, false, pci != null && pci.ignoreScopes()));
							page.setPageSource(this);
							signalRecompileToInspectTicker();
						}
					}
				}
			}
			page.setLoadType(LOAD_PHYSICAL);
			if (pci != null) pci.setPageUsed(page); //
			return page;
		}

		// page doesn't exist
		Resource classRootDir = mapping.getClassRootDirectory();
		Resource classFile = classRootDir.getRealResource(getJavaName() + ".class");
		boolean isNew = false;
		synchronized (this) {
			// synchronized (SystemUtil.createToken("PageSource", getRealpathWithVirtual())) {
			// new class
			if (flush || !classFile.exists()) {
				LogUtil.log(config, Log.LEVEL_TRACE, "compile", "compile [" + getDisplayPath() + "] no previous class file or flush");

				pcn.set(page = compile(config, classRootDir, null, false, pci != null && pci.ignoreScopes()));
				flush = false;
				isNew = true;
			}
			// load page
			else {
				try {
					String cn = pcn.getClassName();
					boolean done = false;
					if (cn != null) {
						try {
							LogUtil.log(config, Log.LEVEL_DEBUG, "compile", "load class from ClassLoader  [" + getDisplayPath() + "]");
							pcn.set(page = newInstance(mapping.getPhysicalClass(cn)));
							done = true;
						}
						catch (ClassNotFoundException cnfe) {
							LogUtil.log(config, "compile", cnfe);
						}
					}
					// when classFile is at least as fresh as source, try loading by name first;
					// avoids the defineClass rename storm when pcn.className was reset (e.g. by clear())
					// but the underlying class is still loaded in the PhysicalClassLoader.
					if (!done && classFile.exists() && classFile.lastModified() >= srcLastModified) {
						try {
							LogUtil.log(config, Log.LEVEL_DEBUG, "compile", "load class from ClassLoader (class file is current) [" + getDisplayPath() + "]");
							pcn.set(page = newInstance(mapping.getPhysicalClass(this.getClassName())));
							done = true;
						}
						catch (ClassNotFoundException cnfe) {
							LogUtil.log(config, "compile", cnfe);
						}
					}
					if (!done) {
						LogUtil.log(config, Log.LEVEL_DEBUG, "compile", "load class from binary  [" + getDisplayPath() + "]");
						byte[] bytes = IOUtil.toBytes(classFile);
						if (ClassUtil.isBytecode(bytes)) pcn.set(page = newInstance(mapping.getPhysicalClass(this.getClassName(), bytes)));
					}

				}
				catch (ClassFormatError cfe) {
					LogUtil.log(config, Log.LEVEL_ERROR, "compile", "size of the class file:" + classFile.length());
					LogUtil.log(config, "compile", cfe);
					pcn.reset();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					LogUtil.log(config, "compile", t);
					pcn.reset();
				}
				if (page == null) {
					LogUtil.log(config, Log.LEVEL_DEBUG, "compile", "compile  [" + getDisplayPath() + "] in case loading of the class fails");
					pcn.set(page = compile(config, classRootDir, null, false, pci != null && pci.ignoreScopes()));
					isNew = true;
				}
			}

			// check if version changed or lasMod
			if (!isNew && (srcLastModified != page.getSourceLastModified() || page.getVersion() != ConfigUtil.getCFMLEngine(config).getInfo().getFullVersionInfo())) {
				isNew = true;
				LogUtil.log(config, Log.LEVEL_DEBUG, "compile", "recompile [" + getDisplayPath() + "] because unloaded page has changed");
				pcn.set(page = compile(config, classRootDir, page, false, pci != null && pci.ignoreScopes()));
			}
			page.setPageSource(this);
			page.setLoadType(LOAD_PHYSICAL);
		}
		if (pci != null) pci.setPageUsed(page);
		return page;
	}

	public boolean releaseWhenOutdatted() {
		if (!mapping.hasPhysical() || !isLoad(LOAD_PHYSICAL)) return false;
		Page page = pcn.getPage();
		Resource srcFile = getPhyscalFile();
		long srcLastModified = srcFile.lastModified();
		// Page exists
		if (page != null) {
			if (srcLastModified == 0 || srcLastModified != page.getSourceLastModified()) {
				synchronized (this) {
					if (srcLastModified == 0 || srcLastModified != page.getSourceLastModified()) {
						if (LogUtil.doesTrace(mapping.getLog())) mapping.getLog().trace("page-source", "release [" + getDisplayPath() + "] from page source pool");
						resetLoaded();
						flush();
						return true;
					}
				}
			}
		}
		return false;
	}

	public void flush() {
		if (LogUtil.doesTrace(mapping.getLog())) mapping.getLog().trace("page-source", "flush [" + getDisplayPath() + "]");
		pcn.reset();
		flush = true;
	}

	private boolean isLoad(byte load) {
		Page page = pcn.getPage();
		return page != null && load == page.getLoadType();
	}

	private Page compile(Config config, Resource classRootDir, Page existing, boolean returnValue, boolean ignoreScopes) throws TemplateException {
		try {
			return _compile(config, classRootDir, existing, returnValue, ignoreScopes, false);
		}
		catch (RuntimeException re) {
			String msg = StringUtil.emptyIfNull(re.getMessage());
			if (StringUtil.indexOfIgnoreCase(msg, "Method code too large!") != -1) {
				throw new TemplateException("There is too much code inside the template [" + getDisplayPath() + "], " + Constants.NAME
						+ " was not able to break it into pieces, move parts of your code to an include or an external component/function", msg);
			}
			throw re;
		}
		catch (ClassFormatError e) {
			String msg = StringUtil.emptyIfNull(e.getMessage());
			if (StringUtil.indexOfIgnoreCase(msg, "Invalid method Code length") != -1) {
				throw new TemplateException("There is too much code inside the template [" + getDisplayPath() + "], " + Constants.NAME
						+ " was not able to break it into pieces, move parts of your code to an include or an external component/function", msg);
			}
			TemplateException te = new TemplateException("ClassFormatError:" + e.getMessage());
			ExceptionUtil.initCauseEL(te, e);
			throw te;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (t instanceof TemplateException) throw (TemplateException) t;
			throw new PageRuntimeException(Caster.toPageException(t));
		}
	}

	private Page _compile(Config config, Resource classRootDir, Page existing, boolean returnValue, boolean ignoreScopes, boolean split)
			throws IOException, SecurityException, IllegalArgumentException, PageException {
		ConfigWebPro cwi = (ConfigWebPro) config;

		long now;
		if ((getPhyscalFile().lastModified() + 10000) > (now = System.currentTimeMillis())) cwi.getCompiler().watch(this, now);// SystemUtil.get
		Result result;
		result = cwi.getCompiler().compile(cwi, this, cwi.getTLDs(), cwi.getFLDs(), classRootDir, returnValue, ignoreScopes);

		try {
			Class<?> clazz = mapping.getPhysicalClass(getClassName(), result.barr);
			// make sure all children are updated
			if (result.javaFunctions != null && !result.javaFunctions.isEmpty()) {
				for (JavaFunction jf: result.javaFunctions) {
					mapping.getPhysicalClass(jf.getClassName(), jf.byteCode);
				}
			}
			return newInstance(clazz);
		}
		catch (RuntimeException re) {
			String msg = StringUtil.emptyIfNull(re.getMessage());
			if (!split && StringUtil.indexOfIgnoreCase(msg, "Method code too large!") != -1) {
				return _compile(config, classRootDir, existing, returnValue, ignoreScopes, true);
			}
			else throw re;
		}
		catch (ClassFormatError cfe) {
			String msg = StringUtil.emptyIfNull(cfe.getMessage());
			if (!split && StringUtil.indexOfIgnoreCase(msg, "Invalid method Code length") != -1) {
				return _compile(config, classRootDir, existing, returnValue, ignoreScopes, true);
			}
			else throw cfe;
		}

		catch (Exception e) {
			PageException pe = Caster.toPageException(e);
			pe.setExtendedInfo("failed to load template " + getDisplayPath());
			throw pe;
		}
	}

	public Page newInstance(Class clazz)
			throws InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException {
		// if (clazz.getName().indexOf("$cf$") != -1) {
		Constructor c = clazz.getConstructor(new Class[] { PageSource.class });
		return (Page) c.newInstance(new Object[] { this });
		// }
		// return (Page) ClassUtil.loadInstance(clazz, new Object[] { this });
	}

	/**
	 * return source path as String
	 * 
	 * @return source path as String
	 */
	@Override
	public String getDisplayPath() {
		if (dspPath != null) return dspPath;

		if (!mapping.hasArchive()) {
			return dspPath = StringUtil.toString(getPhyscalFile(), null);
		}
		else if (isLoad(LOAD_PHYSICAL)) {
			return dspPath = StringUtil.toString(getPhyscalFile(), null);
		}
		else if (isLoad(LOAD_ARCHIVE)) {
			return dspPath = StringUtil.toString(getArchiveSourcePath(), null);
		}
		else {
			boolean pse = physcalExists();
			boolean ase = archiveExists();

			if (mapping.isPhysicalFirst()) {
				if (pse) return dspPath = getPhyscalFile().toString();
				else if (ase) return dspPath = getArchiveSourcePath();
				return dspPath = getPhyscalFile().toString();
			}
			if (ase) return dspPath = getArchiveSourcePath();
			else if (pse) return dspPath = getPhyscalFile().toString();
			return dspPath = getArchiveSourcePath();
		}
	}

	public boolean isComponent() {
		String ext = ResourceUtil.getExtension(getRealpath(), "");
		return Constants.isCFMLComponentExtension(ext);
	}

	/**
	 * return file object, based on physical path and realpath
	 * 
	 * @return file Object
	 */
	private String getArchiveSourcePath() {
		return "zip://" + mapping.getArchive().getAbsolutePath() + "!" + relPath;
	}

	private static String extractRealpath(String relapth, String newPath) {
		int len1 = relapth == null ? 0 : relapth.length();
		int len2 = newPath.length();
		int pos;
		char c1, c2;
		StringBuilder sb = new StringBuilder();
		boolean done = false;
		for (int i = 0; i < len1; i++) {
			c1 = relapth.charAt((len1 - 1) - i);
			pos = (len2 - 1) - i;
			c2 = pos < 0 ? c1 : newPath.charAt(pos);

			if (!done && Character.toLowerCase(c1) == Character.toLowerCase(c2)) sb.insert(0, c2);
			else {
				done = true;
				sb.insert(0, c1);
			}
		}

		return sb.toString();
	}

	/**
	 * return file object, based on physical path and realpath
	 * 
	 * @return file Object
	 */
	@Override
	public Resource getPhyscalFile() {
		if (!mapping.hasPhysical()) {
			return null;
		}
		if (physcalSource == null) {
			synchronized (this) {
				// synchronized (SystemUtil.createToken("PageSource", getRealpathWithVirtual())) {
				if (physcalSource == null) {
					Resource tmp = mapping.getPhysical().getRealResource(relPath);
					physcalSource = ResourceUtil.toExactResource(tmp);
					// fix if the case not match
					if (!tmp.getAbsolutePath().equals(physcalSource.getAbsolutePath())) {
						String relpath = extractRealpath(relPath, physcalSource.getAbsolutePath());
						// just a security!
						if (relPath.equalsIgnoreCase(relpath)) {
							this.relPath = relpath;
							createClassAndPackage();
						}
					}
				}
			}
		}
		return physcalSource;
	}

	public Resource getArchiveFile() {
		if (!mapping.hasArchive()) return null;
		if (archiveSource == null) {
			synchronized (this) {
				// synchronized (SystemUtil.createToken("PageSource", getRealpathWithVirtual())) {
				if (archiveSource == null) {
					String path = "zip://" + mapping.getArchive().getAbsolutePath() + "!" + relPath;
					archiveSource = ThreadLocalPageContext.getConfigServer().getResource(path);
				}
			}
		}
		return archiveSource;
	}

	public Resource getArchiveClass() {

		if (!mapping.hasArchive()) return null;
		if (archiveClass == null) {
			synchronized (this) {
				// synchronized (SystemUtil.createToken("PageSource", getRealpathWithVirtual())) {
				if (archiveClass == null) {
					String path = "zip://" + mapping.getArchive().getAbsolutePath() + "!" + getJavaName() + ".class";
					archiveClass = ThreadLocalPageContext.getConfigServer().getResource(path);
				}
			}
		}
		return archiveClass;

	}

	/**
	 * merge to realpath to one
	 * 
	 * @param mapping
	 * @param parentRealPath
	 * @param newRealPath
	 * @param isOutSide
	 * @return merged realpath
	 */
	private static String mergeRealPathes(String parentRealPath, String newRealPath, RefBoolean isOutSide) {
		parentRealPath = pathRemoveLast(parentRealPath, isOutSide);
		while (newRealPath.startsWith("../")) {
			parentRealPath = pathRemoveLast(parentRealPath, isOutSide);
			newRealPath = newRealPath.substring(3);
		}

		// check if come back
		// String path = parentRealPath.concat("/").concat(newRealPath);
		// print.e(path);

		/*
		 * if (path.startsWith("../") && mapping.hasPhysical() && !mapping.hasArchive()) { int count = 0; do
		 * { count++; path = path.substring(3); } while (path.startsWith("../"));
		 * 
		 * String strRoot = mapping.getPhysical().getAbsolutePath().replace('\\', '/'); if
		 * (!StringUtil.endsWith(strRoot, '/')) { strRoot += '/'; } int rootLen = strRoot.length(); String[]
		 * arr = ListUtil.toStringArray(ListUtil.listToArray(path, '/'), "");// path.split("/"); int tmpLen;
		 * for (int i = count; i > 0; i--) { if (arr.length > i) { String tmp = '/' + list(arr, 0, i);
		 * tmpLen = rootLen - tmp.length(); if (strRoot.lastIndexOf(tmp) == tmpLen && tmpLen >= 0) {
		 * StringBuilder rtn = new StringBuilder(); while (i < count - i) { count--; rtn.append("../"); }
		 * isOutSide.setValue(rtn.length() != 0); return (rtn.length() == 0 ? "/" : rtn.toString()) +
		 * list(arr, i, arr.length); } } } }
		 */
		return parentRealPath.concat("/").concat(newRealPath);
	}

	/**
	 * convert a String array to a string list, but only part of it
	 * 
	 * @param arr String Array
	 * @param from start from here
	 * @param len how many element
	 * @return String list
	 */
	private static String list(String[] arr, int from, int len) {
		StringBuilder sb = new StringBuilder();
		for (int i = from; i < len; i++) {
			sb.append(arr[i]);
			if (i + 1 != arr.length) sb.append('/');
		}
		return sb.toString();
	}

	/**
	 * remove the last elemtn of a path
	 * 
	 * @param path path to remove last element from it
	 * @param isOutSide
	 * @return path with removed element
	 */
	private static String pathRemoveLast(String path, RefBoolean isOutSide) {
		if (path.length() == 0) {
			if (isOutSide != null) isOutSide.setValue(true);
			return "..";
		}
		else if (path.endsWith("..")) {
			if (isOutSide != null) isOutSide.setValue(true);
			return path.concat("/..");// path+"/..";
		}
		return path.substring(0, path.lastIndexOf('/'));
	}

	@Override
	public String getRealpath() {
		return relPath;
	}

	@Override
	public String getRealpathWithVirtual() {
		if (mapping.getVirtual().length() == 1 || mapping.ignoreVirtual()) return relPath;
		return mapping.getVirtual() + relPath;
	}

	private String _getClassName() {
		if (className == null) createClassAndPackage();
		return className;
	}

	@Override
	public String getClassName() {
		if (className == null) createClassAndPackage();
		if (packageName.length() == 0) return className;
		return packageName.concat(".").concat(className);
	}

	@Override
	public String getFileName() {
		if (fileName == null) createClassAndPackage();
		return fileName;
	}

	@Override
	public String getJavaName() {
		if (javaName == null) createClassAndPackage();
		return javaName;
	}

	private String _getPackageName() {
		if (packageName == null) createClassAndPackage();
		return packageName;
	}

	@Override
	public String getComponentName() {
		if (compName == null) createComponentName();
		return compName;
	}

	private void createClassAndPackage() {
		if (className == null) {
			synchronized (this) {
				// synchronized (SystemUtil.createToken("PageSource", getRealpathWithVirtual())) {
				if (className == null) {
					String str = relPath;
					StringBuilder packageName = new StringBuilder();
					StringBuilder javaName = new StringBuilder();
					String[] arr = ListUtil.toStringArrayEL(ListUtil.listToArrayRemoveEmpty(str, '/'));

					String varName, className = null, fileName = null;
					for (int i = 0; i < arr.length; i++) {
						if (i == (arr.length - 1)) {
							int index = arr[i].lastIndexOf('.');
							if (index != -1) {
								String ext = arr[i].substring(index + 1);
								varName = StringUtil.toVariableName(arr[i].substring(0, index) + "_" + ext);
							}
							else varName = StringUtil.toVariableName(arr[i]);
							varName = varName + (Constants.CFML_CLASS_SUFFIX);
							className = varName.toLowerCase();
							fileName = arr[i];
						}
						else {
							varName = StringUtil.toVariableName(arr[i]);
							if (i != 0) {
								packageName.append('.');
							}
							packageName.append(varName);
						}
						javaName.append('/');
						javaName.append(varName);
					}

					this.packageName = packageName.toString().toLowerCase();
					this.javaName = javaName.toString().toLowerCase();
					this.fileName = fileName;
					this.className = className;
				}
			}
		}
	}

	private void createComponentName() {
		Resource res = this.getPhyscalFile();
		String str = null;
		final String relPath = this.relPath;
		if (res != null) {

			str = res.getAbsolutePath();
			int begin = str.length() - relPath.length();
			if (begin < 0) { // TODO patch, analyze the complete functionality and improve
				str = ListUtil.last(str, "\\/", true);
			}
			else {
				str = str.substring(begin);
				if (!str.equalsIgnoreCase(relPath)) {
					str = relPath;
				}
			}
		}
		else str = relPath;

		StringBuilder compName = new StringBuilder();
		String[] arr;

		// virtual part
		if (!mapping.ignoreVirtual()) {
			arr = ListUtil.toStringArrayEL(ListUtil.listToArrayRemoveEmpty(mapping.getVirtual(), "\\/"));
			for (int i = 0; i < arr.length; i++) {
				if (compName.length() > 0) compName.append('.');
				compName.append(arr[i]);
			}
		}

		// physical part
		arr = ListUtil.toStringArrayEL(ListUtil.listToArrayRemoveEmpty(str, '/'));
		for (int i = 0; i < arr.length; i++) {
			if (compName.length() > 0) compName.append('.');
			if (i == (arr.length - 1)) {
				compName.append(ResourceUtil.removeExtension(arr[i], arr[i]));
			}
			else compName.append(arr[i]);
		}
		this.compName = compName.toString();
	}

	@Override
	public Mapping getMapping() {
		return mapping;
	}

	@Override
	public boolean exists() {
		if (mapping.isPhysicalFirst()) return physcalExists() || archiveExists();
		return archiveExists() || physcalExists();
	}

	@Override
	public boolean physcalExists() {
		return ResourceUtil.exists(getPhyscalFile());
	}

	private boolean archiveExists() {
		if (!mapping.hasArchive()) return false;
		try {
			String clazz = getClassName();
			if (clazz == null) return getArchiveFile().exists();
			mapping.getArchiveClass(clazz);
			return true;
		}
		catch (ClassNotFoundException cnfe) {
			return false;
		}
		catch (Exception e) {
			return getArchiveFile().exists();
		}
	}

	/**
	 * return the inputstream of the source file
	 * 
	 * @return return the inputstream for the source from physical or archive
	 * @throws FileNotFoundException
	 */
	private InputStream getSourceAsInputStream() throws IOException {
		if (!mapping.hasArchive()) return IOUtil.toBufferedInputStream(getPhyscalFile().getInputStream());
		else if (isLoad(LOAD_PHYSICAL)) return IOUtil.toBufferedInputStream(getPhyscalFile().getInputStream());
		else if (isLoad(LOAD_ARCHIVE)) {
			StringBuilder name = new StringBuilder(_getPackageName().replace('.', '/'));
			if (name.length() > 0) name.append("/");
			name.append(getFileName());

			return mapping.getArchiveResourceAsStream(name.toString());
		}
		else {
			return null;
		}
	}

	@Override
	public String[] getSource() throws IOException {
		// if(source!=null) return source;
		InputStream is = getSourceAsInputStream();
		if (is == null) return null;
		try {
			return IOUtil.toStringArray(IOUtil.getReader(is, getMapping().getConfig().getTemplateCharset()));
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) return true;
		if (!(obj instanceof PageSource)) return false;

		/*
		 * if (LogUtil.does(getMapping().getConfig().getLog("application"), Log.LEVEL_DEBUG)) { PageSource
		 * ps = ((PageSource) obj); LogUtil.log(Log.LEVEL_DEBUG, "page-source", "compare [" +
		 * getDisplayPath() + "]\n"
		 * 
		 * + "- class-name(" + getClassName().equals(ps.getClassName()) + "): " + getClassName() + ":" +
		 * ps.getClassName() + "- mapping-virtual(" + (getMapping() == ps.getMapping()) + "): " +
		 * getMapping().getVirtual() + ":" + ps.getMapping().getVirtual()
		 * 
		 * ); }
		 */

		return getDisplayPath().equals(((PageSource) obj).getDisplayPath());
	}

	/**
	 * is given object equal to this
	 *
	 * @param ps
	 * @return is same
	 */
	public boolean equals(PageSource ps) {
		if (this == ps) return true;
		if (ps == null) return false;

		/*
		 * if (LogUtil.does(getMapping().getConfig().getLog("application"), Log.LEVEL_DEBUG)) {
		 * LogUtil.log(Log.LEVEL_DEBUG, "page-source", "compare [" + getDisplayPath() + "]\n"
		 * 
		 * + "- class-name(" + getClassName().equals(ps.getClassName()) + "): " + getClassName() + ":" +
		 * ps.getClassName() + "- mapping-virtual(" + (getMapping() == ps.getMapping()) + "): " +
		 * getMapping().getVirtual() + ":" + ps.getMapping().getVirtual()
		 * 
		 * ); }
		 */
		return getDisplayPath().equals(ps.getDisplayPath());
	}

	@Override
	@Deprecated
	public PageSource getRealPage(String realPath) {
		return getRealPageSource(null, realPath);
	}

	// FUTURE add to interface
	public PageSource getRealPageSource(PageContext pc, String realPath) {
		RefBoolean _isOutSide = new RefBooleanImpl(isOutSide);
		String realResolved = resolveReal(realPath, _isOutSide);

		// in case we step outside the mapping we need to open up to all mappings
		if (realResolved.startsWith(".")) {
			// First try to resolve using the physical path of the mapping
			if (mapping.hasPhysical()) {
				Resource targetResource = mapping.getPhysical().getRealResource(realResolved);
				if (targetResource.exists()) {
					PageSource ps = ThreadLocalPageContext.get(pc).toPageSource(targetResource, null);
					if (ps != null) {
						return ps;
					}
				}
			}

			// Fall back to virtual path resolution for archives or when physical resolution fails
			realResolved = mergeRealPathes(mapping.getVirtualLowerCaseWithSlash(), realResolved, null);
			PageContextImpl pci = (PageContextImpl) ThreadLocalPageContext.get(pc);
			return pci.getPageSource(realResolved);
		}
		return mapping.getPageSource(realResolved, _isOutSide.toBooleanValue());
	}

	private String resolveReal(String realPath, RefBoolean _isOutSide) {
		if (realPath.equals(".") || realPath.equals("..")) realPath += '/';
		else realPath = realPath.replace('\\', '/');

		if (realPath.indexOf('/') == 0 || ResourceUtil.isWindowsPath(realPath)) {
			_isOutSide.setValue(false);
		}
		else if (realPath.startsWith("./")) {
			realPath = mergeRealPathes(this.relPath, realPath.substring(2), _isOutSide);
		}
		else {
			realPath = mergeRealPathes(this.relPath, realPath, _isOutSide);
		}
		return realPath;
	}

	@Override
	public final void setLastAccessTime(long lastAccess) {
		this.lastAccess = lastAccess;
	}

	@Override
	public final long getLastAccessTime() {
		return lastAccess;
	}

	@Override
	public final void setLastAccessTime() {
		accessCount.plus(1);
		this.lastAccess = System.currentTimeMillis();
	}

	@Override
	public final int getAccessCount() {
		return accessCount.toInt();
	}

	@Override
	public Resource getResource() {
		Resource p = getPhyscalFile();
		Resource a = getArchiveFile();
		if (mapping.isPhysicalFirst()) {
			if (a == null) return p;
			if (p == null) return a;

			if (p.exists()) return p;
			if (a.exists()) return a;
			return p;
		}
		if (p == null) return a;
		if (a == null) return p;

		if (a.exists()) return a;
		if (p.exists()) return p;
		return a;

		// return getArchiveFile();
	}

	@Override
	public Resource getResourceTranslated(PageContext pc) throws ExpressionException {
		Resource res = null;
		if (!isLoad(LOAD_ARCHIVE)) res = getPhyscalFile();

		// there is no physical resource
		if (res == null) {
			String path = getDisplayPath();
			if (path != null) {
				if (path.startsWith("ra://")) path = "zip://" + path.substring(5);
				res = ResourceUtil.toResourceNotExisting(pc, null, path, false, false);
			}
		}
		return res;
	}

	public void clear() {
		mapping.clear(pcn.getClassName());
		pcn.reset();
		notFound = false;
	}

	/**
	 * clear page, but only when page use the same classloader as provided
	 *
	 * @param cl
	 */
	public boolean clear(ClassLoader cl) {
		Page page = pcn.getPage();
		if (page != null && page.getClass().getClassLoader().equals(cl)) {
			pcn.reset();
			notFound = false;
			return true;
		}
		return false;
	}

	public boolean isLoad() {
		return pcn.getPage() != null;//// load!=LOAD_NONE;
	}

	@Override
	public String toString() {
		return getDisplayPath();
	}

	public static PageSource best(PageSource[] arr) {
		if (ArrayUtil.isEmpty(arr)) return null;
		if (arr.length == 1) return arr[0];
		for (int i = 0; i < arr.length; i++) {
			if (pageExist(arr[i])) return arr[i];
		}
		return arr[0];
	}

	public static Resource best(Resource[] arr) {
		if (ArrayUtil.isEmpty(arr)) return null;
		if (arr.length == 1) return arr[0];
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != null && arr[i].exists()) return arr[i];
		}
		return arr[0];
	}

	public static boolean pageExist(PageSource ps) {
		return (ps.getMapping().isTrusted() && ((PageSourceImpl) ps).isLoad()) || ps.exists();
	}

	public static Page loadPage(PageContext pc, PageSource[] arr, Page defaultValue) throws PageException {
		if (ArrayUtil.isEmpty(arr)) return null;
		Page p;
		for (int i = 0; i < arr.length; i++) {
			p = arr[i].loadPageThrowTemplateException(pc, false, (Page) null);
			if (p != null) return p;
		}
		return defaultValue;
	}

	public static Page loadPage(PageContext pc, PageSource[] arr) throws PageException {
		if (ArrayUtil.isEmpty(arr)) return null;

		Page p;
		for (int i = 0; i < arr.length; i++) {
			p = arr[i].loadPageThrowTemplateException(pc, false, (Page) null);
			if (p != null) return p;
		}
		throw new MissingIncludeException(arr[0]);
	}

	@Override
	@Deprecated
	public int getDialect() {
		return CFMLEngine.DIALECT_CFML;
	}

	/**
	 * return if the PageSource represent a template (no component,no interface)
	 * 
	 * @param pc
	 * @param ps
	 * @param defaultValue
	 * @return
	 */
	public static boolean isTemplate(PageContext pc, PageSource ps, boolean defaultValue) {
		try {
			return !(ps.loadPage(pc, false) instanceof CIPage);
		}
		catch (PageException e) {
			LogUtil.log(pc, PageSourceImpl.class.getName(), e);
			return defaultValue;
		}
	}

	@Override
	public boolean executable() {
		return ((getMapping().getInspectTemplate() == Config.INSPECT_NEVER || getMapping().getInspectTemplate() == ConfigPro.INSPECT_AUTO) && isLoad()) || exists();
	}

	public void resetLoaded() {
		if (LogUtil.doesTrace(mapping.getLog())) mapping.getLog().trace("page-source", "reset loaded [" + getDisplayPath() + "]");
		Page p = pcn.getPage();
		if (p != null) p.setLoadType((byte) 0);
		notFound = false;
	}

	private void signalRecompileToInspectTicker() {
		if (mapping.getInspectTemplate() != ConfigPro.INSPECT_AUTO) return;
		Config cfg = mapping.getConfig();
		ConfigServerImpl cs;
		if (cfg instanceof ConfigServerImpl) cs = (ConfigServerImpl) cfg;
		else if (cfg instanceof ConfigWebImpl) cs = ((ConfigWebImpl) cfg).getConfigServerImpl();
		else return;
		cs.requestFastTick();
	}

	@Override
	public final int hashCode() {
		return getMapping().getVirtual().hashCode() + getClassName().hashCode();
	}
}