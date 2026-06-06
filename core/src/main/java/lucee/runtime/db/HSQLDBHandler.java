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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleException;

import lucee.commons.db.DBUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.SystemOut;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
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
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.CollectionUtil;
import lucee.transformer.library.ClassDefinitionImpl;

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
	// QoQ instances are created per-execution to avoid thread-safety issues with caseSensitive state
	// QoQ qoq = new QoQ();
	private static Object lock = new SerializableObject();
	private static boolean hsqldbDisable;
	private static boolean hsqldbDebug;
	private static int hsqldbPoolSize;
	private static boolean hsqldbNullsLastInDesc;
	private static final Struct columnUsageCache = new StructImpl(StructImpl.TYPE_MAX, 32, 500);
	private static BlockingQueue<Integer> dbQueue;
	private static DataSource[] dsCache;
	private static ClassDefinition<?> hsqldbClassDefinition = null;
	private static final Object CLASS_DEF_LOCK = new Object();
	private static volatile boolean hsqldbFallbackWarningLogged = false;

	static {
		hsqldbDisable = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.qoq.hsqldb.disable", "false"), false);
		hsqldbDebug = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.qoq.hsqldb.debug", "false"), false);
		hsqldbNullsLastInDesc = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.qoq.hsqldb.orderBy.nullsLastInDesc", "true"), true);
		int defaultPoolSize = Math.min(Runtime.getRuntime().availableProcessors(), 8);
		hsqldbPoolSize = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.qoq.hsqldb.poolsize", String.valueOf(defaultPoolSize)), defaultPoolSize);

		// Pool size of 0 means disable HSQLDB
		if (hsqldbPoolSize <= 0) {
			hsqldbDisable = true;
			hsqldbPoolSize = 1; // Prevent array allocation errors
		}

		// Initialize queue with DB numbers
		dbQueue = new ArrayBlockingQueue<>(hsqldbPoolSize);
		dsCache = new DataSource[hsqldbPoolSize];
		for (int i = 0; i < hsqldbPoolSize; i++) {
			dbQueue.offer(i);
		}
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
	private static String createTable(Connection conn, PageContext pc, String dbTableName, String cfQueryName, boolean doSimpleTypes, boolean caseSensitive) throws SQLException, PageException {
		return createTable(conn, pc, dbTableName, cfQueryName, doSimpleTypes, caseSensitive, null);
	}

	/**
	 * Creates a table in the HSQLDB database. If usedColumns is provided, only those columns are
	 * created (minimal table). If null, all source columns are created.
	 */
	private static String createTable(Connection conn, PageContext pc, String dbTableName, String cfQueryName, boolean doSimpleTypes, boolean caseSensitive, Struct usedColumns)
			throws SQLException, PageException {

		// Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_MILLI);
		// stopwatch.start();

		String sql = buildCreateTableSql(pc, dbTableName, cfQueryName, doSimpleTypes, caseSensitive, usedColumns);
		Statement stat = conn.createStatement();
		stat.execute(sql);
		// SystemOut.print("Create Table: [" + dbTableName + "] took " + stopwatch.time());
		return sql;
	}

	/**
	 * Builds the CREATE TABLE SQL string without executing it. When usedColumns is null, all source
	 * columns are included (used for cache keys and cache-miss table creation). When usedColumns is
	 * provided, only those columns are included (minimal table on cache hit).
	 */
	private static String buildCreateTableSql(PageContext pc, String dbTableName, String cfQueryName, boolean doSimpleTypes, boolean caseSensitive) throws PageException {
		return buildCreateTableSql(pc, dbTableName, cfQueryName, doSimpleTypes, caseSensitive, null);
	}

	private static String buildCreateTableSql(PageContext pc, String dbTableName, String cfQueryName, boolean doSimpleTypes, boolean caseSensitive, Struct usedColumns) throws PageException {
		Query query = Caster.toQuery(pc.getVariable(StringUtil.removeQuotes(cfQueryName, true)));
		Key[] cols = CollectionUtil.keys(query);
		int[] types = query.getTypes();

		String comma = "";
		// String escape = "\""; // use double qoutes around column and tables names to avoid problems with
		// reserved words
		String escape = "";

		StringBuilder create = new StringBuilder("DECLARE LOCAL TEMPORARY TABLE ").append(escape).append(StringUtil.toUpperCase(dbTableName)).append(escape).append(" (");

		for (int i = 0; i < cols.length; i++) {
			String col = StringUtil.toUpperCase(cols[i].getString()); // quoted objects are case insensitive
			// skip columns not referenced in the SQL (when we know which ones are needed)
			if (usedColumns != null && !usedColumns.containsKey(col)) continue;
			String type = (doSimpleTypes) ? (caseSensitive ? "VARCHAR" : "VARCHAR_IGNORECASE") : toUsableType(types[i], caseSensitive);
			create.append(comma);
			create.append(escape);
			create.append(col);
			create.append(escape);
			create.append(" ");
			create.append(type);
			comma = ",";
		}
		create.append(") ON COMMIT PRESERVE ROWS");
		return create.toString();
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

	private static void populateTable(Connection conn, PageContext pc, String dbTableName, String cfQueryName, boolean doSimpleTypes, Struct tableCols)
			throws SQLException, PageException {

		// Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_MILLI);
		// stopwatch.start();
		Query query = Caster.toQuery(pc.getVariable(StringUtil.removeQuotes(cfQueryName, true)));

		Key[] cols = CollectionUtil.keys(query);
		ArrayList<QueryColumn> targetColumns = new ArrayList<QueryColumn>();

		int[] srcTypes = query.getTypes();
		int[] srcQueryTypes = toInnerTypes(srcTypes);
		ArrayList<Integer> targetTypeList = new ArrayList<Integer>();
		String comma = "";

		StringBuilder insert = new StringBuilder("INSERT INTO  ").append(StringUtil.toUpperCase(dbTableName)).append(" (");
		StringBuilder values = new StringBuilder("VALUES (");
		// tableCols = null; // set this to avoid optimised loading of only required tables
		for (int i = 0; i < cols.length; i++) {
			String col = StringUtil.toUpperCase(cols[i].getString()); // quoted objects are case insensitive in HSQLDB
			if (tableCols == null || tableCols.containsKey(col)) {
				// grab the QueryColumn directly by index — avoids O(n) name lookup via getIndexFromKey
				targetColumns.add(query.getColumn(cols[i]));
				targetTypeList.add(srcQueryTypes[i]);
				insert.append(comma);
				insert.append(col);

				values.append(comma);
				values.append("?");
				comma = ",";
			}
		}
		insert.append(")");
		values.append(")");

		if (tableCols != null && targetColumns.size() == 0) {
			// SystemOut.print("Populate Table, table has no used columns: " + dbTableName);
			return;
		}

		// SystemOut.print("SQL: " + Caster.toString(insert));
		// SystemOut.print("SQL: " + Caster.toString(values));

		// INSERT STATEMENT
		// HashMap integerTypes=getIntegerTypes(types);
		Statement stat = conn.createStatement();
		stat.execute("SET FILES LOG FALSE");
		conn.setAutoCommit(false);

		PreparedStatement prepStat = conn.prepareStatement(insert.toString() + values.toString());

		int rows = query.getRecordcount();
		int count = targetColumns.size();
		int rowsToCommit = 0;

		QueryColumn[] columns = targetColumns.toArray(new QueryColumn[0]);
		int[] targetTypes = new int[count];
		for (int i = 0; i < count; i++) targetTypes[i] = targetTypeList.get(i);

		// aprint.o(query);
		// aprint.o(tableCols); aprint.o(srcTypes);
		// aprint.o(srcQueryTypes); aprint.o(targetTypes); aprint.o(targetCols);
		for (int y = 0; y < rows; y++) {
			for (int i = 0; i < count; i++) {
				int type = targetTypes[i];
				Object value = columns[i].get(y + 1, null);
				// col = targetCols.get(i);

				// print.out("*** "+type+":"+Caster.toString(value));
				if (doSimpleTypes) {
					prepStat.setObject(i + 1, Caster.toString(value));
				}
				else {
					if (value == null) prepStat.setNull(i + 1, type);
					else if (type == STRING) prepStat.setObject(i + 1, Caster.toString(value));
					else if (type == INT) prepStat.setInt(i + 1, (value.equals("")) ? 0 : Caster.toIntValue(value));
					else if (type == DOUBLE) prepStat.setDouble(i + 1, (value.equals("")) ? 0 : Caster.toDoubleValue(value));
					else if (type == DATE) {
						// print.out(new java.util.Date(new
						// Date(DateCaster.toDateAdvanced(value,pc.getTimeZone()).getTime()).getTime()));

						prepStat.setTimestamp(i + 1, (value.equals("")) ? null : new Timestamp(DateCaster.toDateAdvanced(value, pc.getTimeZone()).getTime()));
						// prepStat.setObject(i+1,Caster.toDate(value,null));
						// prepStat.setDate(i+1,(value==null || value.equals(""))?null:new
						// Date(DateCaster.toDateAdvanced(value,pc.getTimeZone()).getTime()));
					}
					else if (type == TIME) prepStat.setTime(i + 1, (value.equals("")) ? null : new Time(DateCaster.toDateAdvanced(value, pc.getTimeZone()).getTime()));
					else if (type == TIMESTAMP)
						prepStat.setTimestamp(i + 1, (value.equals("")) ? null : new Timestamp(DateCaster.toDateAdvanced(value, pc.getTimeZone()).getTime()));
					else if (type == BINARY) prepStat.setBytes(i + 1, Caster.toBinary(value));
					else SystemOut.print("HSQLDB QoQ unsupported type [" + type + " / " + toUsableType(type) + "] at row [" + y + "]");
				}

			}
			rowsToCommit++;
			prepStat.addBatch();
			if (y % 5000 == 0) {
				prepStat.executeBatch();
				rowsToCommit = 0;
			}
		}
		if (rowsToCommit > 0) prepStat.executeBatch();
		conn.commit();
		Statement stat2 = conn.createStatement();
		stat2.execute("SET FILES LOG TRUE");
		// SystemOut.print("Populate Table: [" + dbTableName + "] with [" + rows + "] rows, [" + count + "]
		// //columns, took " + stopwatch.time() + "ms");
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
		return toUsableType(type, false);
	}

	private static String toUsableType(int type, boolean caseSensitive) {
		if (type == Types.VARCHAR) return caseSensitive ? "VARCHAR" : "VARCHAR_IGNORECASE";
		if (type == Types.NVARCHAR) return caseSensitive ? "VARCHAR" : "VARCHAR_IGNORECASE";
		if (type == Types.NCHAR) return "CHAR";
		if (type == Types.NCLOB) return "CLOB";
		if (type == Types.JAVA_OBJECT) return "OBJECT";

		return QueryImpl.getColumTypeName(type);

	}

	/**
	 * find out which columns are used for query, by creating a view and reading the VIEW_COLUMN_USAGE
	 *
	 * @param conn
	 * @param sql
	 * @param createSql
	 * @throws PageException
	 */
	private static Struct getUsedColumnsForQuery(Connection conn, SQL sql, StringBuilder createSql) throws PageException {
		// Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_MILLI);
		// stopwatch.start();

		Key sqlKey = Caster.toKey(sql.toString() + createSql.toString());
		Object cachedColumnUsage = columnUsageCache.get(sqlKey, null);
		if (cachedColumnUsage != null) {
			return (Struct) cachedColumnUsage;
		}

		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String view = "V_QOQ_TEMP";
		Struct tables = new StructImpl();

		// TODO consider if worth doing, if recordcount / column count is too small

		try {
			Statement stat = conn.createStatement();
			stat.execute("CREATE VIEW " + view + " AS " + sql.toString()); // + StringUtil.toUpperCase(sql.toString()));

			StringBuilder viewUsage = new StringBuilder("SELECT COLUMN_NAME, TABLE_NAME ");
			viewUsage.append("FROM INFORMATION_SCHEMA.VIEW_COLUMN_USAGE WHERE VIEW_NAME='");
			viewUsage.append(view);
			viewUsage.append("' ORDER BY TABLE_NAME, COLUMN_NAME");
			rs = stat.executeQuery(viewUsage.toString());
			// dump out the column names, not sure what they are lol (can be removed)
			rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			String name = null;
			int colPos = -1;
			int tablePos = -1;
			for (int i = 1; i <= columnCount; i++) {
				name = rsmd.getColumnName(i);
				if (name.equals("COLUMN_NAME")) colPos = i;
				else if (name.equals("TABLE_NAME")) tablePos = i;
			}

			// load used tables and columns into a nested struct
			while (rs.next()) {
				Key tableName = Caster.toKey(rs.getString(tablePos));
				if (!tables.containsKey(tableName)) tables.setEL(tableName, new StructImpl());
				Struct tableCols = ((Struct) tables.get(tableName));
				tableCols.setEL(Caster.toKey(rs.getString(colPos)), null);
			}
			// don't need the view anymore, bye bye
			stat.execute("DROP VIEW " + view);
		}
		catch (Exception e) {
			tables = null; // give up trying to be smart
		}
		finally {
			try {
				if (rs != null) {
					rs.close();
				}
			}
			catch (SQLException e) {
			}
		}
		// SystemOut.print("getUsedColumnsForQuery: took " + stopwatch.time());
		if (tables != null) columnUsageCache.setEL(sqlKey, tables);
		return tables;
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
		return execute(pc, sql, maxrows, fetchsize, timeout, false, "auto");
	}

	public QueryImpl execute(PageContext pc, final SQL sql, int maxrows, int fetchsize, TimeSpan timeout, boolean caseSensitive, String engine) throws PageException {
		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_NANO);
		stopwatch.start();
		String prettySQL = null;
		Selects selects = null;

		boolean useNative = "native".equals(engine) || "auto".equals(engine);
		boolean useHsqldb = "hsqldb".equals(engine) || "auto".equals(engine);

		Exception qoqException = null;

		// First Chance - try native QoQ
		if (useNative) {
			QoQ qoq = new QoQ();
			qoq.setCaseSensitive(caseSensitive);
			try {
				SelectParser parser = new SelectParser();
				selects = parser.parse(sql.getSQLString());

				// Try native QoQ - it returns null if it can't handle the query (e.g., joins)
				// This avoids expensive exception-based flow control for unsupported features
				QueryImpl q = qoq.execute(pc, sql, selects, maxrows);
				if (q != null) {
					q.setExecutionTime(stopwatch.time());
					return q;
				}
				// else: fall through to HSQLDB (Second Chance)
			}
			catch (SQLParserException spe) {
				qoqException = spe;
				if (spe.getCause() != null && spe.getCause() instanceof IllegalQoQException) {
					throw Caster.toPageException(spe);
				}
				prettySQL = SQLPrettyfier.prettyfie(sql.getSQLString());
				try {
					executer.setCaseSensitive(caseSensitive);
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
			if (hsqldbDisable || !useHsqldb || rootCause instanceof IllegalQoQException) {
				// re-throw the original outer exception
				throw Caster.toPageException(qoqException);
			}

			// Debugging option to to log all QoQ that fall back on hsqldb in the datasource log
			if (hsqldbDebug) {
				ThreadLocalPageContext.getLog(pc, "datasource").error("QoQ [" + sql.getSQLString() + "] errored and is falling back to HyperSQL.", qoqException);
			}
		}
		else if (!useHsqldb || hsqldbDisable) {
			// Native returned null (couldn't handle the query, e.g. JOINs) with no exception, and
			// HSQLDB fallback is blocked.
			String which = !useHsqldb ? "engine=native" : "lucee.qoq.hsqldb.disable=true";
			throw new DatabaseException("Native QoQ engine could not handle this query and HSQLDB fallback is disabled (" + which + ")", null, sql, null);
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
			return _execute(pc, sql, maxrows, fetchsize, timeout, stopwatch, tables, isUnion, caseSensitive);

		}
		catch (ParseException e) {
			throw new DatabaseException(e.getMessage(), null, sql, null);
		}

	}

	private QueryImpl _execute(PageContext pc, SQL sql, int maxrows, int fetchsize, TimeSpan timeout, Stopwatch stopwatch, Set<String> tables, boolean isUnion,
			boolean caseSensitive) throws PageException {
		try {
			return __execute(pc, SQLImpl.duplicate(sql), maxrows, fetchsize, timeout, stopwatch, tables, false, caseSensitive);
		}
		catch (PageException pe) {
			if (isUnion || StringUtil.indexOf(pe.getMessage(), "NumberFormatException:") != -1) {
				// SystemOut.print("HSQLDB Retry with Simple Types after: " + pe.getMessage());
				return __execute(pc, sql, maxrows, fetchsize, timeout, stopwatch, tables, true, caseSensitive);
			}
			throw pe;
		}
	}

	private static ClassDefinition<?> getHSQLDBClassDefinition(PageContext pc) throws ApplicationException {
		ClassDefinition<?> cd = hsqldbClassDefinition;
		if (cd == null) {
			ConfigPro config = (ConfigPro) pc.getConfig();
			Log log = ((PageContextImpl) pc).getLog("datasource");

			JDBCDriver driver = config.getJDBCDriverById("hsqldb", null);
			if (driver != null) {
				cd = driver.cd;
			}
			else {
				synchronized (CLASS_DEF_LOCK) {
					cd = hsqldbClassDefinition;
					if (cd == null) {
						if (LogUtil.doesDebug(log)) {
							log.debug("query-of-query", "HSQLDB extension not installed. Attempting to download and use embedded HSQLDB driver (org.lucee.hsqldb:2.7.2.jdk11).");
						}
						cd = new ClassDefinitionImpl<>("org.hsqldb.jdbcDriver", "org.lucee.hsqldb", "2.7.2.jdk11", config.getIdentification());
						try {
							cd.getClazz();
						}
						catch (Exception e) {
							ApplicationException ae = new ApplicationException("Failed to load HSQLDB driver for query-of-query fallback. " + "Please install the HSQLDB extension "
									+ "or define a custom 'qoq' datasource to resolve this issue.");
							ExceptionUtil.initCauseEL(ae, e);
							throw ae;
						}
						hsqldbClassDefinition = cd;
					}
				}
			}
		}
		return cd;
	}

	private static DataSource getHSQLDBDatasource(PageContext pc, SQL sql, int dbNum) throws ClassException, BundleException, SQLException, ApplicationException {

		// Check cached DataSource for this dbNum
		DataSource ds = dsCache[dbNum];
		if (ds != null) return ds;

		ConfigPro config = (ConfigPro) pc.getConfig();
		Log log = ((PageContextImpl) pc).getLog("datasource");

		// Only log the fallback warning once per JVM
		if (!hsqldbFallbackWarningLogged && LogUtil.doesWarn(log)) {
			hsqldbFallbackWarningLogged = true;
			log.warn("query-of-query", "Query-of-query statement could not be processed by the native SQL parser and is falling back to HSQLDB datasource. " + "Statement: [" + sql	+ "]. ");
		}

		// Get or load HSQLDB class definition (cached after first load)
		ClassDefinition<?> cd = getHSQLDBClassDefinition(pc);

		// Create datasource with unique database name using dbNum
		String dbName = "qoq_" + dbNum;
		// nulls_first=true + nulls_order=false makes HSQLDB treat nulls as a low
		// value (first in ASC, last in DESC), matching native QoQ engine + ACF.
		String nullsOrdering = hsqldbNullsLastInDesc ? "sql.nulls_first=true;sql.nulls_order=false;" : "";
		String connStr = "jdbc:hsqldb:mem:" + dbName + ";sql.regular_names=false;sql.enforce_strict_size=false;sql.enforce_types=false;sql.concat_nulls=false;" + nullsOrdering;

		// We don't use connection pooling - each query creates a fresh connection and closes it immediately.
		// Concurrency is controlled by the BlockingQueue (dbQueue), not by connection pool limits.
		ds = new DataSourceImpl(config, QOQ_DATASOURCE_NAME, cd, "hypersonic-hsqldb",
				connStr, null, null, "", -1, "sa", "", null,
				-1, -1, -1, 0, 0, 0, -1,
				true, true, DataSource.ALLOW_ALL, new StructImpl(), false, false, false, null, "", ParamSyntaxImpl.DEFAULT, false, false, false, false, log);

		dsCache[dbNum] = ds;
		return ds;
	}

	public static QueryImpl __execute(PageContext pc, SQL sql, int maxrows, int fetchsize, TimeSpan timeout, Stopwatch stopwatch, Set<String> tables, boolean doSimpleTypes)
			throws PageException {
		return __execute(pc, sql, maxrows, fetchsize, timeout, stopwatch, tables, doSimpleTypes, false);
	}

	public static QueryImpl __execute(PageContext pc, SQL sql, int maxrows, int fetchsize, TimeSpan timeout, Stopwatch stopwatch, Set<String> tables, boolean doSimpleTypes,
			boolean caseSensitive) throws PageException {

		ArrayList<String> qoqTables = new ArrayList<String>();
		QueryImpl nqr = null;
		ConfigPro config = (ConfigPro) pc.getConfig();
		DatasourceConnection dc = null;
		Connection conn = null;
		Integer dbNum = null;
		try {
			// Get a database number from the queue (blocks if all DBs are in use)
			try {
				long remainingMs = pc.getRequestTimeout() - (System.currentTimeMillis() - pc.getStartTime());
				if (remainingMs <= 0) {
					throw new DatabaseException("Request timeout exceeded while waiting for available HSQLDB instance", null, sql, null);
				}
				dbNum = dbQueue.poll(remainingMs, TimeUnit.MILLISECONDS);
				if (dbNum == null) {
					throw new DatabaseException("Timeout waiting for available HSQLDB instance after " + remainingMs + "ms", null, sql, null);
				}
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new DatabaseException("Interrupted while waiting for available Query of Query HSQLDB instance", null, sql, null);
			}

			// Create datasource with unique database name
			DataSource ds = getHSQLDBDatasource(pc, sql, dbNum);
			conn = ds.getConnection(config, null, null);
			// Wrap in DatasourceConnection for QueryImpl compatibility (but with null pool)
			dc = new DatasourceConnectionImpl(null, conn, (DataSourcePro) ds, null, null);
			DBUtil.setAutoCommitEL(conn, false);

				try {
					Iterator<String> it = tables.iterator();
					String cfQueryName = null; // name of the source cfml query variable
					String dbTableName = null; // name of the target table in the database
					String modSql = null;

					// First pass: fix up SQL table names and build the full CREATE SQL for cache key
					StringBuilder createSql = new StringBuilder();
					ArrayList<String[]> tableNames = new ArrayList<String[]>(); // [cfQueryName, dbTableName] pairs
					while (it.hasNext()) {
						cfQueryName = it.next().toString();
						dbTableName = cfQueryName.replace('.', '_');

						if (!cfQueryName.toLowerCase().equals(dbTableName.toLowerCase())) {
							// TODO this could match the wrong strings, ??
							modSql = StringUtil.replace(sql.getSQLString(), cfQueryName, dbTableName, false);
							sql.setSQLString(modSql);
						}
						if (sql.getItems() != null && sql.getItems().length > 0) sql = new SQLImpl(sql.toString());
						// build the full CREATE SQL (all columns) for a stable cache key
						createSql.append(buildCreateTableSql(pc, dbTableName, cfQueryName, doSimpleTypes, caseSensitive));
						tableNames.add(new String[] { cfQueryName, dbTableName });
					}

					// check if we already know which columns are needed (cache-only check)
					Key cacheKey = Caster.toKey(sql.toString() + createSql.toString());
					Struct allTableColumns = (Struct) columnUsageCache.get(cacheKey, null);

					if (allTableColumns != null) {
						// Cache hit — create minimal tables with only needed columns
						for (String[] tn : tableNames) {
							Key tableKey = Caster.toKey(tn[1]);
							Struct tableCols = allTableColumns.containsKey(tableKey) ? ((Struct) allTableColumns.get(tableKey)) : null;
							createTable(conn, pc, tn[1], tn[0], doSimpleTypes, caseSensitive, tableCols);
							qoqTables.add(tn[1]);
						}
					}
					else {
						// Cache miss — create full tables (needed for VIEW discovery), then discover used columns
						for (String[] tn : tableNames) {
							createTable(conn, pc, tn[1], tn[0], doSimpleTypes, caseSensitive);
							qoqTables.add(tn[1]);
						}
						allTableColumns = getUsedColumnsForQuery(conn, sql, createSql);
					}
					// load data into tables
					Struct tableColumns = null;
					Key tableKey = null;
					for (String[] tn : tableNames) {
						tableKey = Caster.toKey(tn[1]);
						if (allTableColumns != null && allTableColumns.containsKey(tableKey)) {
							tableColumns = ((Struct) allTableColumns.get(tableKey));
						}
						else {
							tableColumns = null;
						}

						// only populate tables with data if there are used columns, or no needed column data at all
						if (tableColumns == null || tableColumns.size() > 0) {
							populateTable(conn, pc, tn[1], tn[0], doSimpleTypes, tableColumns);
						}
					}

					DBUtil.setReadOnlyEL(conn, true);
					try {
						nqr = new QueryImpl(pc, dc, sql, maxrows, fetchsize, timeout, "query", null, false, false, null);
					}
					catch (PageException pe) {
						throw pe;
					}
					finally {
						DBUtil.setReadOnlyEL(conn, false);
						DBUtil.commitEL(conn);
						DBUtil.setAutoCommitEL(conn, true);
					}

			}
			catch (SQLException e) {
				IllegalQoQException ex = new IllegalQoQException("QoQ HSQLDB: error executing sql statement on query.", e.getMessage(), sql, null);
				ExceptionUtil.initCauseEL(ex, e);
				throw ex;
			}
		}
		catch (Exception ee) {
			IllegalQoQException ex = new IllegalQoQException("QoQ HSQLDB: error executing sql statement on query.", ee.getMessage(), sql, null);
			ExceptionUtil.initCauseEL(ex, ee);
			throw ex;
		}
		finally {
			// Close connection directly (no pooling) - temp tables auto-drop on close
			if (conn != null) {
				try {
					conn.close();
				}
				catch (SQLException e) {
					// Log but don't throw - we're cleaning up
				}
			}
			// Return database number to queue for reuse
			if (dbNum != null) {
				dbQueue.offer(dbNum);
			}
		}
		// TODO we are swallowing errors, shouldn't be passing a null value back
		if (nqr != null) nqr.setExecutionTime(stopwatch.time());
		return nqr;

	}
}