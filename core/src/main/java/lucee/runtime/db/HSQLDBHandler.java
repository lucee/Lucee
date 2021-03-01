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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


import lucee.commons.db.DBUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.IllegalQoQException;
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
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

import lucee.commons.lang.SystemOut;
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
	private static boolean hsqldbDisable;
	private static boolean hsqldbDebug;

	static {
		hsqldbDisable = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.qoq.hsqldb.disable", "false"), false);
		hsqldbDebug = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.qoq.hsqldb.debug", "false"), false);
	}

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
	private static void createTable(Connection conn, PageContext pc, String name, Query query, boolean doSimpleTypes, ArrayList<String> usedTables)
			throws SQLException, PageException {

		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_MILLI);
		stopwatch.start();

		Statement stat;
		usedTables.add(name);
		stat = conn.createStatement();
		
		Key[] cols = CollectionUtil.keys(query);
		int[] types = query.getTypes();
		int[] innerTypes = toInnerTypes(types);

		// CREATE STATEMENT		
		String comma = "";
		String escape = "\""; // use double qoutes around column and tables names to avoid problems with reserved words

		// TODO use DECLARE LOCAL TEMPORARY TABLE
		StringBuilder create = new StringBuilder("CREATE TABLE ").append(escape).append(StringUtil.toUpperCase(name)).append(escape).append(" (");

		for (int i = 0; i < cols.length; i++) {
			String col = StringUtil.toUpperCase(cols[i].getString()); // quoted objects are case insensitive
			String type = (doSimpleTypes) ? "VARCHAR_IGNORECASE" : toUsableType(types[i]);
			create.append(comma);
			create.append(escape);
			create.append(col);
			create.append(escape);
			create.append(" ");
			create.append(type);
			comma = ",";
		}
		create.append(")");
		SystemOut.print("SQL: " + Caster.toString(create));
		stat.execute(create.toString());
		SystemOut.print("Create Table: [" + name + "] took " + stopwatch.time());
	}

	/**
	 * populates a table to the memory database, but only the required columns from the source query
	 * 
	 * @param conn
	 * @param pc
	 * @param name name of the new table
	 * @param query data source for table
	 * @throws SQLException
	 * @throws PageException
	 */
	
	private static void populateTable(Connection conn, PageContext pc, String name, Query query, boolean doSimpleTypes, Struct tableCols)
			throws SQLException, PageException {

		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_MILLI);
		stopwatch.start();

		Key[] cols = CollectionUtil.keys(query);
		ArrayList<String> usedCols = new ArrayList<String>();
		
		int[] types = query.getTypes();
		int[] innerTypes = toInnerTypes(types);
		String comma = "";
		String escape = "\"";
		
		StringBuilder insert = new StringBuilder("INSERT INTO  ").append(escape).append(StringUtil.toUpperCase(name)).append(escape).append(" (");
		StringBuilder values = new StringBuilder("VALUES (");
		Key colName = null;

		for (int i = 0; i < cols.length; i++) {
			String col = StringUtil.toUpperCase(cols[i].getString()); // quoted objects are case insensitive in HSQLDB
			//colName = Caster.toKey(cols[i].getString());
			if (tableCols == null || tableCols.containsKey(col)){
				usedCols.add(col);

				insert.append(comma);
				insert.append(escape);
				insert.append(col);
				insert.append(escape);

				values.append(comma);
				values.append("?");
				comma = ",";
			}
		}
		insert.append(")");
		values.append(")");

		if (tableCols != null && usedCols.size() == 0){
			SystemOut.print("Populate Table, table has no used columns: " + name);
			return;
		}

		SystemOut.print("SQL: " + Caster.toString(insert));
		SystemOut.print("SQL: " + Caster.toString(values));
		
		// INSERT STATEMENT
		// HashMap integerTypes=getIntegerTypes(types);
		PreparedStatement prepStat = conn.prepareStatement(insert.toString() + values.toString());

		int rows = query.getRecordcount();	
		int count = usedCols.size();
		
		QueryColumn[] columns = new QueryColumn[count];
		for (int i = 0; i < count; i++) {
			columns[i] = query.getColumn(usedCols.get(i));
		}
		for (int y = 0; y < rows; y++) {
			for (int i = 0; i < count; i++) {
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

						prepStat.setTimestamp(i + 1, (value.equals("")) ? null : new Timestamp(DateCaster.toDateAdvanced(query.getAt(usedCols.get(i), y + 1), pc.getTimeZone()).getTime()));
						// prepStat.setObject(i+1,Caster.toDate(value,null));
						// prepStat.setDate(i+1,(value==null || value.equals(""))?null:new
						// Date(DateCaster.toDateAdvanced(value,pc.getTimeZone()).getTime()));
					}
					else if (type == TIME)
						prepStat.setTime(i + 1, (value.equals("")) ? null : new Time(DateCaster.toDateAdvanced(query.getAt(usedCols.get(i), y + 1), pc.getTimeZone()).getTime()));
					else if (type == TIMESTAMP)
						prepStat.setTimestamp(i + 1, (value.equals("")) ? null : new Timestamp(DateCaster.toDateAdvanced(query.getAt(usedCols.get(i), y + 1), pc.getTimeZone()).getTime()));
					else if (type == DOUBLE) prepStat.setDouble(i + 1, (value.equals("")) ? 0 : Caster.toDoubleValue(query.getAt(usedCols.get(i), y + 1)));
					else if (type == INT) prepStat.setLong(i + 1, (value.equals("")) ? 0 : Caster.toLongValue(query.getAt(usedCols.get(i), y + 1)));
					else if (type == STRING) prepStat.setObject(i + 1, Caster.toString(value));
				}

			}
			prepStat.execute();
		}
		SystemOut.print("Populate Table: [" +name + "] took " + stopwatch.time());
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
	 * wrap the execute statement, urrghh ugly
	 * 
	 * @param conn
	 * @param sql
	 */
	private static void executeStatement(Connection conn, String sql) {
		try {
			_executeStatement(conn, sql);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	/**
	 * toggle database session
	 * 
	 * @param conn
	 * @param sql
	 * @throws DatabaseException
	 */
	private static void _executeStatement(Connection conn, String sql) throws SQLException {
		Statement stat = conn.createStatement();
		stat.execute(sql);		
		//DBUtil.commitEL(conn);
	}

	/**
	 * toggle database session
	 * 
	 * @param conn
	 * @param sql
	 * @throws DatabaseException
	 */
	private static Struct getUsedColumnsForQuery(Connection conn, SQL sql) throws SQLException {
		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_MILLI);
		stopwatch.start();

		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String view = "V_QOQ_TEMP";
		Struct tables = new StructImpl();

		return tables;
		/*
		// this doesn't work yet, I think due to hsqldb being ancient aka 1.8.0
		// INFORMATION_SCHEMA.VIEW_COLUMN_USAGE doesn't exist
		// if VIEW_COLUMN_USAGE doesn't contain all the columns required, we could use the QoQ parser?

		try {
			Statement stat = conn.createStatement();
			stat.execute("CREATE VIEW " + view + " AS " + sql.toString());
			
			StringBuilder viewUsage = new StringBuilder("SELECT COLUMN_NAME, TABLE_NAME ");
			viewUsage.append("FROM INFORMATION_SCHEMA.VIEW_COLUMN_USAGE WHERE VIEW_NAME='");
			viewUsage.append(view);
			viewUsage.append("' ORDER BY TABLE_NAME, COLUMN_NAME");
			rs = stat.executeQuery(viewUsage.toString()); 

			// dump out the column names, not sure what they are lol (can be removed)
			rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			String name = null;
			for (int i = 1; i <= columnCount; i++ ) {
				name = rsmd.getColumnName(i);
				SystemOut.print("View Column : [" + name + "]");
			}
			
			// load tables and columns into a nested struct
			Key tableName = null;
			Struct tableCols = null;
			while(rs.next()){
				tableName = Caster.toKey(rs.getString("TABLE_NAME"));
				if (!tables.containsKey(tableName))
					tables.setEL(tableName, new StructImpl());
				tableCols = ((Struct) tables.get(tableName));
				tableCols.setEl(Caster.toKey(rs.getString("COLUMN_NAME")), null); 
			}
			// don't need the view anymore, bye bye
			stat.execute("DROP VIEW " + view);
		} catch (Exception e) {
			SystemOut.print("Exception: " + e.toString());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				SystemOut.print(e.toString());
			}
		}
		SystemOut.print("getUsedColumnsForQuery: took " + stopwatch.time());
		return tables;
		*/
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

		Exception qoqException = null;

		// First Chance
		try {
			SelectParser parser = new SelectParser();
			selects = parser.parse(sql.getSQLString());
			QueryImpl q = (QueryImpl) qoq.execute(pc, sql, selects, maxrows);
			q.setExecutionTime(stopwatch.time());
			return q;
		}
		catch (SQLParserException spe) {
			qoqException = spe;
			if (spe.getCause() != null && spe.getCause() instanceof IllegalQoQException) {
				throw Caster.toPageException(spe);
			}
			prettySQL = SQLPrettyfier.prettyfie(sql.getSQLString());
			try {
				QueryImpl query = executer.execute(pc, sql, prettySQL, maxrows);
				query.setExecutionTime(stopwatch.time());
				return query;
			}
			catch (Exception ex) {
			}

		}
		catch (Exception e) {
			qoqException = e;
		}

		// If our first pass at the QoQ failed, lets look at the exception to see what we want to do with
		// it.
		if (qoqException != null) {

			// Track the root cause
			Exception rootCause = qoqException;

			// Unwrap any RuntimeExceptions thrown from Java streams
			if (qoqException instanceof RuntimeException && qoqException.getCause() != null && qoqException.getCause() instanceof Exception) {
				rootCause = (Exception) qoqException.getCause();
				// Exceptions from an async Java stream will be wrapped in TWO RuntimeExceptions!
				if (rootCause instanceof RuntimeException && rootCause.getCause() != null && rootCause.getCause() instanceof Exception) {
					rootCause = (Exception) rootCause.getCause();
				}
			}

			// We don't need to catch these, so re-throw
			if (rootCause instanceof RuntimeException) {
				// re-throw the original outer exception
				throw new RuntimeException(qoqException);
			}

			// Debugging option to completely disable HyperSQL for testing
			// Or if it's an IllegalQoQException that means, stop trying and throw the original message.
			if (hsqldbDisable || rootCause instanceof IllegalQoQException) {
				// re-throw the original outer exception
				throw Caster.toPageException(qoqException);
			}

			// Debugging option to to log all QoQ that fall back on hsqldb in the datasource log
			if (hsqldbDebug) {
				ThreadLocalPageContext.getLog(pc, "datasource").error("QoQ [" + sql.getSQLString() + "] errored and is falling back to HyperSQL.", qoqException);
			}
		}

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
			ConfigPro config = (ConfigPro) pc.getConfig();
			DatasourceConnection dc = null;
			Connection conn = null;
			try {
				DatasourceConnPool pool = config.getDatasourceConnectionPool(config.getDataSource(QOQ_DATASOURCE_NAME), "sa", "");
				dc = pool.borrowObject();
				conn = dc.getConnection();

				//executeStatement(conn, "CONNECT"); // create a new HSQLDB session for temp tables
				DBUtil.setAutoCommitEL(conn, false);

				// sql.setSQLString(HSQLUtil.sqlToZQL(sql.getSQLString(),false));
				try {
					Iterator<String> it = tables.iterator();
					String tableName = null;
					Key tableKey = null;
					// int len=tables.size();
					while (it.hasNext()) {
						tableName = it.next().toString();// tables.get(i).toString();
						String modTableName = tableName.replace('.', '_');
						// this could match the wrong strings??
						String modSql = StringUtil.replace(sql.getSQLString(), tableName, modTableName, false);
						sql.setSQLString(modSql);
						if (sql.getItems() != null && sql.getItems().length > 0) sql = new SQLImpl(sql.toString());

						createTable(conn, pc, modTableName, Caster.toQuery(pc.getVariable(StringUtil.removeQuotes(tableName, true))), 
							doSimpleTypes, usedTables); 
					}
					
					// create the sql as a view, to find out which table columns are needed
					Struct allTableColumns = getUsedColumnsForQuery(conn, sql);
					Struct tableColumns = null;
					
					// load data into tables
					it = usedTables.iterator();
					while (it.hasNext()) {
						tableName = it.next().toString();
						tableKey = Caster.toKey(tableName);
						
						tableColumns = allTableColumns.containsKey(tableKey) ? ((Struct) tableColumns.get(tableKey)) : null;
						// only populate tables with data if there are used columns, or no needed column data at all
						if (tableColumns != null || allTableColumns.size() == 0){
							populateTable(conn, pc, tableName, Caster.toQuery(pc.getVariable(StringUtil.removeQuotes(tableName, true))), 
								doSimpleTypes, tableColumns); 
						}
					}

					DBUtil.setReadOnlyEL(conn, true);
					try {
						SystemOut.print("QoQ HSQLDB SQL: " + sql.toString());
						nqr = new QueryImpl(pc, dc, sql, maxrows, fetchsize, timeout, "query", null, false, false, null);
					}
					finally {
						DBUtil.setReadOnlyEL(conn, false);
						DBUtil.commitEL(conn);
						DBUtil.setAutoCommitEL(conn, true);
					}

				}
				catch (SQLException e) {
					DatabaseException de = new DatabaseException("QoQ HSQLDB: error executing sql statement on query [" + e.getMessage() + "]", null , sql, null);
					de.setDetail(e.getMessage());
					throw de;
				}

			}
			finally {
				if (conn != null) {
					removeAll(conn, usedTables);
					//executeStatement(conn, "DISCONNECT"); // close HSQLDB session with temp tables
					DBUtil.setAutoCommitEL(conn, true);
				}
				if (dc != null) ((DatasourceConnectionPro) dc).release();

				// manager.releaseConnection(dc);
			}
			nqr.setExecutionTime(stopwatch.time());
			return nqr;
		}
	}
}