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
import lucee.commons.io.cache.CacheEntry;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.Config;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * 
 */
public final class CacheGetMetadata extends BIF {

	private static final long serialVersionUID = -470089623854482521L;

	private static final Collection.Key CACHE_HITCOUNT = KeyImpl.getInstance("cache_hitcount");
	private static final Collection.Key CACHE_MISSCOUNT = KeyImpl.getInstance("cache_misscount");
	private static final Collection.Key CACHE_CUSTOM = KeyImpl.getInstance("cache_custom");
	private static final Collection.Key CREATED_TIME = KeyImpl.getInstance("createdtime");
	private static final Collection.Key IDLE_TIME = KeyImpl.getInstance("idletime");
	private static final Collection.Key LAST_HIT = KeyImpl.getInstance("lasthit");
	private static final Collection.Key LAST_UPDATED = KeyImpl.getInstance("lastupdated");

	public static Struct call(PageContext pc, String id) throws PageException {
		return call(pc, id, null);
	}

	public static Struct call(PageContext pc, String id, String cacheName) throws PageException {
		try {
			Cache cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT);
			CacheEntry entry = cache.getCacheEntry(CacheUtil.key(id));

			Struct info = new StructImpl();
			info.set(CACHE_HITCOUNT, new Double(cache.hitCount()));
			info.set(CACHE_MISSCOUNT, new Double(cache.missCount()));
			info.set(CACHE_CUSTOM, cache.getCustomInfo());
			info.set(KeyConstants._custom, entry.getCustomInfo());

			info.set(CREATED_TIME, entry.created());
			info.set(KeyConstants._hitcount, new Double(entry.hitCount()));
			info.set(IDLE_TIME, new Double(entry.idleTimeSpan()));
			info.set(LAST_HIT, entry.lastHit());
			info.set(LAST_UPDATED, entry.lastModified());
			info.set(KeyConstants._size, new Double(entry.size()));
			info.set(KeyConstants._timespan, new Double(entry.liveTimeSpan()));
			return info;
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		throw new FunctionException(pc, "CacheGetMetadata", 1, 2, args.length);
	}
}