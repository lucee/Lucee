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
 * Implements the CFML Function listtoarray
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.util.ListUtil;

public final class ListToArray extends BIF {

	private static final long serialVersionUID = 5883854318455975404L;

	public static Array call(PageContext pc, String list) {
		if (list.length() == 0) return new ArrayImpl();
		return ListUtil.listToArrayRemoveEmpty(list, ',');
	}

	public static Array call(PageContext pc, String list, String delimiter) {
		return call(pc, list, delimiter, false, false);
	}

	public static Array call(PageContext pc, String list, String delimiter, boolean includeEmptyFields) {
		return call(pc, list, delimiter, includeEmptyFields, false);
	}

	public static Array call(PageContext pc, String list, String delimiter, boolean includeEmptyFields, boolean multiCharacterDelimiter) {
		// empty
		if (list.length() == 0) {
			Array a = new ArrayImpl();
			if (includeEmptyFields) a.appendEL("");
			return a;
		}
		return ListUtil.listToArray(list, delimiter, includeEmptyFields, multiCharacterDelimiter);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]));
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]), Caster.toBooleanValue(args[3]));

		throw new FunctionException(pc, "ListToArray", 1, 4, args.length);
	}
}