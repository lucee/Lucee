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
import java.util.Iterator;

import lucee.commons.io.cache.Cache;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.Config;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ListUtil;

/**
 * 
 */
public final class CacheRemove extends BIF {

	private static final long serialVersionUID = -5823359978885018762L;

	public static String call(PageContext pc, Object ids) throws PageException {
		return call(pc, ids, false, null);
	}

	public static String call(PageContext pc, Object ids, boolean throwOnError) throws PageException {
		return call(pc, ids, throwOnError, null);
	}

	public static String call(PageContext pc, Object ids, boolean throwOnError, String cacheName) throws PageException {
		Array arr = toArray(ids);//
		Iterator it = arr.valueIterator();
		String id;
		Cache cache;
		try {
			cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		StringBuilder sb = null;
		try {
			while (it.hasNext()) {
				id = CacheUtil.key(Caster.toString(it.next()));
				if (!cache.remove(id) && throwOnError) {
					if (sb == null) sb = new StringBuilder();
					else sb.append(',');
					sb.append(id);
				}
			}
		}
		catch (IOException e) {
		}
		if (throwOnError && sb != null) throw new ApplicationException("can not remove the elements with the following id(s) [" + sb + "]");
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, args[0]);
		if (args.length == 2) return call(pc, args[0], Caster.toBooleanValue(args[1]));
		if (args.length == 3) return call(pc, args[0], Caster.toBooleanValue(args[1]), Caster.toString(args[2]));
		throw new FunctionException(pc, "CacheRemove", 1, 3, args.length);
	}

	private static Array toArray(Object oIds) throws PageException {
		if (Decision.isArray(oIds)) {
			return Caster.toArray(oIds);
		}
		return ListUtil.listToArray(Caster.toString(oIds), ',');
	}
}