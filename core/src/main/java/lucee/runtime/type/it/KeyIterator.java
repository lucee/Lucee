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

/**
 * Iterator Implementation for an Object Array
 */
public final class KeyIterator implements Iterator<Collection.Key>, Enumeration<Collection.Key> {

	private Collection.Key[] arr;
	private int pos;

	/**
	 * constructor for the class
	 * 
	 * @param arr Base Array
	 */
	public KeyIterator(Collection.Key[] arr) {

		this.arr = arr == null ? new Collection.Key[0] : arr;
		this.pos = 0;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("this operation is not suppored");
	}

	@Override
	public boolean hasNext() {
		return (arr.length) > pos;
	}

	@Override
	public Collection.Key next() {
		Key key = arr[pos++];
		if (key == null) return null;
		return key;
	}

	@Override
	public boolean hasMoreElements() {
		return hasNext();
	}

	@Override
	public Collection.Key nextElement() {
		return next();
	}
}