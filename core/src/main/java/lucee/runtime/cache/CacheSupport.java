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
package lucee.runtime.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.CacheEntryFilter;
import lucee.commons.io.cache.CacheKeyFilter;
import lucee.commons.io.cache.CachePro;
import lucee.commons.io.cache.exp.CacheException;
import lucee.runtime.type.Struct;

public abstract class CacheSupport implements CachePro {

	@Override
	public List<String> keys(CacheKeyFilter filter) throws IOException {
		boolean all = CacheUtil.allowAll(filter);

		List<String> keys = keys();
		List<String> list = new ArrayList<String>();
		Iterator<String> it = keys.iterator();
		String key;
		while (it.hasNext()) {
			key = it.next();
			if (all || filter.accept(key)) list.add(key);
		}
		return list;
	}

	@Override
	public void verify() throws CacheException {
		getCustomInfo();
	}

	@Override
	public List<String> keys(CacheEntryFilter filter) throws IOException {
		boolean all = CacheUtil.allowAll(filter);

		List<String> keys = keys();
		List<String> list = new ArrayList<String>();
		Iterator<String> it = keys.iterator();
		String key;
		CacheEntry entry;
		while (it.hasNext()) {
			key = it.next();
			entry = getQuiet(key, null);
			if (all || filter.accept(entry)) list.add(key);
		}
		return list;
	}

	@Override
	public List<CacheEntry> entries() throws IOException {
		List<String> keys = keys();
		List<CacheEntry> list = new ArrayList<CacheEntry>();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			list.add(getQuiet(it.next(), null));
		}
		return list;
	}

	@Override
	public List<CacheEntry> entries(CacheKeyFilter filter) throws IOException {
		List<String> keys = keys();
		List<CacheEntry> list = new ArrayList<CacheEntry>();
		Iterator<String> it = keys.iterator();
		String key;
		while (it.hasNext()) {
			key = it.next();
			if (filter.accept(key)) list.add(getQuiet(key, null));
		}
		return list;
	}

	@Override
	public List<CacheEntry> entries(CacheEntryFilter filter) throws IOException {
		List<String> keys = keys();
		List<CacheEntry> list = new ArrayList<CacheEntry>();
		Iterator<String> it = keys.iterator();
		CacheEntry entry;
		while (it.hasNext()) {
			entry = getQuiet(it.next(), null);
			if (filter.accept(entry)) list.add(entry);
		}
		return list;
	}

	// there was the wrong generic type defined in the older interface, because of that we do not define
	// a generic type at all here, just to be sure
	@Override
	public List values() throws IOException {
		List<String> keys = keys();
		List<Object> list = new ArrayList<Object>();
		Iterator<String> it = keys.iterator();
		String key;
		while (it.hasNext()) {
			key = it.next();
			list.add(getQuiet(key, null).getValue());
		}
		return list;
	}

	// there was the wrong generic type defined in the older interface, because of that we do not define
	// a generic type at all here, just to be sure
	@Override
	public List values(CacheEntryFilter filter) throws IOException {
		if (CacheUtil.allowAll(filter)) return values();

		List<String> keys = keys();
		List<Object> list = new ArrayList<Object>();
		Iterator<String> it = keys.iterator();
		String key;
		CacheEntry entry;
		while (it.hasNext()) {
			key = it.next();
			entry = getQuiet(key, null);
			if (filter.accept(entry)) list.add(entry.getValue());
		}
		return list;
	}

	// there was the wrong generic type defined in the older interface, because of that we do not define
	// a generic type at all here, just to be sure
	@Override
	public List values(CacheKeyFilter filter) throws IOException {
		if (CacheUtil.allowAll(filter)) return values();

		List<String> keys = keys();

		List<Object> list = new ArrayList<Object>();
		Iterator<String> it = keys.iterator();
		String key;
		while (it.hasNext()) {
			key = it.next();
			if (filter.accept(key)) {
				CacheEntry ce = getQuiet(key, null);
				if (ce != null) // possible that the entry is gone since keys(); call above
					list.add(ce.getValue());
			}
		}
		return list;
	}

	@Override
	public int remove(CacheEntryFilter filter) throws IOException {
		if (CacheUtil.allowAll(filter)) return clear();

		List<String> keys = keys();
		int count = 0;
		Iterator<String> it = keys.iterator();
		String key;
		CacheEntry entry;
		while (it.hasNext()) {
			key = it.next();
			entry = getQuiet(key, null);
			if (filter == null || filter.accept(entry)) {
				remove(key);
				count++;
			}
		}
		return count;
	}

	@Override
	public int remove(CacheKeyFilter filter) throws IOException {
		if (CacheUtil.allowAll(filter)) return clear();

		List<String> keys = keys();
		int count = 0;
		Iterator<String> it = keys.iterator();
		String key;
		while (it.hasNext()) {
			key = it.next();
			if (filter == null || filter.accept(key)) {
				remove(key);
				count++;
			}
		}
		return count;
	}

	@Override
	public Struct getCustomInfo() {
		return CacheUtil.getInfo(this);
	}

	@Override
	public Object getValue(String key) throws IOException {
		return getCacheEntry(key).getValue();
	}

	@Override
	public Object getValue(String key, Object defaultValue) {
		CacheEntry entry = getCacheEntry(key, null);
		if (entry == null) return defaultValue;
		return entry.getValue();
	}

	protected static boolean valid(CacheEntry entry) {
		if (entry == null) return false;
		long now = System.currentTimeMillis();
		if (entry.liveTimeSpan() > 0 && entry.liveTimeSpan() + getTime(entry.lastModified()) < now) {
			return false;
		}
		if (entry.idleTimeSpan() > 0 && entry.idleTimeSpan() + getTime(entry.lastHit()) < now) {
			return false;
		}
		return true;
	}

	private static long getTime(Date date) {
		return date == null ? 0 : date.getTime();
	}

	@Override
	public CacheEntry getCacheEntry(String key) throws IOException {
		CacheEntry entry = getCacheEntry(key, null);
		if (entry == null) throw new CacheException("there is no valid cache entry with key [" + key + "]");
		return entry;
	}

	public CacheEntry getQuiet(String key) throws IOException {
		CacheEntry entry = getQuiet(key, null);
		if (entry == null) throw new CacheException("there is no valid cache entry with key [" + key + "]");
		return entry;
	}

	public abstract CacheEntry getQuiet(String key, CacheEntry defaultValue);

	/**
	 * remove all entries
	 * 
	 * @return returns the count of the removal or -1 if this information is not available
	 */
	public abstract int clear() throws IOException;

}