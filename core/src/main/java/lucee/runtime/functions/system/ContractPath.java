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
/**
 * Implements the CFML Function expandpath
 */
package lucee.runtime.functions.system;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.ext.function.Function;

public final class ContractPath implements Function {
	public static String call(PageContext pc, String absPath) {
		return call(pc, absPath, false);
	}

	public static String call(PageContext pc, String absPath, boolean placeHolder) {
		Resource res = ResourceUtil.toResourceNotExisting(pc, absPath);
		if (!res.exists()) return absPath;

		if (placeHolder) {
			String cp = SystemUtil.addPlaceHolder(res, null);
			if (!StringUtil.isEmpty(cp)) return cp;
		}

		// Config config=pc.getConfig();
		PageSource ps = pc.toPageSource(res, null);
		if (ps == null) return absPath;

		String realPath = ps.getRealpath();
		realPath = realPath.replace('\\', '/');
		if (StringUtil.endsWith(realPath, '/')) realPath = realPath.substring(0, realPath.length() - 1);

		String mapping = ps.getMapping().getVirtual();
		mapping = mapping.replace('\\', '/');
		if (StringUtil.endsWith(mapping, '/')) mapping = mapping.substring(0, mapping.length() - 1);

		return mapping + realPath;
	}
}