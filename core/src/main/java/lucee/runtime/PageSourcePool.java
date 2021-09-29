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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import lucee.commons.collection.LongKeyList;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.config.Config;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.type.dt.DateTimeImpl;

/**
 * pool to handle pages
 */
public final class PageSourcePool implements Dumpable {
	private ConcurrentHashMap<String, SoftReference<PageSource>> pageSources = new ConcurrentHashMap<String, SoftReference<PageSource>>();
	// timeout timeout for files
	private long timeout;
	// max size of the pool cache
	private int high_watermark;
	private int low_watermark;

	/**
	 * constructor of the class
	 */
	public PageSourcePool() {
		this.timeout = 10000;
		this.high_watermark = 1000;
		this.low_watermark = 900;
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

	public PageSource getOrSetPageSource(final String key, final Function<String, PageSource> getSources, boolean updateAccessTime) {
		SoftReference<PageSource> tmp = this.pageSources.computeIfAbsent(
			key.toLowerCase(), 
			(lkey) -> {
				return new SoftReference<PageSource>(
					getSources.apply(key)
				);
			}
		);
		if (tmp == null) return null;
		PageSource ps = tmp.get();
		if (updateAccessTime) ps.setLastAccessTime();
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
		SoftReference<PageSource> tmp = pageSources.computeIfPresent(
			key.toLowerCase(), 
			(k, v) -> {
				PageSource ps = v.get();
				if (ps != null) {
					((PageSourceImpl) ps).flush();
				}
				return v;
			}
		);
		if (tmp!=null && tmp.get() != null) return true;

		tmp = pageSources.search(5000L, (k, v) -> {
			if (v == null) {
				return null;
			}
			PageSource ps = v.get();
			if (ps != null) {
				if (k.equalsIgnoreCase(ps.getClassName())) {
					return v;
				}
			}
			return null;
		});
		if (tmp != null) {
			PageSource ps = tmp.get();
			if (ps != null) {
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

	/**
	 * clear unused pages from page pool
	 */
	public void clearUnused(Config config) {
		if (size() > high_watermark) {
			LogUtil.log(config, Log.LEVEL_INFO, PageSourcePool.class.getName(), "PagePool size [" + size() + "] has exceeded max size [" + this.high_watermark + "]. Clearing unused...");
			String[] keys = keys();
			LongKeyList list = new LongKeyList();
			long currentTime = System.currentTimeMillis();
			pageSources.forEachEntry(5000L, (entry) -> {
				PageSource ps = entry.getValue().get();
				if (ps == null) {
					pageSources.remove(entry.getKey(), entry.getValue());
					return;
				}
				long updateTime = ps.getLastAccessTime();
				if (updateTime + timeout < currentTime) {
					long add = ((ps.getAccessCount() - 1) * 10000);
					if (add > timeout) add = timeout;
					list.add(updateTime + add, entry);
				}
			});

			while (size() > low_watermark) {
				Map.Entry<String, SoftReference<PageSource>> entry = (Map.Entry<String, SoftReference<PageSource>>)list.shift();
				if (entry == null) break;
				pageSources.remove(entry.getKey(), entry.getValue());
			}
			LogUtil.log(config, Log.LEVEL_INFO, PageSourcePool.class.getName(), "New pagePool size [" + size() + "].");
		}
	}

	@Override
	public DumpData toDumpData(final PageContext pageContext, final int maxlevel, final DumpProperties dp) {
		final int newML = maxlevel-1;
		
		DumpTable table = new DumpTable("#FFCC00", "#FFFF00", "#000000");
		table.setTitle("Page Source Pool");
		table.appendRow(1, new SimpleDumpData("Count"), new SimpleDumpData(pageSources.size()));
		pageSources.forEach((key, value) -> {
			PageSource ps = value.get();
			if (ps == null ) return;
			DumpTable inner = new DumpTable("#FFCC00", "#FFFF00", "#000000");
			inner.setWidth("100%");
			inner.appendRow(1, new SimpleDumpData("source"), new SimpleDumpData(ps.getDisplayPath()));
			inner.appendRow(1, new SimpleDumpData("last access"), DumpUtil.toDumpData(new DateTimeImpl(pageContext, ps.getLastAccessTime(), false), pageContext, newML, dp));
			inner.appendRow(1, new SimpleDumpData("access count"), new SimpleDumpData(ps.getAccessCount()));
			table.appendRow(1, new SimpleDumpData("Sources"), inner);
		});
		return table;
	}

	/**
	 * remove all Page from Pool using this classloader
	 * 
	 * @param cl
	 */
	public void clearPages(ClassLoader cl) {
		this.pageSources.forEach(
			(key, value) -> {
				PageSourceImpl psi = (PageSourceImpl) value.get();
				if (psi != null) {
					if (cl != null) {
						psi.clear(cl);
					} else {
						psi.clear();
					}
				}
			}
		);
	}

	public void resetPages(ClassLoader cl) {
		this.pageSources.forEach(
			(key, value) -> {
				PageSourceImpl psi = (PageSourceImpl) value.get();
				if (psi != null) {
					if (cl != null) {
						psi.clear(cl);
					} else {
						psi.resetLoaded();
					}
				}
			}
		);
	}

	public void clear() {
		clearPages(null);
		// pageSources.clear();
	}

	public int getMaxSize() {
		return high_watermark;
	}
}