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
package lucee.runtime;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.SystemUtil;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.type.dt.DateTimeImpl;

/**
 * pool to handle pages
 */
public final class PageSourcePool implements Dumpable {
	// TODO must not be thread safe, is used in sync block only
	private final Map<String, SoftReference<PageSource>> pageSources = new ConcurrentHashMap<String, SoftReference<PageSource>>();
	// timeout timeout for files
	private long timeout;
	// max size of the pool cache
	private int maxSize = 10000;
	private int maxSize_min = 1000;

	/**
	 * constructor of the class
	 */
	public PageSourcePool() {
		this.timeout = 10000;
		this.maxSize = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.pagePool.maxSize", null), maxSize);
		maxSize_min = Math.max(this.maxSize - 1000, 1000);
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
		PageSource ps = tmp == null ? null : tmp.get();
		if (ps == null) return null;
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

	/**
	 * removes a page from the page pool
	 * 
	 * @param key key reference to page object
	 * @return page object matching to key reference
	 */
	/*
	 * private boolean remove(String key) {
	 * 
	 * if (pageSources.remove(key.toLowerCase()) != null) return true;
	 * 
	 * Set<String> set = pageSources.keySet(); String[] keys = set.toArray(new String[set.size()]); //
	 * done this way to avoid ConcurrentModificationException SoftReference<PageSource> tmp; PageSource
	 * ps; for (String k: keys) { tmp = pageSources.get(k); ps = tmp == null ? null : tmp.get(); if (ps
	 * != null && key.equalsIgnoreCase(ps.getClassName())) { pageSources.remove(k); return true; } }
	 * return false; }
	 */

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
		return pageSources.size();
	}

	/**
	 * @return returns if pool is empty or not
	 */
	public boolean isEmpty() {
		return pageSources.isEmpty();
	}

	public void cleanLoaders() {
		if (pageSources.size() < maxSize) return;
		synchronized (pageSources) {
			{
				for (Entry<String, SoftReference<PageSource>> e: pageSources.entrySet()) {
					if (e.getValue() == null || e.getValue().get() == null) pageSources.remove(e.getKey());
				}
			}
			if (pageSources.size() < maxSize) return;
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
					return Operator.compare(ll.getLastAccessTime(), rr.getLastAccessTime());
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

	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		maxlevel--;
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
	}

	public void clear() {
		clearPages(null);
		// pageSources.clear();
	}

	public int getMaxSize() {
		return maxSize;
	}
}