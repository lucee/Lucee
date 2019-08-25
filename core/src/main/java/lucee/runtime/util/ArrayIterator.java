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
package lucee.runtime.util;

import java.util.Iterator;

/**
 * Iterator Implementation for an Object Array
 */
public final class ArrayIterator implements Iterator {

	private Object[] arr;
	private int offset;
	private int length;

	/**
	 * constructor for the class
	 * 
	 * @param arr Base Array
	 */
	public ArrayIterator(Object[] arr) {
		this.arr = arr;
		this.offset = 0;
		this.length = arr.length;
	}

	public ArrayIterator(Object[] arr, int offset, int length) {
		this.arr = arr;
		this.offset = offset;
		this.length = offset + length;
		if (this.length > arr.length) this.length = arr.length;

	}

	public ArrayIterator(Object[] arr, int offset) {
		this.arr = arr;
		this.offset = offset;
		this.length = arr.length;

	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("this operation is not suppored");
	}

	@Override
	public boolean hasNext() {
		return (length) > offset;
	}

	@Override
	public Object next() {
		return arr[offset++];
	}

}