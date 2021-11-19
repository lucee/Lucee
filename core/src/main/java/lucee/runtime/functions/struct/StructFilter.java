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
 * Implements the CFML Function arrayavg
 */
package lucee.runtime.functions.struct;

import lucee.commons.lang.CFTypes;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.closure.Filter;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;

public final class StructFilter extends BIF {

	public static Struct call(PageContext pc, Struct sct, UDF udf) throws PageException {
		return _call(pc, sct, udf, false, 20);
	}

	public static Struct call(PageContext pc, Struct sct, UDF udf, boolean parallel) throws PageException {
		return _call(pc, sct, udf, parallel, 20);
	}

	public static Struct call(PageContext pc, Struct sct, UDF udf, boolean parallel, double maxThreads) throws PageException {
		return _call(pc, sct, udf, parallel, (int) maxThreads);
	}

	public static Struct _call(PageContext pc, Struct sct, UDF filter, boolean parallel, int maxThreads) throws PageException {

		// check UDF return type
		int type = filter.getReturnType();
		if (type != CFTypes.TYPE_BOOLEAN && type != CFTypes.TYPE_ANY)
			throw new ExpressionException("invalid return type [" + filter.getReturnTypeAsString() + "] for UDF Filter, valid return types are [boolean,any]");

		return (Struct) Filter.call(pc, sct, filter, parallel, maxThreads, Filter.TYPE_STRUCT);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]));
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]));
		if (args.length == 4) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]));
		throw new FunctionException(pc, "StructFilter", 2, 4, args.length);
	}
}