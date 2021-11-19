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
package lucee.runtime.type;

import java.io.Serializable;
import java.sql.Types;
import java.util.Date;

import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

/**
 * Helper class for the QueryColumnImpl
 */
public final class QueryColumnUtil implements Serializable {

	private static final long serialVersionUID = 4654833724194716718L;

	/**
	 * reset the type of the column
	 */
	protected static void resetType(QueryColumnImpl column) {
		column.type = Types.OTHER;
	}

	/**
	 * redefine type of value
	 * 
	 * @param value
	 * @return redefined type of the value
	 */
	protected static Object reDefineType(QueryColumnImpl column, Object value) {
		column.typeChecked = false;
		if (value == null || column.type == Types.OTHER) return value;
		if (value instanceof String && ((String) value).isEmpty()) return value;

		switch (column.type) {

		// Numeric Values
		case Types.DOUBLE:
			return reDefineDouble(column, value);
		case Types.BIGINT:
			return reDefineDecimal(column, value);
		case Types.NUMERIC:
			return reDefineDouble(column, value);
		case Types.INTEGER:
			return reDefineInteger(column, value);
		case Types.TINYINT:
			return reDefineTinyInt(column, value);
		case Types.FLOAT:
			return reDefineFloat(column, value);
		case Types.DECIMAL:
			return reDefineDecimal(column, value);
		case Types.REAL:
			return reDefineFloat(column, value);
		case Types.SMALLINT:
			return reDefineShort(column, value);

		// DateTime Values
		case Types.TIMESTAMP:
			return reDefineDateTime(column, value);
		case Types.DATE:
			return reDefineDateTime(column, value);
		case Types.TIME:
			return reDefineDateTime(column, value);

		// Char
		case Types.CHAR:
			return reDefineString(column, value);
		case Types.VARCHAR:
			return reDefineString(column, value);
		case Types.LONGVARCHAR:
			return reDefineString(column, value);
		case Types.CLOB:
			return reDefineClob(column, value);

		// Boolean
		case Types.BOOLEAN:
			return reDefineBoolean(column, value);
		case Types.BIT:
			return reDefineBoolean(column, value);

		// Binary
		case Types.BINARY:
			return reDefineBinary(column, value);
		case Types.VARBINARY:
			return reDefineBinary(column, value);
		case Types.LONGVARBINARY:
			return reDefineBinary(column, value);
		case Types.BLOB:
			return reDefineBlob(column, value);

		// Others
		case Types.ARRAY:
			return reDefineOther(column, value);
		case Types.DATALINK:
			return reDefineOther(column, value);
		case Types.DISTINCT:
			return reDefineOther(column, value);
		case Types.JAVA_OBJECT:
			return reDefineOther(column, value);
		case Types.NULL:
			return reDefineOther(column, value);
		case Types.STRUCT:
			return reDefineOther(column, value);
		case Types.REF:
			return reDefineOther(column, value);
		default:
			return value;
		}

	}

	private static Object reDefineBoolean(QueryColumnImpl column, Object value) {
		if (Decision.isCastableToBoolean(value)) return value;

		resetType(column);
		return value;
	}

	private static Object reDefineDouble(QueryColumnImpl column, Object value) {
		if (Decision.isCastableToNumeric(value)) return value;

		resetType(column);
		return value;
	}

	private static Object reDefineFloat(QueryColumnImpl column, Object value) {
		if (Decision.isCastableToNumeric(value)) return value;
		resetType(column);
		return value;
	}

	private static Object reDefineInteger(QueryColumnImpl column, Object value) {
		if (Decision.isCastableToNumeric(value)) return value;
		resetType(column);
		return value;
	}

	private static Object reDefineShort(QueryColumnImpl column, Object value) {

		double dbl = Caster.toDoubleValue(value, true, Double.NaN);
		if (Decision.isValid(dbl)) {
			short sht = (short) dbl;
			if (sht == dbl) return value;

			column.type = Types.DOUBLE;
			return value;
		}
		resetType(column);
		return value;
	}

	private static Object reDefineTinyInt(QueryColumnImpl column, Object value) {
		if (Decision.isCastableToNumeric(value)) return value;
		resetType(column);
		return value;
	}

	private static Object reDefineDecimal(QueryColumnImpl column, Object value) {
		if (Decision.isCastableToNumeric(value)) return value;
		resetType(column);
		return value;
	}

	private static Object reDefineDateTime(QueryColumnImpl column, Object value) {
		if (Decision.isDateSimple(value, true)) return value;
		resetType(column);
		return value;
	}

	private static Object reDefineString(QueryColumnImpl column, Object value) {
		if (Decision.isCastableToString(value)) return value;
		resetType(column);
		return value;
	}

	private static Object reDefineClob(QueryColumnImpl column, Object value) {
		if (Decision.isCastableToString(value)) return value;
		resetType(column);
		return value;
	}

	private static Object reDefineBinary(QueryColumnImpl column, Object value) {
		if (Decision.isCastableToBinary(value, false)) return value;

		resetType(column);
		return value;

	}

	private static Object reDefineBlob(QueryColumnImpl column, Object value) {
		if (Decision.isCastableToBinary(value, false)) return value;
		resetType(column);
		return value;

	}

	private static Object reDefineOther(QueryColumnImpl column, Object value) {
		resetType(column);
		return value;
	}

	/**
	 * reorganize type of a column
	 * 
	 * @param reorganize
	 */
	protected static void reOrganizeType(QueryColumnImpl column) {
		if ((column.type == Types.OTHER) && !column.typeChecked) {
			column.typeChecked = true;
			if (column.size() > 0) {
				checkOther(column, column.data[0]);

				// get Type
				for (int i = 1; i < column.size(); i++) {
					switch (column.type) {
					case Types.NULL:
						checkOther(column, column.data[i]);
						break;
					case Types.TIMESTAMP:
						checkDate(column, column.data[i]);
						break;
					// case Types.DATE:checkDate(column.data[i]);break;
					case Types.BOOLEAN:
						checkBoolean(column, column.data[i]);
						break;
					case Types.DOUBLE:
						checkDouble(column, column.data[i]);
						break;
					case Types.VARCHAR:
						checkBasic(column, column.data[i]);
						break;
					default:
						break;
					}
				}
			}
		}
	}

	private static void checkOther(QueryColumnImpl column, Object value) {
		// NULL
		if (value == null) {
			column.type = Types.NULL;
			return;
		}
		// DateTime
		if (Decision.isDateSimple(value, false)) {
			column.type = Types.TIMESTAMP;
			return;
		}
		// Boolean
		if (Decision.isBoolean(value)) {
			column.type = Types.BOOLEAN;
			return;
		}
		// Double
		if (Decision.isNumber(value)) {
			column.type = Types.DOUBLE;
			return;
		}
		// String
		String str = Caster.toString(value, null);
		if (str != null) {
			column.type = Types.VARCHAR;
			return;
		}
	}

	private static void checkDate(QueryColumnImpl column, Object value) {
		// NULL
		if (value == null) return;
		// DateTime
		if (Decision.isDateSimple(value, false)) {
			column.type = Types.TIMESTAMP;
			return;
		}
		// String
		String str = Caster.toString(value, null);
		if (str != null) {
			column.type = Types.VARCHAR;
			return;
		}
		// Other
		column.type = Types.OTHER;
		return;
	}

	private static void checkBoolean(QueryColumnImpl column, Object value) {
		// NULL
		if (value == null) return;
		// Boolean
		if (Decision.isBoolean(value)) {
			column.type = Types.BOOLEAN;
			return;
		}
		// Double
		if (Decision.isNumber(value)) {
			column.type = Types.DOUBLE;
			return;
		}
		// String
		String str = Caster.toString(value, null);
		if (str != null) {
			column.type = Types.VARCHAR;
			return;
		}
		// Other
		column.type = Types.OTHER;
		return;
	}

	private static void checkDouble(QueryColumnImpl column, Object value) {
		// NULL
		if (value == null) return;
		// Double
		if (Decision.isNumber(value)) {
			column.type = Types.DOUBLE;
			return;
		}
		// String
		String str = Caster.toString(value, null);
		if (str != null) {
			column.type = Types.VARCHAR;
			return;
		}
		// Other
		column.type = Types.OTHER;
		return;
	}

	private static void checkBasic(QueryColumnImpl column, Object value) {
		// NULL
		if (value == null) return;
		// Date
		if (value instanceof Date || value instanceof Number) return;
		// String
		String str = Caster.toString(value, null);
		if (str != null) {
			return;
		}
		// OTHER
		column.type = Types.OTHER;
		return;
	}
}