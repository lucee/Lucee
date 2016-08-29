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
package lucee.runtime.type.scope.session;

import java.util.Date;

import lucee.commons.collection.MapPro;
import lucee.commons.collection.concurrent.ConcurrentHashMapPro;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.storage.StorageCache;
import lucee.runtime.type.scope.storage.StorageScopeItem;
import lucee.runtime.type.scope.storage.StorageVal;

public final class StorageSessionCache extends StorageCache implements Session {
	
	private static final long serialVersionUID = -875719423763891692L;

	private StorageSessionCache(PageContext pc,String cacheName, String appName,MapPro<Collection.Key,StorageScopeItem> data, long lastStored) { 
		super(pc,cacheName,appName,"session",SCOPE_SESSION,data, lastStored);
	}

	/**
	 * Constructor of the class, clone existing
	 * @param other
	 */
	private StorageSessionCache(StorageCache other,boolean deepCopy) {
		super(other,deepCopy);
	}
	
	@Override
	public Collection duplicate(boolean deepCopy) {
    	return new StorageSessionCache(this,deepCopy);
	}
	
	/**
	 * load an new instance of the client datasource scope
	 * @param cacheName 
	 * @param appName
	 * @param pc
	 * @return client datasource scope
	 * @throws PageException
	 */
	public synchronized static Session getInstance(String cacheName, String appName, PageContext pc, Session existing, Log log) throws PageException {
		StorageVal sv = _loadData(pc, cacheName, appName,"session", log);
		if(appName!=null && appName.startsWith("no-in-memory-cache-")) existing=null;
		if(sv!=null) {
			long time = sv.lastModified();
			
			if(existing instanceof StorageCache) {
				if(((StorageCache)existing).lastModified()>=time) {
					return existing;
				}
			}
			return new StorageSessionCache(pc,cacheName,appName,sv.getValue(),time);
		}
		else if(existing!=null) {
			return  existing;
		}

		StorageSessionCache session = new StorageSessionCache(pc,cacheName,appName,new ConcurrentHashMapPro<Collection.Key,StorageScopeItem>(),0);
		session.store(pc.getConfig());
		return session;
	}

	public static Session getInstance(String cacheName, String appName, PageContext pc, Session existing, Log log, Session defaultValue) {
		try {
			return getInstance(cacheName, appName, pc,existing, log);
		}
		catch (PageException e) {}
		return defaultValue;
	}

	
	public synchronized static boolean hasInstance(String cacheName, String appName, PageContext pc) {
		try {
			return _loadData(pc, cacheName, appName,"session", null)!=null;
		} 
		catch (PageException e) {
			return false;
		}
	}
}