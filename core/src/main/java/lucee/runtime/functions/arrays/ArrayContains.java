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
 * Implements the CFML Function listcontains
 */
package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ArrayUtil;

public final class ArrayContains extends BIF {

	private static final long serialVersionUID = -5400552848978801342L;

	public static double call(PageContext pc, Array array, Object value, boolean substringMatch) throws PageException {
		if (substringMatch) {
			if (!Decision.isSimpleValue(value))
				throw new FunctionException(pc, "ArrayContains", 3, "substringMatch", "substringMatch can not be true when the value that is searched for is a complex object");
			return callLegacy(pc, array, value);
		}
		return ArrayFind.call(pc, array, value);
	}

	public static double call(PageContext pc, Array array, Object value) throws PageException {
		return call(pc, array, value, false);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toArray(args[0]), args[1], Caster.toBoolean(args[2]));
		else if (args.length == 2) return call(pc, Caster.toArray(args[0]), args[1]);
		else throw new FunctionException(pc, "ArrayContains", 2, 3, args.length);
	}

	/* legacy implementation */
	static double callLegacy(PageContext pc, Array array, Object value) throws PageException {
		String str = Caster.toString(value, null);
		if (str != null) return ArrayUtil.arrayContainsIgnoreEmpty(array, str, false) + 1;
		return ArrayFind.call(pc, array, value);
	}

}