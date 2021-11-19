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
package lucee.runtime.cache.legacy;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.Config;
import lucee.runtime.type.dt.TimeSpan;

public abstract class CacheItem {

	protected final String fileName;

	public static CacheItem getInstance(PageContext pc, String id, String key, boolean useId, Resource dir, String cacheName, TimeSpan timespan) throws IOException {
		HttpServletRequest req = pc.getHttpServletRequest();
		Cache cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_TEMPLATE, null);
		if (cache != null) return new CacheItemCache(pc, req, id, key, useId, cache, timespan);
		return new CacheItemFS(pc, req, id, key, useId, dir);
	}

	public CacheItem(PageContext pc, HttpServletRequest req, String id, String key, boolean useId) {

		// raw
		String filename = req.getServletPath();
		if (!StringUtil.isEmpty(req.getQueryString())) {
			filename += "?" + req.getQueryString();
			if (useId) filename += "&cfcache_id=" + id;
		}
		else {
			if (useId) filename += "?cfcache_id=" + id;
		}
		if (useId && !StringUtil.isEmpty(key)) filename = key;
		if (!StringUtil.isEmpty(req.getContextPath())) filename = req.getContextPath() + filename;
		fileName = filename;

	}

	public abstract boolean isValid();

	public abstract boolean isValid(TimeSpan timespan);

	public abstract void writeTo(OutputStream os, String charset) throws IOException;

	public abstract String getValue() throws IOException;

	public abstract void store(String result) throws IOException;

	public abstract void store(byte[] barr, boolean append) throws IOException;

	// protected abstract void _flushAll(PageContext pc, Resource dir) throws IOException;

	// protected abstract void _flush(PageContext pc, Resource dir, String expireurl) throws
	// IOException;

	public static void flushAll(PageContext pc, Resource dir, String cacheName) throws IOException {
		Cache cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_TEMPLATE, null);
		if (cache != null) CacheItemCache._flushAll(pc, cache);
		else CacheItemFS._flushAll(pc, dir);
	}

	public static void flush(PageContext pc, Resource dir, String cacheName, String expireurl) throws IOException {
		Cache cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_TEMPLATE, null);
		if (cache != null) CacheItemCache._flush(pc, cache, expireurl);
		else CacheItemFS._flush(pc, dir, expireurl);
	}
}