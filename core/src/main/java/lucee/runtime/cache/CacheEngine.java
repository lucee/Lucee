/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.exp.CacheException;
import lucee.commons.io.log.LogUtil;
import lucee.loader.util.Util;
import lucee.runtime.cache.util.CacheKeyFilterAll;
import lucee.runtime.cache.util.WildCardFilter;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;

// MUST this must be come from configuration
public class CacheEngine {

	private static Map caches = new HashMap();
	private Cache cache;

	public CacheEngine(Cache cache) {
		this.cache = cache;
	}

	public void delete(String key, boolean throwWhenNotExists) throws IOException {
		if (!cache.remove(key) && throwWhenNotExists) throw new CacheException("there is no entry in cache with key [" + key + "]");
	}

	public boolean exists(String key) throws IOException {
		return cache.contains(key);
	}

	public int flush(String key, String filter) throws IOException {
		if (!Util.isEmpty(key)) return cache.remove(key) ? 1 : 0;
		if (!Util.isEmpty(filter)) return cache.remove(new WildCardFilter(filter, false));
		return cache.remove(CacheKeyFilterAll.getInstance());
	}

	public Object get(String key, Object defaultValue) {
		return cache.getValue(key, defaultValue);
	}

	public Object get(String key) throws IOException {
		return cache.getValue(key);
	}

	public Array keys(String filter) {
		try {
			List keys;
			if (Util.isEmpty(filter)) keys = cache.keys();
			else keys = cache.keys(new WildCardFilter(filter, false));
			return Caster.toArray(keys);
		}
		catch (Exception e) {
		}
		return new ArrayImpl();
	}

	public Struct list(String filter) {

		Struct sct = new StructImpl();
		try {
			List entries;
			if (Util.isEmpty(filter)) entries = cache.entries();
			else entries = cache.entries(new WildCardFilter(filter, false));

			Iterator it = entries.iterator();
			CacheEntry entry;
			while (it.hasNext()) {
				entry = (CacheEntry) it.next();
				sct.setEL(entry.getKey(), entry.getValue());
			}
		}
		catch (Exception e) {
			LogUtil.log(null, "cache", e);
		}
		return sct;
	}

	public void set(String key, Object value, TimeSpan timespan) throws IOException {
		Long until = timespan == null ? null : Long.valueOf(timespan.getMillis());
		cache.put(key, value, null, until);
	}

	public Struct info() throws IOException {
		return cache.getCustomInfo();
	}

	public Struct info(String key) throws IOException {
		if (key == null) return info();
		CacheEntry entry = cache.getCacheEntry(key);
		return entry.getCustomInfo();
	}

	public Cache getCache() {
		return cache;
	}
}