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

import lucee.commons.digest.HashUtil;
import lucee.runtime.type.Duplicable;

public class FileCacheItemString extends FileCacheItem implements Duplicable {
	private static final long serialVersionUID = 1655467049819824671L;
	public final String data;

	public FileCacheItemString(String path, String data, long executionTimeNS) {
		super(path, executionTimeNS);
		this.data = data;
	}

	@Override
	public String toString() {
		return data;
	}

	@Override
	public String getHashFromValue() {
		return Long.toString(HashUtil.create64BitHash(data));
	}

	@Override
	public long getPayload() {
		return data.length();
	}

	@Override
	public String getData() {
		return data;
	}

	@Override
	public Object duplicate(boolean deepCopy) {
		return new FileCacheItemString(path, data, getExecutionTime());
	}
}