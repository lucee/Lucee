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

import lucee.runtime.type.query.QueryReturnType;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.res.util.UDFFilter;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.tag.Directory;

public final class DirectoryList {

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

	public static Object call(PageContext pc, String path, boolean recurse, String strListInfo, Object oFilter, String sort, String type, String returntype)
			throws PageException {
		return call(pc, path, recurse, strListInfo, oFilter, sort, type, returntype, null);
	}

	public static Object call(PageContext pc, String path, boolean recurse, String strListInfo, Object oFilter, String sort, String type, String returntype, String columnKey)
			throws PageException {
		int rt = QueryReturnType.parse(returntype, "directoryList");
		Key ck = QueryReturnType.toColumnKey(columnKey);
		int listInfo = toListInfo(strListInfo);
		// the returntype only applies to a query result, the other list infos return an array of strings
		if (rt != QueryReturnType.QUERY && listInfo != Directory.LIST_INFO_QUERY_ALL) {
			throw new FunctionException(pc, "DirectoryList", 8, "returntype", "returntype [" + returntype.trim() + "] can only be used when argument [listInfo] is [query]");
		}
		return convert(_call(pc, path, recurse, listInfo, oFilter, sort, StringUtil.isEmpty(type) ? Directory.TYPE_ALL : Directory.toType(type)), rt, ck);
	}

	private static Object convert(Object res, int returnType, Key columnKey) throws PageException {
		if (res instanceof Query) return QueryReturnType.convert((Query) res, returnType, columnKey);
		return res;
	}

	public static Object _call(PageContext pc, String path, boolean recurse, int listInfo, Object oFilter, String sort, int type) throws PageException {
		Resource dir = ResourceUtil.toResourceNotExisting(pc, path);
		ResourceFilter filter = UDFFilter.createResourceAndResourceNameFilter(pc, oFilter);
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