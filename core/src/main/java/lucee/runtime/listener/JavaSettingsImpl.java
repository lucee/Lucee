/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.MavenClassLoader;
import lucee.commons.io.res.util.ResourceClassLoader;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.mvn.MavenUtil;
import lucee.runtime.mvn.POM;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.osgi.BundleFile;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class JavaSettingsImpl implements JavaSettings {

	private static final int DEFAULT_WATCH_INTERVAL = 60;

	private List<POM> poms;
	private List<BD> osgis;
	private final Resource[] resources;
	private Resource[] resourcesTranslated;
	private final Resource[] bundles;
	private List<Resource> bundlesTranslated;
	private final boolean loadCFMLClassPath;
	private final boolean reloadOnChange;
	private final int watchInterval;
	private final String[] watchedExtensions;
	private boolean hasBundlesTranslated;
	private Map<String, ResourceClassLoader> classLoaders = new ConcurrentHashMap<String, ResourceClassLoader>();
	private Config config;

	public JavaSettingsImpl(Config config, List<POM> poms, List<BD> osgis, Resource[] resources, Resource[] bundles, Boolean loadCFMLClassPath, boolean reloadOnChange,
			int watchInterval, String[] watchedExtensions) {
		this.config = config == null ? ThreadLocalPageContext.getConfig() : config;
		this.poms = poms;
		this.osgis = osgis;
		this.resources = resources == null ? new Resource[0] : resources;
		this.bundles = bundles == null ? new Resource[0] : bundles;
		this.loadCFMLClassPath = Boolean.TRUE.equals(loadCFMLClassPath);
		this.reloadOnChange = reloadOnChange;
		this.watchInterval = watchInterval;
		this.watchedExtensions = watchedExtensions == null ? new String[] { "jar", "class" } : watchedExtensions;

		// TODO needed? SystemExitScanner.validate(resources);
	}

	public boolean hasPoms() {
		return poms != null && poms.size() > 0;
	}

	public List<POM> getPoms() {
		return poms;
	}

	public boolean hasOSGis() {
		return osgis != null && osgis.size() > 0;
	}

	public ClassLoader getRPCClassLoader(ClassLoader parent, boolean reload) throws IOException {
		return getClassLoader(parent, null, false);

	}

	public ResourceClassLoader getResourceClassLoader(Resource[] resources) throws IOException {
		ResourceClassLoader parent = ((ConfigPro) config).getResourceClassLoader();
		return (ResourceClassLoader) getClassLoader(parent, resources, false);
	}

	private ClassLoader getClassLoader(ClassLoader parent, Resource[] resources, boolean reload) throws IOException {
		String key = hash(resources) + ":" + parent.getName();
		ResourceClassLoader classLoader = reload ? null : classLoaders.get(key);

		if (classLoader == null) {
			Collection<Resource> allResources = getAllResources(resources);
			if (allResources.size() > 0) {
				ResourceClassLoader modified = new ResourceClassLoader(allResources, parent);
				classLoaders.put(key, modified);
				return modified;
			}
			return parent;
		}

		return classLoader;
	}

	public Collection<Resource> getAllResources(Resource[] resources) throws IOException {
		Map<String, Resource> mapJars = new HashMap<>();
		Resource[] tmp;

		// maven
		if (poms != null) {
			for (POM pom: poms) {
				tmp = pom.getJars();
				if (tmp != null) {
					for (Resource r: tmp) {
						mapJars.put(r.getAbsolutePath(), r);
					}
				}
			}
		}

		// TODO OSGi

		// jars
		Resource[] jars = getResourcesTranslated();
		if (jars != null && jars.length > 0) {
			for (Resource r: jars) {
				mapJars.put(r.getAbsolutePath(), r);
			}
		}

		// resources passed in
		if (resources != null && resources.length > 0) {
			for (Resource r: resources) {
				mapJars.put(r.getAbsolutePath(), r);
			}
		}
		return mapJars.values();
	}

	private ClassLoader getClassLoaderOld(ClassLoader parent, Resource[] resources, boolean reload) throws IOException {
		String key = hash(resources) + ":" + parent.getName();
		ResourceClassLoader classLoader = reload ? null : classLoaders.get(key);
		ResourceClassLoader modified = null;
		if (classLoader == null) {
			// maven
			if (poms != null) {
				for (POM pom: poms) {
					parent = modified = new MavenClassLoader(pom, parent);
				}
			}

			// TODO OSGi

			// jars
			Resource[] jars = getResourcesTranslated();
			if (jars.length > 0) {
				parent = modified = new ResourceClassLoader(jars, parent);
			}

			if (resources != null && resources.length > 0) {
				parent = modified = new ResourceClassLoader(resources, parent);
			}

			if (modified != null) classLoaders.put(key, modified);
			return parent;
		}

		return classLoader;
	}

	private String hash(Resource[] resources) {
		if (resources == null || resources.length == 0) return "";
		Arrays.sort(resources);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < resources.length; i++) {
			sb.append(ResourceUtil.getCanonicalPathEL(resources[i]));
			sb.append(';');
		}
		return HashUtil.create64BitHashAsString(sb.toString());
	}

	@Override
	public Resource[] getResources() {
		return resources;
	}

	@Override
	public Resource[] getResourcesTranslated() {
		if (resourcesTranslated == null) {
			List<Resource> list = new ArrayList<Resource>();
			_getResourcesTranslated(list, resources, true);
			resourcesTranslated = list.toArray(new Resource[list.size()]);
		}
		return resourcesTranslated;
	}

	// FUTURE interface
	public Resource[] getBundles() {
		return bundles;
	}

	// FUTURE interface
	public List<Resource> getBundlesTranslated() {
		if (!hasBundlesTranslated) {
			List<Resource> list = new ArrayList<Resource>();
			_getBundlesTranslated(list, bundles, true, true);
			bundlesTranslated = list;
			if (bundlesTranslated != null) {
				synchronized (this) {
					if (bundlesTranslated != null && bundlesTranslated.isEmpty()) {
						bundlesTranslated = null;
					}
				}
			}
			hasBundlesTranslated = true;
		}
		return bundlesTranslated;
	}

	public static void _getResourcesTranslated(List<Resource> list, Resource[] resources, boolean deep) {
		if (ArrayUtil.isEmpty(resources)) return;
		for (Resource resource: resources) {
			if (resource.isFile()) {
				if (ResourceUtil.getExtension(resource, "").equalsIgnoreCase("jar")) list.add(resource);
			}
			else if (deep && resource.isDirectory()) {
				list.add(resource); // add as possible classes dir
				_getResourcesTranslated(list, resource.listResources(), false);
			}
		}
	}

	public static void _getBundlesTranslated(List<Resource> list, Resource[] resources, boolean deep, boolean checkFiles) {
		if (ArrayUtil.isEmpty(resources)) return;
		for (Resource resource: resources) {
			if (resource.isDirectory()) {
				list.add(ResourceUtil.getCanonicalResourceEL(resource));
				if (deep) _getBundlesTranslated(list, resource.listResources(), false, false);
			}
			else if (checkFiles && resource.isFile()) {
				BundleFile bf = BundleFile.getInstance(resource, null);
				if (bf != null && bf.isBundle()) list.add(resource);
			}
		}
	}

	@Override
	public boolean loadCFMLClassPath() {
		return loadCFMLClassPath;
	}

	@Override
	public boolean reloadOnChange() {
		return reloadOnChange;
	}

	@Override
	public int watchInterval() {
		return watchInterval;
	}

	@Override
	public String[] watchedExtensions() {
		return watchedExtensions;
	}

	public static JavaSettings getInstance(Config config, Struct sct) {

		// TODO faster hash?
		String id = HashUtil.create64BitHashAsString(sct.toString());

		JavaSettings js = ((ConfigPro) config).getJavaSettings(id);
		if (js != null) {
			return js;
		}

		// maven
		List<POM> poms = null;
		{
			Object obj = sct.get(KeyConstants._maven, null);
			if (obj == null) obj = sct.get(KeyConstants._mvn, null);
			if (obj != null) {
				Array arr = Caster.toArray(obj, null);
				if (arr == null) {
					Struct tmp = Caster.toStruct(obj, null);
					if (tmp != null) {
						arr = new ArrayImpl();
						arr.appendEL(tmp);
					}
				}

				if (arr != null) {
					Iterator<Object> it = arr.valueIterator();
					String g, a, v, s;
					// TODO add method getMavenDir to config
					Resource dir = ((ConfigPro) config).getMavenDir();
					dir.mkdirs();
					Log log = config.getLog("application");
					while (it.hasNext()) {
						Struct el = Caster.toStruct(it.next(), null);
						if (el != null) {
							g = Caster.toString(el.get(KeyConstants._groupId, null), null);
							a = Caster.toString(el.get(KeyConstants._artifactId, null), null);
							v = Caster.toString(el.get(KeyConstants._version, null), null);
							s = Caster.toString(el.get(KeyConstants._scope, null), null);
							if (!StringUtil.isEmpty(g) && !StringUtil.isEmpty(a)) {
								if (poms == null) poms = new ArrayList<>();
								poms.add(POM.getInstance(dir, g, a, v, MavenUtil.toScopes(s, POM.SCOPE_COMPILE), log));
							}
						}
					}
				}
			}
		}

		// osgi
		List<BD> osgis = null;
		{
			Object obj = sct.get(KeyConstants._osgi, null);
			if (obj != null) {
				Array arr = Caster.toArray(obj, null);
				if (arr == null) {
					Struct tmp = Caster.toStruct(obj, null);
					if (tmp != null) {
						arr = new ArrayImpl();
						arr.appendEL(tmp);
					}
				}

				if (arr != null) {
					Iterator<Object> it = arr.valueIterator();
					String n, v;
					Log log = config.getLog("application");
					while (it.hasNext()) {
						Struct el = Caster.toStruct(it.next(), null);
						if (el != null) {
							n = Caster.toString(el.get(KeyConstants._name, null), null);
							if (StringUtil.isEmpty(n, true)) n = Caster.toString(el.get(KeyConstants._bundleName, null), null);
							v = Caster.toString(el.get(KeyConstants._version, null), null);
							if (StringUtil.isEmpty(v, true)) v = Caster.toString(el.get(KeyConstants._bundleVersion, null), null);

							if (!StringUtil.isEmpty(n, true)) {
								if (osgis == null) osgis = new ArrayList<>();
								osgis.add(new BD(n.trim(), v == null ? v : v.trim()));
							}
						}
					}
				}
			}
		}

		// load paths
		List<Resource> paths;
		{
			Object obj = sct.get(KeyConstants._loadPaths, null);
			if (obj != null) {
				paths = loadPaths(ThreadLocalPageContext.get(), obj);
			}
			else paths = new ArrayList<Resource>();
		}

		// bundles paths
		List<Resource> bundles;
		{
			Object obj = sct.get(KeyConstants._bundlePaths, null);
			if (obj == null) obj = sct.get(KeyConstants._bundles, null);
			if (obj == null) obj = sct.get(KeyConstants._bundleDirectory, null);
			if (obj == null) obj = sct.get(KeyConstants._bundleDirectories, null);
			if (obj != null) {
				bundles = loadPaths(ThreadLocalPageContext.get(), obj);
			}
			else bundles = new ArrayList<Resource>();
		}
		// loadCFMLClassPath
		Boolean loadCFMLClassPath = Caster.toBoolean(sct.get(KeyConstants._loadCFMLClassPath, null), null);
		if (loadCFMLClassPath == null) loadCFMLClassPath = Caster.toBoolean(sct.get(KeyConstants._loadColdFusionClassPath, null), null);
		//// if (loadCFMLClassPath == null) loadCFMLClassPath = base.loadCFMLClassPath();

		// reloadOnChange
		//// boolean reloadOnChange = Caster.toBooleanValue(sct.get(KeyConstants._reloadOnChange, null),
		// base.reloadOnChange());
		boolean reloadOnChange = Caster.toBooleanValue(sct.get(KeyConstants._reloadOnChange, null), false);

		// watchInterval
		//// int watchInterval = Caster.toIntValue(sct.get(KeyConstants._watchInterval, null),
		// base.watchInterval());
		int watchInterval = Caster.toIntValue(sct.get(KeyConstants._watchInterval, null), DEFAULT_WATCH_INTERVAL);

		// watchExtensions
		Object obj = sct.get(KeyConstants._watchExtensions, null);
		List<String> extensions = new ArrayList<String>();
		if (obj != null) {
			Array arr;
			if (Decision.isArray(obj)) {
				try {
					arr = Caster.toArray(obj);
				}
				catch (PageException e) {
					arr = new ArrayImpl();
				}
			}
			else {
				arr = lucee.runtime.type.util.ListUtil.listToArrayRemoveEmpty(Caster.toString(obj, ""), ',');
			}
			Iterator<Object> it = arr.valueIterator();
			String ext;
			while (it.hasNext()) {
				ext = Caster.toString(it.next(), null);
				if (StringUtil.isEmpty(ext)) continue;
				ext = ext.trim();
				if (ext.startsWith(".")) ext = ext.substring(1);
				if (ext.startsWith("*.")) ext = ext.substring(2);
				extensions.add(ext);
			}
		}
		js = new JavaSettingsImpl(config, poms, osgis, paths.toArray(new Resource[paths.size()]), bundles.toArray(new Resource[bundles.size()]), loadCFMLClassPath, reloadOnChange,
				watchInterval, extensions.toArray(new String[extensions.size()]));
		((ConfigPro) config).setJavaSettings(id, js);
		return js;
	}

	private static java.util.List<Resource> loadPaths(PageContext pc, Object obj) {

		Resource res;
		if (!Decision.isArray(obj)) {
			String list = Caster.toString(obj, null);
			if (!StringUtil.isEmpty(list)) {
				obj = ListUtil.listToArray(list, ',');
			}
		}

		if (Decision.isArray(obj)) {
			Array arr = Caster.toArray(obj, null);
			java.util.List<Resource> list = new ArrayList<Resource>();
			Iterator<Object> it = arr.valueIterator();
			while (it.hasNext()) {
				try {
					String path = Caster.toString(it.next(), null);
					if (path == null) continue;
					res = AppListenerUtil.toResourceExisting(pc.getConfig(), pc.getApplicationContext(), path, false);
					if (res == null || !res.exists()) res = ResourceUtil.toResourceExisting(pc, path, true, null);
					if (res != null) list.add(res);
				}
				catch (Exception e) {
					LogUtil.log(pc, ModernApplicationContext.class.getName(), e);
				}
			}
			return list;
		}
		return null;
	}

	public static List<Resource> getBundleDirectories(PageContext pc) {
		pc = ThreadLocalPageContext.get(pc);
		if (pc == null) return null;
		ApplicationContext ac = pc.getApplicationContext();
		if (ac == null) return null;
		JavaSettingsImpl js = (JavaSettingsImpl) ac.getJavaSettings();
		if (js == null) return null;

		return js.getBundlesTranslated();
	}

	public static class BD {

		public final String name;
		public final String version;

		public BD(String name, String version) {
			this.name = name;
			this.version = version;
		}

	}
}