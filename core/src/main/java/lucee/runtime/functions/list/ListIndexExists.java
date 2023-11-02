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
 * Implements the CFML Function structkeyexists
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public final class ListIndexExists extends BIF {

	private static final long serialVersionUID = 7642583305678735361L;

	public static boolean call(PageContext pc, String list, double index) {
		return call(pc, list, index, ",", false);
	}

	public static boolean call(PageContext pc, String list, double index, String delimiter) {
		return call(pc, list, index, delimiter, false);
	}

	public static boolean call(PageContext pc, String list, double index, String delimiter, boolean includeEmptyFields) {
		if (includeEmptyFields) return ListUtil.listToArray(list, delimiter).get((int) index, null) != null;
		return ListUtil.listToArrayRemoveEmpty(list, delimiter).get((int) index, null) != null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]));
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]));

		throw new FunctionException(pc, "ListIndexExists", 2, 4, args.length);
	}
}