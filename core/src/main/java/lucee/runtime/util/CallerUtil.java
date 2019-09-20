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
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Null;

public class CallerUtil {

	public final static int TYPE_DATA = 1;
	public final static int TYPE_UDF_ARGS = 2;
	public final static int TYPE_UDF_NAMED_ARGS = 3;
	public final static int TYPE_BIF = 4;

	public static Object get(PageContext pc, Object coll, Key[] keys, Object defaultValue) {
		if (coll == null) return defaultValue;
		int to = keys.length - 1;
		for (int i = 0; i <= to; i++) {
			coll = ((VariableUtilImpl) pc.getVariableUtil()).getCollection(pc, coll, keys[i], Null.NULL);
			if (coll == Null.NULL || (coll == null && i < to)) return defaultValue;
		}
		return coll;
	}

	// TODO work in progress
	public static Object get(PageContext pc, Object coll, int[] types, Key[] keys, Object[][] args, Object defaultValue) throws PageException {
		if (coll == null) return defaultValue;
		int to = keys.length - 1;
		VariableUtilImpl vu = (VariableUtilImpl) pc.getVariableUtil();
		for (int i = 0; i <= to; i++) {
			switch (types[i]) {
			case TYPE_DATA:
				coll = vu.getCollection(pc, coll, keys[i], Null.NULL);
				break;
			case TYPE_UDF_ARGS:
				coll = vu.callFunctionWithoutNamedValues(pc, coll, keys[i], args[i], false, Null.NULL);
				break;
			case TYPE_UDF_NAMED_ARGS:
				coll = vu.callFunctionWithNamedValues(pc, coll, keys[i], args[i], false, Null.NULL);
				break;
			case TYPE_BIF:
				coll = null;// TODO
				break;
			}

			if (coll == Null.NULL || (coll == null && i < to)) return defaultValue;
		}
		return coll;
	}
}