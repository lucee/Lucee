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
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.tag.Directory;

public final class DirectoryDelete {
	public static String call(PageContext pc, String path) throws PageException {
		return call(pc, path, false);
	}

	public static String call(PageContext pc, String path, boolean recurse) throws PageException {
		if (StringUtil.isEmpty(path, true)) throw new FunctionException(pc, "DirectoryDelete", 1, "path", "The argument [path] is required and cannot be empty");
		Resource dir = ResourceUtil.toResourceNotExisting(pc, path);
		Directory.actionDelete(pc, dir, recurse, null);
		return null;
	}
}
