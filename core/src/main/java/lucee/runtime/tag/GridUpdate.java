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

import lucee.runtime.exp.TagNotSupported;
import lucee.runtime.ext.tag.TagImpl;

/**
 * Used in a cfgrid, cfgridupdate allows you to perform updates to data sources directly from edited
 * grid data. The cfgridupdate tag provides a direct interface with your data source. The
 * cfgridupdate tag applies delete row actions first, then INSERT row actions, and then UPDATE row
 * actions. If an error is encountered, row processing stops.
 *
 *
 *
 **/
public final class GridUpdate extends TagImpl {

	private String password;
	private String datasource;
	private String providerdsn;
	private boolean keyonly;
	private String tablename;
	private String connectstring;
	private String dbtype;
	private String grid;
	private String dbname;
	private String username;
	private String dbserver;
	private String tableowner;
	private String provider;
	private String tablequalifier;

	/**
	 * constructor for the tag class
	 **/
	public GridUpdate() throws TagNotSupported {
		throw new TagNotSupported("GridUpdate");
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
	 * set the value datasource The name of the data source for the update action.
	 * 
	 * @param datasource value to set
	 **/
	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	/**
	 * set the value providerdsn Data source name for the COM provider (OLE-DB only).
	 * 
	 * @param providerdsn value to set
	 **/
	public void setProviderdsn(String providerdsn) {
		this.providerdsn = providerdsn;
	}

	/**
	 * set the value keyonly Yes or No. Yes specifies that in the update action, the WHERE criteria is
	 * confined to the key values. No specifies that in addition to the key values, the original values
	 * of any changed fields are included in the WHERE criteria. Default is Yes.
	 * 
	 * @param keyonly value to set
	 **/
	public void setKeyonly(boolean keyonly) {
		this.keyonly = keyonly;
	}

	/**
	 * set the value tablename The name of the table to update.
	 * 
	 * @param tablename value to set
	 **/
	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public void setConnectstring(String connectstring) {
		this.connectstring = connectstring;
	}

	/**
	 * set the value dbtype The database driver type
	 * 
	 * @param dbtype value to set
	 **/
	public void setDbtype(String dbtype) {
		this.dbtype = dbtype;
	}

	/**
	 * set the value grid The name of the cfgrid form element that is the source for the update action.
	 * 
	 * @param grid value to set
	 **/
	public void setGrid(String grid) {
		this.grid = grid;
	}

	/**
	 * set the value dbname The database name (Sybase System 11 driver and SQLOLEDB provider only). If
	 * specified, dbName overrides the default database specified in the data source.
	 * 
	 * @param dbname value to set
	 **/
	public void setDbname(String dbname) {
		this.dbname = dbname;
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
	 * set the value dbserver For native database drivers and the SQLOLEDB provider, specifies the name
	 * of the database server computer. If specified, dbServer overrides the server specified in the
	 * data source.
	 * 
	 * @param dbserver value to set
	 **/
	public void setDbserver(String dbserver) {
		this.dbserver = dbserver;
	}

	/**
	 * set the value tableowner For data sources that support table ownership (such as SQL Server,
	 * Oracle, and Sybase SQL Anywhere), use this field to specify the owner of the table.
	 * 
	 * @param tableowner value to set
	 **/
	public void setTableowner(String tableowner) {
		this.tableowner = tableowner;
	}

	/**
	 * set the value provider COM provider (OLE-DB only).
	 * 
	 * @param provider value to set
	 **/
	public void setProvider(String provider) {
		this.provider = provider;
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
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		password = "";
		datasource = "";
		providerdsn = "";
		keyonly = false;
		tablename = "";
		connectstring = "";
		dbtype = "";
		grid = "";
		dbname = "";
		username = "";
		dbserver = "";
		tableowner = "";
		provider = "";
		tablequalifier = "";
	}
}