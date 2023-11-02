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
package lucee.commons.io.res.type.cache;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.Resources;
import lucee.commons.io.res.util.ResourceLockImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cache.ram.RamCache;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Constants;
import lucee.runtime.type.Struct;

/**
 * Resource Provider for ram resource
 */
public final class CacheResourceProvider implements ResourceProviderPro {

	private String scheme = "ram";

	boolean caseSensitive = true;
	private long lockTimeout = 1000;
	private ResourceLockImpl lock = new ResourceLockImpl(lockTimeout, caseSensitive);
	private Map arguments;

	private Cache defaultCache;

	private Set<Integer> inits = new HashSet<Integer>();

	// private Config config;

	/**
	 * initialize ram resource
	 * 
	 * @param scheme
	 * @param arguments
	 * @return RamResource
	 */
	@Override
	public ResourceProvider init(String scheme, Map arguments) {
		if (!StringUtil.isEmpty(scheme)) this.scheme = scheme;

		if (arguments != null) {
			this.arguments = arguments;
			Object oCaseSensitive = arguments.get("case-sensitive");
			if (oCaseSensitive != null) {
				caseSensitive = Caster.toBooleanValue(oCaseSensitive, true);
			}

			// lock-timeout
			Object oTimeout = arguments.get("lock-timeout");
			if (oTimeout != null) {
				lockTimeout = Caster.toLongValue(oTimeout, lockTimeout);
			}
		}
		lock.setLockTimeout(lockTimeout);
		lock.setCaseSensitive(caseSensitive);

		return this;
	}

	@Override
	public Resource getResource(String path) {
		path = ResourceUtil.removeScheme(scheme, path);
		if (!StringUtil.startsWith(path, '/')) path = "/" + path;
		return new CacheResource(this, path);
	}

	/**
	 * returns core for this path if exists, otherwise return null
	 * 
	 * @param path
	 * @return core or null
	 */
	CacheResourceCore getCore(String path, String name) {
		Object obj = getCache().getValue(toKey(path, name), null);
		if (obj instanceof CacheResourceCore) return (CacheResourceCore) obj;
		return null;
	}

	void touch(String path, String name) throws IOException {
		Cache cache = getCache();
		CacheEntry ce = cache.getCacheEntry(toKey(path, name), null);
		if (ce != null) {
			cache.put(ce.getKey(), ce.getValue(), ce.idleTimeSpan(), ce.liveTimeSpan());
		}
	}

	Struct getMeta(String path, String name) {
		CacheEntry ce = getCache().getCacheEntry(toKey(path, name), null);
		if (ce != null) return ce.getCustomInfo();
		return null;
	}

	String[] getChildNames(String path) throws IOException {
		List list = getCache().values(new ChildrenFilter(path));
		String[] arr = new String[list.size()];
		Iterator it = list.iterator();
		int index = 0;
		while (it.hasNext()) {
			arr[index++] = ((CacheResourceCore) it.next()).getName();
		}
		// TODO remove none CacheResourceCore elements
		return arr;
	}
	/*
	 * CacheResourceCore[] getChildren(String path) { List list = getCache().values(new
	 * ChildrenFilter(path)); CacheResourceCore[] arr = new CacheResourceCore[list.size()]; Iterator it
	 * = list.iterator(); int index=0; while(it.hasNext()){ arr[index++]=(CacheResourceCore) it.next();
	 * } // TODO remove none CacheResourceCore elements return arr; }
	 */

	/**
	 * create a new core
	 * 
	 * @param path
	 * @param type
	 * @return created core
	 * @throws IOException
	 */
	CacheResourceCore createCore(String path, String name, int type) throws IOException {
		CacheResourceCore value = new CacheResourceCore(type, path, name);
		getCache().put(toKey(path, name), value, null, null);
		return value;
	}

	void removeCore(String path, String name) throws IOException {
		getCache().remove(toKey(path, name));
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public void setResources(Resources resources) {
		// this.resources=resources;
	}

	@Override
	public void lock(Resource res) throws IOException {
		lock.lock(res);
	}

	@Override
	public void unlock(Resource res) {
		lock.unlock(res);
	}

	@Override
	public void read(Resource res) throws IOException {
		lock.read(res);
	}

	@Override
	public boolean isAttributesSupported() {
		return true;
	}

	@Override
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean isModeSupported() {
		return true;
	}

	@Override
	public Map getArguments() {
		return arguments;
	}

	public Cache getCache() {
		PageContext pc = ThreadLocalPageContext.get();
		Cache c = CacheUtil.getDefault(pc, Config.CACHE_TYPE_RESOURCE, null);
		if (c == null) {
			// CFMLEngineImpl e=null;
			if (defaultCache == null) {
				defaultCache = new RamCache().init(0, 0, RamCache.DEFAULT_CONTROL_INTERVAL);
			}
			c = defaultCache;
		}
		if (!inits.contains(c.hashCode())) {
			String k = toKey("null", "");
			try {
				if (!c.contains(k)) {
					CacheResourceCore value = new CacheResourceCore(CacheResourceCore.TYPE_DIRECTORY, null, "");
					c.put(k, value, Constants.LONG_ZERO, Constants.LONG_ZERO);
				}
			}
			catch (IOException e) {
				// simply ignore
			}
			inits.add(c.hashCode());
		}
		return c;
	}

	private String toKey(String path, String name) {
		if (caseSensitive) return path + ":" + name;
		return (path + ":" + name).toLowerCase();
	}

	@Override
	public char getSeparator() {
		return '/';
	}

}