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
package lucee.runtime.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.rest.path.Path;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ListUtil;

public class RestUtil {

	public static String[] splitPath(String path) {
		return ListUtil.listToStringArray(path, '/');
	}

	/**
	 * check if caller path match the cfc path
	 * 
	 * @param variables
	 * @param restPath
	 * @param callerPath
	 * @return match until which index of the given cfc path, returns -1 if there is no match
	 */
	public static int matchPath(Struct variables, Path[] restPath, String[] callerPath) {
		if (restPath.length > callerPath.length) return -1;

		int index = 0;
		for (; index < restPath.length; index++) {
			if (!restPath[index].match(variables, callerPath[index])) return -1;
		}
		return index - 1;
	}

	/**
	 * clears the PageContext output buffer andsets the REST response's status code and message
	 *
	 * @param pc
	 * @param status
	 * @param msg
	 */
	public static void setStatus(PageContext pc, int status, String msg) {
		pc.clear();
		if (msg != null) {
			try {
				pc.forceWrite(msg);
			}
			catch (IOException e) {}
		}
		HttpServletResponse rsp = pc.getHttpServletResponse();
		rsp.setHeader("Connection", "close"); // IE unter IIS6, Win2K3 und Resin
		rsp.setStatus(status);
	}

	public static void release(Mapping[] mappings) {
		for (int i = 0; i < mappings.length; i++) {
			mappings[i].release();
		}
	}

	public static boolean isMatch(PageContext pc, Mapping mapping, Resource res) {
		Resource p = mapping.getPhysical();
		if (p != null) {
			return p.equals(res);
		}
		return ResourceUtil.toResourceNotExisting(pc, mapping.getStrPhysical()).equals(res);
	}

}