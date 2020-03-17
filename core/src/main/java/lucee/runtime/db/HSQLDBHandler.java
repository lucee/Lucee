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
package lucee.runtime.db;

import static lucee.runtime.db.DatasourceManagerImpl.QOQ_DATASOURCE_NAME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import lucee.commons.db.DBUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.sql.SQLParserException;
import lucee.runtime.sql.SelectParser;
import lucee.runtime.sql.Selects;
import lucee.runtime.sql.old.ParseException;
import lucee.runtime.timer.Stopwatch;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.CollectionUtil;

/**
 * class to reexecute queries on the resultset object inside the cfml environment
 */
public final class HSQLDBHandler {

	private static final int STRING = 0;
	private static final int INT = 1;
	private static final int DOUBLE = 2;
	private static final int DATE = 3;
	private static final int TIME = 4;
	private static final int TIMESTAMP = 5;
	private static final int BINARY = 6;

	Executer executer = new Executer();
	QoQ qoq = new QoQ();
	private static Object lock = new SerializableObject();

	/**
	 * constructor of the class
	 */
	public HSQLDBHandler() {

	}

	/**
	 * adds a table to the memory database
	 * 
	 * @param conn
	 * @param pc
	 * @param name name of the new table
	 * @param query data source for table
	 * @throws SQLException
	 * @throws PageException
	 */
	private static void addTable(Connection conn, PageContext pc, String name, Query query, boolean doSimpleTypes, ArrayList<String> usedTables)
			throws SQLException, PageException {
		Statement stat;
		usedTables.add(name);
		stat = conn.createStatement();
		Key[] keys = CollectionUtil.keys(query);
		int[] types = query.getTypes();
		int[] innerTypes = toInnerTypes(types);
		// CREATE STATEMENT
		String comma = "";
		StringBuilder create = new StringBuilder("CREATE TABLE " + name + " (");
		StringBuilder insert = new StringBuilder("INSERT INTO  " + name + " (");
		StringBuilder values = new StringBuilder("VALUES (");
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i].getString();
			String type = (doSimpleTypes) ? "VARCHAR_IGNORECASE" : toUsableType(types[i]);

			create.append(comma + key);
			create.append(" ");
			create.append(type);
			insert.append(comma + key);
			values.append(comma + "?");
			comma = ",";
		}
		create.append(")");
		insert.append(")");
		values.append(")");
		stat.execute(create.toString());
		PreparedStatement prepStat = conn.prepareStatement(insert.toString() + values.toString());

		// INSERT STATEMENT
		// HashMap integerTypes=getIntegerTypes(types);

		int count = query.getRecordcount();
		QueryColumn[] columns = new QueryColumn[keys.length];
		for (int i = 0; i < keys.length; i++) {
			columns[i] = query.getColumn(keys[i]);
		}
		for (int y = 0; y < count; y++) {
			for (int i = 0; i < keys.length; i++) {
				int type = innerTypes[i];
				Object value = columns[i].get(y + 1, null);

				// print.out("*** "+type+":"+Caster.toString(value));
				if (doSimpleTypes) {

					prepStat.setObject(i + 1, Caster.toString(value));
				}
				else {
					if (value == null) prepStat.setNull(i + 1, types[i]);
					else if (type == BINARY) prepStat.setBytes(i + 1, Caster.toBinary(value));
					else if (type == DATE) {
						// print.out(new java.util.Date(new
						// Date(DateCaster.toDateAdvanced(value,pc.getTimeZone()).getTime()).getTime()));

						prepStat.setTimestamp(i + 1, (value.equals("")) ? null : new Timestamp(DateCaster.toDateAdvanced(query.getAt(keys[i], y + 1), pc.getTimeZone()).getTime()));
						// prepStat.setObject(i+1,Caster.toDate(value,null));
						// prepStat.setDate(i+1,(value==null || value.equals(""))?null:new
						// Date(DateCaster.toDateAdvanced(value,pc.getTimeZone()).getTime()));
					}
					else if (type == TIME)
						prepStat.setTime(i + 1, (value.equals("")) ? null : new Time(DateCaster.toDateAdvanced(query.getAt(keys[i], y + 1), pc.getTimeZone()).getTime()));
					else if (type == TIMESTAMP)
						prepStat.setTimestamp(i + 1, (value.equals("")) ? null : new Timestamp(DateCaster.toDateAdvanced(query.getAt(keys[i], y + 1), pc.getTimeZone()).getTime()));
					else if (type == DOUBLE) prepStat.setDouble(i + 1, (value.equals("")) ? 0 : Caster.toDoubleValue(query.getAt(keys[i], y + 1)));
					else if (type == INT) prepStat.setLong(i + 1, (value.equals("")) ? 0 : Caster.toLongValue(query.getAt(keys[i], y + 1)));
					else if (type == STRING) prepStat.setObject(i + 1, Caster.toString(value));
				}

			}
			prepStat.execute();
		}

	}

	private static int[] toInnerTypes(int[] types) {
		int[] innerTypes = new int[types.length];
		for (int i = 0; i < types.length; i++) {
			int type = types[i];

			if (type == Types.BIGINT || type == Types.BIT || type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT) innerTypes[i] = INT;
			else if (type == Types.DECIMAL || type == Types.DOUBLE || type == Types.NUMERIC || type == Types.REAL) innerTypes[i] = DOUBLE;
			else if (type == Types.DATE) innerTypes[i] = DATE;
			else if (type == Types.TIME) innerTypes[i] = TIME;
			else if (type == Types.TIMESTAMP) innerTypes[i] = TIMESTAMP;
			else if (type == Types.BINARY || type == Types.LONGVARBINARY || type == Types.VARBINARY) innerTypes[i] = BINARY;
			else innerTypes[i] = STRING;

		}
		return innerTypes;
	}

	private static String toUsableType(int type) {
		if (type == Types.NCHAR) return "CHAR";
		if (type == Types.NCLOB) return "CLOB";
		if (type == Types.NVARCHAR) return "VARCHAR_IGNORECASE";
		if (type == Types.VARCHAR) return "VARCHAR_IGNORECASE";
		if (type == Types.JAVA_OBJECT) return "VARCHAR_IGNORECASE";

		return QueryImpl.getColumTypeName(type);

	}

	/**
	 * remove a table from the memory database
	 * 
	 * @param conn
	 * @param name
	 * @throws DatabaseException
	 */
	private static void removeTable(Connection conn, String name) throws SQLException {
		name = name.replace('.', '_');
		Statement stat = conn.createStatement();
		stat.execute("DROP TABLE " + name);
		DBUtil.commitEL(conn);
	}

	/**
	 * remove all table inside the memory database
	 * 
	 * @param conn
	 */
	private static void removeAll(Connection conn, ArrayList<String> usedTables) {
		int len = usedTables.size();

		for (int i = 0; i < len; i++) {

			String tableName = usedTables.get(i).toString();
			// print.out("remove:"+tableName);
			try {
				removeTable(conn, tableName);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
	}

	/**
	 * executes a query on the queries inside the cfml environment
	 * 
	 * @param pc Page Context
	 * @param sql
	 * @param maxrows
	 * @return result as Query
	 * @throws PageException
	 * @throws PageException
	 */
	public QueryImpl execute(PageContext pc, final SQL sql, int maxrows, int fetchsize, TimeSpan timeout) throws PageException {
		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_NANO);
		stopwatch.start();
		String prettySQL = null;
		Selects selects = null;

		// First Chance
		try {
			SelectParser parser = new SelectParser();
			selects = parser.parse(sql.getSQLString());
			QueryImpl q = qoq.execute(pc, sql, selects, maxrows);
			q.setExecutionTime(stopwatch.time());
			return q;
		}
		catch (SQLParserException spe) {
			// sp
			prettySQL = SQLPrettyfier.prettyfie(sql.getSQLString());
			try {
				QueryImpl query = executer.execute(pc, sql, prettySQL, maxrows);
				query.setExecutionTime(stopwatch.time());
				return query;
			}
			catch (PageException ex) {}

		}
		catch (PageException e) {}
		// if(true) throw new RuntimeException();

		// SECOND Chance with hsqldb
		try {
			boolean isUnion = false;
			Set<String> tables = null;
			if (selects != null) {
				HSQLUtil2 hsql2 = new HSQLUtil2(selects);
				isUnion = hsql2.isUnion();
				tables = hsql2.getInvokedTables();
			}
			else {
				if (prettySQL == null) prettySQL = SQLPrettyfier.prettyfie(sql.getSQLString());
				HSQLUtil hsql = new HSQLUtil(prettySQL);
				tables = hsql.getInvokedTables();
				isUnion = hsql.isUnion();
			}

			String strSQL = StringUtil.replace(sql.getSQLString(), "[", "", false);
			strSQL = StringUtil.replace(strSQL, "]", "", false);
			sql.setSQLString(strSQL);
			return _execute(pc, sql, maxrows, fetchsize, timeout, stopwatch, tables, isUnion);

		}
		catch (ParseException e) {
			throw new DatabaseException(e.getMessage(), null, sql, null);
		}

	}

	private QueryImpl _execute(PageContext pc, SQL sql, int maxrows, int fetchsize, TimeSpan timeout, Stopwatch stopwatch, Set<String> tables, boolean isUnion)
			throws PageException {
		try {
			return __execute(pc, SQLImpl.duplicate(sql), maxrows, fetchsize, timeout, stopwatch, tables, false);
		}
		catch (PageException pe) {
			if (isUnion || StringUtil.indexOf(pe.getMessage(), "NumberFormatException:") != -1) {
				return __execute(pc, sql, maxrows, fetchsize, timeout, stopwatch, tables, true);
			}
			throw pe;
		}
	}

	public static QueryImpl __execute(PageContext pc, SQL sql, int maxrows, int fetchsize, TimeSpan timeout, Stopwatch stopwatch, Set<String> tables, boolean doSimpleTypes)
			throws PageException {
		ArrayList<String> usedTables = new ArrayList<String>();
		synchronized (lock) {

			QueryImpl nqr = null;
			ConfigImpl config = (ConfigImpl) pc.getConfig();
			DatasourceConnectionPool pool = config.getDatasourceConnectionPool();
			DatasourceConnection dc = pool.getDatasourceConnection(config, config.getDataSource(QOQ_DATASOURCE_NAME), "sa", "");
			Connection conn = dc.getConnection();
			try {
				DBUtil.setAutoCommitEL(conn, false);

				// sql.setSQLString(HSQLUtil.sqlToZQL(sql.getSQLString(),false));
				try {
					Iterator<String> it = tables.iterator();
					// int len=tables.size();
					while (it.hasNext()) {
						String tableName = it.next().toString();// tables.get(i).toString();

						String modTableName = tableName.replace('.', '_');
						String modSql = StringUtil.replace(sql.getSQLString(), tableName, modTableName, false);
						sql.setSQLString(modSql);
						if (sql.getItems() != null && sql.getItems().length > 0) sql = new SQLImpl(sql.toString());

						addTable(conn, pc, modTableName, Caster.toQuery(pc.getVariable(tableName)), doSimpleTypes, usedTables);
					}
					DBUtil.setReadOnlyEL(conn, true);
					try {
						nqr = new QueryImpl(pc, dc, sql, maxrows, fetchsize, timeout, "query", null, false, false, null);
					}
					finally {
						DBUtil.setReadOnlyEL(conn, false);
						DBUtil.commitEL(conn);
						DBUtil.setAutoCommitEL(conn, true);
					}

				}
				catch (SQLException e) {
					DatabaseException de = new DatabaseException("there is a problem to execute sql statement on query", null, sql, null);
					de.setDetail(e.getMessage());
					throw de;
				}

			}
			finally {
				removeAll(conn, usedTables);
				DBUtil.setAutoCommitEL(conn, true);
				pool.releaseDatasourceConnection(dc);

				// manager.releaseConnection(dc);
			}
			nqr.setExecutionTime(stopwatch.time());
			return nqr;
		}
	}
}