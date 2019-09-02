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

import java.io.IOException;

import lucee.commons.digest.HashUtil;
import lucee.commons.digest.MD5;
import lucee.runtime.coder.Base64Coder;
import lucee.runtime.type.Duplicable;

public class FileCacheItemBinary extends FileCacheItem implements Duplicable {

	private static final long serialVersionUID = -7426486016811317332L;
	public final byte[] data;

	public FileCacheItemBinary(String path, byte[] data, long executionTimeNS) {
		super(path, executionTimeNS);
		this.data = data;
	}

	@Override
	public String toString() {
		return Base64Coder.encode(data);
	}

	@Override
	public String getHashFromValue() {
		try {
			return MD5.getDigestAsString(data);
		}
		catch (IOException e) {
			return Long.toString(HashUtil.create64BitHash(toString()));
		}
	}

	@Override
	public long getPayload() {
		return data.length;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public Object duplicate(boolean deepCopy) {
		if (data != null) {
			byte[] tmp = new byte[data.length];
			for (int i = 0; i < data.length; i++) {
				tmp[i] = data[i];
			}
			return new FileCacheItemBinary(path, tmp, getExecutionTime());
		}
		return new FileCacheItemBinary(path, data, getExecutionTime());
	}
}