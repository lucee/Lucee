/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.type.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Array;

public class ArrayAsArrayList extends ArrayList {

	Array array;

	private ArrayAsArrayList(Array array) {
		this.array = array;
	}

	public static ArrayList toArrayList(Array array) {
		return new ArrayAsArrayList(array);
	}

	@Override
	public boolean add(Object o) {
		try {
			array.append(o);
		}
		catch (PageException e) {
			return false;
		}
		return true;
	}

	@Override
	public void add(int index, Object element) {
		try {
			array.insert(index + 1, element);
		}
		catch (PageException e) {
			throw new IndexOutOfBoundsException(e.getMessage());
		}
	}

	@Override
	public boolean addAll(Collection c) {
		Iterator it = c.iterator();
		while (it.hasNext()) {
			add(it.next());
		}
		return !c.isEmpty();
	}

	@Override
	public boolean addAll(int index, Collection c) {
		Iterator it = c.iterator();
		while (it.hasNext()) {
			add(index++, it.next());
		}
		return !c.isEmpty();
	}

	@Override
	public void clear() {
		array.clear();
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	@Override
	public boolean containsAll(Collection c) {
		Iterator it = c.iterator();
		while (it.hasNext()) {
			if (!contains(it.next())) return false;
		}
		return true;
	}

	@Override
	public Object get(int index) {
		try {
			return array.getE(index + 1);
		}
		catch (PageException e) {
			throw new IndexOutOfBoundsException(e.getMessage());
		}
	}

	@Override
	public int indexOf(Object o) {
		Iterator<Object> it = array.valueIterator();
		int index = 0;
		while (it.hasNext()) {
			if (it.next().equals(o)) return index;
			index++;
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return array.size() == 0;
	}

	@Override
	public Iterator iterator() {
		return array.valueIterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		Iterator<Object> it = array.valueIterator();
		int index = 0;
		int rtn = -1;
		while (it.hasNext()) {
			if (it.next().equals(o)) rtn = index;
			index++;
		}
		return rtn;
	}

	@Override
	public ListIterator listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator listIterator(int index) {
		return array.toList().listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		int index = indexOf(o);
		if (index == -1) return false;

		try {
			array.removeE(index + 1);
		}
		catch (PageException e) {
			return false;
		}
		return true;
	}

	@Override
	public Object remove(int index) {
		try {
			return array.removeE(index + 1);
		}
		catch (PageException e) {
			throw new IndexOutOfBoundsException(e.getMessage());
		}
	}

	@Override
	public boolean removeAll(Collection c) {
		Iterator it = c.iterator();
		boolean rtn = false;
		while (it.hasNext()) {
			if (remove(it.next())) rtn = true;
		}
		return rtn;
	}

	@Override
	public boolean retainAll(Collection c) {
		new ArrayList().retainAll(c);
		boolean modified = false;
		Iterator it = iterator();
		while (it.hasNext()) {
			if (!c.contains(it.next())) {
				it.remove();
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public Object set(int index, Object element) {
		try {
			if (!array.containsKey(index + 1)) throw new IndexOutOfBoundsException("Index: " + (index + 1) + ", Size: " + size());
			return array.setE(index + 1, element);
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public int size() {
		return array.size();
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		return array.toList().subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return array.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		return array.toArray();
	}

	@Override
	public Object clone() {
		return toArrayList((Array) Duplicator.duplicate(array, true));
	}

	@Override
	public void ensureCapacity(int minCapacity) {
		throw new PageRuntimeException("not supported");
	}

	@Override
	public void trimToSize() {
		throw new PageRuntimeException("not supported");
	}
}