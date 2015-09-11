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
package lucee.runtime.cache.tag.request;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerFilter;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;

public class RequestCacheHandler implements CacheHandler {
	
	private static ThreadLocal<Map<String,CacheItem>> data=new ThreadLocal<Map<String,CacheItem>>() {
		@Override 
		protected Map<String,CacheItem> initialValue() {
			return new HashMap<String, CacheItem>();
		}
	};
	private int cacheType;
	private String id;

	public RequestCacheHandler() {}
	
	@Override
	public void init(ConfigWeb cw,String id,int cacheType){
		this.id=id;
		this.cacheType=cacheType; 
	}

	@Override
	public CacheItem get(PageContext pc, String id) {
		return duplicate(data.get().get(id));
	}

	@Override
	public boolean remove(PageContext pc, String id) {
		return data.get().remove(id)!=null;
	}

	@Override
	public void set(PageContext pc, String id,Object cachedwithin, CacheItem value) {
		// cachedwithin is ignored in this cache, it should be "request"
		data.get().put(id,duplicate(value));
	}

	private CacheItem duplicate(CacheItem value) {
		if(value==null) return null;
		return (CacheItem) Duplicator.duplicate(value, true);
	}

	@Override
	public void clear(PageContext pc) {
		data.get().clear();
	}

	@Override
	public void clear(PageContext pc, CacheHandlerFilter filter) {
		Iterator<Entry<String, CacheItem>> it = data.get().entrySet().iterator();
		Entry<String, CacheItem> e;
		while(it.hasNext()){
			e = it.next();
			if(filter==null || filter.accept(e.getValue()))
				it.remove();
		}
	}

	@Override
	public int size(PageContext pc) {
		return data.get().size();
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
	
	@Override
	public boolean acceptCachedWithin(Object cachedWithin) {
		String str=Caster.toString(cachedWithin,"").trim();
		return str.equalsIgnoreCase("request");
	}

	@Override
	public String pattern() {
		return "request";
	}
}