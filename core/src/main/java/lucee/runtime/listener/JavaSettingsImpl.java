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
import java.util.List;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.type.util.ArrayUtil;

public class JavaSettingsImpl implements JavaSettings {

	private final Resource[] resources;
	private Resource[] resourcesTranslated;
	private final boolean loadCFMLClassPath;
	private final boolean reloadOnChange;
	private final int watchInterval;
	private final String[] watchedExtensions;

	public JavaSettingsImpl(){
		this.resources=new Resource[0];
		this.loadCFMLClassPath=false;
		this.reloadOnChange=false;
		this.watchInterval=60;
		this.watchedExtensions=new String[]{"jar","class"};
	}

	public JavaSettingsImpl(Resource[] resources, Boolean loadCFMLClassPath,boolean reloadOnChange, int watchInterval, String[] watchedExtensions) {

		this.resources=resources;
		this.loadCFMLClassPath=loadCFMLClassPath;
		this.reloadOnChange=reloadOnChange;
		this.watchInterval=watchInterval;
		this.watchedExtensions=watchedExtensions;
	}

	@Override
	public Resource[] getResources() {
		return resources;
	}
	
	// FUTURE add to interface
	public Resource[] getResourcesTranslated() {
		if(resourcesTranslated==null) {
			List<Resource> list=new ArrayList<Resource>();
			_getResourcesTranslated(list,resources, true);
			resourcesTranslated=list.toArray(new Resource[list.size()]);
		}
		return resourcesTranslated;
	}
	
	public static void _getResourcesTranslated(List<Resource> list, Resource[] resources, boolean deep) {
		if(ArrayUtil.isEmpty(resources)) return;
		for(int i=0;i<resources.length;i++){
			if(resources[i].isFile()) {
				if(ResourceUtil.getExtension(resources[i], "").equalsIgnoreCase("jar"))
					list.add(resources[i]);
			}
			else if(deep && resources[i].isDirectory()){
				list.add(resources[i]); // add as possible classes dir
				_getResourcesTranslated(list,resources[i].listResources(),false);
				
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

}