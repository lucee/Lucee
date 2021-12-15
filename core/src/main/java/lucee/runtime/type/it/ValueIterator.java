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

import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;

public class ValueIterator implements Iterator<Object>, Enumeration<Object> {

	private Collection coll;
	protected Key[] keys;
	protected int pos;

	public ValueIterator(Collection coll, Collection.Key[] keys) {
		this.coll = coll;
		this.keys = keys;
	}

	@Override
	public boolean hasNext() {
		return (keys.length) > pos;
	}

	@Override
	public Object next() {
		Key key = keys[pos++];
		if (key == null) return null;
		return coll.get(key, null);
	}

	@Override
	public boolean hasMoreElements() {
		return hasNext();
	}

	@Override
	public Object nextElement() {
		return next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("this operation is not suppored");
	}
}