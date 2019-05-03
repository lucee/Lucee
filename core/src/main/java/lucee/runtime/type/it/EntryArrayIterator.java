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
package lucee.runtime.type.it;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.type.Array;

public class EntryArrayIterator implements Iterator<Entry<Integer, Object>>, Enumeration<Entry<Integer, Object>> {

	private Array coll;
	protected int[] keys;
	protected int pos;

	public EntryArrayIterator(Array coll, int[] keys) {
		this.coll = coll;
		this.keys = keys;
	}

	@Override
	public boolean hasNext() {
		return (keys.length) > pos;
	}

	@Override
	public Entry<Integer, Object> next() {
		int key = keys[pos++];
		return new EntryImpl(coll, key);
	}

	@Override
	public boolean hasMoreElements() {
		return hasNext();
	}

	@Override
	public Entry<Integer, Object> nextElement() {
		return next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("this operation is not suppored");
	}

	public class EntryImpl implements Entry<Integer, Object> {

		private Array arr;
		private Integer index;

		public EntryImpl(Array coll, Integer index) {
			this.arr = coll;
			this.index = index;
		}

		@Override
		public Integer getKey() {
			return index;
		}

		@Override
		public Object getValue() {
			return arr.get(index.intValue(), null);
		}

		@Override
		public Object setValue(Object value) {
			return arr.setEL(index.intValue(), value);
		}
	}
}