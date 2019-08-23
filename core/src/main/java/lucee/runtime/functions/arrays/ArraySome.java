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
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.closure.ClosureFunc;
import lucee.runtime.functions.closure.Some;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.UDF;

public class ArraySome extends BIF {

	private static final long serialVersionUID = -7449844630816343951L;

	public static boolean call(PageContext pc, Array array, UDF udf) throws PageException {
		return _call(pc, array, udf, false, 20);
	}

	public static boolean call(PageContext pc, Array array, UDF udf, boolean parallel) throws PageException {
		return _call(pc, array, udf, parallel, 20);
	}

	public static boolean call(PageContext pc, Array array, UDF udf, boolean parallel, double maxThreads) throws PageException {
		return _call(pc, array, udf, parallel, (int) maxThreads);
	}

	private static boolean _call(PageContext pc, Array array, UDF udf, boolean parallel, int maxThreads) throws PageException {
		return Some._call(pc, array, udf, parallel, maxThreads, ClosureFunc.TYPE_ARRAY);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toArray(args[0]), Caster.toFunction(args[1]));
		if (args.length == 3) return call(pc, Caster.toArray(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]));
		if (args.length == 4) return call(pc, Caster.toArray(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]));

		throw new FunctionException(pc, "ArraySome", 2, 4, args.length);

	}

}