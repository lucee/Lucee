/**
 *
 * Copyright (c) 2026, Lucee Association Switzerland. All rights reserved.
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

import lucee.commons.lang.CFTypes;
import lucee.runtime.PageContext;
import lucee.runtime.db.SQLCaster;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;

/**
 * Converts every value of a query to the type declared by its column and returns the query. By
 * default a new query is returned, the original stays untouched. When <code>inline</code> is set to
 * true, the given query is modified in place instead.
 */
public final class QueryConvertTypes extends BIF {

	private static final long serialVersionUID = -1616895878900894757L;

	public static Query call(PageContext pc, Query query) throws PageException {
		return call(pc, query, false);
	}

	public static Query call(PageContext pc, Query query, boolean inline) throws PageException {
		Query target = inline ? query : (Query) query.duplicate(true);

		Collection.Key[] columnNames = target.getColumnNames();
		for (int c = 0; c < columnNames.length; c++) {
			QueryColumn column = target.getColumn(columnNames[c]);
			short type = SQLCaster.toCFType(column.getType(), CFTypes.TYPE_UNDEFINED);

			// columns without a convertible type (e.g. OTHER/OBJECT) are left untouched
			if (type == CFTypes.TYPE_UNDEFINED) continue;

			String strType = column.getTypeAsString();
			int len = column.size();
			for (int row = 1; row <= len; row++) {
				Object value = column.get(row, null);
				// keep null values as they are
				if (value == null) continue;
				// convert (if it fails the original value is kept)
				column.set(row, Caster.castTo(pc, type, strType, value, value));
			}
		}
		return target;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toQuery(args[0]));
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toBooleanValue(args[1]));
		throw new lucee.runtime.exp.FunctionException(pc, "QueryConvertTypes", 1, 2, args.length);
	}
}
