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
package lucee.runtime.services;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import coldfusion.sql.DataSource;
import coldfusion.sql.DataSourceDef;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;

public class DataSourceImpl implements DataSource {

	private lucee.runtime.db.DataSource ds;

	public DataSourceImpl(lucee.runtime.db.DataSource ds) {
		this.ds = ds;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(ds.getUsername(), ds.getPassword());
	}

	@Override
	public Connection getConnection(String user, String pass) throws SQLException {
		try {
			PageContext pc = ThreadLocalPageContext.get();
			return pc.getDataSourceManager().getConnection(pc, ds.getName(), user, pass).getConnection();
		}
		catch (PageException e) {
			throw new SQLException(e.getMessage());
		}
	}

	@Override
	public DataSourceDef getDataSourceDef() {
		return new DatSourceDefImpl(ds);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isDisabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void remove() throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDataSourceDef(DataSourceDef dsDef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLogWriter(PrintWriter pw) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLoginTimeout(int timeout) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	// used only with java 7, do not set @Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}

}