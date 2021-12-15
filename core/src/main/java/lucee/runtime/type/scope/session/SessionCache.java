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

import lucee.commons.io.log.Log;
import lucee.commons.lang.SerializableObject;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.storage.StorageScopeCache;
import lucee.runtime.type.scope.storage.StorageValue;

public final class SessionCache extends StorageScopeCache implements Session {

	private static final long serialVersionUID = -875719423763891692L;
	private static SerializableObject token = new SerializableObject();

	private SessionCache(PageContext pc, String cacheName, String appName, Struct sct, long lastStored) {
		super(pc, cacheName, appName, "session", SCOPE_SESSION, sct, lastStored);
	}

	/**
	 * Constructor of the class, clone existing
	 * 
	 * @param other
	 */
	private SessionCache(StorageScopeCache other, boolean deepCopy) {
		super(other, deepCopy);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new SessionCache(this, deepCopy);
	}

	/**
	 * load an new instance of the client datasource scope
	 * 
	 * @param cacheName
	 * @param appName
	 * @param pc
	 * @return client datasource scope
	 * @throws PageException
	 */
	public static Session getInstance(String cacheName, String appName, PageContext pc, Session existing, Log log) throws PageException {
		if (appName != null && appName.startsWith("no-in-memory-cache-")) existing = null;

		synchronized (token) {
			StorageValue sv = _loadData(pc, cacheName, appName, "session", log);
			if (sv != null) {
				long time = sv.lastModified();

				if (existing instanceof StorageScopeCache) {
					if (((StorageScopeCache) existing).lastModified() >= time) {
						return existing;
					}
				}
				return new SessionCache(pc, cacheName, appName, sv.getValue(), time);
			}
			else if (existing != null) {
				return existing;
			}

			SessionCache session = new SessionCache(pc, cacheName, appName, new StructImpl(), 0);
			session.store(pc);
			return session;
		}
	}

	public static Session getInstance(String cacheName, String appName, PageContext pc, Session existing, Log log, Session defaultValue) {
		try {
			return getInstance(cacheName, appName, pc, existing, log);
		}
		catch (PageException e) {
		}
		return defaultValue;
	}

	public static boolean hasInstance(String cacheName, String appName, PageContext pc) {
		try {
			return _loadData(pc, cacheName, appName, "session", null) != null;
		}
		catch (PageException e) {
			return false;
		}
	}
}