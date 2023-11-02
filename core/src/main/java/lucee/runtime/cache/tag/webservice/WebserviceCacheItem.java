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
package lucee.runtime.cache.tag.webservice;

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
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Duplicable;

public class WebserviceCacheItem implements CacheItem, Serializable, Dumpable, Duplicable {

	private static final long serialVersionUID = -8462614105941179140L;

	private final Object data;
	private final String url;
	private final String methodName;
	private final long executionTimeNS;

	public WebserviceCacheItem(Object data, String url, String methodName, long executionTimeNS) {
		this.data = data;
		this.url = url;
		this.methodName = methodName;
		this.executionTimeNS = executionTimeNS;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		DumpTable table = new DumpTable("#669999", "#ccffff", "#000000");
		table.setTitle("WebserviceCacheEntry");
		table.appendRow(1, new SimpleDumpData("URL"), DumpUtil.toDumpData(new SimpleDumpData(url), pageContext, maxlevel, properties));
		table.appendRow(1, new SimpleDumpData("Method Name"), DumpUtil.toDumpData(new SimpleDumpData(methodName), pageContext, maxlevel, properties));

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

	public Object getData() {
		return data;
	}

	@Override
	public String getName() {
		return url + "&method=" + methodName;
	}

	@Override
	public long getPayload() {
		return data instanceof Collection ? ((Collection) data).size() : 1;
	}

	@Override
	public String getMeta() {
		return url;
	}

	@Override
	public long getExecutionTime() {
		return executionTimeNS;
	}

	@Override
	public Object duplicate(boolean deepCopy) {
		return new WebserviceCacheItem(Duplicator.duplicate(data, deepCopy), url, methodName, executionTimeNS);
	}
}