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

import java.io.Serializable;

import lucee.commons.io.cache.CacheEventListener;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;


public class EHCacheEventListener implements net.sf.ehcache.event.CacheEventListener,Serializable {

	private static final long serialVersionUID = 5931737203770901097L;

	private CacheEventListener listener;

	public EHCacheEventListener(CacheEventListener listener) {
		this.listener=listener;
	}
	

	@Override
	public void notifyElementExpired(Ehcache cache, Element element) {
		listener.onExpires(new EHCacheEntry(element));
	}

	@Override
	public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
		listener.onPut(new EHCacheEntry(element));
	}

	@Override
	public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
		listener.onRemove(new EHCacheEntry(element));
	}
	
	
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyElementEvicted(Ehcache arg0, Element arg1) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
		listener.onPut(new EHCacheEntry(element));
	}

	@Override
	public void notifyRemoveAll(Ehcache arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object clone(){
		return new EHCacheEventListener(listener.duplicate()); 
	}
}