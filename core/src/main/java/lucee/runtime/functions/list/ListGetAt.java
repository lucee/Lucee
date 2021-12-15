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
 * Implements the CFML Function listgetat
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public final class ListGetAt extends BIF {

	private static final long serialVersionUID = -8227074223983816122L;

	public static String call(PageContext pc, String list, double posNumber) throws PageException {
		return call(pc, list, posNumber, ",", false);
	}

	public static String call(PageContext pc, String list, double posNumber, String delimiter) throws PageException {
		return call(pc, list, posNumber, delimiter, false);
	}

	public static String call(PageContext pc, String list, double posNumber, String delimiter, boolean includeEmptyFields) throws PageException {
		int pos = (int) posNumber;
		String rtn = ListUtil.getAt(list, delimiter, pos - 1, !includeEmptyFields, null);
		if (rtn == null) throw new FunctionException(pc, "listGetAt", 2, "posNumber", "invalid string list index [" + pos + "]");
		return rtn;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), ",", false);
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), false);
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]));
		throw new FunctionException(pc, "ListGetAt", 2, 4, args.length);
	}
}