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
package lucee.commons.collection;

import java.util.Set;

public class SyncSet<E> extends SyncCollection<E> implements Set<E> {
	private static final long serialVersionUID = 487447009682186044L;

	public SyncSet(Set<E> s) {
		super(s);
	}

	public SyncSet(Set<E> s, Object mutex) {
		super(s, mutex);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		synchronized (mutex) {
			return c.equals(o);
		}
	}

	@Override
	public int hashCode() {
		synchronized (mutex) {
			return c.hashCode();
		}
	}
}