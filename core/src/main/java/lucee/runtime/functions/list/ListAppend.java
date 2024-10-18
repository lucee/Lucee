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
 * Implements the CFML Function listappend
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public final class ListAppend extends BIF {

	private static final long serialVersionUID = -4893447489733907241L;

	public static String call(PageContext pc, String list, String value) {
		return call(pc, list, value, ",", true);
	}

	public static String call(PageContext pc, String list, String value, String delimiter) {
		return call(pc, list, value, delimiter, true);
	}

	public static String call(PageContext pc, String list, String value, String delimiter, boolean includeEmptyFields) {
		if (delimiter.length() == 0) return list;

		if (list.length() == 0) return includeEmptyFields ? value : ListUtil.listRemoveEmpty(value, delimiter);

		if (!includeEmptyFields) {
			list = ListUtil.listRemoveEmpty(list, delimiter);
			value = ListUtil.listRemoveEmpty(value, delimiter);
		}
		return new StringBuilder(list).append(delimiter).append(value).toString();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), ",", true);
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), true);
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]));

		throw new FunctionException(pc, "ListAppend", 2, 4, args.length);
	}

}