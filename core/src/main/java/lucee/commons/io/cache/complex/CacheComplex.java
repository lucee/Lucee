package lucee.commons.io.cache.complex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.CacheEntryFilter;
import lucee.commons.io.cache.CacheKeyFilter;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.Config;
import lucee.runtime.type.Struct;

public class CacheComplex implements Cache {

	private Cache cache;
	private CacheConnection cc;

	public CacheComplex(CacheConnection cc, Cache cache) {
		this.cc = cc;
		this.cache = cache;

	}

	@Override
	public List<CacheEntry> entries() throws IOException {
		return entries(cache.entries());
	}

	@Override
	public List<CacheEntry> entries(CacheKeyFilter filter) throws IOException {
		return entries(cache.entries(filter));
	}

	@Override
	public List<CacheEntry> entries(CacheEntryFilter filter) throws IOException {
		return entries(cache.entries(filter));
	}

	private List<CacheEntry> entries(List<CacheEntry> entries) {
		if (entries == null || entries.size() == 0) return entries;

		Iterator<CacheEntry> it = entries.iterator();
		ArrayList<CacheEntry> list = new ArrayList<CacheEntry>(entries.size());
		CacheEntry entry;
		while (it.hasNext()) {
			entry = it.next();
			if (entry != null) list.add(new CacheComplexEntry(this, entry));
		}
		return list;
	}

	@Override
	public CacheEntry getCacheEntry(String key) throws IOException {
		CacheEntry entry = cache.getCacheEntry(key);
		if (entry == null) return entry;
		return new CacheComplexEntry(this, entry);
	}

	@Override
	public CacheEntry getCacheEntry(String key, CacheEntry defaultValue) {
		CacheEntry entry = cache.getCacheEntry(key, defaultValue);
		if (entry == null || entry == defaultValue) return entry;
		return new CacheComplexEntry(this, entry);
	}

	@Override
	public Struct getCustomInfo() throws IOException {
		return CacheUtil.getInfo(cache.getCustomInfo(), cache);
	}

	@Override
	public Object getValue(String key) throws IOException {
		Object value = cache.getValue(key);
		if (value instanceof CacheComplexData) return ((CacheComplexData) value).value;
		return value;
	}

	@Override
	public Object getValue(String key, Object defaultValue) {
		Object value = cache.getValue(key, defaultValue);
		if (value instanceof CacheComplexData) return ((CacheComplexData) value).value;
		return value;
	}

	@Override
	public long hitCount() throws IOException {
		return cache.hitCount();
	}

	@Override
	public long missCount() throws IOException {
		return cache.missCount();
	}

	@Override
	public void put(String key, Object value, Long idle, Long until) throws IOException {
		cache.put(key, value == null ? null : new CacheComplexData(value, idle, until), idle, until);
	}

	@Override
	public int remove(CacheEntryFilter filter) throws IOException {
		return cache.remove(filter);
	}

	@Override
	public List<String> keys(CacheEntryFilter filter) throws IOException {
		return cache.keys(filter);
	}

	@Override
	public List<Object> values() throws IOException {
		return values(cache.values());
	}

	@Override
	public List<Object> values(CacheKeyFilter filter) throws IOException {
		return values(cache.values(filter));
	}

	@Override
	public List<Object> values(CacheEntryFilter filter) throws IOException {
		return values(cache.values(filter));
	}

	public List<Object> values(List<Object> values) throws IOException {
		if (values == null || values.size() == 0) return values;

		ArrayList<Object> list = new ArrayList<Object>();
		Iterator<Object> it = values.iterator();
		Object v;
		while (it.hasNext()) {
			v = it.next();
			if (v instanceof CacheComplexData) list.add(((CacheComplexData) v).value);
			else list.add(v);
		}
		return list;
	}

	/////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean contains(String key) throws IOException {
		return cache.contains(key);
	}

	@Override
	public void init(Config config, String arg1, Struct arg2) throws IOException {
		cache.init(config, arg1, arg2);
	}

	@Override
	public List<String> keys() throws IOException {
		return cache.keys();
	}

	@Override
	public List<String> keys(CacheKeyFilter filter) throws IOException {
		return cache.keys(filter);
	}

	@Override
	public boolean remove(String key) throws IOException {
		return cache.remove(key);
	}

	@Override
	public int remove(CacheKeyFilter filter) throws IOException {
		return cache.remove(filter);
	}

}
