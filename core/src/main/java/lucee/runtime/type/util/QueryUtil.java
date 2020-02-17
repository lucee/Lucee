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
package lucee.runtime.type.util;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.util.Date;

import lucee.commons.lang.FormatUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.sql.SQLUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.db.CFTypes;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.SQL;
import lucee.runtime.db.driver.PreparedStatementPro;
import lucee.runtime.db.driver.StatementPro;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpRow;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.functions.arrays.ArrayFind;
import lucee.runtime.op.Caster;
import lucee.runtime.query.caster.Cast;
import lucee.runtime.query.caster.OtherCast;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.QueryColumnImpl;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.query.SimpleQuery;

public class QueryUtil {

	public static Cast toCast(ResultSet result, int type) throws SQLException {
		if (type == Types.TIMESTAMP) {
			if (isTeradata(result)) return Cast.TIMESTAMP_NOTZ;
			return Cast.TIMESTAMP;
		}
		else if (type == Types.TIME) {
			if (isTeradata(result)) return Cast.TIME_NOTZ;
			return Cast.TIME;
		}
		else if (type == Types.DATE) {
			if (isTeradata(result)) return Cast.DATE_NOTZ;
			return Cast.DATE;
		}
		else if (type == Types.CLOB) return Cast.CLOB;
		else if (type == Types.BLOB) return Cast.BLOB;
		else if (type == Types.BIT) return Cast.BIT;
		else if (type == Types.ARRAY) return Cast.ARRAY;
		else if (type == Types.BIGINT) return Cast.BIGINT;

		// ORACLE
		else if (isOracleType(type) && isOracle(result)) {
			if (type == CFTypes.ORACLE_OPAQUE) return Cast.ORACLE_OPAQUE;
			else if (type == CFTypes.ORACLE_BLOB) return Cast.ORACLE_BLOB;
			else if (type == CFTypes.ORACLE_CLOB) return Cast.ORACLE_CLOB;
			else if (type == CFTypes.ORACLE_NCLOB) return Cast.ORACLE_NCLOB;
			else if (type == CFTypes.ORACLE_TIMESTAMPTZ) return Cast.ORACLE_TIMESTAMPTZ;
			else if (type == CFTypes.ORACLE_TIMESTAMPLTZ) return Cast.ORACLE_TIMESTAMPLTZ;
			else if (type == CFTypes.ORACLE_TIMESTAMPNS) return Cast.ORACLE_TIMESTAMPNS;

			/*
			 * TODO if(type==CFTypes.ORACLE_DISTINCT) return Cast.ORACLE_DISTINCT;
			 * if(type==CFTypes.ORACLE_JAVA_OBJECT) return Cast.ORACLE_JAVA_OBJECT; if(type==CFTypes.ORACLE_REF)
			 * return Cast.ORACLE_REF; if(type==CFTypes.ORACLE_STRUCT) return Cast.ORACLE_STRUCT;
			 */
		}
		return new OtherCast(type);
	}

	private static boolean isOracleType(int type) {
		switch (type) {
		case CFTypes.ORACLE_OPAQUE:
		case CFTypes.ORACLE_BLOB:
		case CFTypes.ORACLE_CLOB:
		case CFTypes.ORACLE_NCLOB:
		case CFTypes.ORACLE_DISTINCT:
		case CFTypes.ORACLE_JAVA_OBJECT:
		case CFTypes.ORACLE_REF:
		case CFTypes.ORACLE_STRUCT:
		case CFTypes.ORACLE_TIMESTAMPTZ:
		case CFTypes.ORACLE_TIMESTAMPLTZ:
		case CFTypes.ORACLE_TIMESTAMPNS:
			return true;
		}
		return false;
	}

	private static boolean isOracle(ResultSet result) {
		try {
			if (result == null) return false;

			Statement stat = result.getStatement();
			if (stat == null) return false;

			Connection conn = stat.getConnection();
			if (conn == null) return false;

			return SQLUtil.isOracle(conn);
		}
		catch (Exception e) {
			return false;
		}
	}

	private static boolean isTeradata(ResultSet result) {
		try {
			if (result == null) return false;

			Statement stat = result.getStatement();
			if (stat == null) return false;

			Connection conn = stat.getConnection();
			if (conn == null) return false;

			return SQLUtil.isTeradata(conn);
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * return column names as Key from a query
	 * 
	 * @param qry
	 * @return
	 */
	public static Key[] getColumnNames(Query qry) {
		Query qp = Caster.toQuery(qry, null);

		if (qp != null) return qp.getColumnNames();
		String[] strNames = qry.getColumns();
		Key[] names = new Key[strNames.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = KeyImpl.getInstance(strNames[i]);
		}
		return names;
	}

	public static String[] toStringArray(Collection.Key[] keys) {
		if (keys == null) return new String[0];
		String[] strKeys = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			strKeys[i] = keys[i].getString();
		}
		return strKeys;
	}

	/**
	 * check if there is a sql restriction
	 * 
	 * @param dc
	 * @param sql
	 * @throws PageException
	 */
	public static void checkSQLRestriction(DatasourceConnection dc, SQL sql) throws PageException {
		Array sqlparts = ListUtil.listToArrayRemoveEmpty(SQLUtil.removeLiterals(sql.getSQLString()), " \t" + System.getProperty("line.separator"));

		// print.ln(List.toStringArray(sqlparts));
		DataSource ds = dc.getDatasource();
		if (!ds.hasAllow(DataSource.ALLOW_ALTER)) checkSQLRestriction(dc, "alter", sqlparts, sql);
		if (!ds.hasAllow(DataSource.ALLOW_CREATE)) checkSQLRestriction(dc, "create", sqlparts, sql);
		if (!ds.hasAllow(DataSource.ALLOW_DELETE)) checkSQLRestriction(dc, "delete", sqlparts, sql);
		if (!ds.hasAllow(DataSource.ALLOW_DROP)) checkSQLRestriction(dc, "drop", sqlparts, sql);
		if (!ds.hasAllow(DataSource.ALLOW_GRANT)) checkSQLRestriction(dc, "grant", sqlparts, sql);
		if (!ds.hasAllow(DataSource.ALLOW_INSERT)) checkSQLRestriction(dc, "insert", sqlparts, sql);
		if (!ds.hasAllow(DataSource.ALLOW_REVOKE)) checkSQLRestriction(dc, "revoke", sqlparts, sql);
		if (!ds.hasAllow(DataSource.ALLOW_SELECT)) checkSQLRestriction(dc, "select", sqlparts, sql);
		if (!ds.hasAllow(DataSource.ALLOW_UPDATE)) checkSQLRestriction(dc, "update", sqlparts, sql);
	}

	private static void checkSQLRestriction(DatasourceConnection dc, String keyword, Array sqlparts, SQL sql) throws PageException {
		if (ArrayFind.find(sqlparts, keyword, false) > 0) {
			throw new DatabaseException("access denied to execute \"" + StringUtil.ucFirst(keyword) + "\" SQL statement for datasource " + dc.getDatasource().getName(), null, sql,
					dc);
		}
	}

	public static DumpData toDumpData(Query query, PageContext pageContext, int maxlevel, DumpProperties dp) {
		maxlevel--;
		Collection.Key[] keys = CollectionUtil.keys(query);
		DumpData[] heads = new DumpData[keys.length + 1];
		// int tmp=1;
		heads[0] = new SimpleDumpData("");
		for (int i = 0; i < keys.length; i++) {
			heads[i + 1] = new SimpleDumpData(keys[i].getString());
		}

		StringBuilder comment = new StringBuilder();

		// table.appendRow(1, new SimpleDumpData("SQL"), new SimpleDumpData(sql.toString()));
		String template = query.getTemplate();
		if (!StringUtil.isEmpty(template)) comment.append("Template: ").append(template).append("\n");
		// table.appendRow(1, new SimpleDumpData("Template"), new SimpleDumpData(template));

		int top = dp.getMaxlevel(); // in Query dump maxlevel is used as Top

		comment.append("Execution Time: ").append(Caster.toString(FormatUtil.formatNSAsMSDouble(query.getExecutionTime()))).append(" ms \n");
		comment.append("Record Count: ").append(Caster.toString(query.getRecordcount()));
		if (query.getRecordcount() > top) comment.append(" (showing top ").append(Caster.toString(top)).append(")");
		comment.append("\n");
		comment.append("Cached: ").append(query.isCached() ? "Yes\n" : "No\n");
		if (query.isCached() && query instanceof Query) {
			comment.append("Cache Type: ").append(query.getCacheType()).append("\n");
		}

		comment.append("Lazy: ").append(query instanceof SimpleQuery ? "Yes\n" : "No\n");

		SQL sql = query.getSql();
		if (sql != null) comment.append("SQL: ").append("\n").append(StringUtil.suppressWhiteSpace(sql.toString().trim())).append("\n");

		// table.appendRow(1, new SimpleDumpData("Execution Time (ms)"), new SimpleDumpData(exeTime));
		// table.appendRow(1, new SimpleDumpData("recordcount"), new SimpleDumpData(getRecordcount()));
		// table.appendRow(1, new SimpleDumpData("cached"), new SimpleDumpData(isCached()?"Yes":"No"));

		DumpTable recs = new DumpTable("query", "#cc99cc", "#ffccff", "#000000");
		recs.setTitle("Query");
		if (dp.getMetainfo()) recs.setComment(comment.toString());
		recs.appendRow(new DumpRow(-1, heads));

		// body
		DumpData[] items;
		int recordcount = query.getRecordcount();
		int columncount = query.getColumnNames().length;
		for (int i = 0; i < recordcount; i++) {
			items = new DumpData[columncount + 1];
			items[0] = new SimpleDumpData(i + 1);
			for (int y = 0; y < keys.length; y++) {
				try {
					Object o = query.getAt(keys[y], i + 1);
					if (o instanceof String) items[y + 1] = new SimpleDumpData(o.toString());
					else if (o instanceof Number) items[y + 1] = new SimpleDumpData(Caster.toString(((Number) o)));
					else if (o instanceof Boolean) items[y + 1] = new SimpleDumpData(((Boolean) o).booleanValue());
					else if (o instanceof Date) items[y + 1] = new SimpleDumpData(Caster.toString(o));
					else if (o instanceof Clob) items[y + 1] = new SimpleDumpData(Caster.toString(o));
					else items[y + 1] = DumpUtil.toDumpData(o, pageContext, maxlevel, dp);
				}
				catch (PageException e) {
					items[y + 1] = new SimpleDumpData("[empty]");
				}
			}
			recs.appendRow(new DumpRow(1, items));

			if (i == top - 1) break;
		}
		if (!dp.getMetainfo()) return recs;

		// table.appendRow(1, new SimpleDumpData("result"), recs);
		return recs;
	}

	public static void removeRows(Query query, int index, int count) throws PageException {
		if (query.getRecordcount() == 0) throw new DatabaseException("cannot remove rows, query is empty", null, null, null);
		if (index < 0 || index >= query.getRecordcount())
			throw new DatabaseException("invalid index [" + index + "], index must be between 0 and " + (query.getRecordcount() - 1), null, null, null);
		if (index + count > query.getRecordcount())
			throw new DatabaseException("invalid count [" + count + "], count+index [" + (count + index) + "] must less or equal to " + (query.getRecordcount()), null, null, null);

		for (int row = count; row >= 1; row--) {
			query.removeRow(index + row);
		}
	}

	public static boolean execute(PageContext pc, Statement stat, boolean createGeneratedKeys, SQL sql) throws SQLException {
		if (stat instanceof StatementPro) {
			StatementPro sp = (StatementPro) stat;
			return createGeneratedKeys ? sp.execute(pc, sql.getSQLString(), Statement.RETURN_GENERATED_KEYS) : sp.execute(pc, sql.getSQLString());
		}
		return createGeneratedKeys ? stat.execute(sql.getSQLString(), Statement.RETURN_GENERATED_KEYS) : stat.execute(sql.getSQLString());
	}

	public static boolean execute(PageContext pc, PreparedStatement ps) throws SQLException {
		if (ps instanceof PreparedStatementPro) {
			PreparedStatementPro psp = (PreparedStatementPro) ps;
			return psp.execute(pc);
		}
		return ps.execute();
	}

	public static String getColumnName(ResultSetMetaData meta, int column) throws SQLException {
		try {
			return meta.getColumnLabel(column);
		}
		catch (SQLException e) {
			return meta.getColumnName(column);
		}
	}

	public static Object getObject(ResultSet rs, int columnIndex, Class type) throws SQLException {
		if (BigDecimal.class == type) return rs.getBigDecimal(columnIndex);
		if (Blob.class == type) return rs.getBlob(columnIndex);
		if (boolean.class == type || Boolean.class == type) return rs.getBoolean(columnIndex);
		if (byte.class == type || Byte.class == type) return rs.getByte(columnIndex);
		if (Clob.class == type) return rs.getClob(columnIndex);
		if (Date.class == type) return rs.getDate(columnIndex);
		if (double.class == type || Double.class == type) return rs.getDouble(columnIndex);
		if (float.class == type || Float.class == type) return rs.getFloat(columnIndex);
		if (int.class == type || Integer.class == type) return rs.getInt(columnIndex);
		if (long.class == type || Long.class == type) return rs.getLong(columnIndex);
		if (short.class == type || Short.class == type) return rs.getShort(columnIndex);
		if (String.class == type) return rs.getString(columnIndex);
		if (Time.class == type) return rs.getTime(columnIndex);
		if (Ref.class == type) return rs.getRef(columnIndex);

		throw new SQLFeatureNotSupportedException("type [" + type.getName() + "] is not supported");
	}

	public static Object getObject(ResultSet rs, String columnLabel, Class type) throws SQLException {
		if (BigDecimal.class == type) return rs.getBigDecimal(columnLabel);
		if (Blob.class == type) return rs.getBlob(columnLabel);
		if (boolean.class == type || Boolean.class == type) return rs.getBoolean(columnLabel);
		if (byte.class == type || Byte.class == type) return rs.getByte(columnLabel);
		if (Clob.class == type) return rs.getClob(columnLabel);
		if (Date.class == type) return rs.getDate(columnLabel);
		if (double.class == type || Double.class == type) return rs.getDouble(columnLabel);
		if (float.class == type || Float.class == type) return rs.getFloat(columnLabel);
		if (int.class == type || Integer.class == type) return rs.getInt(columnLabel);
		if (long.class == type || Long.class == type) return rs.getLong(columnLabel);
		if (short.class == type || Short.class == type) return rs.getShort(columnLabel);
		if (String.class == type) return rs.getString(columnLabel);
		if (Time.class == type) return rs.getTime(columnLabel);
		if (Ref.class == type) return rs.getRef(columnLabel);

		throw new SQLFeatureNotSupportedException("type [" + type.getName() + "] is not supported");
	}

	/**
	 * return the value at the given position (row), returns the default empty value ("" or null) for
	 * wrong row or null values. this method only exist for backward compatibility and should not be
	 * used for new functinality
	 * 
	 * @param column
	 * @param row
	 * @return
	 * @deprecated use instead QueryColumn.get(int,Object)
	 */
	@Deprecated
	public static Object getValue(QueryColumn column, int row) {// print.ds();
		if (NullSupportHelper.full()) return column.get(row, null);
		Object v = column.get(row, "");
		return v == null ? "" : v;
	}

	@Deprecated
	public static Object getValue(PageContext pc, QueryColumn column, int row) {// print.ds();
		if (NullSupportHelper.full(pc)) return column.get(row, null);
		Object v = column.get(row, "");
		return v == null ? "" : v;
	}

	public static QueryColumnImpl duplicate2QueryColumnImpl(QueryImpl targetQuery, QueryColumn col, boolean deepCopy) {
		if (col instanceof QueryColumnImpl) return ((QueryColumnImpl) col).cloneColumnImpl(deepCopy);

		// fill array for column
		Array content = new ArrayImpl();
		int len = col.size();
		for (int i = 1; i <= len; i++) {
			content.setEL(i, col.get(i, null));
		}

		// create and return column
		try {
			return new QueryColumnImpl(targetQuery, col.getKey(), content, col.getType());
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
	}
}