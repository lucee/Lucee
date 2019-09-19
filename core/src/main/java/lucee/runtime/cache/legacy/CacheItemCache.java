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
package lucee.runtime.cache.legacy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import lucee.commons.io.IOUtil;
import lucee.commons.io.cache.Cache;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.cache.util.CacheKeyFilterAll;
import lucee.runtime.cache.util.WildCardFilter;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.TimeSpan;

public class CacheItemCache extends CacheItem {

	private Cache cache;
	private TimeSpan timespan;
	private String lcFileName;

	public CacheItemCache(PageContext pc, HttpServletRequest req, String id, String key, boolean useId, Cache cache, TimeSpan timespan) {
		super(pc, req, id, key, useId);
		this.cache = cache;
		this.timespan = timespan;
		lcFileName = fileName;
	}

	@Override
	public boolean isValid() {
		try {
			return cache.getValue(lcFileName) != null;
		}
		catch (IOException e) {
			return false;
		}
	}

	@Override
	public boolean isValid(TimeSpan timespan) {
		return isValid();
	}

	@Override
	public void writeTo(OutputStream os, String charset) throws IOException {
		byte[] barr = getValue().getBytes(StringUtil.isEmpty(charset, true) ? "UTF-8" : charset);
		IOUtil.copy(new ByteArrayInputStream(barr), os, true, false);
	}

	@Override
	public String getValue() throws IOException {
		try {
			return Caster.toString(cache.getValue(lcFileName));
		}
		catch (PageException e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	@Override
	public void store(String value) throws IOException {
		cache.put(lcFileName, value, null, valueOf(timespan));

	}

	@Override
	public void store(byte[] barr, boolean append) throws IOException {
		String value = (append) ? getValue() : "";
		value += IOUtil.toString(barr, "UTF-8");
		store(value);
	}

	public static void _flushAll(PageContext pc, Cache cache) throws IOException {
		cache.remove(CacheKeyFilterAll.getInstance());
	}

	public static void _flush(PageContext pc, Cache cache, String expireurl) throws IOException {
		cache.remove(new WildCardFilter(expireurl, true));
	}

	private static Long valueOf(TimeSpan timeSpan) {
		if (timeSpan == null) return null;
		return Long.valueOf(timeSpan.getMillis());
	}

}