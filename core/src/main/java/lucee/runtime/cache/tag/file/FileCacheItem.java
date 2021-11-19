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
package lucee.runtime.cache.tag.file;

import java.io.Serializable;

import lucee.runtime.PageContext;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.dump.SimpleDumpData;

public abstract class FileCacheItem implements CacheItem, Serializable, Dumpable {

	private static final long serialVersionUID = -8462614105941179140L;

	private final long executionTimeNS;
	protected final String path;

	public FileCacheItem(String path, long executionTimeNS) {
		this.path = path;
		this.executionTimeNS = executionTimeNS;
	}

	public static FileCacheItem getInstance(String path, Object data, long executionTimeNS) {
		if (data instanceof byte[]) return new FileCacheItemBinary(path, (byte[]) data, executionTimeNS);
		return new FileCacheItemString(path, (String) data, executionTimeNS);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		DumpTable table = new DumpTable("#669999", "#ccffff", "#000000");
		table.setTitle("FileCacheEntry");
		table.appendRow(1, new SimpleDumpData("Path"), new SimpleDumpData(path));
		return table;
	}

	@Override
	public String getName() {
		return path;
	}

	@Override
	public String getMeta() {
		return path;
	}

	@Override
	public long getExecutionTime() {
		return executionTimeNS;
	}

	public abstract Object getData();

}