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
import java.lang.reflect.Method;

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
public final class CacheDelete extends BIF {

	private static final long serialVersionUID = 4148677299207997607L;
	private static Method remove;

	@Deprecated
	public static String call(PageContext pc, String id) throws PageException {
		return _call(pc, id, false, null);
	}

	@Deprecated
	public static String call(PageContext pc, String id, boolean throwOnError) throws PageException {
		return _call(pc, id, throwOnError, null);
	}

	@Deprecated
	public static String call(PageContext pc, String id, boolean throwOnError, String cacheName) throws PageException {
		return _call(pc, id, throwOnError, cacheName);
	}

	public static String call(PageContext pc, Object id) throws PageException {
		return _call(pc, id, false, null);
	}

	public static String call(PageContext pc, Object id, boolean throwOnError) throws PageException {
		return _call(pc, id, throwOnError, null);
	}

	public static String call(PageContext pc, Object id, boolean throwOnError, String cacheName) throws PageException {
		return _call(pc, id, throwOnError, cacheName);
	}

	public static String _call(PageContext pc, Object id, boolean throwOnError, String cacheName) throws PageException {

		try {
			Cache cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT);
			if (Decision.isArray(id)) {
				Array arr = Caster.toArray(id);
				if (arr.size() == 1) {
					id = Caster.toString(arr.getE(1));
				}
				else {
					if (!remove(cache, toKeys(arr)) && throwOnError) {
						throw new ApplicationException("can not remove the element with the following ids [" + ListUtil.arrayToList(arr, ", ") + "]");
					}
					return null;
				}

			}
			if (!cache.remove(CacheUtil.key(Caster.toString(id))) && throwOnError) {
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
		if (args.length == 1) return _call(pc, args[0], false, null);
		if (args.length == 2) return _call(pc, args[0], Caster.toBooleanValue(args[1]), null);
		if (args.length == 3) return _call(pc, args[0], Caster.toBooleanValue(args[1]), Caster.toString(args[2]));
		throw new FunctionException(pc, "CacheDelete", 1, 3, args.length);
	}

	private static String[] toKeys(Array array) throws PageException {
		String[] arr = new String[array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = CacheUtil.key(Caster.toString(array.get(i + 1, null)));
		}
		return arr;
	}

	private static boolean remove(Cache cache, String[] keys) throws PageException, IOException {
		// // public boolean remove(String[] keys) throws IOException { FUTURE add to interface
		try {
			return Caster.toBooleanValue(getMethod(cache).invoke(cache, new Object[] { keys }));
		}
		catch (NoSuchMethodException e) {
			boolean rtn = true;
			for (String key: keys) {
				if (!cache.remove(key)) rtn = false;
			}
			return rtn;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static Method getMethod(Cache cache) throws NoSuchMethodException, SecurityException {
		if (remove == null || remove.getDeclaringClass() != cache.getClass()) {
			remove = cache.getClass().getMethod("remove", new Class[] { String[].class });
		}
		return remove;
	}
}