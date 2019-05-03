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

import java.util.Iterator;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Objects;

public class ObjectsIterator implements Iterator<Object> {

	private Iterator<Key> keys;
	private Objects objs;

	public ObjectsIterator(Key[] keys, Objects objs) {
		this.keys = new KeyIterator(keys);
		this.objs = objs;
	}

	public ObjectsIterator(Iterator<Key> keys, Objects objs) {
		this.keys = keys;
		this.objs = objs;
	}

	@Override
	public boolean hasNext() {
		return keys.hasNext();
	}

	@Override
	public Object next() {
		return objs.get(ThreadLocalPageContext.get(), KeyImpl.toKey(keys.next(), null), null);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("this operation is not suppored");
	}

}