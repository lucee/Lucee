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
package lucee.runtime.type;

import java.io.Serializable;
import java.util.ArrayList;

import lucee.runtime.exp.ExpressionException;

/**
 * CFML array object
 */
public final class ArrayInt implements Serializable {

	private static final int NULL = 0;
	private int[] arr;
	private final int cap = 32;
	private int size = 0;
	private int offset = 0;
	private int offCount = 0;

	/**
	 * constructor with default dimesnion (1)
	 */
	public ArrayInt() {
		arr = new int[offset + cap];
	}

	/**
	 * constructor with to data to fill
	 * 
	 * @param objects Objects array data to fill
	 */
	public ArrayInt(int[] objects) {
		arr = objects;
		size = arr.length;
		offset = 0;
	}

	public int get(int key, int defaultValue) {
		if (key > size || key < 1) {
			return defaultValue;
		}
		int o = arr[(offset + key) - 1];
		if (o == NULL) return defaultValue;
		return o;
	}

	public int get(int key) throws ExpressionException {
		if (key < 1 || key > size) {
			throw invalidPosition(key);
		}

		int o = arr[(offset + key) - 1];

		if (o == NULL) {
			throw invalidPosition(key);
		}
		return o;
	}

	/**
	 * Exception method if key doesn't exist at given position
	 * 
	 * @param pos
	 * @return exception
	 */
	private ExpressionException invalidPosition(int pos) {
		return new ExpressionException("Element at position [" + pos + "] doesn't exist in array");
	}

	public int set(int key, int value) {
		if (offset + key > arr.length) enlargeCapacity(key);
		if (key > size) size = key;
		return arr[(offset + key) - 1] = value;
	}

	/**
	 * !!! all methods that use this method must be sync enlarge the inner array to given size
	 * 
	 * @param key min size of the array
	 */
	private synchronized void enlargeCapacity(int key) {
		int diff = offCount - offset;

		int newSize = arr.length;
		if (newSize < 1) newSize = 1;
		while (newSize < key + offset + diff) {
			newSize *= 2;
		}
		if (newSize > arr.length) {
			int[] na = new int[newSize];
			for (int i = offset; i < offset + size; i++) {
				na[i + diff] = arr[i];
			}
			arr = na;
			offset += diff;
		}
	}

	/*
	 * * !!! all methods that use this method must be sync enlarge the offset if 0 / private void
	 * enlargeOffset() { if(offset==0) { offCount=offCount==0?1:offCount*2; offset=offCount; int[]
	 * narr=new int[arr.length+offset]; for(int i=0;i<size;i++) { narr[offset+i]=arr[i]; } arr=narr; } }
	 */

	public int size() {
		return size;
	}

	public int[] keys() {
		ArrayList lst = new ArrayList();
		int count = 0;
		for (int i = offset; i < offset + size; i++) {
			int o = arr[i];
			count++;
			if (o != NULL) lst.add(Integer.valueOf(count));
		}

		int[] ints = new int[lst.size()];

		for (int i = 0; i < ints.length; i++) {
			ints[i] = ((Integer) lst.get(i)).intValue();
		}
		return ints;
	}

	public int remove(int key) throws ExpressionException {
		if (key > size || key < 1) throw invalidPosition(key);
		int obj = get(key, NULL);
		for (int i = (offset + key) - 1; i < (offset + size) - 1; i++) {
			arr[i] = arr[i + 1];
		}
		size--;
		return obj;
	}

	public int removeEL(int key) {
		if (key > size || key < 1) return NULL;
		int obj = get(key, NULL);

		for (int i = (offset + key) - 1; i < (offset + size) - 1; i++) {
			arr[i] = arr[i + 1];
		}
		size--;
		return obj;
	}

	public void clear() {
		if (size() > 0) {
			arr = new int[cap];
			size = 0;
			offCount = 1;
			offset = 0;
		}
	}

	public int add(int o) {
		if (offset + size + 1 > arr.length) enlargeCapacity(size + 1);
		arr[offset + size] = o;
		size++;
		return o;
	}

	public int[] toArray() {
		int[] rtn = new int[size];
		int count = 0;
		for (int i = offset; i < offset + size; i++) {
			rtn[count++] = arr[i];
		}
		return rtn;
	}

	public boolean contains(int key) {
		return get(key, NULL) != NULL;
	}
}