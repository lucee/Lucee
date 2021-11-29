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
 * Implements the CFML Function replacelist
 */
package lucee.runtime.functions.string;

import java.util.Iterator;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ListUtil;

public final class ReplaceList extends BIF {

	private static final long serialVersionUID = -3935300433837460732L;

	public static String call(PageContext pc, String str, String list1, String list2) {
		return _call(pc, str, list1, list2, ",", ",", false, false);
	}

	public static String call(PageContext pc, String str, String list1, String list2, String delimiter_list1) throws PageException {
		if (Decision.isBoolean(delimiter_list1)) return _call(pc, str, list1, list2, ",", ",", false, Caster.toBooleanValue(delimiter_list1));
		return _call(pc, str, list1, list2, delimiter_list1, delimiter_list1, false, false);
	}

	public static String call(PageContext pc, String str, String list1, String list2, String delimiter_list1, String delimiter_list2) throws PageException {
		if (Decision.isBoolean(delimiter_list2)) return _call(pc, str, list1, list2, delimiter_list1, delimiter_list1, false, Caster.toBooleanValue(delimiter_list2));
		return _call(pc, str, list1, list2, delimiter_list1, delimiter_list2, false, false);
	}

	public static String call(PageContext pc, String str, String list1, String list2, String delimiter_list1, String delimiter_list2, boolean includeEmptyFields) {
		return _call(pc, str, list1, list2, delimiter_list1, delimiter_list2, false, includeEmptyFields);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 6) return _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toString(args[4]),
				false, Caster.toBooleanValue(args[5]));
		if (args.length == 5) {

			if (Decision.isBoolean(args[4])) return _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toString(args[3]), false, Caster.toBooleanValue(args[4]));

			return _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toString(args[4]), false, false);
		}
		if (args.length == 4) {
			
			if (Decision.isBoolean(args[3])) return _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), ",", ",", false, Caster.toBooleanValue(args[3]));

			return _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), ",", false, false);
		}
		if (args.length == 3) return _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), ",", ",", false, false);
		throw new FunctionException(pc, "ReplaceList", 3, 5, args.length);
	}

	static String _call(PageContext pc, String str, String list1, String list2, String delimiter_list1, String delimiter_list2, boolean ignoreCase, boolean includeEmptyFields) {
		if (delimiter_list1 == null) delimiter_list1 = ",";
		if (delimiter_list2 == null) delimiter_list2 = ",";

		Array arr1 = ListUtil.listToArray(list1, delimiter_list1, false, false);
		Array arr2 = ListUtil.listToArray(list2, delimiter_list2, includeEmptyFields, false);

		Iterator<Object> it1 = arr1.valueIterator();
		Iterator<Object> it2 = arr2.valueIterator();

		while (it1.hasNext()) {
			str = StringUtil.replace(str, Caster.toString(it1.next(), null), ((it2.hasNext()) ? Caster.toString(it2.next(), null) : ""), false, ignoreCase);
		}
		return str;
	}

}