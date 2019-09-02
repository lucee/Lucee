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
package lucee.runtime.type.util;

import java.util.Map;

import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;

public class StructMapEntry implements Map.Entry<String, Object> {

	private Collection.Key key;
	private Object value;
	private Struct sct;

	public StructMapEntry(Struct sct, Collection.Key key, Object value) {
		this.sct = sct;
		this.key = key;
		this.value = value;
	}

	@Override
	public String getKey() {
		return key.getString();
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public Object setValue(Object value) {
		Object old = value;
		sct.setEL(key, value);
		this.value = value;
		return old;
	}

}