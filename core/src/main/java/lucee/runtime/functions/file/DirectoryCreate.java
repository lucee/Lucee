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
package lucee.runtime.functions.file;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.tag.Directory;
import lucee.runtime.tag.util.FileUtil;

public class DirectoryCreate {

	public static String call(PageContext pc, String path) throws PageException {
		return call(pc, path, true);
	}

	public static String call(PageContext pc, String path, boolean createPath) throws PageException {
		return call(pc, path, createPath, false);
	}

	public static String call(PageContext pc, String path, boolean createPath, boolean ignoreExists) throws PageException {
		Resource dir = ResourceUtil.toResourceNotExisting(pc, path);
		Directory.actionCreate(pc, dir, null, createPath, -1, null, null, ignoreExists ? FileUtil.NAMECONFLICT_SKIP : FileUtil.NAMECONFLICT_ERROR);
		return null;
	}
}