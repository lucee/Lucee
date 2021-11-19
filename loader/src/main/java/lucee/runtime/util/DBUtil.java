/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.util;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;

import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.SQL;
import lucee.runtime.db.SQLItem;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;

public interface DBUtil {

	/**
	 * 
	 * converts the value defined inside a SQLItem to the type defined in stat item
	 * @param item item 
	 * @return Object
	 * @throws PageException Page Exception
	 */

	public Object toSqlType(SQLItem item) throws PageException;

	@Deprecated
	public void setValue(TimeZone tz, PreparedStatement stat, int parameterIndex, SQLItem item) throws PageException, SQLException;

	/**
	 * fill a SQLItem to into a PreparedStatement
	 *
	 * @param pc Page Context
	 * @param tz timezone
	 * @param stat statement
	 * @param parameterIndex parameter index
	 * @param item item 
	 * @throws PageException Page Exception
	 * @throws SQLException SQL Exception
	 */
	public void setValue(PageContext pc, TimeZone tz, PreparedStatement stat, int parameterIndex, SQLItem item) throws PageException, SQLException;

	/**
	 * Cast a SQL Item to a String (Display) Value
	 * 
	 * @param item Item
	 * @return String Value
	 */
	public String toString(SQLItem item);

	/**
	 * cast a type defined in java.sql.Types to String SQL Type
	 * 
	 * @param type SQL Type
	 * @return SQL Type as String
	 * @throws PageException Page Exception
	 */
	public String toStringType(int type) throws PageException;

	/**
	 * cast a String SQL Type to type defined in java.sql.Types
	 * 
	 * @param strType String SQL Type
	 * @return SQL Type as int
	 * @throws PageException Page Exception
	 */
	public int toSQLType(String strType) throws PageException;

	/**
	 * create a Blob Object
	 * 
	 * @param conn Connection
	 * @param value value
	 * @return Returns a Blob Object.
	 * @throws PageException Page Exception
	 * @throws SQLException SQL Exception
	 */
	public Blob toBlob(Connection conn, Object value) throws PageException, SQLException;

	/**
	 * create a Clob Object
	 * 
	 * @param conn Connection
	 * @param value value
	 * @return Returns a Clob Object.
	 * @throws PageException Page Exception
	 * @throws SQLException SQL Exception
	 */
	public Clob toClob(Connection conn, Object value) throws PageException, SQLException;

	/**
	 * checks if this is an oracle connection
	 * 
	 * @param conn Connection
	 * @return Returns a Boolean.
	 */
	public boolean isOracle(Connection conn);

	public String getDatabaseName(DatasourceConnection dc) throws SQLException;

	/**
	 * close silently a SQL Statement
	 * 
	 * @param stat SQL statement
	 */
	public void closeSilent(Statement stat);

	/**
	 * close silently a SQL Connection
	 * 
	 * @param conn Connection
	 */
	public void closeSilent(Connection conn);

	/**
	 * close silently a SQL ResultSet
	 * 
	 * @param rs Result set
	 */
	public void closeSilent(ResultSet rs);

	public SQLItem toSQLItem(Object value, int type);

	public SQL toSQL(String sql, SQLItem[] items);

	public void releaseDatasourceConnection(Config config, DatasourceConnection dc, boolean async);

	/*
	 * FUTURE public void releaseDatasourceConnection(PageContext pc, DatasourceConnection dc,boolean
	 * managed); public void releaseDatasourceConnection(Config config, DatasourceConnection dc);
	 */
	public DatasourceConnection getDatasourceConnection(PageContext pc, DataSource datasource, String user, String pass) throws PageException;

	/*
	 * FUTURE public DatasourceConnection getDatasourceConnection(PageContext pc,DataSource datasource,
	 * String user, String pass, boolean managed) throws PageException; public DatasourceConnection
	 * getDatasourceConnection(Config config,DataSource datasource, String user, String pass) throws
	 * PageException;
	 * 
	 */

	public DatasourceConnection getDatasourceConnection(PageContext pc, String datasourceName, String user, String pass) throws PageException;

	public Key[] getColumnNames(Query qry);

	public String getColumnName(ResultSetMetaData meta, int column) throws SQLException;

	public Object getObject(ResultSet rs, int columnIndex, Class type) throws SQLException;

	public Object getObject(ResultSet rs, String columnLabel, Class type) throws SQLException;

}