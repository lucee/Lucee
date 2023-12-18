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
package lucee.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.MappingUtil;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ArrayUtil;

/**
 * Mapping class
 */
public final class MappingImpl implements Mapping {

	private static final long serialVersionUID = 6431380676262041196L;

	private static final Class<PageSource> SUBPAGE_CONSTR = PageSource.class;

	private String virtual;
	private String lcVirtual;
	private boolean topLevel;
	private short inspect;
	private boolean physicalFirst;
	private transient Map<String, PhysicalClassLoaderReference> loaders = new HashMap<>();
	private Resource archive;

	private final Config config;
	private Resource classRootDirectory;
	private final PageSourcePool pageSourcePool = new PageSourcePool();

	private boolean readonly = false;
	private boolean hidden = false;
	private final String strArchive;

	private final String strPhysical;
	private Resource physical;

	private String lcVirtualWithSlash;
	private Map<String, SoftReference<Object>> customTagPath = new ConcurrentHashMap<String, SoftReference<Object>>();

	private boolean appMapping;
	private boolean ignoreVirtual;

	private ApplicationListener appListener;

	private Bundle archiveBundle;

	private long archMod;

	private int listenerMode;
	private int listenerType;

	private boolean checkPhysicalFromWebroot;
	private boolean checkArchiveFromWebroot;

	public MappingImpl(Config config, String virtual, String strPhysical, String strArchive, short inspect, boolean physicalFirst, boolean hidden, boolean readonly,
			boolean topLevel, boolean appMapping, boolean ignoreVirtual, ApplicationListener appListener, int listenerMode, int listenerType) {
		this(config, virtual, strPhysical, strArchive, inspect, physicalFirst, hidden, readonly, topLevel, appMapping, ignoreVirtual, appListener, listenerMode, listenerType, true,
				true);
	}

	/**
	 * constructor of the class
	 * 
	 * @param config
	 * @param virtual
	 * @param strPhysical
	 * @param strArchive
	 * @param inspect
	 * @param physicalFirst
	 * @param hidden
	 * @param readonly
	 * @param topLevel
	 * @param appMapping
	 * @param ignoreVirtual
	 * @param appListener
	 */
	public MappingImpl(Config config, String virtual, String strPhysical, String strArchive, short inspect, boolean physicalFirst, boolean hidden, boolean readonly,
			boolean topLevel, boolean appMapping, boolean ignoreVirtual, ApplicationListener appListener, int listenerMode, int listenerType, boolean checkPhysicalFromWebroot,
			boolean checkArchiveFromWebroot) {
		this.ignoreVirtual = ignoreVirtual;
		this.config = config;
		this.hidden = hidden;
		this.readonly = readonly;
		this.strPhysical = StringUtil.isEmpty(strPhysical, true) ? null : strPhysical.trim();
		this.strArchive = StringUtil.isEmpty(strArchive, true) ? null : strArchive.trim();
		this.inspect = inspect;
		this.topLevel = topLevel;
		this.appMapping = appMapping;
		this.physicalFirst = physicalFirst;
		this.appListener = appListener;
		this.listenerMode = listenerMode;
		this.listenerType = listenerType;
		this.checkPhysicalFromWebroot = checkPhysicalFromWebroot;
		this.checkArchiveFromWebroot = checkArchiveFromWebroot;

		// virtual
		if (virtual.length() == 0) virtual = "/";
		if (!virtual.equals("/") && virtual.endsWith("/")) this.virtual = virtual.substring(0, virtual.length() - 1);
		else this.virtual = virtual;
		this.lcVirtual = this.virtual.toLowerCase();
		this.lcVirtualWithSlash = lcVirtual.endsWith("/") ? this.lcVirtual : this.lcVirtual + '/';
	}

	private void initPhysical() {
		ServletContext cs = (config instanceof ConfigWeb) ? ((ConfigWeb) config).getServletContext() : null;
		physical = ConfigWebUtil.getResource(cs, strPhysical, config.getConfigDir(), FileUtil.TYPE_DIR, config, checkPhysicalFromWebroot, false);
		if (archive == null) this.physicalFirst = true;
		else if (physical == null) this.physicalFirst = false;

	}

	private void initArchive() {
		ServletContext cs = (config instanceof ConfigWeb) ? ((ConfigWeb) config).getServletContext() : null;
		archive = ConfigWebUtil.getResource(cs, strArchive, config.getConfigDir(), FileUtil.TYPE_FILE, config, checkArchiveFromWebroot, true);
		loadArchive();

		if (archive == null) this.physicalFirst = true;
		else if (physical == null) this.physicalFirst = false;
	}

	private void loadArchive() {
		if (archive == null || archMod == archive.lastModified()) return;

		CFMLEngine engine = ConfigWebUtil.getEngine(config);
		BundleContext bc = engine.getBundleContext();
		try {
			archiveBundle = OSGiUtil.installBundle(bc, getArchive(), true);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			archMod = archive.lastModified();
			LogUtil.log(config, "OSGi", t);
			archive = null;
		}
	}

	@Override
	public Class<?> getArchiveClass(String className) throws ClassNotFoundException {
		getArchive();// this calls init the archive if necessary
		if (archiveBundle != null) {
			return archiveBundle.loadClass(className);
		}
		// else if(archiveClassLoader!=null) return archiveClassLoader.loadClass(className);
		throw new ClassNotFoundException("there is no archive context to load " + className + " from it");
	}

	@Override
	public Class<?> getArchiveClass(String className, Class<?> defaultValue) {
		getArchive();// this calls init the archive if necessary
		try {
			if (archiveBundle != null) return archiveBundle.loadClass(className);
			// else if(archiveClassLoader!=null) return archiveClassLoader.loadClass(className);
		}
		catch (ClassNotFoundException e) {
		}

		return defaultValue;
	}

	@Override
	public InputStream getArchiveResourceAsStream(String name) {
		// MUST implement
		return null;
	}

	public Class<?> loadClass(String className) {
		Class<?> clazz;
		if (isPhysicalFirst()) {
			clazz = getPhysicalClass(className, (Class<?>) null);
			if (clazz != null) return clazz;
			clazz = getArchiveClass(className, null);
			if (clazz != null) return clazz;
		}

		clazz = getArchiveClass(className, null);
		if (clazz != null) return clazz;
		clazz = getPhysicalClass(className, (Class<?>) null);
		if (clazz != null) return clazz;

		return null;
	}

	private Class<?> loadClass(String className, byte[] code) throws IOException, ClassNotFoundException {

		PhysicalClassLoaderReference pclr = loaders.get(className);
		PhysicalClassLoader pcl = pclr == null ? null : pclr.get();
		if (pcl == null || code != null) {// || pcl.getSize(true) > 3
			if (pcl != null) {
				pcl.clear();
			}
			pcl = new PhysicalClassLoader(config, getClassRootDirectory(), pageSourcePool);
			synchronized (loaders) {
				loaders.put(className, new PhysicalClassLoaderReference(pcl));
			}
		}

		if (code != null) {
			try {
				return pcl.loadClass(className, code);
			}
			catch (UnmodifiableClassException e) {
				throw ExceptionUtil.toIOException(e);
			}
		}
		return pcl.loadClass(className);
	}

	public void cleanLoaders() {
		pageSourcePool.cleanLoaders();
	}

	public void clear(String className) {
		PhysicalClassLoaderReference ref = loaders.remove(className);
		PhysicalClassLoader pcl;
		if (ref != null) {
			pcl = ref.get();
			if (pcl != null) {
				pcl.clear(false);
			}
		}
	}

	public int getSize() {
		return loaders.size();
	}

	@Override
	public Class<?> getPhysicalClass(String className) throws ClassNotFoundException, IOException {
		return loadClass(className, null);
		// return touchPhysicalClassLoader(className.contains("_cfc$cf")).loadClass(className);
	}

	public Class<?> getPhysicalClass(String className, Class<?> defaultValue) {
		try {
			return getPhysicalClass(className);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public Class<?> getPhysicalClass(String className, byte[] code) throws IOException {
		try {
			return loadClass(className, code);
		}
		catch (Exception e) {
			throw ExceptionUtil.toIOException(e);
		}

		// return touchPhysicalClassLoader(className.contains("_cfc$cf")).loadClass(className, code);

		// boolean isCFC = className.indexOf("_cfc$")!=-1;//aaaa ResourceUtil.getExtension(ps.getRealpath(),
		// "").equalsIgnoreCase("cfc");
		// return touchClassLoader().loadClass(className,code,isCFC);
	}

	/**
	 * remove all Page from Pool using this classloader
	 * 
	 * @param cl
	 */
	public void clearPages(ClassLoader cl) {
		pageSourcePool.clearPages(cl);
	}

	public void clearUnused() {
		pageSourcePool.cleanLoaders();
	}

	public void resetPages(ClassLoader cl) {
		pageSourcePool.resetPages(cl);
	}

	@Override
	public Resource getPhysical() {
		if (physical == null && strPhysical != null) initPhysical(); // possible that the target path only exists AFTER startup
		return physical;
	}

	@Override
	public String getVirtualLowerCase() {
		return lcVirtual;
	}

	@Override
	public String getVirtualLowerCaseWithSlash() {
		return lcVirtualWithSlash;
	}

	@Override
	public Resource getArchive() {
		if (archive == null && strArchive != null) initArchive(); // possible that the target path only exists AFTER startup
		return archive;
	}

	@Override
	public boolean hasArchive() {
		return getArchive() != null;
	}

	@Override
	public boolean hasPhysical() {
		return getPhysical() != null;
	}

	@Override
	public Resource getClassRootDirectory() {
		if (classRootDirectory == null) {
			String path = getPhysical() != null ? getPhysical().getAbsolutePath() : getArchive().getAbsolutePath();

			classRootDirectory = config.getClassDirectory().getRealResource(StringUtil.toIdentityVariableName(path));
		}
		return classRootDirectory;
	}

	/**
	 * clones a mapping and make it readOnly
	 * 
	 * @param config
	 * @return cloned mapping
	 * @throws IOException
	 */
	public MappingImpl cloneReadOnly(Config config) {
		return new MappingImpl(config, virtual, strPhysical, strArchive, inspect, physicalFirst, hidden, true, topLevel, appMapping, ignoreVirtual, appListener, listenerMode,
				listenerType, checkPhysicalFromWebroot, checkArchiveFromWebroot);
	}

	@Override
	public short getInspectTemplate() {
		if (inspect == Config.INSPECT_UNDEFINED) return config.getInspectTemplate();
		return inspect;
	}

	/**
	 * inspect template setting (Config.INSPECT_*), if not defined with the mapping,
	 * Config.INSPECT_UNDEFINED is returned
	 * 
	 * @return
	 */
	public short getInspectTemplateRaw() {
		return inspect;
	}

	@Override
	public PageSource getPageSource(String realPath) {
		boolean isOutSide = false;
		realPath = realPath.replace('\\', '/');
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
		return getPageSource(realPath, isOutSide);
	}

	public Resource getResource(String realPath) {
		// TODO merge the functionality with the method above
		boolean isOutSide = false;
		realPath = realPath.replace('\\', '/');
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
		return getResource(realPath, isOutSide);
	}

	@Override
	public PageSource getPageSource(String path, boolean isOut) {
		PageSource source = pageSourcePool.getPageSource(path, true);
		if (source != null) return source;

		PageSourceImpl newSource = new PageSourceImpl(this, path, isOut);
		pageSourcePool.setPage(path, newSource);

		return newSource;// new PageSource(this,path);
	}

	/**
	 * in contrust to getPageSource this function will not store the requested path in the pool and
	 * 
	 * @param path
	 * @param isOut
	 * @return
	 */
	public Resource getResource(String path, boolean isOut) {
		// TODO rewrite so PageSourceImpl not need to be loaded
		return new PageSourceImpl(this, path, isOut).getResource();
	}

	// to not delete,used for argus monitor!
	public PageSourcePool getPageSourcePool() {
		return pageSourcePool;
	}

	public Array getDisplayPathes(Array arr) throws PageException {
		List<PageSource> values = pageSourcePool.values(true);
		for (PageSource ps: values) {
			if (ps != null) arr.append(ps.getDisplayPath());
		}
		return arr;
	}

	public List<PageSource> getPageSources(boolean loaded) {
		return pageSourcePool.values(loaded);
	}

	@Override
	public void check() {
		// make sure everything is loaded
		getPhysical();
		getArchive();
	}

	@Override
	public Config getConfig() {
		return config;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public boolean isPhysicalFirst() {
		check();
		// now we can trust the result
		return physicalFirst;
	}

	@Override
	public boolean isReadonly() {
		return readonly;
	}

	@Override
	public String getStrArchive() {
		return strArchive;
	}

	@Override
	public String getStrPhysical() {
		return strPhysical;
	}

	@Override
	@Deprecated
	public boolean isTrusted() {
		return getInspectTemplate() == Config.INSPECT_NEVER;
	}

	@Override
	public String getVirtual() {
		return virtual;
	}

	public boolean isAppMapping() {
		return appMapping;
	}

	@Override
	public boolean isTopLevel() {
		return topLevel;
	}

	public PageSource getCustomTagPath(String name, boolean doCustomTagDeepSearch) {
		return searchFor(name, name.toLowerCase().trim(), doCustomTagDeepSearch);
	}

	public boolean ignoreVirtual() {
		return ignoreVirtual;
	}

	private PageSource searchFor(String filename, String lcName, boolean doCustomTagDeepSearch) {
		PageSource source = getPageSource(filename);
		if (isOK(source)) {
			return source;
		}
		customTagPath.remove(lcName);
		if (doCustomTagDeepSearch) {
			source = MappingUtil.searchMappingRecursive(this, filename, false);
			if (isOK(source)) return source;
		}
		return null;
	}

	public static boolean isOK(PageSource ps) {
		if (ps == null) return false;
		return ps.executable();
	}

	public static PageSource isOK(PageSource[] arr) {
		if (ArrayUtil.isEmpty(arr)) return null;
		for (int i = 0; i < arr.length; i++) {
			if (isOK(arr[i])) return arr[i];
		}
		return null;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return toString(false);
	}

	private String toString(boolean forCompare) {
		return new StringBuilder().append("StrPhysical:").append(getStrPhysical()).append(";StrArchive:").append(getStrArchive()).append(";Virtual:").append(getVirtual())
				.append(";Archive:").append(getArchive()).append(";Physical:").append(getPhysical()).append(";topLevel:").append(topLevel).append(";inspect:")
				.append(ConfigWebUtil.inspectTemplate(getInspectTemplateRaw(), "")).append(";physicalFirst:").append(physicalFirst).append(";hidden:").append(hidden)
				.append(";readonly:").append(forCompare ? "" : readonly).append(";").toString();

	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof MappingImpl)) return false;
		return ((MappingImpl) o).toString(true).equals(toString(true));
	}

	public ApplicationListener getApplicationListener() {
		if (appListener != null) return appListener;
		return config.getApplicationListener();
	}

	public boolean getDotNotationUpperCase() {
		return ((ConfigPro) config).getDotNotationUpperCase();
	}

	public void shrink() {
		// MUST implement

	}

	@Override
	public int getListenerMode() {
		return listenerMode;
	}

	@Override
	public int getListenerType() {
		return listenerType;
	}

	public void flush() {
		pageSourcePool.clear();
	}

	public SerMapping toSerMapping() {
		return new SerMapping("application", getVirtualLowerCase(), getStrPhysical(), getStrArchive(), isPhysicalFirst(), ignoreVirtual());
	}

	public static class SerMapping implements Serializable {

		public final String type;
		public final String virtual;
		public final String physical;
		public final String archive;
		public final boolean physicalFirst;
		public final boolean ignoreVirtual;

		public SerMapping(String type, String virtual, String physical, String archive, boolean physicalFirst, boolean ignoreVirtual) {
			this.type = type;
			this.virtual = virtual;
			this.physical = physical;
			this.archive = archive;
			this.physicalFirst = physicalFirst;
			this.ignoreVirtual = ignoreVirtual;
		}

		public Mapping toMapping() {
			ConfigWebPro cwi = (ConfigWebPro) ThreadLocalPageContext.getConfig();
			return cwi.getApplicationMapping(type, virtual, physical, archive, physicalFirst, ignoreVirtual);
		}
	}

	public static CIPage loadCIPage(PageSource ps, String className) {
		// TODO check if the sub class itself has changed or not, maybe just the main class has, if there is
		// no change there is no need to load it new
		try {
			MappingImpl m = ((MappingImpl) ps.getMapping());
			Resource res = m.getClassRootDirectory().getRealResource(className + ".class");
			String cn = className.replace('/', '.').replace('\\', '.');
			Class<?> clazz = m.loadClass(cn, IOUtil.toBytes(res));
			return (CIPage) clazz.getConstructor(SUBPAGE_CONSTR).newInstance(ps);
		}
		catch (Exception e) {
			throw Caster.toPageRuntimeException(e);
		}
	}

	private static class PhysicalClassLoaderReference extends SoftReference<PhysicalClassLoader> {

		private long lastModified;

		public PhysicalClassLoaderReference(PhysicalClassLoader pcl) {
			super(pcl);
			this.lastModified = System.currentTimeMillis();
		}

		@Override
		public PhysicalClassLoader get() {
			this.lastModified = System.currentTimeMillis();
			return super.get();
		}

		public long lastModified() {
			return lastModified;
		}
	}

}