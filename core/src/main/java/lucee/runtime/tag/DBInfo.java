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
package lucee.runtime.tag;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.sql.SQLUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageContext;
import lucee.runtime.config.Constants;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceManager;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.timer.Stopwatch;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.SVArray;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * Handles all interactions with files. The attributes you use with cffile depend on the value of
 * the action attribute. For example, if the action = "write", use the attributes associated with
 * writing a text file.
 *
 *
 *
 **/
public final class DBInfo extends TagImpl {

	private static final Key TABLE_NAME = KeyImpl.intern("TABLE_NAME");
	private static final Key COLUMN_NAME = KeyImpl.intern("COLUMN_NAME");
	private static final Key IS_PRIMARYKEY = KeyImpl.intern("IS_PRIMARYKEY");
	private static final Key IS_FOREIGNKEY = KeyImpl.intern("IS_FOREIGNKEY");
	private static final Key COLUMN_DEF = KeyImpl.intern("COLUMN_DEF");
	private static final Key COLUMN_DEFAULT_VALUE = KeyImpl.intern("COLUMN_DEFAULT_VALUE");
	private static final Key COLUMN_DEFAULT = KeyImpl.intern("COLUMN_DEFAULT");
	private static final Key REFERENCED_PRIMARYKEY = KeyImpl.intern("REFERENCED_PRIMARYKEY");
	private static final Key REFERENCED_PRIMARYKEY_TABLE = KeyImpl.intern("REFERENCED_PRIMARYKEY_TABLE");
	private static final Key USER = KeyImpl.intern("USER");
	private static final Key TABLE_SCHEM = KeyImpl.intern("TABLE_SCHEM");
	private static final Key DECIMAL_DIGITS = KeyImpl.intern("DECIMAL_DIGITS");

	private static final Key DATABASE_NAME = KeyImpl.intern("database_name");
	private static final Key TABLE_CAT = KeyImpl.intern("TABLE_CAT");
	private static final Key PROCEDURE = KeyImpl.intern("procedure");
	private static final Key CATALOG = KeyImpl.intern("catalog");
	private static final Key SCHEMA = KeyImpl.intern("schema");
	private static final Key DATABASE_PRODUCTNAME = KeyImpl.intern("DATABASE_PRODUCTNAME");
	private static final Key DATABASE_VERSION = KeyImpl.intern("DATABASE_VERSION");
	private static final Key DRIVER_NAME = KeyImpl.intern("DRIVER_NAME");
	private static final Key DRIVER_VERSION = KeyImpl.intern("DRIVER_VERSION");
	private static final Key JDBC_MAJOR_VERSION = KeyImpl.intern("JDBC_MAJOR_VERSION");
	private static final Key JDBC_MINOR_VERSION = KeyImpl.intern("JDBC_MINOR_VERSION");

	private static final int TYPE_NONE = 0;
	private static final int TYPE_DBNAMES = 1;
	private static final int TYPE_TABLES = 2;
	private static final int TYPE_TABLE_COLUMNS = 3;
	private static final int TYPE_VERSION = 4;
	private static final int TYPE_PROCEDURES = 5;
	private static final int TYPE_PROCEDURE_COLUMNS = 6;
	private static final int TYPE_FOREIGNKEYS = 7;
	private static final int TYPE_INDEX = 8;
	private static final int TYPE_USERS = 9;
	private static final int TYPE_TERMS = 10;
	private static final Collection.Key CARDINALITY = KeyImpl.init("CARDINALITY");

	private DataSource datasource;
	private String name;
	private int type;
	private String dbname;
	private String password;
	private String pattern;
	private String table;
	private String procedure;
	private String username;
	private String strType;

	@Override
	public void release() {
		super.release();
		datasource = null;
		name = null;
		type = TYPE_NONE;
		dbname = null;
		password = null;
		pattern = null;
		table = null;
		procedure = null;
		username = null;

	}

	/**
	 * @param procedure the procedure to set
	 */
	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}

	/**
	 * @param datasource the datasource to set
	 */
	public void setDatasource(String datasource) throws PageException { // exist for old bytecode in archives
		this.datasource = lucee.runtime.tag.Query.toDatasource(pageContext, datasource);
	}

	public void setDatasource(Object datasource) throws PageException {
		this.datasource = lucee.runtime.tag.Query.toDatasource(pageContext, datasource);
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param type the type to set
	 * @throws ApplicationException
	 */
	public void setType(String strType) throws ApplicationException {
		this.strType = strType;
		strType = strType.toLowerCase().trim();

		if ("dbnames".equals(strType)) this.type = TYPE_DBNAMES;
		else if ("dbname".equals(strType)) this.type = TYPE_DBNAMES;
		else if ("tables".equals(strType)) this.type = TYPE_TABLES;
		else if ("table".equals(strType)) this.type = TYPE_TABLES;
		else if ("columns".equals(strType)) this.type = TYPE_TABLE_COLUMNS;
		else if ("column".equals(strType)) this.type = TYPE_TABLE_COLUMNS;
		else if ("version".equals(strType)) this.type = TYPE_VERSION;
		else if ("procedures".equals(strType)) this.type = TYPE_PROCEDURES;
		else if ("procedure".equals(strType)) this.type = TYPE_PROCEDURES;

		else if ("table_columns".equals(strType)) this.type = TYPE_TABLE_COLUMNS;
		else if ("table_column".equals(strType)) this.type = TYPE_TABLE_COLUMNS;
		else if ("column_table".equals(strType)) this.type = TYPE_TABLE_COLUMNS;
		else if ("column_tables".equals(strType)) this.type = TYPE_TABLE_COLUMNS;

		else if ("tablecolumns".equals(strType)) this.type = TYPE_TABLE_COLUMNS;
		else if ("tablecolumn".equals(strType)) this.type = TYPE_TABLE_COLUMNS;
		else if ("columntable".equals(strType)) this.type = TYPE_TABLE_COLUMNS;
		else if ("columntables".equals(strType)) this.type = TYPE_TABLE_COLUMNS;

		else if ("procedure_columns".equals(strType)) this.type = TYPE_PROCEDURE_COLUMNS;
		else if ("procedure_column".equals(strType)) this.type = TYPE_PROCEDURE_COLUMNS;
		else if ("column_procedure".equals(strType)) this.type = TYPE_PROCEDURE_COLUMNS;
		else if ("column_procedures".equals(strType)) this.type = TYPE_PROCEDURE_COLUMNS;

		else if ("procedurecolumns".equals(strType)) this.type = TYPE_PROCEDURE_COLUMNS;
		else if ("procedurecolumn".equals(strType)) this.type = TYPE_PROCEDURE_COLUMNS;
		else if ("columnprocedure".equals(strType)) this.type = TYPE_PROCEDURE_COLUMNS;
		else if ("columnprocedures".equals(strType)) this.type = TYPE_PROCEDURE_COLUMNS;

		else if ("foreignkeys".equals(strType)) this.type = TYPE_FOREIGNKEYS;
		else if ("foreignkey".equals(strType)) this.type = TYPE_FOREIGNKEYS;
		else if ("index".equals(strType)) this.type = TYPE_INDEX;
		else if ("users".equals(strType)) this.type = TYPE_USERS;
		else if ("user".equals(strType)) this.type = TYPE_USERS;

		else if ("term".equals(strType)) this.type = TYPE_TERMS;
		else if ("terms".equals(strType)) this.type = TYPE_TERMS;

		else throw new ApplicationException("invalid value for attribute type [" + strType + "]",
				"valid values are [dbname,tables,columns,version,procedures,foreignkeys,index,users]");

	}

	/**
	 * @param dbname the dbname to set
	 */
	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public void setDbnames(String dbname) {
		this.dbname = dbname;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @param table the table to set
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public int doStartTag() throws PageException {
		Object ds = getDatasource(pageContext, datasource);

		DataSourceManager manager = pageContext.getDataSourceManager();
		DatasourceConnection dc = ds instanceof DataSource ? manager.getConnection(pageContext, (DataSource) ds, username, password)
				: manager.getConnection(pageContext, Caster.toString(ds), username, password);

		try {
			if (type == TYPE_TABLE_COLUMNS) typeColumns(dc.getConnection());
			else if (type == TYPE_DBNAMES) typeDBNames(dc.getConnection());
			else if (type == TYPE_FOREIGNKEYS) typeForeignKeys(dc.getConnection());
			else if (type == TYPE_INDEX) typeIndex(dc.getConnection());
			else if (type == TYPE_PROCEDURES) typeProcedures(dc.getConnection());
			else if (type == TYPE_PROCEDURE_COLUMNS) typeProcedureColumns(dc.getConnection());
			else if (type == TYPE_TERMS) typeTerms(dc.getConnection().getMetaData());
			else if (type == TYPE_TABLES) typeTables(dc.getConnection());
			else if (type == TYPE_VERSION) typeVersion(dc.getConnection().getMetaData());
			else if (type == TYPE_USERS) typeUsers(dc.getConnection());

		}
		catch (SQLException sqle) {
			throw new DatabaseException(sqle, dc);
		}
		finally {
			manager.releaseConnection(pageContext, dc);
		}

		return SKIP_BODY;
	}

	private void typeColumns(Connection conn) throws PageException, SQLException {
		String _dbName = dbname(conn);
		required("table", table);
		DatabaseMetaData metaData = conn.getMetaData();
		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_NANO);
		stopwatch.start();
		table = setCase(metaData, table);
		pattern = setCase(metaData, pattern);
		if (StringUtil.isEmpty(pattern, true)) pattern = null;

		String schema = null;
		int index = table.indexOf('.');
		if (index > 0) {
			schema = table.substring(0, index);
			table = table.substring(index + 1);
		}

		checkTable(metaData, _dbName);

		Query qry = new QueryImpl(metaData.getColumns(_dbName, schema, table, StringUtil.isEmpty(pattern) ? "%" : pattern), "query", pageContext.getTimeZone());

		int len = qry.getRecordcount();

		if (qry.getColumn(COLUMN_DEF, null) != null) qry.rename(COLUMN_DEF, COLUMN_DEFAULT_VALUE);
		else if (qry.getColumn(COLUMN_DEFAULT, null) != null) qry.rename(COLUMN_DEFAULT, COLUMN_DEFAULT_VALUE);

		// make sure decimal digits exists
		QueryColumn col = qry.getColumn(DECIMAL_DIGITS, null);
		if (col == null) {
			Array arr = new ArrayImpl();
			for (int i = 1; i <= len; i++) {
				arr.append(lucee.runtime.op.Constants.DOUBLE_ZERO);
			}
			qry.addColumn(DECIMAL_DIGITS, arr);
		}

		// add is primary
		Map<String, Set<String>> primaries = new HashMap<>();
		Array isPrimary = new ArrayImpl();
		Set<String> set;
		Object o;
		String tblCat, tblScheme, tblName;
		for (int i = 1; i <= len; i++) {

			// decimal digits
			o = qry.getAt(DECIMAL_DIGITS, i, null);
			if (o == null) qry.setAtEL(DECIMAL_DIGITS, i, lucee.runtime.op.Constants.DOUBLE_ZERO);

			tblCat = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_CAT, i), null), true);
			tblScheme = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_SCHEM, i), null), true);
			tblName = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_NAME, i), null), true);

			set = primaries.get(tblName);
			if (set == null) {
				try {
					set = toSet(metaData.getPrimaryKeys(tblCat, tblScheme, tblName), true, "COLUMN_NAME");
					primaries.put(tblName, set);
				}
				catch (Exception e) {}
			}
			isPrimary.append(set != null && set.contains(qry.getAt(COLUMN_NAME, i)) ? "YES" : "NO");
		}

		qry.addColumn(IS_PRIMARYKEY, isPrimary);

		// add is foreignkey
		Map foreigns = new HashMap();
		Array isForeign = new ArrayImpl();
		Array refPrim = new ArrayImpl();
		Array refPrimTbl = new ArrayImpl();
		// Map map,inner;
		Map<String, Map<String, SVArray>> map;
		Map<String, SVArray> inner;
		for (int i = 1; i <= len; i++) {

			tblCat = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_CAT, i), null), true);
			tblScheme = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_SCHEM, i), null), true);
			tblName = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_NAME, i), null), true);

			map = (Map) foreigns.get(tblName);
			if (map == null) {
				map = toMap(metaData.getImportedKeys(tblCat, tblScheme, tblName), true, "FKCOLUMN_NAME", new String[] { "PKCOLUMN_NAME", "PKTABLE_NAME" });
				foreigns.put(tblName, map);
			}
			inner = map.get(qry.getAt(COLUMN_NAME, i));
			if (inner != null) {
				isForeign.append("YES");
				refPrim.append(inner.get("PKCOLUMN_NAME"));
				refPrimTbl.append(inner.get("PKTABLE_NAME"));
			}
			else {
				isForeign.append("NO");
				refPrim.append("N/A");
				refPrimTbl.append("N/A");
			}
		}

		qry.addColumn(IS_FOREIGNKEY, isForeign);
		qry.addColumn(REFERENCED_PRIMARYKEY, refPrim);
		qry.addColumn(REFERENCED_PRIMARYKEY_TABLE, refPrimTbl);

		qry.setExecutionTime(stopwatch.time());

		pageContext.setVariable(name, qry);
	}

	private Map<String, Map<String, SVArray>> toMap(ResultSet result, boolean closeResult, String columnName, String[] additional) throws SQLException {
		Map<String, Map<String, SVArray>> map = new HashMap<String, Map<String, SVArray>>();
		Map<String, SVArray> inner;
		String col;
		SVArray item;
		if (result == null) return map;
		try {
			while (result.next()) {
				col = result.getString(columnName);
				inner = map.get(col);
				if (inner != null) {
					for (int i = 0; i < additional.length; i++) {
						item = inner.get(additional[i]);
						item.add(result.getString(additional[i]));
						item.setPosition(item.size());
					}
				}
				else {
					inner = new HashMap<String, SVArray>();
					map.put(col, inner);
					for (int i = 0; i < additional.length; i++) {
						item = new SVArray();
						item.add(result.getString(additional[i]));
						inner.put(additional[i], item);
					}
				}
			}
		}
		finally {
			if (closeResult) IOUtil.close(result);
		}
		return map;
	}

	private Set<String> toSet(ResultSet result, boolean closeResult, String columnName) throws SQLException {
		Set<String> set = new HashSet<String>();
		if (result == null) return set;

		try {
			while (result.next()) {
				set.add(result.getString(columnName));
			}
			return set;
		}
		finally {
			if (closeResult) IOUtil.close(result);
		}

	}

	private void typeDBNames(Connection conn) throws PageException, SQLException {

		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_NANO);
		stopwatch.start();
		DatabaseMetaData metaData = conn.getMetaData();
		lucee.runtime.type.Query catalogs = new QueryImpl(metaData.getCatalogs(), "query", pageContext.getTimeZone());
		lucee.runtime.type.Query scheme = new QueryImpl(metaData.getSchemas(dbname(conn), null), "query", pageContext.getTimeZone());

		Pattern p = null;
		if (pattern != null && !"%".equals(pattern)) p = SQLUtil.pattern(pattern, true);

		String[] columns = new String[] { "database_name", "type" };
		String[] types = new String[] { "VARCHAR", "VARCHAR" };
		lucee.runtime.type.Query qry = new QueryImpl(columns, types, 0, "query");
		int row = 1, len = catalogs.getRecordcount();
		String value;
		// catalog
		for (int i = 1; i <= len; i++) {
			value = (String) catalogs.getAt(TABLE_CAT, i);
			if (!matchPattern(value, p)) continue;
			qry.addRow();
			qry.setAt(DATABASE_NAME, row, value);
			qry.setAt(KeyConstants._type, row, "CATALOG");
			row++;
		}
		// scheme
		len = scheme.getRecordcount();
		for (int i = 1; i <= len; i++) {
			value = (String) scheme.getAt(TABLE_SCHEM, i);
			if (!matchPattern(value, p)) continue;
			qry.addRow();
			qry.setAt(DATABASE_NAME, row, value);
			qry.setAt(KeyConstants._type, row, "SCHEMA");
			row++;
		}

		qry.setExecutionTime(stopwatch.time());

		pageContext.setVariable(name, qry);
	}

	private void typeForeignKeys(Connection conn) throws PageException, SQLException {
		required("table", table);
		DatabaseMetaData metaData = conn.getMetaData();
		String _dbName = dbname(conn);
		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_NANO);
		stopwatch.start();
		table = setCase(metaData, table);
		int index = table.indexOf('.');
		String schema = null;
		if (index > 0) {
			schema = table.substring(0, index);
			table = table.substring(index + 1);
		}

		checkTable(metaData, _dbName);

		lucee.runtime.type.Query qry = new QueryImpl(metaData.getExportedKeys(_dbName, schema, table), "query", pageContext.getTimeZone());
		qry.setExecutionTime(stopwatch.time());

		pageContext.setVariable(name, qry);
	}

	private void checkTable(DatabaseMetaData metaData, String _dbName) throws SQLException, ApplicationException {
		ResultSet tables = null;
		if (StringUtil.isEmpty(table)) return;
		try {
			tables = metaData.getTables(_dbName, null, setCase(metaData, table), null);
			if (!tables.next()) throw new ApplicationException("there is no table that match the following pattern [" + table + "]");
		}
		finally {
			if (tables != null) tables.close();

		}
	}

	private String setCase(DatabaseMetaData metaData, String id) throws SQLException {
		if (StringUtil.isEmpty(id)) return "%";

		if (metaData.storesLowerCaseIdentifiers()) return id.toLowerCase();
		if (metaData.storesUpperCaseIdentifiers()) return id.toUpperCase();
		return id;
	}

	private void typeIndex(Connection conn) throws PageException, SQLException {
		required("table", table);
		DatabaseMetaData metaData = conn.getMetaData();
		String _dbName = dbname(conn);
		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_NANO);
		stopwatch.start();

		table = setCase(metaData, table);
		int index = table.indexOf('.');
		String schema = null;
		if (index > 0) {
			schema = table.substring(0, index);
			table = table.substring(index + 1);
		}

		checkTable(metaData, _dbName);

		ResultSet tables = metaData.getIndexInfo(_dbName, schema, table, false, true);
		lucee.runtime.type.Query qry = new QueryImpl(tables, "query", pageContext.getTimeZone());

		// type int 2 string
		int rows = qry.getRecordcount();
		String strType;
		int type, card;
		for (int row = 1; row <= rows; row++) {

			// type
			switch (type = Caster.toIntValue(qry.getAt(KeyConstants._type, row))) {
			case 0:
				strType = "Table Statistic";
				break;
			case 1:
				strType = "Clustered Index";
				break;
			case 2:
				strType = "Hashed Index";
				break;
			case 3:
				strType = "Other Index";
				break;
			default:
				strType = Caster.toString(type);
			}
			qry.setAt(KeyConstants._type, row, strType);

			// CARDINALITY
			card = Caster.toIntValue(qry.getAt(CARDINALITY, row), 0);
			qry.setAt(CARDINALITY, row, Caster.toDouble(card));

		}
		qry.setExecutionTime(stopwatch.time());

		pageContext.setVariable(name, qry);
	}

	private void typeProcedures(Connection conn) throws SQLException, PageException {
		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_NANO);
		stopwatch.start();
		DatabaseMetaData metaData = conn.getMetaData();
		String schema = null;
		pattern = setCase(metaData, pattern);
		if (StringUtil.isEmpty(pattern, true)) {
			pattern = null;
		}

		lucee.runtime.type.Query qry = new QueryImpl(metaData.getProcedures(dbname(conn), schema, StringUtil.isEmpty(pattern) ? "%" : pattern), "query", pageContext.getTimeZone());
		qry.setExecutionTime(stopwatch.time());

		pageContext.setVariable(name, qry);
	}

	private void typeProcedureColumns(Connection conn) throws SQLException, PageException {
		required("procedure", procedure);
		DatabaseMetaData metaData = conn.getMetaData();
		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_NANO);
		stopwatch.start();

		procedure = setCase(metaData, procedure);
		pattern = setCase(metaData, pattern);
		if (StringUtil.isEmpty(pattern, true)) pattern = null;
		String schema = null;
		int index = procedure.indexOf('.');
		if (index > 0) {
			schema = procedure.substring(0, index);
			procedure = procedure.substring(index + 1);
		}

		lucee.runtime.type.Query qry = new QueryImpl(
				metaData.getProcedureColumns(dbname(conn), schema, StringUtil.isEmpty(procedure) ? "%" : procedure, StringUtil.isEmpty(pattern) ? "%" : pattern), "query",
				pageContext.getTimeZone());
		qry.setExecutionTime(stopwatch.time());

		pageContext.setVariable(name, qry);
	}

	private void typeTerms(DatabaseMetaData metaData) throws SQLException, PageException {
		Struct sct = new StructImpl();
		sct.setEL(PROCEDURE, metaData.getProcedureTerm());
		sct.setEL(CATALOG, metaData.getCatalogTerm());
		sct.setEL(SCHEMA, metaData.getSchemaTerm());

		pageContext.setVariable(name, sct);
	}

	private void typeTables(Connection conn) throws PageException, SQLException {
		DatabaseMetaData metaData = conn.getMetaData();
		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_NANO);
		stopwatch.start();

		pattern = setCase(metaData, pattern);
		lucee.runtime.type.Query qry = new QueryImpl(metaData.getTables(dbname(conn), null, StringUtil.isEmpty(pattern) ? "%" : pattern, null), "query", pageContext.getTimeZone());
		qry.setExecutionTime(stopwatch.time());

		pageContext.setVariable(name, qry);
	}

	private String dbname(Connection conn) {
		if (!StringUtil.isEmpty(dbname, true)) return dbname.trim();
		try {
			return conn.getCatalog();
		}
		catch (SQLException e) {
			return null;
		}
	}

	private void typeVersion(DatabaseMetaData metaData) throws PageException, SQLException {

		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_NANO);
		stopwatch.start();

		Key[] columns = new Key[] { DATABASE_PRODUCTNAME, DATABASE_VERSION, DRIVER_NAME, DRIVER_VERSION, JDBC_MAJOR_VERSION, JDBC_MINOR_VERSION };
		String[] types = new String[] { "VARCHAR", "VARCHAR", "VARCHAR", "VARCHAR", "DOUBLE", "DOUBLE" };

		lucee.runtime.type.Query qry = new QueryImpl(columns, types, 1, "query");

		qry.setAt(DATABASE_PRODUCTNAME, 1, metaData.getDatabaseProductName());
		qry.setAt(DATABASE_VERSION, 1, metaData.getDatabaseProductVersion());
		qry.setAt(DRIVER_NAME, 1, metaData.getDriverName());
		qry.setAt(DRIVER_VERSION, 1, metaData.getDriverVersion());
		qry.setAt(JDBC_MAJOR_VERSION, 1, new Double(metaData.getJDBCMajorVersion()));
		qry.setAt(JDBC_MINOR_VERSION, 1, new Double(metaData.getJDBCMinorVersion()));

		qry.setExecutionTime(stopwatch.time());

		pageContext.setVariable(name, qry);
	}

	private void typeUsers(Connection conn) throws PageException, SQLException {

		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_NANO);
		stopwatch.start();
		DatabaseMetaData metaData = conn.getMetaData();

		checkTable(metaData, dbname(conn));
		ResultSet result = metaData.getSchemas();
		Query qry = new QueryImpl(result, "query", pageContext.getTimeZone());

		qry.rename(TABLE_SCHEM, USER);

		qry.setExecutionTime(stopwatch.time());

		pageContext.setVariable(name, qry);
	}

	private void required(String name, String value) throws ApplicationException {
		if (value == null) throw new ApplicationException("Missing attribute [" + name + "]. The type [" + strType + "] requires the attribute [" + name + "].");
	}

	private static boolean matchPattern(String value, Pattern pattern) {
		if (pattern == null) return true;
		return SQLUtil.match(pattern, value);
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	public static Object getDatasource(PageContext pageContext, DataSource datasource) throws ApplicationException {
		if (datasource == null) {
			Object ds = pageContext.getApplicationContext().getDefDataSource();

			if (StringUtil.isEmpty(ds)) {
				boolean isCFML = pageContext.getRequestDialect() == CFMLEngine.DIALECT_CFML;
				throw new ApplicationException("attribute [datasource] is required, when no default datasource is defined",
						"you can define a default datasource as attribute [defaultdatasource] of the tag "
								+ (isCFML ? Constants.CFML_APPLICATION_TAG_NAME : Constants.LUCEE_APPLICATION_TAG_NAME) + " or as data member of the "
								+ (isCFML ? Constants.CFML_APPLICATION_EVENT_HANDLER : Constants.LUCEE_APPLICATION_EVENT_HANDLER) + " (this.defaultdatasource=\"mydatasource\";)");
			}
			return ds;
		}
		return datasource;
	}
}