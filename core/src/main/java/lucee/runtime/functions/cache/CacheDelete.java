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
package lucee.runtime.functions.cache;

import java.io.IOException;

import lucee.commons.io.cache.Cache;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.Config;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

/**
 * 
 */
public final class CacheDelete extends BIF {

	private static final long serialVersionUID = 4148677299207997607L;

	public static String call(PageContext pc, String id) throws PageException {
		return call(pc, id, false, null);
	}

	public static String call(PageContext pc, String id, boolean throwOnError) throws PageException {
		return call(pc, id, throwOnError, null);
	}

	public static String call(PageContext pc, String id, boolean throwOnError, String cacheName) throws PageException {
		try {
			Cache cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT);
			if (!cache.remove(CacheUtil.key(id)) && throwOnError) {
				throw new ApplicationException("can not remove the element with the following id [" + id + "]");
			}
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toBooleanValue(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toBooleanValue(args[1]), Caster.toString(args[2]));
		throw new FunctionException(pc, "CacheDelete", 1, 3, args.length);
	}
}