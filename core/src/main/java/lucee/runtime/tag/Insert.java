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
package lucee.runtime.tag;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import lucee.commons.db.DBUtil;
import lucee.commons.io.log.Log;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceManager;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.SQL;
import lucee.runtime.db.SQLImpl;
import lucee.runtime.db.SQLItem;
import lucee.runtime.db.SQLItemImpl;
import lucee.runtime.debug.DebuggerImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.functions.displayFormatting.DecimalFormat;
import lucee.runtime.op.Caster;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.query.QueryResult;
import lucee.runtime.type.scope.Form;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.ListUtil;

/**
 * Inserts records in data sources.
 *
 *
 *
 **/
public final class Insert extends TagImpl {

	/** If specified, password overrides the password value specified in the ODBC setup. */
	private String password;

	/** Name of the data source that contains your table. */
	private DataSource datasource;

	/** If specified, username overrides the username value specified in the ODBC setup. */
	private String username;

	/**
	 * A comma-separated list of form fields to insert. If this attribute is not specified, all fields
	 * in the form are included in the operation.
	 */
	private String formfields;

	/**
	 * For data sources that support table ownership such as SQL Server, Oracle, and Sybase SQL
	 ** Anywhere, use this field to specify the owner of the table.
	 */
	private String tableowner = "";

	/** Name of the table you want the form fields inserted in. */
	private String tablename;

	/**
	 * For data sources that support table qualifiers, use this field to specify the qualifier for the
	 ** table. The purpose of table qualifiers varies across drivers. For SQL Server and Oracle, the
	 * qualifier refers to the name of the database that contains the table. For the Intersolv dBase
	 * driver, the qualifier refers to the directory where the DBF files are located.
	 */
	private String tablequalifier = "";

	@Override
	public void release() {
		super.release();
		password = null;
		username = null;
		formfields = null;
		tableowner = "";
		tablequalifier = "";
		datasource = null;
	}

	/**
	 * set the value password If specified, password overrides the password value specified in the ODBC
	 * setup.
	 * 
	 * @param password value to set
	 **/
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * set the value datasource Name of the data source that contains your table.
	 * 
	 * @param datasource value to set
	 **/
	public void setDatasource(String datasource) throws PageException { // exist for old bytecode in archives
		this.datasource = lucee.runtime.tag.Query.toDatasource(pageContext, datasource);
	}

	public void setDatasource(Object datasource) throws PageException {
		this.datasource = lucee.runtime.tag.Query.toDatasource(pageContext, datasource);
	}

	/**
	 * set the value username If specified, username overrides the username value specified in the ODBC
	 * setup.
	 * 
	 * @param username value to set
	 **/
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * set the value formfields A comma-separated list of form fields to insert. If this attribute is
	 * not specified, all fields in the form are included in the operation.
	 * 
	 * @param formfields value to set
	 **/
	public void setFormfields(String formfields) {
		this.formfields = formfields.toLowerCase().trim();
	}

	/**
	 * set the value tableowner For data sources that support table ownership such as SQL Server,
	 * Oracle, and Sybase SQL Anywhere, use this field to specify the owner of the table.
	 * 
	 * @param tableowner value to set
	 **/
	public void setTableowner(String tableowner) {
		this.tableowner = tableowner;
	}

	/**
	 * set the value tablename Name of the table you want the form fields inserted in.
	 * 
	 * @param tablename value to set
	 **/
	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	/**
	 * set the value tablequalifier For data sources that support table qualifiers, use this field to
	 * specify the qualifier for the table. The purpose of table qualifiers varies across drivers. For
	 * SQL Server and Oracle, the qualifier refers to the name of the database that contains the table.
	 * For the Intersolv dBase driver, the qualifier refers to the directory where the DBF files are
	 * located.
	 * 
	 * @param tablequalifier value to set
	 **/
	public void setTablequalifier(String tablequalifier) {
		this.tablequalifier = tablequalifier;
	}

	@Override
	public int doStartTag() {
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws PageException {
		Object ds = DBInfo.getDatasource(pageContext, datasource);

		DataSourceManager manager = pageContext.getDataSourceManager();
		DatasourceConnection dc = ds instanceof DataSource ? manager.getConnection(pageContext, (DataSource) ds, username, password)
				: manager.getConnection(pageContext, Caster.toString(ds), username, password);
		try {

			Struct meta = getMeta(dc, tablequalifier, tableowner, tablename);

			SQL sql = createSQL(meta);
			if (sql != null) {
				QueryImpl query = new QueryImpl(pageContext, dc, sql, -1, -1, null, "query");

				if (pageContext.getConfig().debug()) {
					String dsn = ds instanceof DataSource ? ((DataSource) ds).getName() : Caster.toString(ds);
					boolean logdb = ((ConfigPro) pageContext.getConfig()).hasDebugOptions(ConfigPro.DEBUG_DATABASE);
					if (logdb) {
						boolean debugUsage = DebuggerImpl.debugQueryUsage(pageContext, (QueryResult) query);
						DebuggerImpl di = (DebuggerImpl) pageContext.getDebugger();

						di.addQuery(debugUsage ? query : null, dsn, "", sql, query.getRecordcount(),
								Query.toTemplateLine(pageContext.getConfig(), sourceTemplate, pageContext.getCurrentPageSource()), query.getExecutionTime());
					}
				}

				// log
				Log log = ThreadLocalPageContext.getLog(pageContext, "datasource");
				if (log.getLogLevel() >= Log.LEVEL_INFO) {
					log.info("insert tag", "executed [" + sql.toString().trim() + "] in " + DecimalFormat.call(pageContext, query.getExecutionTime() / 1000000D) + " ms");
				}
			}
			return EVAL_PAGE;
		}
		catch (PageException pe) {
			ThreadLocalPageContext.getLog(pageContext, "datasource").error("insert tag", pe);
			throw pe;
		}
		finally {
			manager.releaseConnection(pageContext, dc);
		}
	}

	public static Struct getMeta(DatasourceConnection dc, String tableQualifier, String tableOwner, String tableName) throws PageException {
		ResultSet columns = null;
		Struct sct = new StructImpl();
		try {
			DatabaseMetaData md = dc.getConnection().getMetaData();
			columns = md.getColumns(tableQualifier, tableOwner, tableName, null);

			String name;
			while (columns.next()) {
				name = columns.getString("COLUMN_NAME");
				sct.setEL(name, new ColumnInfo(name, getInt(columns, "DATA_TYPE"), getBoolean(columns, "IS_NULLABLE")));
			}
		}
		catch (SQLException sqle) {
			throw new DatabaseException(sqle, dc);
		}
		finally {
			DBUtil.closeEL(columns);
		}
		return sct;
	}

	private static int getInt(ResultSet columns, String columnLabel) throws PageException, SQLException {
		try {
			return columns.getInt(columnLabel);
		}
		catch (Exception e) {
			return Caster.toIntValue(columns.getObject(columnLabel));
		}
	}

	private static boolean getBoolean(ResultSet columns, String columnLabel) throws PageException, SQLException {
		try {
			return columns.getBoolean(columnLabel);
		}
		catch (Exception e) {
			return Caster.toBooleanValue(columns.getObject(columnLabel));
		}
	}

	/**
	 * @param meta
	 * @return return SQL String for insert
	 * @throws PageException
	 */
	private SQL createSQL(Struct meta) throws PageException {
		String[] fields = null;
		Form form = pageContext.formScope();
		if (formfields != null) fields = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(formfields, ','));
		else fields = CollectionUtil.keysAsString(pageContext.formScope());

		StringBuffer names = new StringBuffer();
		StringBuffer values = new StringBuffer();
		ArrayList<SQLItem> items = new ArrayList<SQLItem>();
		String field;
		for (int i = 0; i < fields.length; i++) {
			field = StringUtil.trim(fields[i], null);
			if (StringUtil.startsWithIgnoreCase(field, "form.")) field = field.substring(5);

			if (!field.equalsIgnoreCase("fieldnames")) {
				if (names.length() > 0) {
					names.append(',');
					values.append(',');
				}
				names.append(field);
				values.append('?');
				ColumnInfo ci = (ColumnInfo) meta.get(field, null);
				if (ci != null) items.add(new SQLItemImpl(form.get(field, null), ci.getType()));
				else items.add(new SQLItemImpl(form.get(field, null)));
			}
		}
		if (items.size() == 0) return null;

		StringBuffer sql = new StringBuffer();
		sql.append("insert into ");
		if (tablequalifier.length() > 0) {
			sql.append(tablequalifier);
			sql.append('.');
		}
		if (tableowner.length() > 0) {
			sql.append(tableowner);
			sql.append('.');
		}
		sql.append(tablename);
		sql.append('(');
		sql.append(names);
		sql.append(")values(");
		sql.append(values);
		sql.append(")");

		return new SQLImpl(sql.toString(), items.toArray(new SQLItem[items.size()]));
	}

}

class ColumnInfo {

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the nullable
	 */
	public boolean isNullable() {
		return nullable;
	}

	private String name;
	private int type;
	private boolean nullable;

	public ColumnInfo(String name, int type, boolean nullable) {
		this.name = name;
		this.type = type;
		this.nullable = nullable;
	}

	@Override
	public String toString() {
		return name + "-" + type + "-" + nullable;
	}

}