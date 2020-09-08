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

import lucee.commons.collection.LongKeyList;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.config.ConfigImpl;
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
	// TODO must not be thread safe, is used in sync block only
	private Map<String, SoftReference<PageSource>> pageSources = new ConcurrentHashMap<String, SoftReference<PageSource>>();
	// timeout timeout for files
	private long timeout;
	// max size of the pool cache
	private int maxSize;

	/**
	 * constructor of the class
	 */
	public PageSourcePool() {
		this.timeout = 10000;
		this.maxSize = 1000;
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
	public boolean remove(String key) {

		if (pageSources.remove(key.toLowerCase()) != null) return true;

		Set<String> set = pageSources.keySet();
		String[] keys = set.toArray(new String[set.size()]); // done this way to avoid ConcurrentModificationException
		SoftReference<PageSource> tmp;
		PageSource ps;
		for (String k: keys) {
			tmp = pageSources.get(k);
			ps = tmp == null ? null : tmp.get();
			if (ps != null && key.equalsIgnoreCase(ps.getClassName())) {
				pageSources.remove(k);
				return true;
			}
		}
		return false;
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
	public void clearUnused(ConfigImpl config) {
		if (size() > maxSize) {
			LogUtil.log(config, Log.LEVEL_INFO, PageSourcePool.class.getName(), "PagePool size [" + size() + "] has exceeded max size [" + maxSize + "]. Clearing unused...");
			String[] keys = keys();
			LongKeyList list = new LongKeyList();
			for (int i = 0; i < keys.length; i++) {
				PageSource ps = getPageSource(keys[i], false);
				long updateTime = ps.getLastAccessTime();
				if (updateTime + timeout < System.currentTimeMillis()) {
					long add = ((ps.getAccessCount() - 1) * 10000);
					if (add > timeout) add = timeout;
					list.add(updateTime + add, keys[i]);
				}
			}
			while (size() > maxSize) {
				Object key = list.shift();
				if (key == null) break;
				remove(key.toString());
			}
			LogUtil.log(config, Log.LEVEL_INFO, PageSourcePool.class.getName(), "New pagePool size [" + size() + "].");
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
		pageSources.clear();
	}

	public int getMaxSize() {
		return maxSize;
	}
}