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
package lucee.runtime.cache.tag;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.op.Duplicator;

public abstract class MapCacheHandler implements CacheHandler {

	private int cacheType;
	private String id;

	public MapCacheHandler() {
	}

	@Override
	public void init(ConfigWeb cw, String id, int cacheType) {
		this.id = id;
		this.cacheType = cacheType;
	}

	@Override
	public CacheItem get(PageContext pc, String id) {
		return duplicate(map().get(id));
	}

	@Override
	public boolean remove(PageContext pc, String id) {
		return map().remove(id) != null;
	}

	@Override
	public void set(PageContext pc, String id, Object cachedwithin, CacheItem value) {
		// cachedwithin is ignored in this cache, it should be "request"
		map().put(id, duplicate(value));
	}

	private CacheItem duplicate(CacheItem value) {
		if (value == null) return null;
		return (CacheItem) Duplicator.duplicate(value, true);
	}

	@Override
	public void clear(PageContext pc) {
		map().clear();
	}

	@Override
	public void clear(PageContext pc, CacheHandlerFilter filter) {
		Iterator<Entry<String, CacheItem>> it = map().entrySet().iterator();
		Entry<String, CacheItem> e;
		while (it.hasNext()) {
			e = it.next();
			if (filter == null || filter.accept(e.getValue())) it.remove();
		}
	}

	@Override
	public int size(PageContext pc) {
		return map().size();
	}

	@Override
	public void clean(PageContext pc) {
		// not necessary
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public void release(PageContext pc) {
		clear(pc);
	}

	protected abstract Map<String, CacheItem> map();
}