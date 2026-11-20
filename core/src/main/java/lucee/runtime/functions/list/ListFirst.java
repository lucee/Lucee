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
 * Implements the CFML Function listfirst
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public final class ListFirst extends BIF {

	private static final long serialVersionUID = 1098339742182832847L;

	public static String call(PageContext pc, String list) {
		return ListUtil.first(list, ",", true, 1);
	}

	public static String call(PageContext pc, String list, String delimiter) {
		if (isEmptyDelimiter(delimiter)) return firstChars(list, 1);
		return ListUtil.first(list, delimiter, true, 1);
	}

	public static String call(PageContext pc, String list, String delimiter, boolean includeEmptyFields) {
		if (isEmptyDelimiter(delimiter)) return firstChars(list, 1);
		return ListUtil.first(list, delimiter, !includeEmptyFields, 1);
	}

	public static String call(PageContext pc, String list, String delimiter, boolean includeEmptyFields, Number count) throws FunctionException {
		int cnt = Caster.toIntValue(count);
		if (cnt < 1) throw new FunctionException(pc, "ListFirst", 4, "count", "Argument count must be a positive value greater than 0");
		if (isEmptyDelimiter(delimiter)) return firstChars(list, cnt);
		return ListUtil.first(list, delimiter, !includeEmptyFields, cnt);
	}

	public static String call(PageContext pc, String list, String delimiter, boolean includeEmptyFields, Number count, boolean multiCharacterDelimiter) throws PageException {
		if (!ListUtil.isMultiCharDelimiter(delimiter, multiCharacterDelimiter)) return call(pc, list, delimiter, includeEmptyFields, count);
		char c = ListUtil.freeChar(list);
		return ListUtil.unmask(call(pc, ListUtil.mask(list, delimiter, c), String.valueOf(c), includeEmptyFields, count), delimiter, c);
	}

	/**
	 * LDEV-4596: an empty delimiter splits the list into single characters (like listToArray( list, "" )),
	 * so listFirst( "ab,c", "" ) is "a"
	 */
	static boolean isEmptyDelimiter(String delimiter) {
		return delimiter != null && delimiter.isEmpty();
	}

	static String firstChars(String list, int count) {
		if (list == null) return "";
		return list.length() <= count ? list : list.substring(0, count);
	}

	static String lastChars(String list, int count) {
		if (list == null) return "";
		return list.length() <= count ? list : list.substring(list.length() - count);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]), ",", false, 1);
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), false, 1);
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]), 1);
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]));
		if (args.length == 5)
			return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]), Caster.toBooleanValue(args[4]));

		throw new FunctionException(pc, "ListFirst", 1, 5, args.length);
	}
}