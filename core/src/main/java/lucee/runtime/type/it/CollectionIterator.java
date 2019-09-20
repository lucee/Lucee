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

import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;

public class CollectionIterator implements Iterator<Object> {

	private Iterator<Collection.Key> keys;
	private Collection coll;

	public CollectionIterator(Key[] keys, Collection coll) {
		this.keys = new KeyIterator(keys);
		this.coll = coll;
	}

	public CollectionIterator(Iterator<Collection.Key> keys, Collection coll) {
		this.keys = keys;
		this.coll = coll;
	}

	@Override
	public boolean hasNext() {
		return keys.hasNext();
	}

	@Override
	public Object next() {
		return coll.get(keys.next(), null);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("this operation is not suppored");
	}

}