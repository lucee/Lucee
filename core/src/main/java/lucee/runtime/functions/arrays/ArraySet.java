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
 * Implements the CFML Function arrayset
 */
package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Array;

public final class ArraySet extends BIF {

	private static final long serialVersionUID = -7804363479876538167L;

	public static boolean call(PageContext pc, Array array, Number from, Number to, Object value) throws PageException {
		int f = Caster.toIntValue(from);
		int t = Caster.toIntValue(to);
		if (f < 1) throw new ExpressionException("Start index of the function arraySet must be greater than zero; now [" + f + "]");
		if (f > t) throw new ExpressionException("End index of the function arraySet must be greater than the Start index; now [start:" + f + ", end:" + t + "]");
		if (array.getDimension() > 1)
			throw new ExpressionException("Function arraySet can only be used with a one-dimensional array; this array has " + array.getDimension() + " dimensions");
		for (int i = f; i <= t; i++) {
			array.setE(i, Duplicator.duplicate(value, true));
		}

		return true;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 4) return call(pc, Caster.toArray(args[0]), Caster.toNumber(pc, args[1]), Caster.toNumber(pc, args[2]), args[3]);
		else throw new FunctionException(pc, "ArraySet", 4, 4, args.length);
	}
}