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

import java.util.Iterator;
import java.util.List;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CacheEntry;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cache.util.WildCardFilter;
import lucee.runtime.config.Config;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * 
 */
public final class CacheGetAll extends BIF {

	private static final long serialVersionUID = 6395709569356486777L;

	public static Struct call(PageContext pc) throws PageException {
		return call(pc, null, null);
	}

	public static Struct call(PageContext pc, String filter) throws PageException {
		return call(pc, filter, null);
	}

	public static Struct call(PageContext pc, String filter, String cacheName) throws PageException {
		try {
			Cache cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT);
			List<CacheEntry> entries = CacheGetAllIds.isFilter(filter) ? cache.entries(new WildCardFilter(filter, true)) : cache.entries();
			Iterator<CacheEntry> it = entries.iterator();
			Struct sct = new StructImpl();
			CacheEntry entry;
			while (it.hasNext()) {
				entry = it.next();
				sct.setEL(KeyImpl.init(entry.getKey()), entry.getValue());
			}
			return sct;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		throw new FunctionException(pc, "CacheGetAll", 0, 2, args.length);
	}
}