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
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public class GetCanonicalPath extends BIF {

	private static final long serialVersionUID = -7516439220584467382L;

	public static String call(PageContext pc, String path) {
		// we only add a slash if there was already one (for FuseBox), otherwise we cannot know for sure it
		// is a directory (when path not exists ....)
		boolean addEndSep = StringUtil.endsWith(path, '/', '\\');
		Resource res = ResourceUtil.toResourceNotExisting(pc, path);
		if (!addEndSep && res.isDirectory()) addEndSep = true;

		path = ResourceUtil.getCanonicalPathEL(res);
		if (addEndSep && !StringUtil.endsWith(path, '/', '\\')) {
			return path + ResourceUtil.getSeparator(res.getResourceProvider());
		}
		return path;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		return call(pc, Caster.toString(args[0]));
	}
}