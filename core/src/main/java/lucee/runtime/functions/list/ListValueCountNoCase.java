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
 * Implements the CFML Function listvaluecountnocase
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class ListValueCountNoCase extends BIF {

	private static final long serialVersionUID = 2648222056209118284L;

	public static double call(PageContext pc, String list, String value) throws PageException {
		return ListValueCount.call(pc, list.toLowerCase(), value.toLowerCase(), ",");
	}

	public static double call(PageContext pc, String list, String value, String delimiter) throws PageException {
		return ListValueCount.call(pc, list.toLowerCase(), value.toLowerCase(), delimiter);

	}

	public static double call(PageContext pc, String list, String value, String delimiter, boolean includeEmptyFields) throws PageException {
		return ListValueCount.call(pc, list.toLowerCase(), value.toLowerCase(), delimiter, includeEmptyFields);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]));

		throw new FunctionException(pc, "ListValueCountNoCase", 2, 4, args.length);
	}
}