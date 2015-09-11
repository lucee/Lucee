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
package lucee.runtime.util;

import lucee.runtime.PageContext;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Null;

public class CallerUtil {
	public static Object get(PageContext pc,Object coll, Key[] keys, Object defaultValue) {
		if(coll==null) return defaultValue;
		int to=keys.length-1;
		for(int i=0;i<=to;i++){
			coll=((VariableUtilImpl)pc.getVariableUtil()).getCollection(pc, coll, keys[i], Null.NULL);
			if(coll==Null.NULL || (coll==null && i<to)) return defaultValue;
		}
		return coll;
	}
}