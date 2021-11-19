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
package lucee.runtime.type.comparator;

import java.util.Comparator;

import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;

public class ArrayOfStructComparator implements Comparator<Struct> {

	private Key key;

	/**
	 * Constructor of the class
	 * 
	 * @param key key used from struct
	 */
	public ArrayOfStructComparator(Collection.Key key) {
		this.key = key;
	}

	@Override
	public int compare(Struct s1, Struct s2) {
		return compareObjects(s1.get(key, ""), s2.get(key, ""));
	}

	private int compareObjects(Object oLeft, Object oRight) {
		return Caster.toString(oLeft, "").compareToIgnoreCase(Caster.toString(oRight, ""));
		// return Caster.toString(oLeft).compareTo(Caster.toString(oRight));
	}

}