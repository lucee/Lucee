/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.functions.cache;

import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.Password;
import lucee.runtime.config.XMLConfigAdmin;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

/**
 * implements BIF CacheRegionRemove. This function only exists for compatibility with other CFML
 * Engines and should be avoided where possible. The preferred method to manipulate Cache
 * connections is via the Administrator interface or in Application.
 */
public class CacheRegionRemove extends BIF {

	public static String call(PageContext pc, String cacheName, String webAdminPassword) throws PageException {
		return _call(pc, cacheName, webAdminPassword);
	}

	public static String call(PageContext pc, String cacheName) throws PageException {
		return _call(pc, cacheName, null);
	}

	static String _call(PageContext pc, String cacheName, String strWebAdminPassword) throws PageException {
		Password webAdminPassword = CacheUtil.getPassword(pc, strWebAdminPassword, false);
		try {
			XMLConfigAdmin adminConfig = XMLConfigAdmin.newInstance(pc.getConfig(), webAdminPassword);
			adminConfig.removeCacheConnection(cacheName);
			adminConfig.storeAndReload();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		throw new FunctionException(pc, "CacheRegionRemove", 1, 2, args.length);
	}
}