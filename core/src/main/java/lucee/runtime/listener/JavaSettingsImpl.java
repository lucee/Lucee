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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.osgi.BundleFile;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

public class JavaSettingsImpl implements JavaSettings {

	private final Resource[] resources;
	private Resource[] resourcesTranslated;
	private final Resource[] bundles;
	private List<Resource> bundlesTranslated;
	private final boolean loadCFMLClassPath;
	private final boolean reloadOnChange;
	private final int watchInterval;
	private final String[] watchedExtensions;
	private boolean hasBundlesTranslated;

	public JavaSettingsImpl() {
		this.resources = new Resource[0];
		this.bundles = new Resource[0];
		this.loadCFMLClassPath = false;
		this.reloadOnChange = false;
		this.watchInterval = 60;
		this.watchedExtensions = new String[] { "jar", "class" };
	}

	public JavaSettingsImpl(Resource[] resources, Resource[] bundles, Boolean loadCFMLClassPath, boolean reloadOnChange, int watchInterval, String[] watchedExtensions) {
		this.resources = resources;
		this.bundles = bundles;
		this.loadCFMLClassPath = loadCFMLClassPath;
		this.reloadOnChange = reloadOnChange;
		this.watchInterval = watchInterval;
		this.watchedExtensions = watchedExtensions;
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
			if (bundlesTranslated != null && bundlesTranslated.isEmpty()) bundlesTranslated = null;
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

	public static JavaSettingsImpl newInstance(JavaSettings base, Struct sct) {
		// load paths
		List<Resource> paths;
		{
			Object obj = sct.get(KeyImpl.getInstance("loadPaths"), null);
			if (obj != null) {
				paths = loadPaths(ThreadLocalPageContext.get(), obj);
			}
			else paths = new ArrayList<Resource>();
		}

		// bundles paths
		List<Resource> bundles;
		{
			Object obj = sct.get(KeyImpl.getInstance("bundlePaths"), null);
			if (obj == null) obj = sct.get(KeyImpl.getInstance("bundles"), null);
			if (obj == null) obj = sct.get(KeyImpl.getInstance("bundleDirectory"), null);
			if (obj == null) obj = sct.get(KeyImpl.getInstance("bundleDirectories"), null);
			if (obj != null) {
				bundles = loadPaths(ThreadLocalPageContext.get(), obj);
			}
			else bundles = new ArrayList<Resource>();
		}
		// loadCFMLClassPath
		Boolean loadCFMLClassPath = Caster.toBoolean(sct.get(KeyImpl.getInstance("loadCFMLClassPath"), null), null);
		if (loadCFMLClassPath == null) loadCFMLClassPath = Caster.toBoolean(sct.get(KeyImpl.getInstance("loadColdFusionClassPath"), null), null);
		if (loadCFMLClassPath == null) loadCFMLClassPath = base.loadCFMLClassPath();

		// reloadOnChange
		boolean reloadOnChange = Caster.toBooleanValue(sct.get(KeyImpl.getInstance("reloadOnChange"), null), base.reloadOnChange());

		// watchInterval
		int watchInterval = Caster.toIntValue(sct.get(KeyImpl.getInstance("watchInterval"), null), base.watchInterval());

		// watchExtensions
		Object obj = sct.get(KeyImpl.getInstance("watchExtensions"), null);
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
		return new JavaSettingsImpl(paths.toArray(new Resource[paths.size()]), bundles.toArray(new Resource[bundles.size()]), loadCFMLClassPath, reloadOnChange, watchInterval,
				extensions.toArray(new String[extensions.size()]));
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
					LogUtil.log(ThreadLocalPageContext.getConfig(), ModernApplicationContext.class.getName(), e);
				}
			}
			return list;
		}
		return null;
	}

	public static List<Resource> getBundles(PageContext pc) {
		pc = ThreadLocalPageContext.get(pc);
		if (pc == null) return null;
		ApplicationContext ac = pc.getApplicationContext();
		if (ac == null) return null;
		JavaSettingsImpl js = (JavaSettingsImpl) ac.getJavaSettings();
		if (js == null) return null;

		return js.getBundlesTranslated();
	}

}