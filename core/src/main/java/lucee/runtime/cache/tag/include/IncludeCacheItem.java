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
package lucee.runtime.cache.tag.include;

import java.io.Serializable;

import lucee.commons.digest.HashUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.type.Duplicable;

public class IncludeCacheItem implements CacheItem, Serializable, Dumpable, Duplicable {

	private static final long serialVersionUID = -3616023500492159529L;

	public final String output;
	private final long executionTimeNS;
	private final String path;
	private final String name;
	private final int payload;

	public IncludeCacheItem(String output, PageSource ps, long executionTimeNS) {
		this.output = output;
		this.path = ps.getDisplayPath();
		this.name = ps.getFileName();
		this.executionTimeNS = executionTimeNS;
		this.payload = output == null ? 0 : output.length();
	}

	public IncludeCacheItem(String output, String path, String name, long executionTimeNS) {
		this.output = output;
		this.path = path;
		this.name = name;
		this.executionTimeNS = executionTimeNS;
		this.payload = output == null ? 0 : output.length();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		DumpTable table = new DumpTable("#669999", "#ccffff", "#000000");
		table.setTitle("IncludeCacheEntry");
		table.appendRow(1, new SimpleDumpData("Output"), DumpUtil.toDumpData(new SimpleDumpData(output), pageContext, maxlevel, properties));
		if (path != null) table.appendRow(1, new SimpleDumpData("Path"), DumpUtil.toDumpData(new SimpleDumpData(path), pageContext, maxlevel, properties));
		return table;
	}

	@Override
	public String toString() {
		return output;
	}

	@Override
	public String getHashFromValue() {
		return Long.toString(HashUtil.create64BitHash(output));
	}

	public String getOutput() {
		return output;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getPayload() {
		return payload;
	}

	@Override
	public String getMeta() {
		return path;
	}

	@Override
	public long getExecutionTime() {
		return executionTimeNS;
	}

	@Override
	public Object duplicate(boolean deepCopy) {
		return new IncludeCacheItem(output, path, name, executionTimeNS);
	}
}