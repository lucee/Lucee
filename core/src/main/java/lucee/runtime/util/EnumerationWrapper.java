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
package lucee.runtime.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * class to make an enumaration from a ser, map or iterator
 */
public final class EnumerationWrapper<E> implements Enumeration<E> {

	private Iterator<E> it;

	/**
	 * @param map Constructor with a Map
	 */
	public EnumerationWrapper(Map<E, ?> map) {
		this(map.keySet().iterator());
	}

	/**
	 * @param set Constructor with a Set
	 */
	public EnumerationWrapper(Set<E> set) {
		this(set.iterator());
	}

	/**
	 * Constructor of the class
	 * 
	 * @param objs
	 */
	public EnumerationWrapper(E[] objs) {
		this(new ArrayIterator(objs));
	}

	/**
	 * @param it Constructor with an iterator
	 */
	public EnumerationWrapper(Iterator<E> it) {
		this.it = it;
	}

	/**
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	@Override
	public boolean hasMoreElements() {
		return it.hasNext();
	}

	/**
	 * @see java.util.Enumeration#nextElement()
	 */
	@Override
	public E nextElement() {
		return it.next();
	}

}