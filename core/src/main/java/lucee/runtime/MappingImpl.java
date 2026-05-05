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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import jakarta.servlet.ServletContext;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.MappingUtil;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.PhysicalClassLoaderFactory;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
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
	private final short inspect;
	private final int inspectTemplateAutoIntervalSlow;
	private final int inspectTemplateAutoIntervalFast;

	private boolean physicalFirst;
	private Resource archive;

	private final Config config;
	private Resource classRootDirectory;
	private final PageSourcePool pageSourcePool = new PageSourcePool(this);

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

	private long startTime = System.currentTimeMillis();

	private short configInspect;

	private Log log;

	private short source = 0;

	public MappingImpl(Config config, String virtual, String strPhysical, String strArchive, short inspect, int inspectTemplateAutoIntervalSlow,
			int inspectTemplateAutoIntervalFast, boolean physicalFirst, boolean hidden, boolean readonly, boolean topLevel, boolean appMapping, boolean ignoreVirtual,
			ApplicationListener appListener, int listenerMode, int listenerType) {
		this(config, virtual, strPhysical, strArchive, inspect, inspectTemplateAutoIntervalSlow, inspectTemplateAutoIntervalFast, physicalFirst, hidden, readonly, topLevel,
				appMapping, ignoreVirtual, appListener, listenerMode, listenerType, true, true);
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
	public MappingImpl(Config config, String virtual, String strPhysical, String strArchive, short inspect, int inspectTemplateAutoIntervalSlow,
			int inspectTemplateAutoIntervalFast, boolean physicalFirst, boolean hidden, boolean readonly, boolean topLevel, boolean appMapping, boolean ignoreVirtual,
			ApplicationListener appListener, int listenerMode, int listenerType, boolean checkPhysicalFromWebroot, boolean checkArchiveFromWebroot) {
		this.ignoreVirtual = ignoreVirtual;
		this.config = config;
		this.hidden = hidden;
		this.readonly = readonly;
		this.strPhysical = StringUtil.isEmpty(strPhysical, true) ? null : strPhysical.trim();
		this.strArchive = StringUtil.isEmpty(strArchive, true) ? null : strArchive.trim();
		this.configInspect = config.getInspectTemplate();
		this.inspect = inspect;
		this.inspectTemplateAutoIntervalSlow = inspectTemplateAutoIntervalSlow;
		this.inspectTemplateAutoIntervalFast = inspectTemplateAutoIntervalFast;
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
		this.log = ThreadLocalPageContext.getLog(config, "application");
	}

	public long getStartTime() {
		return startTime;
	}

	public Log getLog() {
		return log;
	}

	private void initPhysical() {
		if (physical == null && strPhysical != null) {
			synchronized (this) {
				if (physical == null && strPhysical != null) {
					ServletContext cs = (config instanceof ConfigWeb) ? ((ConfigWeb) config).getServletContext() : null;
					physical = ConfigUtil.getResource(cs, strPhysical, config.getConfigDir(), FileUtil.TYPE_DIR, config, checkPhysicalFromWebroot, false);
					if (strArchive == null) this.physicalFirst = true;
					else if (physical == null) this.physicalFirst = false;
				}
			}
		}
	}

	private void initArchive() {
		if (archive == null && strArchive != null) {
			synchronized (this) {
				if (archive == null && strArchive != null) {
					ServletContext cs = (config instanceof ConfigWeb) ? ((ConfigWeb) config).getServletContext() : null;
					Resource tmp = ConfigUtil.getResource(cs, strArchive, config.getConfigDir(), FileUtil.TYPE_FILE, config, checkArchiveFromWebroot, true);

					if (tmp == null || archMod == tmp.lastModified()) return;
					CFMLEngine engine = ConfigUtil.getEngine(config);
					BundleContext bc = engine.getBundleContext();
					try {
						archiveBundle = OSGiUtil.installBundle(bc, tmp, true);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						archMod = tmp.lastModified();
						config.getLog("application").error("OSGi", "failed to load archive [" + tmp + "], archive is ignored", t);
						tmp = null;
					}

					if (tmp == null && strPhysical != null) this.physicalFirst = true;
					else if (strPhysical == null) this.physicalFirst = false;
					archive = tmp;
				}
			}
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
		catch (ClassNotFoundException e) {}

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
		PhysicalClassLoader pcl = PhysicalClassLoaderFactory.getPhysicalClassLoader(config, getClassRootDirectory(), false);

		if (code != null) {
			try {
				return pcl.loadClass(className, code);
			}
			catch (UnmodifiableClassException e) {
				pcl = PhysicalClassLoaderFactory.getPhysicalClassLoader(config, getClassRootDirectory(), true);
				try {
					return pcl.loadClass(className, code);
				}
				catch (UnmodifiableClassException ex) {
					throw ExceptionUtil.toIOException(ex);
				}
			}
		}
		return pcl.loadClass(className);
	}

	public void cleanLoaders() {
		pageSourcePool.cleanLoaders();
	}

	public void clear(String className) {

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
	 * @return count of pages cleared
	 */
	public int clearPages(ClassLoader cl) {
		return pageSourcePool.clearPages(cl);
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
			Resource tmp = getPhysical();
			if (tmp == null) tmp = getArchive();
			if (tmp != null) {
				classRootDirectory = config.getClassDirectory().getRealResource(StringUtil.toIdentityVariableName(tmp.getAbsolutePath()));
			}
		}
		return classRootDirectory;
	}

	/**
	 * clones a mapping and make it readOnly
	 * 
	 * @param config
	 * @return cloned mapping
	 */
	public MappingImpl cloneReadOnly(Config config) {
		return new MappingImpl(config, virtual, strPhysical, strArchive, inspect, inspectTemplateAutoIntervalSlow, inspectTemplateAutoIntervalFast, physicalFirst, hidden, true,
				topLevel, appMapping, ignoreVirtual, appListener, listenerMode, listenerType, checkPhysicalFromWebroot, checkArchiveFromWebroot);
	}

	@Override
	public short getInspectTemplate() {
		if (inspect == Config.INSPECT_UNDEFINED) return config.getInspectTemplate();
		return inspect;
	}

	public short getConfigInspectTemplate() {
		return configInspect;
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

	public int getInspectTemplateAutoInterval(boolean slow) {
		if (slow) {
			if (inspectTemplateAutoIntervalSlow <= ConfigPro.INSPECT_INTERVAL_UNDEFINED) return ((ConfigPro) config).getInspectTemplateAutoInterval(slow);
			return inspectTemplateAutoIntervalSlow;
		}
		if (inspectTemplateAutoIntervalFast <= ConfigPro.INSPECT_INTERVAL_UNDEFINED) return ((ConfigPro) config).getInspectTemplateAutoInterval(slow);
		return inspectTemplateAutoIntervalFast;
	}

	public int getInspectTemplateAutoIntervalRaw(boolean slow) {
		if (slow) {
			return inspectTemplateAutoIntervalSlow;
		}
		return inspectTemplateAutoIntervalFast;
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
			else if (!ResourceUtil.isWindowsPath(realPath)) {
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
			else if (!ResourceUtil.isWindowsPath(realPath)) {
				realPath = "/" + realPath;
			}
		}
		return getResource(realPath, isOutSide);
	}

	@Override
	public PageSource getPageSource(String path, boolean isOut) {
		if (path.indexOf("//") != -1) {
			path = StringUtil.replace(path, "//", "/", false);
		}
		PageSource source = pageSourcePool.getPageSource(path, true);
		if (source == null) {
			synchronized (SystemUtil.createToken("MappingImpl", path)) {
				source = pageSourcePool.getPageSource(path, true);
				if (source == null) {
					source = new PageSourceImpl(this, path, isOut);
					pageSourcePool.setPage(path, source);

				}
			}
		}
		return source;
	}

	/**
	 * in contrust to getPageSource this function will not store the requested path in the pool and
	 * 
	 * @param path
	 * @param isOut
	 * @return
	 */
	public Resource getResource(String path, boolean isOut) {
		return getPageSource(path, isOut).getResource();
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
		return getInspectTemplate() == ConfigPro.INSPECT_AUTO || getInspectTemplate() == Config.INSPECT_NEVER;
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
		return new StringBuilder()

				.append("StrPhysical:").append(getStrPhysical())

				.append(";StrArchive:").append(getStrArchive())

				.append(";Virtual:").append(getVirtual())

				.append(";Archive:").append(getArchive())

				.append(";Physical:").append(getPhysical())

				.append(";topLevel:").append(topLevel)

				.append(";inspect:").append(ConfigUtil.inspectTemplate(getInspectTemplateRaw(), ""))

				.append(";inspect-slow:").append(inspectTemplateAutoIntervalSlow)

				.append(";inspect-fast:").append(inspectTemplateAutoIntervalFast)

				.append(";config-inspect:").append(ConfigUtil.inspectTemplate(getConfigInspectTemplate(), ""))

				.append(";physicalFirst:").append(physicalFirst)

				.append(";hidden:").append(hidden)

				.append(";readonly:").append(forCompare ? "" : readonly)

				.append(";").toString();

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

	public Boolean getListenerSingelton() {
		return false;
	}

	@Override
	public int getListenerMode() {
		return listenerMode;
	}

	@Override
	public int getListenerType() {
		return listenerType;
	}

	@Override
	public void flush() {
		pageSourcePool.clear();
	}

	public void close() {
		pageSourcePool.clearPages(null);
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
			ConfigWebPro cwi = ThreadLocalPageContext.getConfigWeb();
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

			return (CIPage) ClassUtil.loadInstance(clazz, new Object[] { ps });
			// return (CIPage) clazz.getConstructor(SUBPAGE_CONSTR).newInstance(ps);
		}
		catch (Exception e) {
			throw Caster.toPageRuntimeException(e);
		}
	}

	public MappingImpl setSource(short source) {
		this.source = source;
		return this;
	}

	public short getSource() {
		return source;
	}
}