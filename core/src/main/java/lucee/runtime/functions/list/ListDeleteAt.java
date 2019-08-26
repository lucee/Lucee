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
 * Implements the CFML Function listdeleteat
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class ListDeleteAt extends BIF {

	private static final long serialVersionUID = 7050644316663288912L;
	private static char[] DEFAULT_DELIMITER = new char[] { ',' };

	public static String call(PageContext pc, String list, double posNumber) throws ExpressionException {
		return _call(pc, list, (int) posNumber, DEFAULT_DELIMITER, false);
	}

	public static String call(PageContext pc, String list, double posNumber, String del) throws ExpressionException {
		return _call(pc, list, (int) posNumber, del.toCharArray(), false);
	}

	public static String call(PageContext pc, String list, double posNumber, String del, boolean includeEmptyFields) throws ExpressionException {
		return _call(pc, list, (int) posNumber, del.toCharArray(), includeEmptyFields);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), ",", false);
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), false);
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]));

		throw new FunctionException(pc, "ListDeleteAt", 2, 4, args.length);
	}

	public static String _call(PageContext pc, String list, int pos, char[] del, boolean includeEmptyFields) throws ExpressionException {

		StringBuilder sb = new StringBuilder();
		int len = list.length();
		int index = 0;
		char last = 0, c;

		if (pos < 1) throw new FunctionException(pc, "ListDeleteAt", 2, "index", "index must be greater than 0");

		pos--;

		int i = 0;

		// ignore all delimiter at start
		if (!includeEmptyFields) for (; i < len; i++) {
			c = list.charAt(i);
			if (!equal(del, c)) break;
			sb.append(c);
		}

		// before
		for (; i < len; i++) {

			c = list.charAt(i);
			if (index == pos && !equal(del, c)) break;
			if (equal(del, c)) {
				if (includeEmptyFields || !equal(del, last)) index++;
			}
			sb.append(c);
			last = c;
		}

		// suppress item
		for (; i < len; i++) {
			if (equal(del, list.charAt(i))) break;
		}

		// ignore following delimiter
		for (; i < len; i++) {
			if (!equal(del, list.charAt(i))) break;
		}

		if (i == len) {

			while (sb.length() > 0 && equal(del, sb.charAt(sb.length() - 1))) {
				sb.delete(sb.length() - 1, sb.length());
			}
			if (pos > index) throw new FunctionException(pc, "ListDeleteAt", 2, "index", "index must be an integer between 1 and " + index);

			return sb.toString();
		}

		// fill the rest
		for (; i < len; i++) {
			sb.append(list.charAt(i));
		}

		return sb.toString();
	}

	private static boolean equal(char[] del, char c) {
		for (int i = 0; i < del.length; i++) {
			if (del[i] == c) return true;
		}
		return false;
	}
}