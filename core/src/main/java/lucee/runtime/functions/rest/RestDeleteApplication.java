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
package lucee.runtime.functions.rest;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.Password;
import lucee.runtime.config.XMLConfigAdmin;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.rest.Mapping;
import lucee.runtime.rest.RestUtil;

public class RestDeleteApplication {
	public static String call(PageContext pc, String dirPath) throws PageException {
		return call(pc, dirPath, null);
	}

	public static String call(PageContext pc, String dirPath, String strWebAdminPassword) throws PageException {
		Password webAdminPassword = CacheUtil.getPassword(pc, strWebAdminPassword, false);

		Resource dir = RestDeleteApplication.toResource(pc, dirPath);
		ConfigWebImpl config = (ConfigWebImpl) pc.getConfig();

		try {
			XMLConfigAdmin admin = XMLConfigAdmin.newInstance((ConfigWebImpl) pc.getConfig(), webAdminPassword);
			Mapping[] mappings = config.getRestMappings();
			Mapping mapping;
			for (int i = 0; i < mappings.length; i++) {
				mapping = mappings[i];
				if (RestUtil.isMatch(pc, mapping, dir)) {
					admin.removeRestMapping(mapping.getVirtual());
					admin.storeAndReload();
				}
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		return null;
	}

	static Resource toResource(PageContext pc, String dirPath) throws PageException {
		Resource dir = ResourceUtil.toResourceNotExisting(pc.getConfig(), dirPath);
		pc.getConfig().getSecurityManager().checkFileLocation(dir);
		if (!dir.isDirectory()) throw new FunctionException(pc, "RestInitApplication", 1, "dirPath", "argument value [" + dirPath + "] must contain an existing directory");

		return dir;
	}
}