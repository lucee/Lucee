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

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.type.dt.TimeSpan;

public class FileCacheEntry implements CacheEntry {

	private static final String ENC = "utf-8";
	private Resource res;
	// private Resource directory;
	// private String name,raw;

	private boolean isOK(TimeSpan timeSpan) {
		return res.exists() && (res.lastModified() + timeSpan.getMillis() >= System.currentTimeMillis());
	}

	@Override
	public String readEntry(TimeSpan timeSpan, String defaultValue) throws IOException {
		if (isOK(timeSpan)) return IOUtil.toString(res, ENC);
		return defaultValue;
	}

	@Override
	public void writeEntry(String entry, boolean append) throws IOException {
		IOUtil.copy(new ByteArrayInputStream(entry.getBytes(ENC)), res.getOutputStream(append), true, true);
	}

}