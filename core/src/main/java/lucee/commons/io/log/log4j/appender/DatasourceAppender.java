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
package lucee.commons.io.log.log4j.appender;

import java.sql.Connection;
import java.sql.SQLException;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.SystemOut;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceConnectionPool;
import lucee.runtime.exp.PageException;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.jdbc.JDBCAppender;
import org.apache.log4j.spi.LoggingEvent;

public class DatasourceAppender extends JDBCAppender implements Appender {
	

	//private DatasourceManagerImpl manager;
	private DataSource datasource;
	private String datasourceName;
	private String username;
	private String password;
	private Config config;
	private DatasourceConnectionPool pool;
	private boolean ignore;

	public DatasourceAppender(Config config, Layout layout, String datasource, String username, String password) throws PageException {
		this.datasourceName=datasource;
		this.username=username;
		this.password=password;
		this.config=config;
		
		if(layout!=null)setLayout(layout);
	}
	

	@Override
	public void append(LoggingEvent event) {
		if(!ignore) super.append(event);
	}
	

	@Override
	public String getSql() {
		String sql = super.getSql();
		return sql;
	}
	
	@Override
	protected Connection getConnection() throws SQLException {
		ignore=true;
		try {
			
			return pool().getDatasourceConnection(config, datasource, username, password);
		} catch (PageException e) {
			throw new SQLException(e);
		}
		finally {
			ignore=false;
		}
	}
	
	private DatasourceConnectionPool pool() throws PageException {
		if(pool==null) {
			if(datasource==null)datasource=config.getDataSource(datasourceName);
			this.pool=((ConfigImpl)config).getDatasourceConnectionPool();
		}
		return pool;
	}

	@Override
	protected void closeConnection(Connection conn) {
		boolean closed=false;
		if(conn instanceof DatasourceConnection) {
			try {
				pool().releaseDatasourceConnection((DatasourceConnection)conn);
				closed=true;
			}
			catch (PageException e) {
				SystemOut.printDate(e);
			}
		}
		if(!closed) 
			IOUtil.closeEL(conn);
	}

	
	
}