/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.component;

import java.util.concurrent.ConcurrentHashMap;

import lucee.runtime.type.Collection.Key;

public class StaticStruct extends ConcurrentHashMap<Key, Member> {

	private static final long serialVersionUID = 4964717564860928637L;
	private static long counter = 1;

	private long index = 0;

	public StaticStruct() {
	}

	public boolean isInit() {
		return index != 0;
	}

	public long index() {
		return index;
	}

	public void setInit(boolean init) {
		if (init) this.index = createIndex();
		else index = 0;
	}

	public static synchronized long createIndex() {
		counter++;
		if (counter < 0) counter = 1;
		return counter;
	}
}