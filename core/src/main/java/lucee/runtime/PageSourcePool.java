/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
package lucee.runtime;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.watch.PageSourcePoolWatcher;
import lucee.commons.lang.SerializableObject;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Constants;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTimeImpl;

/**
 * pool to handle pages
 */
public final class PageSourcePool implements Dumpable {
	// TODO must not be thread safe, is used in sync block only
	private final Map<String, SoftReference<PageSource>> pageSources = new ConcurrentHashMap<String, SoftReference<PageSource>>();
	private int maxSize_min = 767;
	private PageSourcePoolWatcher watcher;
	private Object token = new SerializableObject();
	private MappingImpl mapping;

	// max size of the pool cache
	private static final int MAXSIZE;
	private static final int MAXSIZE_MIN;
	// timeout timeout for files
	private static final int TIMEOUT;

	static {
		MAXSIZE = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.pagePool.maxSize", null), 10000);
		MAXSIZE_MIN = Math.max(MAXSIZE - 1000, 1000);
		TIMEOUT = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.pagePool.timeout", null), 10000);

	}

	/**
	 * constructor of the class
	 */
	public PageSourcePool(MappingImpl mapping) {
		this.mapping = mapping;
		// print.ds();
	}

	/**
	 * return pages matching to key
	 * 
	 * @param key key for the page
	 * @param updateAccesTime define if do update access time
	 * @return page
	 */
	public PageSource getPageSource(String key, boolean updateAccesTime) { // DO NOT CHANGE INTERFACE (used by Argus Monitor)
		SoftReference<PageSource> tmp = pageSources.get(key.toLowerCase());
		if (tmp == null) return null;
		PageSource ps = tmp.get();
		if (ps == null) {
			pageSources.remove(key.toLowerCase());
			return null;
		}
		if (updateAccesTime) ps.setLastAccessTime();
		return ps;
	}

	/**
	 * sts a page object to the page pool
	 * 
	 * @param key key reference to store page object
	 * @param ps pagesource to store
	 */
	public void setPage(String key, PageSource ps) {
		if (pageSources.size() > MAXSIZE) {
			cleanLoaders();
		}
		if ((mapping.getInspectTemplate() == ConfigPro.INSPECT_AUTO) && (watcher == null || pageSources.size() == 0)) {
			synchronized (token) {
				if ((mapping.getInspectTemplate() == ConfigPro.INSPECT_AUTO) && (watcher == null || pageSources.size() == 0)) {
					if (watcher != null) {
						watcher.stopIfNecessary();
					}
					watcher = new PageSourcePoolWatcher(mapping, this, pageSources);
					watcher.startIfNecessary();
				}
			}
		}

		ps.setLastAccessTime();
		pageSources.put(key.toLowerCase(), new SoftReference<PageSource>(ps));
	}

	/**
	 * returns if page object exists
	 * 
	 * @param key key reference to a page object
	 * @return has page object or not
	 */
	public boolean exists(String key) {
		return pageSources.containsKey(key.toLowerCase());
	}

	/**
	 * @return returns an array of all keys in the page pool
	 */
	public String[] keys() {
		if (pageSources == null) return new String[0];
		Set<String> set = pageSources.keySet();
		return set.toArray(new String[set.size()]);
	}

	public List<PageSource> values(boolean loaded) {
		List<PageSource> vals = new ArrayList<>();
		if (pageSources == null) return vals;

		PageSource ps;
		for (SoftReference<PageSource> sr: pageSources.values()) {
			ps = sr.get();
			if (ps != null && (!loaded || ((PageSourceImpl) ps).isLoad())) vals.add(ps);

		}
		return vals;
	}

	public boolean flushPage(String key) {
		SoftReference<PageSource> tmp = pageSources.get(key.toLowerCase());
		PageSource ps = tmp == null ? null : tmp.get();
		if (ps != null) {
			((PageSourceImpl) ps).flush();
			return true;
		}

		Iterator<SoftReference<PageSource>> it = pageSources.values().iterator();
		while (it.hasNext()) {
			ps = it.next().get();
			if (key.equalsIgnoreCase(ps.getClassName())) {
				((PageSourceImpl) ps).flush();
				return true;
			}
		}
		return false;
	}

	/**
	 * @return returns the size of the pool
	 */
	public int size() {
		int size = 0;

		for (Entry<String, SoftReference<PageSource>> entry: pageSources.entrySet()) {
			if (entry.getValue().get() != null) size++;
			else {
				pageSources.remove(entry.getKey());
			}
		}
		return size;
	}

	/**
	 * @return returns if pool is empty or not
	 */
	public boolean isEmpty() {
		return size() > 0;
	}

	public void cleanLoaders() {
		if (pageSources.size() < MAXSIZE) return;
		synchronized (pageSources) {
			{
				for (Entry<String, SoftReference<PageSource>> e: pageSources.entrySet()) {
					if (e.getValue() == null || e.getValue().get() == null) pageSources.remove(e.getKey());
				}
			}
			if (pageSources.size() < MAXSIZE) return;
			ArrayList<Entry<String, SoftReference<PageSource>>> entryList = new ArrayList<>(pageSources.entrySet());

			// Sort the list by the 'lastModified' timestamp in ascending order
			entryList.sort(new Comparator<Entry<String, SoftReference<PageSource>>>() {

				@Override
				public int compare(Entry<String, SoftReference<PageSource>> left, Entry<String, SoftReference<PageSource>> right) {
					SoftReference<PageSource> l = left.getValue();
					SoftReference<PageSource> r = right.getValue();
					if (l == null) return -1;
					if (r == null) return 1;

					PageSource ll = l.get();
					PageSource rr = r.get();
					if (ll == null) return -1;
					if (rr == null) return 1;

					long lll = ll.getLastAccessTime();
					long rrr = rr.getLastAccessTime();

					if ((lll) < (rrr)) return -1;
					else if ((lll) > (rrr)) return 1;
					else return 0;
				}
			});
			SoftReference<PageSource> ref;
			PageSource ps;
			int max = entryList.size() - maxSize_min;
			for (Entry<String, SoftReference<PageSource>> e: entryList) {
				if (--max == 0) break;
				// Remove the entry from the map by its key
				ref = pageSources.remove(e.getKey());
				if (ref != null) {
					ps = ref.get();
					if (ps instanceof PageSourceImpl) {
						((PageSourceImpl) ps).clear();
					}
				}
			}
			System.gc();
		}

		resetWatcherWhenEmpty(false, true);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		maxlevel--;
		size(); // calling size because it get rid of all the blanks
		Iterator<SoftReference<PageSource>> it = pageSources.values().iterator();

		DumpTable table = new DumpTable("#FFCC00", "#FFFF00", "#000000");
		table.setTitle("Page Source Pool");
		table.appendRow(1, new SimpleDumpData("Count"), new SimpleDumpData(pageSources.size()));
		while (it.hasNext()) {
			PageSource ps = it.next().get();
			DumpTable inner = new DumpTable("#FFCC00", "#FFFF00", "#000000");
			inner.setWidth("100%");
			inner.appendRow(1, new SimpleDumpData("source"), new SimpleDumpData(ps.getDisplayPath()));
			inner.appendRow(1, new SimpleDumpData("last access"), DumpUtil.toDumpData(new DateTimeImpl(pageContext, ps.getLastAccessTime(), false), pageContext, maxlevel, dp));
			inner.appendRow(1, new SimpleDumpData("access count"), new SimpleDumpData(ps.getAccessCount()));
			table.appendRow(1, new SimpleDumpData("Sources"), inner);
		}
		return table;
	}

	/**
	 * remove all Page from Pool using this classloader
	 * 
	 * @param cl
	 */
	public void clearPages(ClassLoader cl) {
		Iterator<SoftReference<PageSource>> it = this.pageSources.values().iterator();
		PageSourceImpl psi;
		SoftReference<PageSource> sr;
		while (it.hasNext()) {
			sr = it.next();
			psi = sr == null ? null : (PageSourceImpl) sr.get();
			if (psi == null) continue;
			if (cl != null) psi.clear(cl);
			else psi.clear();
		}

		if (cl == null) {
			pageSources.clear();
		}

		resetWatcherWhenEmpty(false, true);
	}

	public void resetPages(ClassLoader cl) {
		Iterator<SoftReference<PageSource>> it = this.pageSources.values().iterator();
		PageSourceImpl psi;
		SoftReference<PageSource> sr;
		while (it.hasNext()) {
			sr = it.next();
			psi = sr == null ? null : (PageSourceImpl) sr.get();
			if (psi == null) continue;
			if (cl != null) psi.clear(cl);
			else psi.resetLoaded();
		}

		resetWatcherWhenEmpty(false, true);
	}

	public void stopWatcher() {
		resetWatcherWhenEmpty(true, true);
	}

	public void clear() {
		clearPages(null);
		// pageSources.clear();
	}

	public int getMaxSize() {
		return MAXSIZE;
	}

	public static void flush(PageContext pc, Resource file) {
		if (Constants.isCFML(file)) {
			ApplicationContext ac = pc.getApplicationContext();
			List<PageSource> sources = ConfigWebUtil.toAllLoadedPageSource((ConfigPro) pc.getConfig(), ac == null ? null : ac.getMappings(), file);
			for (PageSource ps: sources) {
				((PageSourceImpl) ps).resetLoaded();
				((PageSourceImpl) ps).flush();
			}
		}
	}

	public static void flush(PageContext pc, Object file) {
		try {
			flush(pc, Caster.toResource(pc, file, false));
			return;
		}
		catch (Exception e) {
		}
	}

	private void resetWatcherWhenEmpty(boolean force, boolean set2null) {
		if (watcher != null && (force || pageSources.isEmpty())) {
			synchronized (token) {
				if (watcher != null && (force || pageSources.isEmpty())) {
					watcher.stopIfNecessary();
					if (set2null) watcher = null;
				}
			}
		}
	}
}