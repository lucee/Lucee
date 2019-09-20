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
package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

public final class ArrayMid extends BIF {

	private static final long serialVersionUID = 4996354700884413289L;

	public static Array call(PageContext pc, Array arr, double start) throws ExpressionException {
		return call(pc, arr, start, -1);
	}

	public static Array call(PageContext pc, Array arr, double start, double count) throws ExpressionException {
		int s = (int) start;
		int c = (int) count;

		if (s < 1) throw new FunctionException(pc, "ArrayMid", 2, "start", "Parameter which is now [" + s + "] must be a positive integer");
		if (c == -1) c = arr.size();
		else if (c < -1) throw new FunctionException(pc, "ArrayMid", 3, "count", "Parameter which is now [" + c + "] must be a non-negative integer or -1 (for string length)");
		c += s - 1;
		if (s > arr.size()) return new ArrayImpl();

		ArrayImpl rtn = new ArrayImpl();
		int len = arr.size();
		Object value;
		for (int i = s; i <= c && i <= len; i++) {
			value = arr.get(i, null);
			rtn.appendEL(value);
		}
		return rtn;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]));
		else if (args.length == 3) return call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]));
		else throw new FunctionException(pc, "ArrayMid", 2, 3, args.length);
	}
}