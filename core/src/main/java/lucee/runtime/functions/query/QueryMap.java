/**
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.closure.ClosureFunc;
import lucee.runtime.functions.closure.Map;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;
import lucee.runtime.type.UDF;

public class QueryMap extends BIF {

	private static final long serialVersionUID = 5225631181634029456L;

	public static Query call(PageContext pc, Query qry, UDF udf) throws PageException {
		return _call(pc, qry, udf, null, false, 20);
	}

	public static Query call(PageContext pc, Query qry, UDF udf, Query resQuery) throws PageException {
		return _call(pc, qry, udf, resQuery, false, 20);
	}

	public static Query call(PageContext pc, Query qry, UDF udf, Query resQuery, boolean parallel) throws PageException {
		return _call(pc, qry, udf, resQuery, parallel, 20);
	}

	public static Query call(PageContext pc, Query qry, UDF udf, Query resQuery, boolean parallel, double maxThreads) throws PageException {
		return _call(pc, qry, udf, resQuery, parallel, (int) maxThreads);
	}

	private static Query _call(PageContext pc, Query qry, UDF udf, Query resQuery, boolean parallel, int maxThreads) throws PageException {
		return (Query) Map._call(pc, qry, udf, parallel, maxThreads, resQuery, ClosureFunc.TYPE_QUERY);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]));
		if (args.length == 3) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toQuery(args[2]));
		if (args.length == 4) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toQuery(args[2]), Caster.toBooleanValue(args[3]));
		if (args.length == 5)
			return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toQuery(args[2]), Caster.toBooleanValue(args[3]), Caster.toDoubleValue(args[4]));

		throw new FunctionException(pc, "QueryMap", 2, 5, args.length);
	}

}