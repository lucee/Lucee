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
package lucee.runtime.cache.tag.query;

import java.io.Serializable;

import lucee.commons.digest.HashUtil;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.udf.UDFArgConverter;
import lucee.runtime.type.Duplicable;
import lucee.runtime.type.Struct;

public class StoredProcCacheItem implements CacheItem, Serializable, Duplicable {

	private static final long serialVersionUID = 7327671003736543783L;

	private final Struct sct;
	private final String procedure;
	private final long executionTime;

	public StoredProcCacheItem(Struct sct, String procedure, long executionTime) {
		this.sct = sct;
		this.procedure = procedure;
		this.executionTime = executionTime;
	}

	@Override
	public String getHashFromValue() {
		return Long.toString(HashUtil.create64BitHash(UDFArgConverter.serialize(sct)));
	}

	@Override
	public String getName() {
		return procedure;
	}

	@Override
	public long getPayload() {
		return sct.size();
	}

	@Override
	public String getMeta() {
		return "";
	}

	@Override
	public long getExecutionTime() {
		return executionTime;
	}

	public Struct getStruct() {
		return sct;
	}

	@Override
	public Object duplicate(boolean deepCopy) {
		return new StoredProcCacheItem((Struct) sct.duplicate(true), procedure, executionTime);
	}
}