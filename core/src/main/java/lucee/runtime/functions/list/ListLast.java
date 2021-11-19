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
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

/**
 * Implements the CFML Function listlast
 */
public final class ListLast extends BIF {

	private static final long serialVersionUID = 2822477678831478329L;

	public static String call(PageContext pc, String list) {
		return ListUtil.last(list, ",", true);
	}

	public static String call(PageContext pc, String list, String delimiter) {
		return ListUtil.last(list, delimiter, true);
	}

	public static String call(PageContext pc, String list, String delimiter, boolean includeEmptyFields) {
		return ListUtil.last(list, delimiter, !includeEmptyFields);
	}

	public static String call(PageContext pc, String list, String delimiter, boolean includeEmptyFields, double count) {
		if (count == 1d) return ListUtil.last(list, delimiter, !includeEmptyFields);
		return ListUtil.last(list, delimiter, !includeEmptyFields, (int) count);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return ListUtil.last(Caster.toString(args[0]), ",", true, 1);
		if (args.length == 2) return ListUtil.last(Caster.toString(args[0]), Caster.toString(args[1]), true, 1);
		if (args.length == 3) return ListUtil.last(Caster.toString(args[0]), Caster.toString(args[1]), !Caster.toBooleanValue(args[2]), 1);
		if (args.length == 4) return ListUtil.last(Caster.toString(args[0]), Caster.toString(args[1]), !Caster.toBooleanValue(args[2]), Caster.toIntValue(args[3]));

		throw new FunctionException(pc, "ListLast", 1, 4, args.length);
	}

}