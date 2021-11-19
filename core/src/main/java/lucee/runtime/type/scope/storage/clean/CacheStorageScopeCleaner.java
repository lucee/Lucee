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
package lucee.runtime.type.scope.storage.clean;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.CacheKeyFilter;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.type.scope.storage.StorageScopeCache;
import lucee.runtime.type.scope.storage.StorageScopeEngine;
import lucee.runtime.type.scope.storage.StorageScopeListener;

public class CacheStorageScopeCleaner extends StorageScopeCleanerSupport {

	private Filter filter;

	public CacheStorageScopeCleaner(int type, StorageScopeListener listener) {
		super(type, listener, INTERVALL_MINUTE);
		// this.strType=VariableInterpreter.scopeInt2String(type);
		filter = new Filter(strType);
	}

	@Override
	public void init(StorageScopeEngine engine) {
		super.init(engine);

	}

	@Override
	protected void _clean() {
		ConfigWeb config = engine.getFactory().getConfig();
		Map<String, CacheConnection> connections = config.getCacheConnections();
		CacheConnection cc;

		if (connections != null) {
			Map.Entry<String, CacheConnection> entry;
			Iterator<Entry<String, CacheConnection>> it = connections.entrySet().iterator();
			while (it.hasNext()) {
				entry = it.next();
				cc = entry.getValue();
				if (cc.isStorage()) {
					try {
						clean(cc, config);
					}
					catch (IOException e) {
						error(e);
					}
				}
			}
		}

	}

	private void clean(CacheConnection cc, ConfigWeb config) throws IOException {
		Cache cache = cc.getInstance(config);
		int len = filter.length(), index;
		List<CacheEntry> entries = cache.entries(filter);
		CacheEntry ce;
		long expires;

		String key, appname, cfid;
		if (entries.size() > 0) {
			Iterator<CacheEntry> it = entries.iterator();
			while (it.hasNext()) {
				ce = it.next();

				Date lm = ce.lastModified();
				long time = lm != null ? lm.getTime() : 0;

				expires = time + ce.idleTimeSpan() - StorageScopeCache.SAVE_EXPIRES_OFFSET;
				if (expires <= System.currentTimeMillis()) {
					key = ce.getKey().substring(len);
					index = key.indexOf(':');
					cfid = key.substring(0, index);
					appname = key.substring(index + 1);

					if (listener != null) listener.doEnd(engine, this, appname, cfid);
					info("remove " + strType + "/" + appname + "/" + cfid + " from cache " + cc.getName());
					engine.remove(type, appname, cfid);
					cache.remove(ce.getKey());
				}
			}
		}

		// engine.remove(type,appName,cfid);

		// return (Struct) cache.getValue(key,null);
	}

	public static class Filter implements CacheKeyFilter {
		private String startsWith;

		public Filter(String type) {
			startsWith = new StringBuilder("lucee-storage:").append(type).append(":").toString().toUpperCase();
		}

		@Override
		public String toPattern() {
			return startsWith + "*";
		}

		@Override
		public boolean accept(String key) {
			return key.startsWith(startsWith);
		}

		public int length() {
			return startsWith.length();
		}

	}
}