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

import java.sql.Types;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.db.CFTypes;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;

/**
 * Converts every value of a query to the java type that matches the type declared by its column
 * and returns the query. By default a new query is returned, the original stays untouched. When
 * <code>inline</code> is set to true, the given query is modified in place instead.
 *
 * <ul>
 * <li>INTEGER, SMALLINT, TINYINT: Integer</li>
 * <li>BIGINT: Long</li>
 * <li>DOUBLE, FLOAT, REAL, NUMERIC: Double</li>
 * <li>DECIMAL: BigDecimal</li>
 * <li>DATE, TIME, TIMESTAMP: DateTime</li>
 * <li>CHAR, VARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, LONGNVARCHAR: String</li>
 * <li>BIT, BOOLEAN: Boolean</li>
 * </ul>
 *
 * All other columns (OTHER/OBJECT, binary, ...) are left untouched. Null stays null. An empty
 * string becomes null in non string columns when full null support is enabled, otherwise it is kept
 * as it is, because that is what a query accepts as a valid value in any column. A value that can
 * not be converted results in an exception naming the column and the row, in that case an inline
 * conversion leaves the query unchanged.
 */
public final class QueryConvertTypes extends BIF {

	private static final long serialVersionUID = -1616895878900894757L;

	private static final int KIND_NONE = 0;
	private static final int KIND_INTEGER = 1;
	private static final int KIND_LONG = 2;
	private static final int KIND_DOUBLE = 3;
	private static final int KIND_DECIMAL = 4;
	private static final int KIND_DATETIME = 5;
	private static final int KIND_STRING = 6;
	private static final int KIND_BOOLEAN = 7;

	public static Query call(PageContext pc, Query query) throws PageException {
		return call(pc, query, false);
	}

	public static Query call(PageContext pc, Query query, boolean inline) throws PageException {
		final boolean fullNull = NullSupportHelper.full(pc);
		Collection.Key[] columnNames = query.getColumnNames();

		// first convert all values into a buffer, so a failing conversion never leaves a partly converted query
		Object[][] converted = new Object[columnNames.length][];
		for (int c = 0; c < columnNames.length; c++) {
			QueryColumn column = query.getColumn(columnNames[c]);
			int sqlType = column.getType();
			int kind = toKind(sqlType);
			// columns without a convertible type (e.g. OTHER/OBJECT) are left untouched
			if (kind == KIND_NONE) continue;

			int len = column.size();
			Object[] values = new Object[len];
			for (int row = 1; row <= len; row++) {
				// null stays null
				Object value = column.get(row, null);
				if (value == null) {
					values[row - 1] = null;
					continue;
				}
				if (kind != KIND_STRING && value instanceof CharSequence && ((CharSequence) value).length() == 0) {
					values[row - 1] = fullNull ? null : value;
					continue;
				}
				try {
					values[row - 1] = convert(pc, kind, value);
				}
				catch (Exception e) {
					throw new DatabaseException("queryConvertTypes: cannot convert the value " + describe(value) + " in column [" + columnNames[c].getString() + "], row [" + row
							+ "] to the column type [" + column.getTypeAsString() + "]", e.getMessage(), null, null);
				}
			}
			converted[c] = values;
		}

		Query target = inline ? query : (Query) query.duplicate(true);
		for (int c = 0; c < columnNames.length; c++) {
			if (converted[c] == null) continue;
			QueryColumn column = target.getColumn(columnNames[c]);
			Object[] values = converted[c];
			for (int row = 1; row <= values.length; row++) {
				column.set(row, values[row - 1]);
			}
		}
		return target;
	}

	private static Object convert(PageContext pc, int kind, Object value) throws PageException {
		switch (kind) {
		case KIND_INTEGER:
			try {
				return Integer.valueOf(Caster.toBigDecimal(value).intValueExact());
			}
			catch (ArithmeticException ae) {
				throw new ExpressionException("the value is not a whole number within the range of an integer");
			}
		case KIND_LONG:
			try {
				return Long.valueOf(Caster.toBigDecimal(value).longValueExact());
			}
			catch (ArithmeticException ae) {
				throw new ExpressionException("the value is not a whole number within the range of a long");
			}
		case KIND_DOUBLE:
			return Caster.toDouble(value);
		case KIND_DECIMAL:
			return Caster.toBigDecimal(value);
		case KIND_DATETIME:
			return Caster.toDatetime(value, pc.getTimeZone());
		case KIND_STRING:
			return Caster.toString(value);
		case KIND_BOOLEAN:
			return Caster.toBoolean(value);
		default:
			return value;
		}
	}

	private static int toKind(int sqlType) {
		switch (sqlType) {
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.TINYINT:
			return KIND_INTEGER;
		case Types.BIGINT:
			return KIND_LONG;
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.REAL:
		case Types.NUMERIC:
			return KIND_DOUBLE;
		case Types.DECIMAL:
			return KIND_DECIMAL;
		case Types.DATE:
		case Types.TIME:
		case Types.TIMESTAMP:
			return KIND_DATETIME;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCHAR:
		case Types.NVARCHAR:
		case Types.LONGNVARCHAR:
		case CFTypes.VARCHAR2:
			return KIND_STRING;
		case Types.BIT:
		case Types.BOOLEAN:
			return KIND_BOOLEAN;
		default:
			return KIND_NONE;
		}
	}

	private static String describe(Object value) {
		if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
			String str = value.toString();
			if (str.length() > 50) str = StringUtil.max(str, 50) + "...";
			return "[" + str + "]";
		}
		return "of type [" + value.getClass().getName() + "]";
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toQuery(args[0]));
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toBooleanValue(args[1]));
		throw new FunctionException(pc, "QueryConvertTypes", 1, 2, args.length);
	}
}
