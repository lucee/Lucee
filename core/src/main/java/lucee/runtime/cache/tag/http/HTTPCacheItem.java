/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.cache.tag.http;

import java.io.Serializable;

import lucee.commons.digest.HashUtil;
import lucee.runtime.PageContext;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.type.Struct;

public class HTTPCacheItem implements CacheItem, Serializable, Dumpable {

	private static final long serialVersionUID = -8462614105941179140L;
	
	private Struct data;
	private String url;
	private long executionTimeNS;


	public HTTPCacheItem(Struct data, String url, long executionTimeNS) {
		this.data = data;
		this.url=url;
		this.executionTimeNS=executionTimeNS;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		DumpTable table = new DumpTable("#669999", "#ccffff", "#000000");
		table.setTitle("HTTPCacheEntry");
		table.appendRow(1, new SimpleDumpData("Output"), data.toDumpData(pageContext, maxlevel, properties));
		if (url != null)
			table.appendRow(1, new SimpleDumpData("URL"), DumpUtil.toDumpData(new SimpleDumpData(url), pageContext, maxlevel, properties));
		return table;
	}

	@Override
	public String toString() {
		return data.toString();
	}

	@Override
	public String getHashFromValue() {
		return Long.toString(HashUtil.create64BitHash(data.toString()));
	}

	public Struct getData() {
		return data;
	}

	@Override
	public String getName() {
		return url;
	}

	@Override
	public long getPayload() {
		return data.size();
	}

	@Override
	public String getMeta() {
		return url;
	}

	@Override
	public long getExecutionTime() {
		return executionTimeNS;
	}

}