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
package lucee.commons.sql;

import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;

public class OracleCaster {

	private static final Object[] ZERO_ARGS = new Object[0];

	// private static final Class OPAQUE=ClassUtil.loadClass("oracle.sql.OPAQUE", null);

	public static Object OPAQUE(Object o) {
		if (o == null) return null;

		try {

			byte[] bytes = Caster.toBytes(Reflector.callMethod(o, "getBytes", ZERO_ARGS), null);
			return new String(bytes, "UTF-8");
		}
		catch (Exception e) {
			// print.printST(e);
		}

		return o;
	}

	/*
	 * private static boolean equals(Class left, Class right) { if(left==right)return true; return
	 * left.equals(right.getName()); }
	 */

}