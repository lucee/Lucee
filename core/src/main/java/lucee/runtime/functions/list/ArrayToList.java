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
/**
 * Implements the CFML Function arraytolist
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.QueryColumn;

public final class ArrayToList extends BIF {

	private static final long serialVersionUID = -4909685848106371747L;

	public static String call(PageContext pc, Array array) throws PageException {
		return call(pc, array, ',');
	}

	public static String call(PageContext pc, Array array, String delimiter) throws PageException {
		if (delimiter.length() == 1) return call(pc, array, delimiter.charAt(0));
		if (array instanceof QueryColumn) array = unwrap(pc, (QueryColumn) array);
		int len = array.size();
		if (len == 0) return "";
		if (len == 1) return Caster.toString(array.get(1, ""));

		Object o = array.get(1, null);
		StringBuilder sb = new StringBuilder(o == null ? "" : Caster.toString(o));
		for (int i = 2; i <= len; i++) {
			sb.append(delimiter);
			o = array.get(i, null);
			sb.append(o == null ? "" : Caster.toString(o));
		}
		return sb.toString();
	}

	public static String call(PageContext pc, Array array, char delimiter) throws PageException {
		if (array instanceof QueryColumn) array = unwrap(pc, (QueryColumn) array);
		int len = array.size();
		if (len == 0) return "";
		if (len == 1) return Caster.toString(array.get(1, ""));

		Object o = array.get(1, null);
		StringBuilder sb = new StringBuilder(o == null ? "" : Caster.toString(o));
		for (int i = 2; i <= len; i++) {
			sb.append(delimiter);
			o = array.get(i, null);
			sb.append(o == null ? "" : Caster.toString(o));
		}
		return sb.toString();
	}

	private static Array unwrap(PageContext pc, QueryColumn col) {
		Array arr = Caster.toArray(col.get(pc, (Object) null), null);
		if (arr != null) return arr;
		return (Array) col;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toArray(args[0]));
		return call(pc, Caster.toArray(args[0]), Caster.toString(args[1]));
	}
}