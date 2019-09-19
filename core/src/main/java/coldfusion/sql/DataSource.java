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
package coldfusion.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

public interface DataSource extends javax.sql.DataSource {

	public void remove() throws SQLException;

	@Override
	public Connection getConnection() throws SQLException;

	@Override
	public Connection getConnection(String user, String pass) throws SQLException;

	public void setDataSourceDef(DataSourceDef dsDef);

	public DataSourceDef getDataSourceDef();

	@Override
	public PrintWriter getLogWriter() throws SQLException;

	@Override
	public int getLoginTimeout() throws SQLException;

	@Override
	public void setLogWriter(PrintWriter pw) throws SQLException;

	@Override
	public void setLoginTimeout(int timeout) throws SQLException;

	public boolean isDisabled();

}