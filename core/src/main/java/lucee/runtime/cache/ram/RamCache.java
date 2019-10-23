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
package lucee.runtime.cache.ram;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.map.ReferenceMap;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.CachePro;
import lucee.commons.io.cache.exp.CacheException;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.cache.CacheSupport;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Constants;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Struct;

public class RamCache extends CacheSupport {

	public static final int DEFAULT_CONTROL_INTERVAL = 60;
	private Map<String, SoftReference<RamCacheEntry>> entries = new ConcurrentHashMap<String, SoftReference<RamCacheEntry>>();
	private long missCount;
	private int hitCount;

	private long idleTime;
	private long until;
	private int controlInterval = DEFAULT_CONTROL_INTERVAL * 1000;
	private boolean decouple;
	private Thread controller;

	// this is used by the config by reflection
	public RamCache() {
		Config config = ThreadLocalPageContext.getConfig();
		if (config != null) {
			CFMLEngineImpl engine = CFMLEngineImpl.toCFMLEngineImpl(ConfigWebUtil.getEngine(config), null);
			if (engine != null) {
				controller = new Controler(engine, this);
				controller.start();
			}
		}
	}

	public static void init(Config config, String[] cacheNames, Struct[] arguments) {// print.ds();
	}

	@Override
	public void init(Config config, String cacheName, Struct arguments) throws IOException {
		// RamCache is also used without calling init, because of that we have this test in constructor and
		// here
		if (controller == null) {
			CFMLEngineImpl engine = CFMLEngineImpl.toCFMLEngineImpl(ConfigWebUtil.getEngine(config), null);
			if (engine != null) {
				controller = new Controler(engine, this);
				controller.start();
			}
		}
		if (controller == null) throw new IOException("was not able to start controller");

		// out of memory
		boolean outOfMemory = Caster.toBooleanValue(arguments.get("outOfMemory", false), false);
		if (outOfMemory) entries = new ConcurrentHashMap<String, SoftReference<RamCacheEntry>>();

		// until
		long until = Caster.toLongValue(arguments.get("timeToLiveSeconds", Constants.LONG_ZERO), Constants.LONG_ZERO) * 1000;
		long idleTime = Caster.toLongValue(arguments.get("timeToIdleSeconds", Constants.LONG_ZERO), Constants.LONG_ZERO) * 1000;

		Object ci = arguments.get("controlIntervall", null);
		if (ci == null) ci = arguments.get("controlInterval", null);
		int intervalInSeconds = Caster.toIntValue(ci, DEFAULT_CONTROL_INTERVAL);
		init(until, idleTime, intervalInSeconds);
	}

	public RamCache init(long until, long idleTime, int intervalInSeconds) {
		this.until = until;
		this.idleTime = idleTime;
		this.controlInterval = intervalInSeconds * 1000;
		return this;
	}

	public void release() {
		entries.clear();
		missCount = 0;
		hitCount = 0;
		idleTime = 0;
		until = 0;
		controlInterval = DEFAULT_CONTROL_INTERVAL * 1000;
		decouple = false;
		if (controller != null && controller.isAlive()) controller.interrupt();
	}

	@Override
	public boolean contains(String key) {
		return _getQuiet(key, null) != null;
	}

	@Override
	public CacheEntry getQuiet(String key, CacheEntry defaultValue) {
		SoftReference<RamCacheEntry> tmp = entries.get(key);
		RamCacheEntry entry = tmp == null ? null : tmp.get();
		if (entry == null) {
			return defaultValue;
		}
		if (!valid(entry)) {
			entries.remove(key);
			return defaultValue;
		}
		if (decouple) entry = new RamCacheEntry(entry.getKey(), decouple(entry.getValue()), entry.idleTimeSpan(), entry.liveTimeSpan());
		return entry;
	}

	private CacheEntry _getQuiet(String key, CacheEntry defaultValue) {
		SoftReference<RamCacheEntry> tmp = entries.get(key);
		RamCacheEntry entry = tmp == null ? null : tmp.get();
		if (entry == null) {
			return defaultValue;
		}
		if (!valid(entry)) {
			entries.remove(key);
			return defaultValue;
		}
		return entry;
	}

	@Override
	public CacheEntry getCacheEntry(String key, CacheEntry defaultValue) {
		RamCacheEntry ce = (RamCacheEntry) _getQuiet(key, null);
		if (ce != null) {
			if (decouple) ce = new RamCacheEntry(ce.getKey(), decouple(ce.getValue()), ce.idleTimeSpan(), ce.liveTimeSpan());
			hitCount++;
			return ce.read();
		}
		missCount++;
		return defaultValue;
	}

	@Override
	public long hitCount() {
		return hitCount;
	}

	@Override
	public long missCount() {
		return missCount;
	}

	@Override
	public List<String> keys() {
		List<String> list = new ArrayList<String>();

		Iterator<Entry<String, SoftReference<RamCacheEntry>>> it = entries.entrySet().iterator();
		SoftReference<RamCacheEntry> entry;
		while (it.hasNext()) {
			entry = it.next().getValue();
			if (valid(entry.get())) list.add(entry.get().getKey());
		}
		return list;
	}

	@Override
	public void put(String key, Object value, Long idleTime, Long until) {

		SoftReference<RamCacheEntry> tmp = entries.get(key);
		RamCacheEntry entry = tmp == null ? null : tmp.get();
		if (entry == null) {
			entries.put(key, new SoftReference<RamCacheEntry>(
					new RamCacheEntry(key, decouple(value), idleTime == null ? this.idleTime : idleTime.longValue(), until == null ? this.until : until.longValue())));
		}
		else entry.update(value);
	}

	@Override
	public boolean remove(String key) {
		SoftReference<RamCacheEntry> tmp = entries.remove(key);
		RamCacheEntry entry = tmp == null ? null : tmp.get();
		if (entry == null) {
			return false;
		}
		return valid(entry);

	}

	@Override
	public int clear() throws IOException {
		int size = entries.size();
		entries.clear();
		return size;
	}

	public static class Controler extends Thread {

		private RamCache ramCache;
		private CFMLEngineImpl engine;

		public Controler(CFMLEngineImpl engine, RamCache ramCache) {
			this.engine = engine;
			this.ramCache = ramCache;
		}

		@Override
		public void run() {
			while (engine.isRunning()) {
				try {
					SystemUtil.sleep(ramCache.controlInterval);
					_run();
				}
				catch (Exception e) {
					LogUtil.log(null, "application", e);
				}
			}
		}

		private void _run() {
			RamCacheEntry[] values = ramCache.entries.values().toArray(new RamCacheEntry[ramCache.entries.size()]);
			for (int i = 0; i < values.length; i++) {
				if (!CacheSupport.valid(values[i])) {
					ramCache.entries.remove(values[i].getKey());
				}
			}
		}
	}

	@Override
	public void verify() throws CacheException {
		// this cache is in memory and always ok
	}

	@Override
	public CachePro decouple() {
		decouple = true;
		return this;
	}

	private Object decouple(Object value) {
		if (!decouple) return value;
		return Duplicator.duplicate(value, true);
	}

	@Override
	public Struct getCustomInfo() {
		Struct info = super.getCustomInfo();
		info.setEL("outOfMemoryHandling", entries instanceof ReferenceMap);
		return info;
	}

}
