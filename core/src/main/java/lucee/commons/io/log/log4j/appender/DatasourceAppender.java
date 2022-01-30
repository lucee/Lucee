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

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.jdbc.JDBCAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.log4j.LogAdapter;
import lucee.commons.io.log.log4j.layout.DatasourceLayout;
import lucee.commons.lang.SystemOut;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceUtil;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceConnectionPro;
import lucee.runtime.db.SQL;
import lucee.runtime.db.SQLImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.QueryImpl;

public class DatasourceAppender extends JDBCAppender implements Appender {

	// private DatasourceManagerImpl manager;
	private DataSource datasource;
	private final String datasourceName;
	private final String username;
	private final String password;
	private final String tableName;
	private final Config config;

	public String getTableName() {
		return tableName;
	}

	private DatasourceConnPool pool;
	private boolean ignore = true;
	private DAThreadLocal in = new DAThreadLocal();
	private Logger logger;

	public DatasourceAppender(Config config, Layout layout, String datasource, String username, String password, String tableName, String custom) throws PageException {
		this.datasourceName = datasource;
		this.username = username;
		this.password = password;
		this.config = config;
		this.tableName = tableName;
		if (layout instanceof DatasourceLayout) {
			DatasourceLayout dl = (DatasourceLayout) layout;
			dl.setTableName(tableName);
			dl.setCustom(custom);
		}
		if (layout != null) setLayout(layout);
	}

	@Override
	public void append(LoggingEvent event) {
		if (Boolean.TRUE.equals(in.get())) {
			logConsole(event);
			return;
		}

		try {
			in.set(Boolean.TRUE);
			if (ignore) {
				try {
					getConnection();
					ignore = false;
				}
				catch (Exception e) {
				}
			}

			if (!ignore) super.append(event);
			else logConsole(event);
			// try{}
			// catch(Exception e) {}
		}
		finally {
			in.set(Boolean.FALSE);
		}
	}

	@Override
	protected Connection getConnection() throws SQLException {

		try {
			RefBoolean first = new RefBooleanImpl(false);
			DatasourceConnection conn = pool(first).borrowObject();
			if (first.toBooleanValue()) touchTable(conn);
			return conn;
		}
		catch (PageException e) {
			throw new SQLException(e);
		}
	}

	private void touchTable(DatasourceConnection dc) throws PageException {
		PageContext optionalPC = ThreadLocalPageContext.get();
		SQLImpl sql = new SQLImpl("select 1 from " + tableName + " where 1=0");
		try {
			new QueryImpl(optionalPC, dc, sql, -1, -1, null, "query");
		}
		catch (PageException pe) {
			pe.printStackTrace();
			try {
				new QueryImpl(optionalPC, dc, createSQL(dc), -1, -1, null, "query");
			}
			catch (PageException pe2) {
				pe2.printStackTrace();
				throw pe;
			}
		}
	}

	private SQL createSQL(DatasourceConnection dc) {
		StringBuilder sb = new StringBuilder("CREATE TABLE ");
		if (DataSourceUtil.isMSSQL(dc)) sb.append("dbo.");
		sb.append(tableName).append(" ( ");

		if (DataSourceUtil.isMSSQL(dc)) sb.append("pid INT PRIMARY KEY IDENTITY (1, 1), ");
		else if (DataSourceUtil.isMySQL(dc)) sb.append("pid INT AUTO_INCREMENT PRIMARY KEY, ");
		else if (DataSourceUtil.isHSQLDB(dc)) sb.append("pid INTEGER IDENTITY PRIMARY KEY, ");
		else if (DataSourceUtil.isPostgres(dc)) sb.append("id SERIAL PRIMARY KEY, ");

		sb.append("id varchar(32) NOT NULL, ");
		sb.append("name varchar(128) NOT NULL, ");
		sb.append("severity varchar(16) NOT NULL, ");
		sb.append("threadid varchar(64) NOT NULL, ");
		sb.append("time datetime NOT NULL, ");
		sb.append("application varchar(64) NOT NULL, ");
		sb.append("message varchar(512) NOT NULL, ");
		sb.append("exception varchar(2048) NOT NULL, ");
		sb.append("custom varchar(2048) NOT NULL ");
		sb.append(");");
		return new SQLImpl(sb.toString());

	}

	private DatasourceConnPool pool(RefBoolean first) throws PageException {
		if (pool == null) {
			if (first != null) first.setValue(true);
			if (datasource == null) datasource = config.getDataSource(datasourceName);
			this.pool = ((ConfigPro) config).getDatasourceConnectionPool(datasource, username, password);
		}
		return pool;
	}

	@Override
	protected void closeConnection(Connection conn) {
		boolean closed = false;
		if (conn instanceof DatasourceConnection) {
			((DatasourceConnectionPro) conn).release();
			closed = true;
		}
		if (!closed) IOUtil.closeEL(conn);
	}

	private void logConsole(LoggingEvent event) {
		try {
			ThrowableInformation ti = event.getThrowableInformation();
			Throwable t;
			if (ti != null && (t = ti.getThrowable()) != null) {
				getConsoleLogger().log(event.getLevel(), event.getMessage(), t);
			}
			else getConsoleLogger().log(event.getLevel(), event.getMessage());
		}
		catch (Exception e) {
			SystemOut.printDate(e);
		}
	}

	private Logger getConsoleLogger() throws PageException {
		ConfigPro config = (ConfigPro) ThreadLocalPageContext.getConfig();
		if (logger == null) {
			LogAdapter la = (LogAdapter) config.getLog("console_datasource_appender", true); // TODO use log level from this logger...
			logger = la.getLogger();
		}
		return logger;
	}

	private static class DAThreadLocal extends ThreadLocal<Boolean> {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	}
}