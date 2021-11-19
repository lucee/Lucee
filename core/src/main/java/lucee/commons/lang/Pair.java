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
package lucee.commons.lang;

import java.io.Serializable;

/**
 * a Simple name value Pair
 */
public final class Pair<K, V> implements Serializable {
	K name;
	V value;

	/**
	 * Constructor of the class
	 * 
	 * @param name
	 * @param value
	 */
	public Pair(K name, V value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * @return the name
	 */
	public K getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(K name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name + ":" + value;
	}

	/**
	 * @return the value
	 */
	public V getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(V value) {
		this.value = value;
	}

}