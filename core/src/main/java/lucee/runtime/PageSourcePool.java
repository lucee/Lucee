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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;

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
	private Cache<String, PageSource> cache;
	RemovalListener<String, PageSource> nullListener = (k, v, c) -> {}; // Only used for typing

	private int maximumSize;

	/**
	 * constructor of the class
	 */
	public PageSourcePool() {
		this(3000);
	}

	public PageSourcePool(int maximumSize) {
		this.maximumSize = maximumSize;
		Caffeine<String, PageSource> cacheBuilder = Caffeine.newBuilder().removalListener(nullListener);

		this.cache = cacheBuilder
			.maximumSize(this.maximumSize)
			.build();
	}

	/**
	 * return pages matching to key
	 * 
	 * @param key key for the page
	 * @param updateAccesTime define if do update access time
	 * @return page
	 */
	public PageSource getPageSource(String key, boolean updateAccesTime) { // DO NOT CHANGE INTERFACE (used by Argus Monitor)
		return this.cache.getIfPresent(key.toLowerCase());
	}

	public PageSource getOrSetPageSource(final String key, final Function<String, PageSource> getSources, boolean updateAccessTime) {
		PageSource ps = this.cache.asMap().computeIfAbsent(
			key.toLowerCase(), 
			(lkey) -> {
				return getSources.apply(key);
			}
		);
		if (ps != null && updateAccessTime) ps.setLastAccessTime();
		return ps;
	}
	/**
	 * sts a page object to the page pool
	 * 
	 * @param key key reference to store page object
	 * @param ps pagesource to store
	 */
	public void setPage(String key, PageSource ps) {
		this.cache.put(key.toLowerCase(), ps);
	}

	/**
	 * @return returns an array of all keys in the page pool
	 */
	public String[] keys() {
		if (this.cache == null) return new String[0];
		Set<String> set = this.cache.asMap().keySet();
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
		PageSource ps = this.cache.asMap().computeIfPresent(
			key.toLowerCase(), 
			(k, v) -> {
				if (v != null && v instanceof PageSourceImpl) {
					((PageSourceImpl) v).flush();
				}
				return v;
			}
		);
		if (ps != null) return true;
		return false;
	}

	/**
	 * @return returns the size of the pool
	 */
	public int size() {
		return (int) this.cache.estimatedSize();
	}

	/**
	 * @return returns if pool is empty or not
	 */
	public boolean isEmpty() {
		return this.cache.asMap().isEmpty();
	}

	/**
	 * clear unused pages from page pool
	 */
	public void clearUnused(Config config) {
		return;
	}

	@Override
	public DumpData toDumpData(final PageContext pageContext, final int maxlevel, final DumpProperties dp) {
		final int newML = maxlevel-1;
		ConcurrentMap<String, PageSource> map = this.cache.asMap();
		
		DumpTable table = new DumpTable("#FFCC00", "#FFFF00", "#000000");
		table.setTitle("Page Source Pool");
		table.appendRow(1, new SimpleDumpData("Count"), new SimpleDumpData(map.size()));
		map.forEach((key, ps) -> {
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
		this.cache.asMap().forEach(
			(key, ps) -> {
				PageSourceImpl psi = (PageSourceImpl) ps;
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
		this.cache.asMap().forEach(
			(key, ps) -> {
				PageSourceImpl psi = (PageSourceImpl) ps;
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
	}

	public int getMaxSize() {
		return maximumSize;
	}
}