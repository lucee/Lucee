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
/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package lucee.commons.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractCollection<E> implements Collection<E> {
	/**
	 * Sole constructor. (For invocation by subclass constructors, typically implicit.)
	 */
	protected AbstractCollection() {
	}

	// Query Operations

	/**
	 * Returns an iterator over the elements contained in this collection.
	 *
	 * @return an iterator over the elements contained in this collection
	 */
	@Override
	public abstract Iterator<E> iterator();

	@Override
	public abstract int size();

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation returns <tt>size() == 0</tt>.
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation iterates over the elements in the collection, checking each element in turn
	 * for equality with the specified element.
	 *
	 * @throws ClassCastException {@inheritDoc}
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public boolean contains(Object o) {
		Iterator<E> it = iterator();
		if (o == null) {
			while (it.hasNext())
				if (it.next() == null) return true;
		}
		else {
			while (it.hasNext())
				if (o.equals(it.next())) return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation returns an array containing all the elements returned by this collection's
	 * iterator, in the same order, stored in consecutive elements of the array, starting with index
	 * {@code 0}. The length of the returned array is equal to the number of elements returned by the
	 * iterator, even if the size of this collection changes during iteration, as might happen if the
	 * collection permits concurrent modification during iteration. The {@code size} method is called
	 * only as an optimization hint; the correct result is returned even if the iterator returns a
	 * different number of elements.
	 *
	 * <p>
	 * This method is equivalent to:
	 *
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	List<E> list = new ArrayList<E>(size());
	 * 	for (E e: this)
	 * 		list.add(e);
	 * 	return list.toArray();
	 * }
	 * </pre>
	 */
	@Override
	public Object[] toArray() {
		// Estimate size of array; be prepared to see more or fewer elements
		Object[] r = new Object[size()];
		Iterator<E> it = iterator();
		for (int i = 0; i < r.length; i++) {
			if (!it.hasNext()) // fewer elements than expected
				return Arrays.copyOf(r, i);
			r[i] = it.next();
		}
		return it.hasNext() ? finishToArray(r, it) : r;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation returns an array containing all the elements returned by this collection's
	 * iterator in the same order, stored in consecutive elements of the array, starting with index
	 * {@code 0}. If the number of elements returned by the iterator is too large to fit into the
	 * specified array, then the elements are returned in a newly allocated array with length equal to
	 * the number of elements returned by the iterator, even if the size of this collection changes
	 * during iteration, as might happen if the collection permits concurrent modification during
	 * iteration. The {@code size} method is called only as an optimization hint; the correct result is
	 * returned even if the iterator returns a different number of elements.
	 *
	 * <p>
	 * This method is equivalent to:
	 *
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	List<E> list = new ArrayList<E>(size());
	 * 	for (E e: this)
	 * 		list.add(e);
	 * 	return list.toArray(a);
	 * }
	 * </pre>
	 *
	 * @throws ArrayStoreException {@inheritDoc}
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		// Estimate size of array; be prepared to see more or fewer elements
		int size = size();
		T[] r = a.length >= size ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		Iterator<E> it = iterator();

		for (int i = 0; i < r.length; i++) {
			if (!it.hasNext()) { // fewer elements than expected
				if (a != r) return Arrays.copyOf(r, i);
				r[i] = null; // null-terminate
				return r;
			}
			r[i] = (T) it.next();
		}
		return it.hasNext() ? finishToArray(r, it) : r;
	}

	/**
	 * The maximum size of array to allocate. Some VMs reserve some header words in an array. Attempts
	 * to allocate larger arrays may result in OutOfMemoryError: Requested array size exceeds VM limit
	 */
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	/**
	 * Reallocates the array being used within toArray when the iterator returned more elements than
	 * expected, and finishes filling it from the iterator.
	 *
	 * @param r the array, replete with previously stored elements
	 * @param it the in-progress iterator over this collection
	 * @return array containing the elements in the given array, plus any further elements returned by
	 *         the iterator, trimmed to size
	 */
	private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
		int i = r.length;
		while (it.hasNext()) {
			int cap = r.length;
			if (i == cap) {
				int newCap = cap + (cap >> 1) + 1;
				// overflow-conscious code
				if (newCap - MAX_ARRAY_SIZE > 0) newCap = hugeCapacity(cap + 1);
				r = Arrays.copyOf(r, newCap);
			}
			r[i++] = (T) it.next();
		}
		// trim if overallocated
		return (i == r.length) ? r : Arrays.copyOf(r, i);
	}

	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) // overflow
			throw new OutOfMemoryError("Required array size too large");
		return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}

	// Modification Operations

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation always throws an <tt>UnsupportedOperationException</tt>.
	 *
	 * @throws UnsupportedOperationException {@inheritDoc}
	 * @throws ClassCastException {@inheritDoc}
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 * @throws IllegalStateException {@inheritDoc}
	 */
	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation iterates over the collection looking for the specified element. If it finds
	 * the element, it removes the element from the collection using the iterator's remove method.
	 *
	 * <p>
	 * Note that this implementation throws an <tt>UnsupportedOperationException</tt> if the iterator
	 * returned by this collection's iterator method does not implement the <tt>remove</tt> method and
	 * this collection contains the specified object.
	 *
	 * @throws UnsupportedOperationException {@inheritDoc}
	 * @throws ClassCastException {@inheritDoc}
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public boolean remove(Object o) {
		Iterator<E> it = iterator();
		if (o == null) {
			while (it.hasNext()) {
				if (it.next() == null) {
					it.remove();
					return true;
				}
			}
		}
		else {
			while (it.hasNext()) {
				if (o.equals(it.next())) {
					it.remove();
					return true;
				}
			}
		}
		return false;
	}

	// Bulk Operations

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object e: c)
			if (!contains(e)) return false;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean modified = false;
		for (E e: c)
			if (add(e)) modified = true;
		return modified;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		Iterator<?> it = iterator();
		while (it.hasNext()) {
			if (c.contains(it.next())) {
				it.remove();
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		Iterator<E> it = iterator();
		while (it.hasNext()) {
			if (!c.contains(it.next())) {
				it.remove();
				modified = true;
			}
		}
		return modified;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation iterates over this collection, removing each element using the
	 * <tt>Iterator.remove</tt> operation. Most implementations will probably choose to override this
	 * method for efficiency.
	 *
	 * <p>
	 * Note that this implementation will throw an <tt>UnsupportedOperationException</tt> if the
	 * iterator returned by this collection's <tt>iterator</tt> method does not implement the
	 * <tt>remove</tt> method and this collection is non-empty.
	 *
	 * @throws UnsupportedOperationException {@inheritDoc}
	 */
	@Override
	public void clear() {
		Iterator<E> it = iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
	}

	// String conversion

	/**
	 * Returns a string representation of this collection. The string representation consists of a list
	 * of the collection's elements in the order they are returned by its iterator, enclosed in square
	 * brackets (<tt>"[]"</tt>). Adjacent elements are separated by the characters <tt>", "</tt> (comma
	 * and space). Elements are converted to strings as by {@link String#valueOf(Object)}.
	 *
	 * @return a string representation of this collection
	 */
	@Override
	public String toString() {
		Iterator<E> it = iterator();
		if (!it.hasNext()) return "[]";

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (;;) {
			E e = it.next();
			sb.append(e == this ? "(this Collection)" : e);
			if (!it.hasNext()) return sb.append(']').toString();
			sb.append(',').append(' ');
		}
	}

}