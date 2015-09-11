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
package lucee.runtime.cache.eh;

import java.util.List;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.CacheEvent;
import lucee.commons.io.cache.CacheEventListener;
import lucee.commons.io.cache.CachePro;
import lucee.runtime.cache.CacheSupport;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.event.RegisteredEventListeners;

public abstract class EHCacheSupport extends CacheSupport implements Cache,CacheEvent {

	@Override
	public void register(CacheEventListener listener) {
		//RegisteredEventListeners listeners=cache.getCacheEventNotificationService();
		//listeners.registerListener(new ExpiresCacheEventListener());
		
		
		net.sf.ehcache.Cache cache = getCache();
		RegisteredEventListeners service = cache.getCacheEventNotificationService();
		service.registerListener(new EHCacheEventListener(listener));
		
		
		//.getCacheEventListeners().add(new EHCacheEventListener(listener));
	}

	@Override
	public boolean contains(String key) {
		if(!getCache().isKeyInCache(key))return false;
		return getCache().get(key)!=null;
	}

	@Override
	public Struct getCustomInfo() {
		Struct info=super.getCustomInfo();
		// custom
		CacheConfiguration conf = getCache().getCacheConfiguration();
		info.setEL("disk_expiry_thread_interval", new Double(conf.getDiskExpiryThreadIntervalSeconds()));
		info.setEL("disk_spool_buffer_size", new Double(conf.getDiskSpoolBufferSizeMB()*1024*1024));
		info.setEL("max_elements_in_memory", new Double(conf.getMaxElementsInMemory()));
		info.setEL("max_elements_on_disk", new Double(conf.getMaxElementsOnDisk()));
		info.setEL("time_to_idle", new Double(conf.getTimeToIdleSeconds()));
		info.setEL("time_to_live", new Double(conf.getTimeToLiveSeconds()));
		info.setEL(KeyConstants._name, conf.getName());
		return info;
	}

	@Override
	public List keys() {
		return getCache().getKeysWithExpiryCheck();
	}
	
	@Override
	public void put(String key, Object value, Long idleTime, Long liveTime) {
		Boolean eternal = idleTime==null && liveTime==null?Boolean.TRUE:Boolean.FALSE;
		Integer idle = idleTime==null?null : new Integer( (int)(idleTime.longValue()/1000) );
		Integer live = liveTime==null?null : new Integer( (int)(liveTime.longValue()/1000) );
		getCache().put(new Element(key, value ,eternal, idle, live));
	}



	@Override
	public CachePro decouple() {
		// is already decoupled by default
		return this;
	}
	

	@Override
	public CacheEntry getQuiet(String key, CacheEntry defaultValue){
		try {
			return new EHCacheEntry(getCache().getQuiet(key));
		} catch (Throwable t) {
			return defaultValue;
		}
	}
	
	@Override
	public CacheEntry getQuiet(String key) {
		return new EHCacheEntry(getCache().getQuiet(key));
	}

	protected abstract net.sf.ehcache.Cache getCache();
	
	
}