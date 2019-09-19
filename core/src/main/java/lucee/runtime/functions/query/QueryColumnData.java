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
package lucee.runtime.functions.query;

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.db.SQLCaster;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.UDF;

public class QueryColumnData extends BIF {

	private static final long serialVersionUID = 3915214686428831274L;

	public static Array call(PageContext pc, Query query, String columnName) throws PageException {
		return call(pc, query, columnName, null);
	}

	public static Array call(PageContext pc, Query query, String columnName, UDF udf) throws PageException {
		Array arr = new ArrayImpl();
		QueryColumn column = query.getColumn(KeyImpl.init(columnName));
		Iterator<Object> it = column.valueIterator();
		Object value;
		short type = SQLCaster.toCFType(column.getType(), lucee.commons.lang.CFTypes.TYPE_UNDEFINED);

		while (it.hasNext()) {
			value = it.next();
			if (!NullSupportHelper.full(pc) && value == null) value = "";

			// callback call
			if (udf != null) value = udf.call(pc, new Object[] { value }, true);

			// convert (if necessary)
			value = Caster.castTo(pc, type, column.getTypeAsString(), value, value);

			arr.append(value);
		}
		return arr;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]));
		return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toFunction(args[2]));
	}
}