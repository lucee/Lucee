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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
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
import lucee.runtime.debug.DebuggerUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.functions.displayFormatting.DecimalFormat;
import lucee.runtime.op.Caster;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Form;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.ListUtil;

/**
 * Updates existing records in data sources.
 *
 *
 *
 **/
public final class Update extends TagImpl {

	/** If specified, password overrides the password value specified in the ODBC setup. */
	private String password;

	/** Name of the data source that contains a table. */
	private DataSource datasource;

	/** If specified, username overrides the username value specified in the ODBC setup. */
	private String username;

	/**
	 * A comma-separated list of form fields to update. If this attribute is not specified, all fields
	 ** in the form are included in the operation.
	 */
	private String formfields;

	/**
	 * For data sources that support table ownership, for example, SQL Server, Oracle, and Sybase SQL
	 * Anywhere, use this field to specify the owner of the table.
	 */
	private String tableowner;

	/** Name of the table you want to update. */
	private String tablename;

	/**
	 * For data sources that support table qualifiers, use this field to specify the qualifier for the
	 ** table. The purpose of table qualifiers varies across drivers. For SQL Server and Oracle, the
	 * qualifier refers to the name of the database that contains the table. For the Intersolv dBase
	 * driver, the qualifier refers to the directory where the DBF files are located.
	 */
	private String tablequalifier;

	@Override
	public void release() {
		super.release();
		password = null;
		username = null;
		formfields = null;
		tableowner = null;
		tablequalifier = null;
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
	 * set the value datasource Name of the data source that contains a table.
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
	 * set the value formfields A comma-separated list of form fields to update. If this attribute is
	 * not specified, all fields in the form are included in the operation.
	 * 
	 * @param formfields value to set
	 **/
	public void setFormfields(String formfields) {
		this.formfields = formfields;
	}

	/**
	 * set the value tableowner For data sources that support table ownership, for example, SQL Server,
	 * Oracle, and Sybase SQL Anywhere, use this field to specify the owner of the table.
	 * 
	 * @param tableowner value to set
	 **/
	public void setTableowner(String tableowner) {
		this.tableowner = tableowner;
	}

	/**
	 * set the value tablename Name of the table you want to update.
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

			Struct meta = Insert.getMeta(dc, tablequalifier, tableowner, tablename);

			String[] pKeys = getPrimaryKeys(dc);
			SQL sql = createSQL(dc, pKeys, meta);
			if (sql != null) {
				QueryImpl query = new QueryImpl(pageContext, dc, sql, -1, -1, null, "query");

				if (pageContext.getConfig().debug()) {
					String dsn = ds instanceof DataSource ? ((DataSource) ds).getName() : Caster.toString(ds);
					boolean logdb = ((ConfigPro) pageContext.getConfig()).hasDebugOptions(ConfigPro.DEBUG_DATABASE);
					if (logdb) {
						boolean debugUsage = DebuggerUtil.debugQueryUsage(pageContext, query);
						DebuggerImpl di = (DebuggerImpl) pageContext.getDebugger();
						di.addQuery(debugUsage ? query : null, dsn, "", sql, query.getRecordcount(),
								Query.toTemplateLine(pageContext.getConfig(), sourceTemplate, pageContext.getCurrentPageSource()), query.getExecutionTime());
					}
				}

				// log
				Log log = ThreadLocalPageContext.getLog(pageContext, "datasource");
				if (log.getLogLevel() >= Log.LEVEL_INFO) {
					log.info("update tag", "executed [" + sql.toString().trim() + "] in " + DecimalFormat.call(pageContext, query.getExecutionTime() / 1000000D) + " ms");
				}
			}
			return EVAL_PAGE;
		}
		catch (PageException pe) {
			ThreadLocalPageContext.getLog(pageContext, "datasource").error("update tag", pe);
			throw pe;
		}
		finally {
			manager.releaseConnection(pageContext, dc);
		}
	}

	private String[] getPrimaryKeys(DatasourceConnection dc) throws PageException {
		lucee.runtime.type.Query query = getPrimaryKeysAsQuery(dc);
		int recCount = query.getRecordcount();
		String[] pKeys = new String[recCount];

		if (recCount == 0) throw new DatabaseException("can't find primary keys of table [" + tablename + "]", null, null, dc);

		for (int row = 1; row <= recCount; row++) {
			pKeys[row - 1] = Caster.toString(query.getAt("column_name", row));
		}

		return pKeys;
	}

	private lucee.runtime.type.Query getPrimaryKeysAsQuery(DatasourceConnection dc) throws PageException {

		// Read Meta Data
		DatabaseMetaData meta;
		try {
			meta = dc.getConnection().getMetaData();
		}
		catch (SQLException e) {
			throw new DatabaseException(e, dc);
		}

		try {
			return new QueryImpl(meta.getPrimaryKeys(tablequalifier, tableowner, tablename), -1, "query", pageContext.getTimeZone());
		}
		catch (SQLException e) {
			try {
				return new QueryImpl(meta.getBestRowIdentifier(tablequalifier, tableowner, tablename, 0, false), -1, "query", pageContext.getTimeZone());
			}
			catch (SQLException sqle) {
				throw new DatabaseException("can't find primary keys of table [" + tablename + "] (" + ExceptionUtil.getMessage(sqle) + ")", null, null, dc);
			}
		}
	}

	/**
	 * @param keys primary Keys
	 * @return return SQL String for update
	 * @throws PageException
	 */
	private SQL createSQL(DatasourceConnection dc, String[] keys, Struct meta) throws PageException {
		String[] fields = null;
		Form form = pageContext.formScope();
		if (formfields != null) fields = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(formfields, ','));
		else fields = CollectionUtil.keysAsString(pageContext.formScope());

		StringBuffer set = new StringBuffer();
		StringBuffer where = new StringBuffer();
		ArrayList setItems = new ArrayList();
		ArrayList whereItems = new ArrayList();
		String field;
		for (int i = 0; i < fields.length; i++) {
			field = StringUtil.trim(fields[i], null);
			if (StringUtil.startsWithIgnoreCase(field, "form.")) field = field.substring(5);

			if (!field.equalsIgnoreCase("fieldnames")) {
				if (ArrayUtil.indexOfIgnoreCase(keys, field) == -1) {
					if (set.length() == 0) set.append(" set ");
					else set.append(",");
					set.append(field);
					set.append("=?");
					ColumnInfo ci = (ColumnInfo) meta.get(field);
					if (ci != null) setItems.add(new SQLItemImpl(form.get(field, null), ci.getType()));
					else setItems.add(new SQLItemImpl(form.get(field, null)));
				}
				else {
					if (where.length() == 0) where.append(" where ");
					else where.append(" and ");
					where.append(field);
					where.append("=?");
					whereItems.add(new SQLItemImpl(form.get(field, null)));
				}
			}
		}
		if ((setItems.size() + whereItems.size()) == 0) return null;

		if (whereItems.size() == 0)
			throw new DatabaseException("can't find primary keys [" + ListUtil.arrayToList(keys, ",") + "] of table [" + tablename + "] in form scope", null, null, dc);

		StringBuffer sql = new StringBuffer();
		sql.append("update ");
		if (tablequalifier != null && tablequalifier.length() > 0) {
			sql.append(tablequalifier);
			sql.append('.');
		}
		if (tableowner != null && tableowner.length() > 0) {
			sql.append(tableowner);
			sql.append('.');
		}
		sql.append(tablename);
		sql.append(set);
		sql.append(where);

		return new SQLImpl(sql.toString(), arrayMerge(setItems, whereItems));
	}

	private SQLItem[] arrayMerge(ArrayList setItems, ArrayList whereItems) {
		SQLItem[] items = new SQLItem[setItems.size() + whereItems.size()];

		int index = 0;
		// Item
		int size = setItems.size();
		for (int i = 0; i < size; i++) {
			items[index++] = (SQLItem) setItems.get(i);
		}
		// Where
		size = whereItems.size();
		for (int i = 0; i < size; i++) {
			items[index++] = (SQLItem) whereItems.get(i);
		}
		return items;
	}
}