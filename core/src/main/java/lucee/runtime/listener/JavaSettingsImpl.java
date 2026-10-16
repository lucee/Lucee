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
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleException;

import lucee.commons.digest.Hash;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.mvn.MavenUtil;
import lucee.runtime.mvn.MavenUtil.GAVSO;
import lucee.runtime.mvn.POM;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.osgi.BundleFile;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public final class JavaSettingsImpl implements JavaSettings {

	private static final int DEFAULT_WATCH_INTERVAL = 60;

	private static final Resource[] RESOURCE_EMPTY = new Resource[0];

	private static final String[] STRING_EMPTY = new String[0];

	private Collection<POM> poms;
	private Collection<BD> osgis;
	private final Resource[] resources;
	private Resource[] resourcesTranslated;
	private final Resource[] bundles;
	private List<Resource> bundlesTranslated;
	private final boolean loadCFMLClassPath;
	private final boolean reloadOnChange;
	private final boolean loadOptionalResources = false;
	private final int watchInterval;
	private final String[] watchedExtensions;
	private boolean hasBundlesTranslated;
	private Config config;

	private Id _id;
	private static Map<String, Reference<JavaSettings>> settings = new ConcurrentHashMap<>();

	private JavaSettingsImpl(Id id, Config config, Collection<POM> poms, Collection<BD> osgis, Resource[] resources, Resource[] bundles, Boolean loadCFMLClassPath,
			boolean reloadOnChange, int watchInterval, String[] watchedExtensions) {
		this.config = config == null ? ThreadLocalPageContext.getConfig() : config;
		this._id = id;
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

	public static JavaSettings merge(Config config, JavaSettings l, JavaSettings r) {
		if (l == null) return r;
		if (r == null) return l;

		JavaSettingsImpl li = (JavaSettingsImpl) l;
		JavaSettingsImpl ri = (JavaSettingsImpl) r;

		Id id = merge(li._id, ri._id);

		JavaSettings js = ((ConfigPro) config).getJavaSettings(id.key);
		if (js != null) {
			return js;
		}

		// poms
		Map<String, POM> mapPOMs = new HashMap<>();
		if (ri.getPoms() != null) {
			for (POM pom: ri.getPoms()) {
				mapPOMs.put(pom.id(), pom);
			}
		}
		if (li.getPoms() != null) {
			for (POM pom: li.getPoms()) {
				mapPOMs.put(pom.id(), pom);
			}
		}

		// osgis
		Map<String, BD> mapOSGIs = new HashMap<>();
		if (ri.osgis != null) {
			for (BD bd: ri.osgis) {
				mapOSGIs.put(bd.toString(), bd);
			}
		}
		if (li.osgis != null) {
			for (BD bd: li.osgis) {
				mapOSGIs.put(bd.toString(), bd);
			}
		}

		// resources
		Map<String, Resource> mapResources = new HashMap<>();
		for (Resource res: ri.getResources()) {
			mapResources.put(res.getAbsolutePath(), res);
		}
		for (Resource res: li.getResources()) {
			mapResources.put(res.getAbsolutePath(), res);
		}

		// bundles
		Map<String, Resource> mapBundles = new HashMap<>();
		for (Resource res: ri.getBundles()) {
			mapBundles.put(res.getAbsolutePath(), res);
		}
		for (Resource res: li.getBundles()) {
			mapBundles.put(res.getAbsolutePath(), res);
		}

		// watched extensions
		Map<String, String> mapWatched = new HashMap<>();
		if (ri.watchedExtensions != null) {
			for (String str: ri.watchedExtensions) {
				mapWatched.put(str, "");
			}
		}
		if (li.watchedExtensions != null) {
			for (String str: li.watchedExtensions) {
				mapWatched.put(str, "");
			}
		}

		js = new JavaSettingsImpl(id, config, mapPOMs.values(), mapOSGIs.values(), mapResources.values().toArray(new Resource[mapResources.size()]),
				mapBundles.values().toArray(new Resource[mapBundles.size()]), ri.loadCFMLClassPath, ri.reloadOnChange, ri.watchInterval,
				mapWatched.keySet().toArray(new String[mapWatched.size()]));

		((ConfigPro) config).setJavaSettings(id.key, js);
		return js;
	}

	private static Id merge(Id left, Id right) {
		Set<String> set = new HashSet<>();
		for (String s: left.data) {
			set.add(s);
		}
		for (String s: right.data) {
			set.add(s);
		}
		List<String> list = new ArrayList<>();
		for (String s: set) {
			list.add(s);
		}
		Collections.sort(list);
		return new Id(HashUtil.create64BitHashAsString(list.toString()), list);
	}

	public boolean hasPoms() {
		return poms != null && poms.size() > 0;
	}

	public String id() {
		return _id.key;
	}

	public Collection<POM> getPoms() {
		return poms;
	}

	public boolean hasOSGis() {
		return osgis != null && osgis.size() > 0;
	}

	public Collection<Resource> getAllResources() throws IOException {
		Map<String, Resource> mapJars = new HashMap<>();

		// maven
		if (poms != null) {
			Collection<POM> jarPoms;
			Map<String, POM> map = new HashMap<>();
			String key;
			POM existing;
			for (POM pom: poms) {
				jarPoms = pom.getJarPOMs(loadOptionalResources);
				if (jarPoms != null) {

					for (POM p: jarPoms) {
						key = p.getGroupId() + ":" + p.getArtifactId();
						existing = map.get(key);
						if (existing != null) {
							try {
								if (OSGiUtil.compare(OSGiUtil.toVersion(existing.getVersion()), OSGiUtil.toVersion(p.getVersion())) < 0) {
									map.put(key, p);
								}
							}
							catch (BundleException e) {
								map.put(key, p);
							}

						}
						else {
							map.put(key, p);
						}
					}
				}
			}
			Resource r;
			for (POM p: map.values()) {
				r = p.getArtifact();
				mapJars.put(r.getAbsolutePath(), r);
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
		/*
		 * if (resources != null && resources.length > 0) { for (Resource r: resources) {
		 * mapJars.put(r.getAbsolutePath(), r); } }
		 */
		return mapJars.values();
	}

	@Override
	public Resource[] getResources() {
		return resources;
	}

	@Override
	public Resource[] getResourcesTranslated() {
		if (resourcesTranslated == null) {
			List<Resource> list = new ArrayList<Resource>();
			_getResourcesTranslated(list, resources, true, true, false);
			resourcesTranslated = list.toArray(new Resource[list.size()]);
		}
		return resourcesTranslated;
	}

	private static List<Resource> getResourcesTranslatedForChecksum(Resource[] resources) {
		List<Resource> list = new ArrayList<Resource>();
		_getResourcesTranslated(list, resources, true, false, true);
		return list;
	}

	@Override
	public Resource[] getBundles() {
		return bundles;
	}

	@Override
	public List<Resource> getBundlesTranslated() {
		if (!hasBundlesTranslated) {
			List<Resource> list = new ArrayList<Resource>();
			_getBundlesTranslated(list, bundles, true, true);
			bundlesTranslated = list;
			if (bundlesTranslated != null && bundlesTranslated.isEmpty()) {
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

	private static void _getResourcesTranslated(List<Resource> list, Resource[] resources, boolean deep, boolean includeDirectories, boolean includeClasses) {
		if (ArrayUtil.isEmpty(resources)) return;
		for (Resource resource: resources) {
			if (resource.isFile()) {
				if (ResourceUtil.getExtension(resource, "").equalsIgnoreCase("jar") || (includeClasses && ResourceUtil.getExtension(resource, "").equalsIgnoreCase("class"))) {
					list.add(resource);
				}
			}
			else if (deep && resource.isDirectory()) {
				if (includeDirectories) list.add(resource); // add as possible classes dir
				_getResourcesTranslated(list, resource.listResources(ExtensionResourceFilter.EXTENSION_JAR_NO_DIR), false, includeDirectories, includeClasses);
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

	public static JavaSettings getInstance(Config config, GAVSO... gavsoArr) {
		Struct sct = new StructImpl();
		sct.setEL(KeyConstants._maven, new ArrayImpl(gavsoArr));
		return getInstance(config, sct, null);
	}

	public static boolean doMerge(Struct data, boolean defaultValue) {
		if (data == null) return defaultValue;
		String strMode = Caster.toString(data.get(KeyConstants._mode, null), null);
		if (StringUtil.isEmpty(strMode)) return defaultValue;
		strMode = strMode.trim();
		if ("replace".equalsIgnoreCase(strMode)) return false;
		if ("extend".equalsIgnoreCase(strMode)) return true;
		if ("inherit".equalsIgnoreCase(strMode)) return true;

		return defaultValue;
	}

	public static JavaSettings getInstance(Config config, Struct data, Object addionalResources) {

		List<String> names = new ArrayList<>();

		// maven
		Collection<POM> poms = null;
		{
			Object obj = data == null ? null : data.get(KeyConstants._maven, null);
			if (obj == null) obj = data == null ? null : data.get(KeyConstants._mvn, null);
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
					Map<String, POM> mapPoms = new HashMap<>();
					Iterator<Object> it = arr.valueIterator();
					// TODO add method getMavenDir to config
					Resource dir = ((ConfigPro) config).getMavenDir();
					dir.mkdirs();
					Log log = LogUtil.getLog(config, "mvn", "application");
					while (it.hasNext()) {
						MavenUtil.GAVSO gavso = MavenUtil.toGAVSO(it.next(), null);
						if (gavso != null) {
							POM tmp = POM.getInstance(dir, gavso.g, gavso.a, gavso.v, MavenUtil.toScopes(gavso.s, POM.SCOPES_FOR_RUNTIME), log);
							mapPoms.put("maven:" + tmp.getGroupId() + ":" + tmp.getArtifactId() + ":" + tmp.getVersion(), tmp);
						}

					}

					for (String k: mapPoms.keySet()) {
						names.add(k);
					}
					poms = mapPoms.values();
				}
			}
		}

		// osgi
		Collection<BD> osgis = null;
		{
			Object obj = data == null ? null : data.get(KeyConstants._osgi, null);
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
					Map<String, BD> mapOsgi = new HashMap<>();
					Iterator<Object> it = arr.valueIterator();
					String n, v;
					BD tmp;
					while (it.hasNext()) {
						Struct el = Caster.toStruct(it.next(), null);
						if (el != null) {
							n = Caster.toString(el.get(KeyConstants._name, null), null);
							if (StringUtil.isEmpty(n, true)) n = Caster.toString(el.get(KeyConstants._bundleName, null), null);
							v = Caster.toString(el.get(KeyConstants._version, null), null);
							if (StringUtil.isEmpty(v, true)) v = Caster.toString(el.get(KeyConstants._bundleVersion, null), null);

							if (!StringUtil.isEmpty(n, true)) {
								tmp = new BD(n.trim(), v == null ? v : v.trim());
								mapOsgi.put("osgi:" + tmp.name + ":" + tmp.version, tmp);
							}
						}
					}

					for (String k: mapOsgi.keySet()) {
						names.add(k);
					}
					osgis = mapOsgi.values();
				}
			}
		}

		// load paths
		Map<String, Resource> mapPath = null;
		{
			Object obj = data == null ? null : data.get(KeyConstants._loadPaths, null);
			if (obj != null) {
				List<Resource> _paths = loadPaths(ThreadLocalPageContext.get(), obj);
				if (mapPath == null) mapPath = new HashMap<>();
				for (Resource p: _paths) {
					p = ResourceUtil.getCanonicalResourceEL(p);
					mapPath.put("paths:" + p.getAbsolutePath(), p);
				}
			}
		}

		// addional resources
		if (addionalResources != null) {
			if (mapPath == null) mapPath = new HashMap<>();
			if (addionalResources instanceof Resource[]) {
				for (Resource r: (Resource[]) addionalResources) {
					r = ResourceUtil.getCanonicalResourceEL(r);
					mapPath.put("paths:" + r.getAbsolutePath(), r);
				}
			}
			else if (addionalResources instanceof List) {
				for (Resource r: (List<Resource>) addionalResources) {
					r = ResourceUtil.getCanonicalResourceEL(r);
					mapPath.put("paths:" + r.getAbsolutePath(), r);
				}
			}
		}
		Collection<Resource> paths = null;
		List<String> pathNames = null;
		if (mapPath != null) {
			pathNames = new ArrayList<>();
			for (String k: mapPath.keySet()) {
				pathNames.add(k);
			}
			paths = mapPath.values();
		}

		// bundles paths
		Collection<Resource> bundles = null;
		{
			Object obj = data == null ? null : data.get(KeyConstants._bundlePaths, null);
			if (obj == null) obj = data == null ? null : data.get(KeyConstants._bundles, null);
			if (obj == null) obj = data == null ? null : data.get(KeyConstants._bundleDirectory, null);
			if (obj == null) obj = data == null ? null : data.get(KeyConstants._bundleDirectories, null);
			if (obj != null) {
				Map<String, Resource> mapBundles = new HashMap<>();
				List<Resource> _bundles = loadPaths(ThreadLocalPageContext.get(), obj);
				for (Resource b: _bundles) {
					b = ResourceUtil.getCanonicalResourceEL(b);
					mapBundles.put("bundles:" + b.getAbsolutePath(), b);
				}
				for (String k: mapBundles.keySet()) {
					names.add(k);
				}
				bundles = mapBundles.values();
			}
		}

		// loadCFMLClassPath
		Boolean loadCFMLClassPath = Caster.toBoolean(data == null ? null : data.get(KeyConstants._loadCFMLClassPath, null), null);
		if (loadCFMLClassPath == null) {
			loadCFMLClassPath = Caster.toBoolean(data == null ? null : data.get(KeyConstants._loadColdFusionClassPath, null), null);

		}
		loadCFMLClassPath = Boolean.TRUE.equals(loadCFMLClassPath);
		names.add("loadCFMLClassPath:" + loadCFMLClassPath);

		// reloadOnChange
		boolean reloadOnChange = Caster.toBooleanValue(data == null ? null : data.get(KeyConstants._reloadOnChange, null), false);

		// watchInterval
		int watchInterval = Caster.toIntValue(data == null ? null : data.get(KeyConstants._watchInterval, null), DEFAULT_WATCH_INTERVAL);

		// watchExtensions
		Object obj = data == null ? null : data.get(KeyConstants._watchExtensions, null);
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
				names.add("ext:" + ext);
				extensions.add(ext);
			}
		}

		Id id = createId(config, names, paths, pathNames, reloadOnChange ? watchInterval : 0);

		JavaSettings js = ((ConfigPro) config).getJavaSettings(id.key);
		if (js != null) {
			return js;
		}

		js = new JavaSettingsImpl(id, config, poms, osgis,

				paths == null || paths.size() == 0 ? RESOURCE_EMPTY : paths.toArray(new Resource[paths.size()]),

				bundles == null || bundles.size() == 0 ? RESOURCE_EMPTY : bundles.toArray(new Resource[bundles.size()]),

				loadCFMLClassPath, reloadOnChange, watchInterval,

				extensions == null || extensions.size() == 0 ? STRING_EMPTY : extensions.toArray(new String[extensions.size()])

		);

		((ConfigPro) config).setJavaSettings(id.key, js);
		return js;
	}

	private static final Map<String, Id> quickMapping = new ConcurrentHashMap<>();

	private static Id createId(Config config, List<String> names, Collection<Resource> paths, List<String> pathNames, int watchInterval) {
		Collections.sort(names);
		String base = names.toString();
		log(config, "createId (watchInterval=" + watchInterval + "s) base: " + base);

		if (paths == null || paths.size() == 0) {
			String id = HashUtil.create64BitHashAsString(base);
			log(config, "no paths, returning base hash: " + id);
			return new Id(id, names);
		}

		Collections.sort(pathNames);
		String quick = HashUtil.create64BitHashAsString(base + ":" + pathNames.toString());
		log(config, "quick hash: " + quick + " from paths: " + pathNames);

		Id cached = quickMapping.get(quick);
		if (cached != null) {
			if (watchInterval <= 0) {
				log(config, "cache hit (no expiry), returning: " + cached.key);
				return cached;
			}
			long ageSeconds = (System.currentTimeMillis() - cached.created) / 1000;
			if (ageSeconds < watchInterval) {
				log(config, "cache hit (age=" + ageSeconds + "s < watchInterval=" + watchInterval + "s), returning: " + cached.key);
				return cached;
			}
			log(config, "cache expired (age=" + ageSeconds + "s >= watchInterval=" + watchInterval + "s), recomputing");
			quickMapping.remove(quick);
		}
		else {
			log(config, "cache miss, computing slow hash");
		}

		String slow;
		try {
			String md5;
			for (Resource res: JavaSettingsImpl.getResourcesTranslatedForChecksum(paths.toArray(new Resource[paths.size()]))) {
				md5 = Hash.md5(res, 1024);
				names.add("checksum:" + md5 + ":" + res.getCanonicalPath());
			}
			Collections.sort(names);
			slow = HashUtil.create64BitHashAsString(names.toString());
			log(config, "slow hash: " + slow);
		}
		catch (IOException e) {
			LogUtil.log(config, Log.LEVEL_ERROR, "page-source", "IOException computing checksums, falling back to quick hash: " + e.getMessage());
			slow = quick;
		}
		Id id = new Id(slow, names);
		quickMapping.put(quick, id);
		log(config, "cached and returning: " + slow);
		return id;
	}

	private static void log(Config config, String msg) {
		if (config != null && LogUtil.doesTrace(config.getLog("application"))) {
			config.getLog("application").trace("page-source", msg);
		}
	}

	public static JavaSettings readJavaSettings(PageContext pc, Struct meta) throws IOException {
		// TODO cache
		String json = meta == null ? null : Caster.toString(meta.get(KeyConstants._javasettings, null), null);
		if (!StringUtil.isEmpty(json, true)) {
			json = json.trim();
			String key = HashUtil.create64BitHashAsString(json, 16);
			Reference<JavaSettings> ref = settings.get(key);
			if (ref != null) {
				JavaSettings val = ref.get();
				if (val != null) {
					return val;
				}
			}
			try {
				if (StringUtil.endsWithIgnoreCase(json, ".json")) {
					pc = ThreadLocalPageContext.get(pc);
					if (pc != null) json = IOUtil.toString(ResourceUtil.toResourceExisting(pc, json), CharsetUtil.UTF8);
					else json = IOUtil.toString(ResourceUtil.toResourceExisting(ThreadLocalPageContext.getConfig(), json), CharsetUtil.UTF8);
				}
				Struct sct = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, json));
				JavaSettings val = JavaSettingsImpl.getInstance(ThreadLocalPageContext.getConfig(pc), sct, null);
				settings.put(key, new SoftReference<JavaSettings>(val));
				return val;
			}
			catch (Exception e) {
				// LogUtil.log("component", e);
				throw ExceptionUtil.toIOException(e);
			}
		}
		return null;
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
					if (res != null) list.add(ResourceUtil.getCanonicalResourceEL(res));
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

		@Override
		public String toString() {
			return name + ":" + version;
		}

	}

	public static class Id {
		final private long created = System.currentTimeMillis();
		final private String key;
		final private List<String> data;

		public Id(String key, List<String> data) {

			this.key = key;
			this.data = data;
		}
	}

}