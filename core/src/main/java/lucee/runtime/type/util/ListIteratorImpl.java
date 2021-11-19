/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2016, Lucee Assosication Switzerland
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
 */
package lucee.runtime.type.util;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ListIteratorImpl<T> implements ListIterator<T> {

	private static final int UNDEFINED = Integer.MIN_VALUE;
	private List<T> list;
	private int index = -1;
	private int current = UNDEFINED;

	/**
	 * Constructor of the class
	 * 
	 * @param arr
	 */
	public ListIteratorImpl(List<T> list) {
		this(list, 0);
	}

	/**
	 * Constructor of the class
	 * 
	 * @param arr
	 * @param index
	 */
	public ListIteratorImpl(List<T> list, int index) {
		this.list = list;
		this.index = index - 1;
	}

	@Override
	public void add(T o) {
		list.add(++index, o);
	}

	@Override
	public void remove() {
		if (current == UNDEFINED) throw new IllegalStateException();
		list.remove(current);
		current = UNDEFINED;
	}

	@Override
	public void set(T o) {
		if (current == UNDEFINED) throw new IllegalStateException();
		list.set(current, o);
	}

	/////////////

	@Override
	public boolean hasNext() {
		return list.size() > index + 1;
	}

	@Override
	public boolean hasPrevious() {
		return index > -1;
	}

	@Override
	public int previousIndex() {
		return index;
	}

	@Override
	public int nextIndex() {
		return index + 1;
	}

	@Override
	public T previous() {
		if (!hasPrevious()) throw new NoSuchElementException();
		current = index;
		return list.get(index--);
	}

	@Override
	public T next() {
		if (!hasNext()) throw new NoSuchElementException();
		return list.get(current = ++index);
	}

}