/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.db;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.TimeZone;

import lucee.commons.date.JREDateTimeUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.sql.SQLUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.dt.DateImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeImpl;
import lucee.runtime.type.sql.BlobImpl;
import lucee.runtime.type.sql.ClobImpl;

/**
 * SQL Caster
 */
public final class SQLCaster {

	private SQLCaster() {
	}

	/**
	 * 
	 * sets a Value to a PreparedStatement
	 * 
	 * @param stat
	 * @param parameterIndex
	 * @param item
	 * @throws SQLException
	 * @throws PageException
	 * @throws DatabaseException
	 */

	public static Object toSqlType(SQLItem item) throws PageException, DatabaseException {
		Object value = item.getValue();
		try {

			if (item.isNulls() || value == null) {
				return null;
			}
			int type = item.getType();
			switch (type) {
			case Types.BIGINT:
				return Caster.toLong(value);
			case Types.BIT:
				return Caster.toBoolean(value);
			case Types.BLOB:
				return BlobImpl.toBlob(value);
			case Types.CHAR:
				return Caster.toString(value);
			case Types.CLOB:
			case Types.NCLOB:
				return ClobImpl.toClob(value);
			case Types.DATE:
				return new Date(Caster.toDate(value, null).getTime());
			case Types.NUMERIC:
			case Types.DECIMAL:
				return new BigDecimal(Caster.toDouble(value).toString());
			case Types.DOUBLE:
				return Caster.toDouble(value);
			case Types.FLOAT:
				return Caster.toFloat(value);
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			case Types.BINARY:
				return Caster.toBinary(value);
			case Types.REAL:
				return Caster.toFloat(value);
			case Types.TINYINT:
				return Caster.toByte(value);
			case Types.SMALLINT:
				return Caster.toShort(value);
			case Types.INTEGER:
				return Caster.toInteger(value);
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			case CFTypes.VARCHAR2:
			case Types.NCHAR:
			case Types.LONGNVARCHAR:
			case Types.NVARCHAR:
			case Types.SQLXML:
				return Caster.toString(value);
			case Types.TIME:
				return new Time(Caster.toDate(value, null).getTime());
			case Types.TIMESTAMP:
				return new Timestamp(Caster.toDate(value, null).getTime());
			case Types.OTHER:
			default:
				if (value instanceof DateTime) return new Date(Caster.toDate(value, null).getTime());
				if (value instanceof lucee.runtime.type.Array) return Caster.toList(value);
				if (value instanceof lucee.runtime.type.Struct) return Caster.toMap(value);

				return value;// toSQLObject(value); TODO alle lucee spezifischen typen sollten in
								// sql typen uebersetzt werden
			}
		}
		catch (PageException pe) {
			if (!NullSupportHelper.full() && value instanceof String && StringUtil.isEmpty((String) value)) return null;
			throw pe;
		}
	}

	public static void setValue(PageContext pc, TimeZone tz, PreparedStatement stat, int parameterIndex, SQLItem item) throws PageException, SQLException, DatabaseException {
		pc = ThreadLocalPageContext.get(pc);
		Object value = item.getValue();
		if (item.isNulls() || value == null) {
			stat.setNull(parameterIndex, item.getType());
			return;
		}
		int type = item.getType();
		boolean fns = NullSupportHelper.full(pc);
		switch (type) {
		/*
		 * case Types.ARRAY: stat.setArray(parameterIndex,toArray(item.getValue())); return;
		 */
		case Types.BIGINT:
			try {
				stat.setLong(parameterIndex, Caster.toLongValue(value));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;
		case Types.BIT:
			try {
				stat.setBoolean(parameterIndex, Caster.toBooleanValue(value));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;
		case Types.BLOB:
			try {
				stat.setBlob(parameterIndex, SQLUtil.toBlob(stat.getConnection(), value));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;
		case Types.CLOB:
			try {
				stat.setClob(parameterIndex, SQLUtil.toClob(stat.getConnection(), value));
				/*
				 * if(value instanceof String) { try{ stat.setString(parameterIndex,Caster.toString(value)); }
				 * catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);
				 * stat.setClob(parameterIndex,SQLUtil.toClob(stat.getConnection(),value)); }
				 * 
				 * } else stat.setClob(parameterIndex,SQLUtil.toClob(stat.getConnection(),value));
				 */
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;
		case Types.CHAR:
			String str = Caster.toString(value);
			// if(str!=null && str.length()==0) str=null;
			stat.setObject(parameterIndex, str, type);
			//// stat.setString(parameterIndex,str);
			return;
		case Types.DECIMAL:
		case Types.NUMERIC:
			try {
				stat.setDouble(parameterIndex, (Caster.toDoubleValue(value)));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;

		case Types.DOUBLE:
		case Types.FLOAT:
			try {
				if (type == Types.FLOAT) stat.setFloat(parameterIndex, Caster.toFloatValue(value));
				else if (type == Types.DOUBLE) stat.setDouble(parameterIndex, Caster.toDoubleValue(value));
				else stat.setObject(parameterIndex, Caster.toDouble(value), type);
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
		case Types.BINARY:
			try {
				stat.setObject(parameterIndex, Caster.toBinary(value), type);
				//// stat.setBytes(parameterIndex,Caster.toBinary(value));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;
		case Types.REAL:
			try {
				stat.setObject(parameterIndex, Caster.toFloat(value), type);
				//// stat.setFloat(parameterIndex,Caster.toFloatValue(value));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;
		case Types.TINYINT:
			try {
				stat.setObject(parameterIndex, Caster.toByte(value), type);
				//// stat.setByte(parameterIndex,Caster.toByteValue(value));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;
		case Types.SMALLINT:
			try {
				stat.setObject(parameterIndex, Caster.toShort(value), type);
				//// stat.setShort(parameterIndex,Caster.toShortValue(value));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;
		case Types.INTEGER:
			try {
				stat.setObject(parameterIndex, Caster.toInteger(value), type);
				//// stat.setInt(parameterIndex,Caster.toIntValue(value));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;

		case Types.SQLXML:
			SQLXML xml = stat.getConnection().createSQLXML();
			xml.setString(Caster.toString(value));
			stat.setObject(parameterIndex, xml, type);
			return;
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.LONGNVARCHAR:
		case Types.NVARCHAR:
		case CFTypes.VARCHAR2:
			stat.setObject(parameterIndex, Caster.toString(value), type);
			//// stat.setString(parameterIndex,Caster.toString(value));
			return;
		case Types.DATE:
			try {
				stat.setDate(parameterIndex, new Date(Caster.toDate(value, tz).getTime()), JREDateTimeUtil.getThreadCalendar(tz));

				// stat.setDate(parameterIndex,new Date((Caster.toDate(value,null).getTime())));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;
		case Types.TIME:
			try {

				// stat.setObject(parameterIndex, new Time((Caster.toDate(value,null).getTime())),
				// type);
				stat.setTime(parameterIndex, new Time(Caster.toDate(value, tz).getTime()), JREDateTimeUtil.getThreadCalendar(tz));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}
			return;
		case Types.TIMESTAMP:
			try {
				// stat.setObject(parameterIndex, new
				// Timestamp((Caster.toDate(value,null).getTime())), type);
				// stat.setObject(parameterIndex, value, type);
				stat.setTimestamp(parameterIndex, new Timestamp(Caster.toDate(value, tz).getTime()), JREDateTimeUtil.getThreadCalendar(tz));
			}
			catch (PageException pe) {
				if (!fns && value instanceof String && StringUtil.isEmpty((String) value)) stat.setNull(parameterIndex, item.getType());
				else throw pe;
			}

			return;
		case Types.OTHER:
			stat.setObject(parameterIndex, value, Types.OTHER);
			return;
		/*
		 * case CF_SQL_STRUCT: case CF_SQL_REFCURSOR: case CF_SQL_NULL: case CF_SQL_ARRAY: case
		 * CF_SQL_DISTINCT:
		 */
		default:
			stat.setObject(parameterIndex, value, type);
			// throw new DatabaseException(toStringType(item.getType())+" is not a supported
			// Type",null,null);

		}
	}

	/**
	 * Cast a SQL Item to a String (Display) Value
	 * 
	 * @param item
	 * @return String Value
	 */
	public static String toString(SQLItem item) {
		try {
			return _toString(item);
		}
		catch (PageException e) {
			try {
				return "[" + toStringType(item.getType()) + "]";
			}
			catch (DatabaseException e1) {
				return "";
			}
		}
	}

	private static String _toString(SQLItem item) throws PageException {
		int type = item.getType();

		// string types
		if (type == Types.VARCHAR || type == Types.LONGVARCHAR || type == Types.CHAR || type == Types.CLOB || type == Types.NVARCHAR || type == Types.NCHAR || type == Types.SQLXML
				|| type == Types.NCLOB || type == Types.LONGNVARCHAR) {
			return (matchString(item));
		}
		// long types
		else if (type == Types.BIGINT) {
			return Caster.toString(Caster.toLongValue(item.getValue()));
		}
		// int types
		else if (type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT) {
			return Caster.toString(Caster.toIntValue(item.getValue()));
		}
		// numeric types
		else if (type == Types.DECIMAL || type == Types.NUMERIC || type == Types.DOUBLE || type == Types.FLOAT) {
			return (Caster.toString(Caster.toDoubleValue(item.getValue())));
		}
		// time types
		else if (type == Types.TIME) {
			return new TimeImpl(DateCaster.toDateAdvanced(item.getValue(), null)).castToString();
		}
		// date types
		else if (type == Types.DATE) {
			return new DateImpl(DateCaster.toDateAdvanced(item.getValue(), null)).castToString();
		}
		// time types
		else if (type == Types.TIMESTAMP) {
			return DateCaster.toDateAdvanced(item.getValue(), null).castToString();
		}
		// invalid type
		else {
			return Caster.toString(item.getValue());
		}
	}

	/*
	 * private static String toString(Clob clob) throws SQLException, IOException { Reader in =
	 * clob.getCharacterStream(); StringBuilder buf = new StringBuilder(); for(int c=in.read();c != -1;c
	 * = in.read()) { buf.append((char)c); } return buf.toString(); }
	 */

	/**
	 * cast a Value to a correspondance CF Type
	 * 
	 * @param item
	 * @return cf type
	 * @throws PageException
	 */

	public static Object toCFTypex(SQLItem item) throws PageException {
		try {
			return _toCFTypex(item);
		}
		catch (PageException e) {
			if (item.isNulls()) return item.getValue();
			throw e;
		}
	}

	public static Object toCFTypeEL(SQLItem item) {
		try {
			return _toCFTypex(item);
		}
		catch (PageException e) {
			return item.getValue();
		}
	}

	private static Object _toCFTypex(SQLItem item) throws PageException {

		int type = item.getType();
		// char varchar
		if (type == Types.VARCHAR || type == Types.LONGVARCHAR || type == CFTypes.VARCHAR2 || type == Types.NVARCHAR || type == Types.LONGNVARCHAR || type == Types.SQLXML) {
			return Caster.toString(item.getValue());
		}
		// char types
		else if (type == Types.CHAR || type == Types.NCHAR) {
			return Caster.toString(item.getValue());
		}
		// long types
		else if (type == Types.BIGINT) {
			return Caster.toLong(item.getValue());
		}
		// int types
		else if (type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT) {
			return Caster.toInteger(item.getValue());
		}
		// numeric types
		else if (type == Types.DOUBLE || type == Types.FLOAT || type == Types.NUMERIC || type == Types.DECIMAL) {
			return Caster.toDouble(item.getValue());
		}
		// time types
		else if (type == Types.TIME) {
			return new TimeImpl(DateCaster.toDateAdvanced(item.getValue(), null)).castToString();
		}
		// date types
		else if (type == Types.DATE) {
			return new DateImpl(DateCaster.toDateAdvanced(item.getValue(), null)).castToString();
		}
		// time types
		else if (type == Types.TIMESTAMP) {
			return DateCaster.toDateAdvanced(item.getValue(), null).castToString();
		}
		// invalid type
		else {
			return item.getValue();
		}
	}

	public static Object toCFType(Object value, Object defaultValue) {
		try {
			if (value instanceof Clob) {
				return IOUtil.toString(((Clob) value).getCharacterStream());
			}
			else if (value instanceof Blob) {
				return IOUtil.toBytes(((Blob) value).getBinaryStream());
			}
			else if (value instanceof Array) {
				return ((java.sql.Array) value).getArray();
			}
			else return value;
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static Object toCFType(Object value) throws PageException {
		try {
			if (value instanceof Clob) {
				return IOUtil.toString(((Clob) value).getCharacterStream());
			}
			else if (value instanceof Blob) {
				return IOUtil.toBytes(((Blob) value).getBinaryStream());
			}
			else if (value instanceof Array) {
				return ((java.sql.Array) value).getArray();
			}
			else if (value instanceof ResultSet) {
				return new QueryImpl((ResultSet) value, "query", null);
			}
			else return value;
		}
		catch (SQLException e) {
			throw new DatabaseException(e, null);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	public static Object toCFType(Object value, int type) throws PageException {
		// char varchar
		if (type == Types.VARCHAR || type == Types.LONGVARCHAR || type == CFTypes.VARCHAR2 || type == Types.NVARCHAR || type == Types.LONGNVARCHAR || type == Types.SQLXML) {
			return Caster.toString(value);
		}
		// char types
		else if (type == Types.CHAR || type == Types.NCHAR) {
			return Caster.toString(value);
		}
		// long types
		else if (type == Types.BIGINT) {
			return Caster.toLong(value);
		}
		// int types
		else if (type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT) {
			return Caster.toInteger(value);
		}
		// numeric types
		else if (type == Types.DOUBLE || type == Types.FLOAT || type == Types.NUMERIC || type == Types.DECIMAL) {
			return Caster.toDouble(value);
		}
		// time types
		else if (type == Types.TIME) {
			return new TimeImpl(DateCaster.toDateAdvanced(value, null)).castToString();
		}
		// time types
		else if (type == Types.DATE) {
			return new DateImpl(DateCaster.toDateAdvanced(value, null)).castToString();
		}
		// time types
		else if (type == Types.TIMESTAMP) {
			return DateCaster.toDateAdvanced(value, null).castToString();
		}
		// invalid type
		else {
			return value;
		}
	}

	private static String matchString(SQLItem item) throws PageException {
		String str = StringUtil.replace(Caster.toString(item.getValue()), "'", "''", false);
		return "'" + str + "'";
	}

	/**
	 * returns CF SQL Type as String
	 * 
	 * @param type
	 * @return SQL Type as String
	 * @throws DatabaseException
	 */
	public static String toStringType(int type, String defaultValue) {
		switch (type) {
		case Types.ARRAY:
			return "CF_SQL_ARRAY";
		case Types.BIGINT:
			return "CF_SQL_BIGINT";
		case Types.BINARY:
			return "CF_SQL_BINARY";
		case Types.BIT:
			return "CF_SQL_BIT";
		case Types.BOOLEAN:
			return "CF_SQL_BOOLEAN";
		case Types.BLOB:
			return "CF_SQL_BLOB";
		case Types.CHAR:
			return "CF_SQL_CHAR";
		case Types.NCLOB:
			return "CF_SQL_NCLOB";
		case Types.SQLXML:
			return "CF_SQL_SQLXML";
		case Types.NCHAR:
			return "CF_SQL_NCHAR";
		case Types.CLOB:
			return "CF_SQL_CLOB";
		case Types.DATALINK:
			return "CF_SQL_DATALINK";
		case Types.DATE:
			return "CF_SQL_DATE";
		case Types.DISTINCT:
			return "CF_SQL_DISTINCT";
		case Types.NUMERIC:
			return "CF_SQL_NUMERIC";
		case Types.DECIMAL:
			return "CF_SQL_DECIMAL";
		case Types.DOUBLE:
			return "CF_SQL_DOUBLE";
		case Types.REAL:
			return "CF_SQL_REAL";
		case Types.FLOAT:
			return "CF_SQL_FLOAT";
		case Types.TINYINT:
			return "CF_SQL_TINYINT";
		case Types.SMALLINT:
			return "CF_SQL_SMALLINT";
		case Types.STRUCT:
			return "CF_SQL_STRUCT";
		case Types.INTEGER:
			return "CF_SQL_INTEGER";
		case Types.VARCHAR:
			return "CF_SQL_VARCHAR";
		case Types.NVARCHAR:
			return "CF_SQL_NVARCHAR";
		case Types.LONGNVARCHAR:
			return "CF_SQL_LONGNVARCHAR";
		case CFTypes.VARCHAR2:
			return "CF_SQL_VARCHAR2";
		case Types.LONGVARBINARY:
			return "CF_SQL_LONGVARBINARY";
		case Types.VARBINARY:
			return "CF_SQL_VARBINARY";
		case Types.LONGVARCHAR:
			return "CF_SQL_LONGVARCHAR";
		case Types.TIME:
			return "CF_SQL_TIME";
		case Types.TIMESTAMP:
			return "CF_SQL_TIMESTAMP";
		case Types.REF:
			return "CF_SQL_REF";
		case CFTypes.CURSOR:
			return "CF_SQL_REFCURSOR";
		case Types.OTHER:
			return "CF_SQL_OTHER";
		case Types.NULL:
			return "CF_SQL_NULL";

		default:
			return null;
		}
	}

	public static String toStringType(int type) throws DatabaseException {
		String rtn = toStringType(type, null);
		if (rtn != null) return rtn;

		throw new DatabaseException("invalid CF SQL Type", null, null, null);
	}

	/*
	 * * cast a String SQL Type to int Type
	 * 
	 * @param strType
	 * 
	 * @return SQL Type as int
	 * 
	 * @throws DatabaseException
	 */
	/*
	 * public static int cfSQLTypeToIntType(String strType) throws DatabaseException {
	 * strType=strType.toUpperCase().trim();
	 * 
	 * if(strType.equals("CF_SQL_ARRAY")) return Types.ARRAY; else if(strType.equals("CF_SQL_BIGINT"))
	 * return Types.BIGINT; else if(strType.equals("CF_SQL_BINARY")) return Types.BINARY; else
	 * if(strType.equals("CF_SQL_BIT")) return Types.BIT; else if(strType.equals("CF_SQL_BLOB")) return
	 * Types.BLOB; else if(strType.equals("CF_SQL_BOOLEAN")) return Types.BOOLEAN; else
	 * if(strType.equals("CF_SQL_CHAR")) return Types.CHAR; else if(strType.equals("CF_SQL_CLOB"))
	 * return Types.CLOB; else if(strType.equals("CF_SQL_DATALINK")) return Types.DATALINK; else
	 * if(strType.equals("CF_SQL_DATE")) return Types.DATE; else if(strType.equals("CF_SQL_DISTINCT"))
	 * return Types.DISTINCT; else if(strType.equals("CF_SQL_DECIMAL")) return Types.DECIMAL; else
	 * if(strType.equals("CF_SQL_DOUBLE")) return Types.DOUBLE; else if(strType.equals("CF_SQL_FLOAT"))
	 * return Types.FLOAT; else if(strType.equals("CF_SQL_IDSTAMP")) return CFTypes.IDSTAMP; else
	 * if(strType.equals("CF_SQL_INTEGER")) return Types.INTEGER; else if(strType.equals("CF_SQL_INT"))
	 * return Types.INTEGER; else if(strType.equals("CF_SQL_LONGVARBINARY"))return Types.LONGVARBINARY;
	 * else if(strType.equals("CF_SQL_LONGVARCHAR"))return Types.LONGVARCHAR; else
	 * if(strType.equals("CF_SQL_MONEY")) return Types.DOUBLE; else if(strType.equals("CF_SQL_MONEY4"))
	 * return Types.DOUBLE; else if(strType.equals("CF_SQL_NUMERIC")) return Types.NUMERIC; else
	 * if(strType.equals("CF_SQL_NULL")) return Types.NULL; else if(strType.equals("CF_SQL_REAL"))
	 * return Types.REAL; else if(strType.equals("CF_SQL_REF")) return Types.REF; else
	 * if(strType.equals("CF_SQL_REFCURSOR")) return CFTypes.CURSOR; else
	 * if(strType.equals("CF_SQL_OTHER")) return Types.OTHER; else if(strType.equals("CF_SQL_SMALLINT"))
	 * return Types.SMALLINT; else if(strType.equals("CF_SQL_STRUCT")) return Types.STRUCT; else
	 * if(strType.equals("CF_SQL_TIME")) return Types.TIME; else if(strType.equals("CF_SQL_TIMESTAMP"))
	 * return Types.TIMESTAMP; else if(strType.equals("CF_SQL_TINYINT")) return Types.TINYINT; else
	 * if(strType.equals("CF_SQL_VARBINARY")) return Types.VARBINARY; else
	 * if(strType.equals("CF_SQL_VARCHAR")) return Types.VARCHAR; else
	 * if(strType.equals("CF_SQL_NVARCHAR")) return Types.NVARCHAR; else
	 * if(strType.equals("CF_SQL_VARCHAR2")) return CFTypes.VARCHAR2;
	 * 
	 * 
	 * else throw new DatabaseException("invalid CF SQL Type ["+strType+"]",null,null,null); }
	 */

	/**
	 * cast a String SQL Type, e.g. from cfqueryparam, to int Type
	 * 
	 * @param strType
	 * @return SQL Type as int
	 * @throws DatabaseException
	 */
	public static int toSQLType(String strType) throws DatabaseException {
		strType = strType.toUpperCase().trim();
		if (strType.startsWith("CF_SQL_")) strType = strType.substring(7);
		if (strType.startsWith("SQL_")) strType = strType.substring(4);

		if (strType.length() > 2) {
			char first = strType.charAt(0);
			if (first == 'A') {
				if (strType.equals("ARRAY")) return Types.ARRAY;
			}
			else if (first == 'B') {
				if (strType.equals("BIGINT")) return Types.BIGINT;
				else if (strType.equals("BINARY")) return Types.BINARY;
				else if (strType.equals("BIT")) return Types.BIT;
				else if (strType.equals("BLOB")) return Types.BLOB;
				else if (strType.equals("BOOLEAN")) return Types.BOOLEAN;
				else if (strType.equals("BOOL")) return Types.BOOLEAN;
			}
			else if (first == 'C') {
				if (strType.equals("CLOB")) return Types.CLOB;
				else if (strType.equals("CHAR")) return Types.CHAR;
				else if (strType.equals("CLOB")) return Types.CLOB;
				else if (strType.equals("CURSOR")) return CFTypes.CURSOR;
			}
			else if (first == 'D') {
				if (strType.equals("DATALINK")) return Types.DATALINK;
				else if (strType.equals("DATE")) return Types.DATE;
				else if (strType.equals("DATETIME")) return Types.TIMESTAMP;
				else if (strType.equals("DISTINCT")) return Types.DISTINCT;
				else if (strType.equals("DECIMAL")) return Types.DECIMAL;
				else if (strType.equals("DOUBLE")) return Types.DOUBLE;
			}
			else if (first == 'F') {
				if (strType.equals("FLOAT")) return Types.FLOAT;
			}
			else if (first == 'I') {
				if (strType.equals("IDSTAMP")) return CFTypes.IDSTAMP;
				else if (strType.equals("INTEGER")) return Types.INTEGER;
				else if (strType.equals("INT")) return Types.INTEGER;
			}
			else if (first == 'L') {
				// if(strType.equals("LONG"))return Types.INTEGER;
				if (strType.equals("LONGVARBINARY")) return Types.LONGVARBINARY;
				else if (strType.equals("LONGVARCHAR")) return Types.LONGVARCHAR;
				else if (strType.equals("LONGNVARCHAR")) return Types.LONGNVARCHAR;
			}
			else if (first == 'M') {
				if (strType.equals("MONEY")) return Types.DOUBLE;
				else if (strType.equals("MONEY4")) return Types.DOUBLE;
			}
			else if (first == 'N') {
				if (strType.equals("NUMERIC")) return Types.NUMERIC;
				else if (strType.equals("NUMBER")) return Types.NUMERIC;
				else if (strType.equals("NULL")) return Types.NULL;
				else if (strType.equals("NCHAR")) return Types.NCHAR;
				else if (strType.equals("NCLOB")) return Types.NCLOB;
				else if (strType.equals("NVARCHAR")) return Types.NVARCHAR;

			}
			else if (first == 'O') {
				if (strType.equals("OTHER")) return Types.OTHER;
				else if ("OBJECT".equals(strType)) return Types.OTHER;
			}
			else if (first == 'R') {
				if (strType.equals("REAL")) return Types.REAL;
				else if (strType.equals("REF")) return Types.REF;
				else if (strType.equals("REFCURSOR")) return CFTypes.CURSOR;
			}
			else if (first == 'S') {
				if (strType.equals("SMALLINT")) return Types.SMALLINT;
				else if (strType.equals("STRUCT")) return Types.STRUCT;
				else if (strType.equals("STRING")) return Types.VARCHAR;
				else if (strType.equals("SQLXML")) return Types.SQLXML;
			}
			else if (first == 'T') {
				if (strType.equals("TEXT")) return Types.VARCHAR;
				else if (strType.equals("TIME")) return Types.TIME;
				else if (strType.equals("TIMESTAMP")) return Types.TIMESTAMP;
				else if (strType.equals("TINYINT")) return Types.TINYINT;
			}
			else if (first == 'V') {
				if (strType.equals("VARBINARY")) return Types.VARBINARY;
				else if (strType.equals("VARCHAR")) return Types.VARCHAR;
				else if (strType.equals("VARCHAR2")) return CFTypes.VARCHAR2;
			}
		}

		throw new DatabaseException("invalid CF SQL Type [" + strType + "]", null, null, null);
	}

	public static short toCFType(int sqlType, short defaultValue) {
		switch (sqlType) {
		case Types.ARRAY:
			return lucee.commons.lang.CFTypes.TYPE_ARRAY;
		case Types.BIGINT:
			return lucee.commons.lang.CFTypes.TYPE_NUMERIC;
		case Types.LONGVARBINARY:
		case Types.VARBINARY:
		case Types.BLOB:
		case Types.BINARY:
			return lucee.commons.lang.CFTypes.TYPE_BINARY;
		case Types.BOOLEAN:
		case Types.BIT:
			return lucee.commons.lang.CFTypes.TYPE_BOOLEAN;
		case Types.LONGVARCHAR:
		case Types.NVARCHAR:
		case CFTypes.VARCHAR2:
		case Types.VARCHAR:
		case Types.CLOB:
		case Types.CHAR:
		case Types.NCLOB:
		case Types.LONGNVARCHAR:
		case Types.NCHAR:
			return lucee.commons.lang.CFTypes.TYPE_STRING;
		case Types.SQLXML:
			return lucee.commons.lang.CFTypes.TYPE_XML;

		case Types.TIME:
		case Types.TIMESTAMP:
		case Types.DATE:
			return lucee.commons.lang.CFTypes.TYPE_DATETIME;
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.TINYINT:
		case Types.FLOAT:
		case Types.REAL:
		case Types.DOUBLE:
		case Types.DECIMAL:
		case Types.NUMERIC:
			return lucee.commons.lang.CFTypes.TYPE_NUMERIC;

		default:
			return defaultValue;
		}

	}
}