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
package lucee.runtime.functions.rest;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.Password;
import lucee.runtime.config.XMLConfigAdmin;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.rest.Mapping;
import lucee.runtime.rest.RestUtil;

public class RestInitApplication {

	public static String call(PageContext pc, String dirPath) throws PageException {
		return _call(pc, dirPath, null, null, null);
	}

	public static String call(PageContext pc, String dirPath, String serviceMapping) throws PageException {
		return _call(pc, dirPath, serviceMapping, null, null);
	}

	public static String call(PageContext pc, String dirPath, String serviceMapping, boolean defaultMapping) throws PageException {
		return _call(pc, dirPath, serviceMapping, defaultMapping, null);
	}

	public static String call(PageContext pc, String dirPath, String serviceMapping, boolean defaultMapping, String webAdminPassword) throws PageException {
		return _call(pc, dirPath, serviceMapping, defaultMapping, webAdminPassword);
	}

	public static String _call(PageContext pc, String dirPath, String serviceMapping, Boolean defaultMapping, String webAdminPassword) throws PageException {
		if (StringUtil.isEmpty(serviceMapping, true)) {
			serviceMapping = pc.getApplicationContext().getName();
		}
		Resource dir = RestDeleteApplication.toResource(pc, dirPath);

		ConfigWebPro config = (ConfigWebPro) pc.getConfig();
		Mapping[] mappings = config.getRestMappings();
		Mapping mapping;

		// id is mapping name

		String virtual = serviceMapping.trim();
		if (!virtual.startsWith("/")) virtual = "/" + virtual;
		if (!virtual.endsWith("/")) virtual += "/";
		boolean hasResetted = false;
		for (int i = 0; i < mappings.length; i++) {
			mapping = mappings[i];
			if (mapping.getVirtualWithSlash().equals(virtual)) {
				// directory has changed
				if (!RestUtil.isMatch(pc, mapping, dir) || (defaultMapping != null && mapping.isDefault() != defaultMapping.booleanValue())) {
					update(pc, dir, virtual, CacheUtil.getPassword(pc, webAdminPassword, false), defaultMapping == null ? mapping.isDefault() : defaultMapping.booleanValue());
				}
				mapping.reset(pc);
				hasResetted = true;
			}
		}
		if (!hasResetted) {
			update(pc, dir, virtual, CacheUtil.getPassword(pc, webAdminPassword, false), defaultMapping == null ? false : defaultMapping.booleanValue());
		}

		return null;
	}

	private static void update(PageContext pc, Resource dir, String virtual, Password webAdminPassword, boolean defaultMapping) throws PageException {
		try {
			XMLConfigAdmin admin = XMLConfigAdmin.newInstance(pc.getConfig(), webAdminPassword);
			admin.updateRestMapping(virtual, dir.getAbsolutePath(), defaultMapping);
			admin.storeAndReload();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

}