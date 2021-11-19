/**
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 **/
package lucee.runtime.functions.file;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.res.util.UDFFilter;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.tag.Directory;

public class DirectoryList {

	public static Object call(PageContext pc, String path) throws PageException {
		return _call(pc, path, false, Directory.LIST_INFO_ARRAY_PATH, null, null, Directory.TYPE_ALL);
	}

	public static Object call(PageContext pc, String path, boolean recurse) throws PageException {
		return _call(pc, path, recurse, Directory.LIST_INFO_ARRAY_PATH, null, null, Directory.TYPE_ALL);
	}

	public static Object call(PageContext pc, String path, boolean recurse, String strListInfo) throws PageException {
		return _call(pc, path, recurse, toListInfo(strListInfo), null, null, Directory.TYPE_ALL);
	}

	public static Object call(PageContext pc, String path, boolean recurse, String strListInfo, Object oFilter) throws PageException {
		return _call(pc, path, recurse, toListInfo(strListInfo), oFilter, null, Directory.TYPE_ALL);
	}

	public static Object call(PageContext pc, String path, boolean recurse, String strListInfo, Object oFilter, String sort) throws PageException {
		return _call(pc, path, recurse, toListInfo(strListInfo), oFilter, sort, Directory.TYPE_ALL);
	}

	public static Object call(PageContext pc, String path, boolean recurse, String strListInfo, Object oFilter, String sort, String type) throws PageException {
		return _call(pc, path, recurse, toListInfo(strListInfo), oFilter, sort, StringUtil.isEmpty(type) ? Directory.TYPE_ALL : Directory.toType(type));
	}

	public static Object _call(PageContext pc, String path, boolean recurse, int listInfo, Object oFilter, String sort, int type) throws PageException {
		Resource dir = ResourceUtil.toResourceNotExisting(pc, path);
		ResourceFilter filter = UDFFilter.createResourceAndResourceNameFilter(oFilter);
		return Directory.actionList(pc, dir, null, type, filter, listInfo, recurse, sort);
	}

	private static int toListInfo(String strListInfo) {

		int listInfo = Directory.LIST_INFO_ARRAY_PATH;
		if (!StringUtil.isEmpty(strListInfo, true)) {

			strListInfo = strListInfo.trim().toLowerCase();

			if ("name".equalsIgnoreCase(strListInfo)) {
				listInfo = Directory.LIST_INFO_ARRAY_NAME;
			}
			else if ("query".equalsIgnoreCase(strListInfo)) {
				listInfo = Directory.LIST_INFO_QUERY_ALL;
			}
		}
		return listInfo;
	}
}