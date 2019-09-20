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
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

public class JavaSettingsImpl implements JavaSettings {

	private final Resource[] resources;
	private Resource[] resourcesTranslated;
	private final boolean loadCFMLClassPath;
	private final boolean reloadOnChange;
	private final int watchInterval;
	private final String[] watchedExtensions;

	public JavaSettingsImpl() {
		this.resources = new Resource[0];
		this.loadCFMLClassPath = false;
		this.reloadOnChange = false;
		this.watchInterval = 60;
		this.watchedExtensions = new String[] { "jar", "class" };
	}

	public JavaSettingsImpl(Resource[] resources, Boolean loadCFMLClassPath, boolean reloadOnChange, int watchInterval, String[] watchedExtensions) {

		this.resources = resources;
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

	public static void _getResourcesTranslated(List<Resource> list, Resource[] resources, boolean deep) {
		if (ArrayUtil.isEmpty(resources)) return;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].isFile()) {
				if (ResourceUtil.getExtension(resources[i], "").equalsIgnoreCase("jar")) list.add(resources[i]);
			}
			else if (deep && resources[i].isDirectory()) {
				list.add(resources[i]); // add as possible classes dir
				_getResourcesTranslated(list, resources[i].listResources(), false);

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
		// loadPaths
		Object obj = sct.get(KeyImpl.init("loadPaths"), null);
		List<Resource> paths;
		if (obj != null) {
			paths = loadPaths(ThreadLocalPageContext.get(), obj);
		}
		else paths = new ArrayList<Resource>();

		// loadCFMLClassPath
		Boolean loadCFMLClassPath = Caster.toBoolean(sct.get(KeyImpl.init("loadCFMLClassPath"), null), null);
		if (loadCFMLClassPath == null) loadCFMLClassPath = Caster.toBoolean(sct.get(KeyImpl.init("loadColdFusionClassPath"), null), null);
		if (loadCFMLClassPath == null) loadCFMLClassPath = base.loadCFMLClassPath();

		// reloadOnChange
		boolean reloadOnChange = Caster.toBooleanValue(sct.get(KeyImpl.init("reloadOnChange"), null), base.reloadOnChange());

		// watchInterval
		int watchInterval = Caster.toIntValue(sct.get(KeyImpl.init("watchInterval"), null), base.watchInterval());

		// watchExtensions
		obj = sct.get(KeyImpl.init("watchExtensions"), null);
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
		return new JavaSettingsImpl(paths.toArray(new Resource[paths.size()]), loadCFMLClassPath, reloadOnChange, watchInterval, extensions.toArray(new String[extensions.size()]));
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
					// print.e("--------------------------------------------------");
					// print.e(path);
					res = AppListenerUtil.toResourceExisting(pc.getConfig(), pc.getApplicationContext(), path, false);

					// print.e(res+"->"+(res!=null && res.exists()));
					if (res == null || !res.exists()) res = ResourceUtil.toResourceExisting(pc, path, true, null);

					// print.e(res+"->"+(res!=null && res.exists()));
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

}