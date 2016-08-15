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
package lucee.runtime.cache.tag.timespan;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.CachePro;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cache.ram.RamCache;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerCollectionImpl;
import lucee.runtime.cache.tag.CacheHandlerFilter;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.util.CacheKeyFilterAll;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.dt.TimeSpan;

public class TimespanCacheHandler implements CacheHandler {

	private int cacheType;
	private Cache defaultCache;
	private String id;

	public TimespanCacheHandler(){}

	@Override
	public void init(ConfigWeb cw,String id,int cacheType){
		this.cacheType=cacheType; 
		this.id=id;
	}
	
	public void setDefaultCache(Cache defaultCache){
		this.defaultCache=defaultCache;
	}

	@Override
	public CacheItem get(PageContext pc, String id) {
		return CacheHandlerCollectionImpl.toCacheItem(getCache(pc).getValue(id,null),null);
	}
	
	@Override
	public boolean remove(PageContext pc, String id) {
		try {
			return getCache(pc).remove(id);
		}
		catch (IOException e) {}
		return false;
	}
	

	@Override
	public void set(PageContext pc, String id, Object cachedWithin, CacheItem value) throws PageException {
		long timeSpan;
		if(Decision.isDate(cachedWithin, false) && !(cachedWithin instanceof TimeSpan))
			timeSpan=Caster.toDate(cachedWithin, null).getTime()-System.currentTimeMillis();
		else
			timeSpan = Caster.toTimespan(cachedWithin).getMillis();
		
		// clear when timespan smaller or equal to 0
		if(timeSpan<=0) {
			try {
				getCache(pc).remove(id);
			} 
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
			return;
		}
		
		try {
			getCache(pc).put(id, value, Long.valueOf(timeSpan), Long.valueOf(timeSpan));
		} catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}
	
	@Override
	public void clean(PageContext pc) {
		try{
		Cache c = getCache(pc);
		List<CacheEntry> entries = c.entries();
		if(entries.size()<100) return;
		
		Iterator<CacheEntry> it = entries.iterator();
		while(it.hasNext()){
			it.next(); // touch them to makes sure the cache remove them, not really good, cache must do this by itself
		}
		}
		catch(IOException ioe){}
	}
	

	@Override
	public void clear(PageContext pc) {
		try {
			getCache(pc).remove(CacheKeyFilterAll.getInstance());
		}
		catch (IOException e) {}
	}
	

	@Override
	public void clear(PageContext pc, CacheHandlerFilter filter) {
		CacheHandlerCollectionImpl.clear(pc, getCache(pc), filter);
	}

	@Override
	public int size(PageContext pc) {
		try {
			return getCache(pc).keys().size();
		}
		catch (IOException e) {
			return 0;
		}
	}
	

	private Cache getCache(PageContext pc) {
		Cache c = CacheUtil.getDefault(pc,cacheType,null);
		if(c==null) {
			if(defaultCache==null){
				CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig(pc));
				if(!(engine instanceof CFMLEngineImpl))
					throw new RuntimeException(engine.getClass().getName()+" is not from type CFMLEngineImpl");
				RamCache rm = new RamCache((CFMLEngineImpl) engine).init(0, 0, RamCache.DEFAULT_CONTROL_INTERVAL);
				rm.decouple();
				defaultCache=rm;
			}
			return defaultCache;
		}
		if(c instanceof CachePro) return ((CachePro) c).decouple();
		return c;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public void release(PageContext pc) {
		// to nothing
	}

	@Override
	public boolean acceptCachedWithin(Object cachedWithin) {
		return Caster.toTimespan(cachedWithin,null)!=null;
	}

	@Override
	public String pattern() {
		// TODO Auto-generated method stub
		return "#createTimespan(0,0,0,10)#";
	}

}