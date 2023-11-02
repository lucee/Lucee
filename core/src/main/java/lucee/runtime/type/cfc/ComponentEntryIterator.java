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
package lucee.runtime.type.cfc;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.Component;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.it.EntryIterator;

public class ComponentEntryIterator extends EntryIterator implements Iterator<Entry<Key, Object>> {

	private Component cfc;
	private int access;

	public ComponentEntryIterator(Component cfc, Key[] keys, int access) {
		super(cfc, keys);
		this.cfc = cfc;
		this.access = access;
	}

	@Override
	public Entry<Key, Object> next() {
		Key key = keys[pos++];
		if (key == null) return null;
		return new CAEntryImpl(cfc, key, access);
	}

	public class CAEntryImpl extends EntryImpl implements Entry<Key, Object> {

		private Component cfc;
		private int access;

		public CAEntryImpl(Component cfc, Key key, int access) {
			super(cfc, key);
			this.cfc = cfc;
			this.access = access;
		}

		@Override
		public Object getValue() {
			return cfc.get(access, key, null);
		}

		@Override
		public Object setValue(Object value) {
			return cfc.setEL(key, value);
		}

	}
}