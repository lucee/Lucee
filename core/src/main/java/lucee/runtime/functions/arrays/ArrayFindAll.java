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
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.Operator;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Closure;
import lucee.runtime.type.UDF;

public final class ArrayFindAll extends BIF {

	private static final long serialVersionUID = -1757019034608924098L;

	public static Array call(PageContext pc, Array array, Object value) throws PageException {
		if (value instanceof UDF) return find(pc, array, (UDF) value);
		return find(array, value, true);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toArray(args[0]), args[1]);
		else throw new FunctionException(pc, "ArrayFindAll", 2, 2, args.length);
	}

	public static Array find(PageContext pc, Array array, UDF udf) throws PageException {
		Array rtn = new ArrayImpl();
		int len = array.size();

		Object[] arr = new Object[1];
		Object res;
		Boolean b;
		for (int i = 1; i <= len; i++) {
			arr[0] = array.get(i, null);
			if (arr[0] != null) {
				res = udf.call(pc, arr, false);
				b = Caster.toBoolean(res, null);
				if (b == null) throw new FunctionException(pc, "ArrayFindAll", 2, "function",
						"return value of the " + (udf instanceof Closure ? "closure" : "function [" + udf.getFunctionName() + "]") + " cannot be casted to a boolean value.",
						CasterException.createMessage(res, "boolean"));
				if (b.booleanValue()) {
					rtn.appendEL(Caster.toDouble(i));
				}
			}
		}
		return rtn;
	}

	public static Array find(Array array, Object value, boolean caseSensitive) throws PageException {
		Array rtn = new ArrayImpl();
		int len = array.size();
		boolean valueIsSimple = Decision.isSimpleValue(value);
		Object o;
		for (int i = 1; i <= len; i++) {
			o = array.get(i, null);
			if (o != null && Operator.equals(o, value, caseSensitive, !valueIsSimple)) {
				rtn.appendEL(Caster.toDouble(i));
			}
		}
		return rtn;
	}
}