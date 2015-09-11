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

import lucee.commons.lang.ClassUtil;

public class OracleCaster {

	private static final Class OPAQUE=ClassUtil.loadClass("oracle.sql.OPAQUE", null);
	
	public static Object OPAQUE(Object o) {
		if(o==null) return null;
			
		try {
			byte[] bv = ((oracle.sql.OPAQUE)o).getBytes();
			
			//OPAQUE op = ((oracle.sql.OPAQUE)o);
			//OpaqueDescriptor desc = ((oracle.sql.OPAQUE)o).getDescriptor();
			
			
			//Method getBytesValue = o.getClass().getMethod("getBytesValue", new Class[0]);
			//byte[] bv = (byte[])getBytesValue.invoke(o, new Object[0]);
			return new String(bv,"UTF-8");
		}
		catch (Exception e) {
			//print.printST(e);
		}
		
		return o;
	}

	private static boolean equals(Class left, Class right) {
		if(left==right)return true;
		return left.equals(right.getName());
	}

}