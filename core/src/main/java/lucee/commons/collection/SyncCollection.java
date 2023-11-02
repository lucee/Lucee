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
package lucee.commons.collection;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import lucee.commons.lang.SerializableObject;

public class SyncCollection<E> implements Collection<E>, Serializable {
	private static final long serialVersionUID = 3053995032091335093L;

	final Collection<E> c; // Backing Collection
	final Object mutex; // Object on which to synchronize

	SyncCollection(Collection<E> c) {
		if (c == null) throw new NullPointerException();
		this.c = c;
		mutex = new SerializableObject();
	}

	SyncCollection(Collection<E> c, Object mutex) {
		this.c = c;
		this.mutex = mutex;
	}

	@Override
	public int size() {
		synchronized (mutex) {
			return c.size();
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (mutex) {
			return c.isEmpty();
		}
	}

	@Override
	public boolean contains(Object o) {
		synchronized (mutex) {
			return c.contains(o);
		}
	}

	@Override
	public Object[] toArray() {
		synchronized (mutex) {
			return c.toArray();
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		synchronized (mutex) {
			return c.toArray(a);
		}
	}

	@Override
	public Iterator<E> iterator() {
		return c.iterator(); // Must be manually synched by user!
	}

	@Override
	public boolean add(E e) {
		synchronized (mutex) {
			return c.add(e);
		}
	}

	@Override
	public boolean remove(Object o) {
		synchronized (mutex) {
			return c.remove(o);
		}
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		synchronized (mutex) {
			return c.containsAll(coll);
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> coll) {
		synchronized (mutex) {
			return c.addAll(coll);
		}
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		synchronized (mutex) {
			return c.removeAll(coll);
		}
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		synchronized (mutex) {
			return c.retainAll(coll);
		}
	}

	@Override
	public void clear() {
		synchronized (mutex) {
			c.clear();
		}
	}

	@Override
	public String toString() {
		synchronized (mutex) {
			return c.toString();
		}
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		synchronized (mutex) {
			s.defaultWriteObject();
		}
	}
}