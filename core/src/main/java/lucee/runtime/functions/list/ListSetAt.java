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
 * Implements the CFML Function listsetat
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ListUtil;

public final class ListSetAt extends BIF {

	private static final long serialVersionUID = -105782799713547552L;

	public static String call(PageContext pc, String list, double posNumber, String value) throws ExpressionException {
		return call(pc, list, posNumber, value, ",", false);
	}

	public static String call(PageContext pc, String list, double posNumber, String value, String delimiter) throws ExpressionException {
		return call(pc, list, posNumber, value, delimiter, false);
	}

	public static String call(PageContext pc, String list, double posNumber, String value, String delimiter, boolean includeEmptyFields) throws ExpressionException {

		if (list.length() == 0) throw new FunctionException(pc, "listSetAt", 1, "list", "can't be empty");

		int pos = ((int) posNumber);
		// int[] removedInfo=new int[2];

		Array arr = ListUtil.listToArray(list, delimiter);
		int len = arr.size();

		// invalid index
		if (pos < 1) throw new FunctionException(pc, "listSetAt", 2, "position", "invalid string list index [" + (pos) + "]");
		else if (len < pos) {
			throw new FunctionException(pc, "listSetAt", 2, "position", "invalid string list index [" + (pos) + "], indexes go from 1 to " + (len));
		}

		StringBuilder sb = new StringBuilder();// RepeatString.call(new StringBuffer(),delimiter,removedInfo[0]);
		boolean hasStart = false;
		boolean hasSet = false;
		String v;
		int count = 0;
		for (int i = 1; i <= len; i++) {
			v = (String) arr.get(i, "");
			if (hasStart) {
				sb.append(delimiter);
			}
			else hasStart = true;

			if (includeEmptyFields || v.length() > 0) count++;
			if (!hasSet && pos == count) {
				sb.append(value);
				hasSet = true;
			}
			else sb.append(arr.get(i, ""));
		}
		if (!hasSet) {
			throw new FunctionException(pc, "listSetAt", 2, "position", "invalid string list index [" + (pos) + "]");
		}

		return sb.toString();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]));
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), Caster.toString(args[3]));
		if (args.length == 5)
			return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toBooleanValue(args[4]));

		throw new FunctionException(pc, "ListSetAt", 3, 5, args.length);
	}
}