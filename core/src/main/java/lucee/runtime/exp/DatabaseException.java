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
package lucee.runtime.exp;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.SQL;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.KeyConstants;

/**
 * Database Exception Object
 */

public final class DatabaseException extends PageExceptionImpl {

	private SQL sql;
	private String sqlstate = "";
	private int errorcode = -1;
	private DataSource datasource;

	public DatabaseException(SQLException sqle, DatasourceConnection dc) {
		super(sqle.getCause() instanceof SQLException ? (sqle = (SQLException) sqle.getCause()).getMessage() : sqle.getMessage(), "database");

		set(sqle);
		set(dc);
	}

	public DatabaseException(String message, String detail, SQL sql, DatasourceConnection dc) {
		super(message, "database");

		set(sql);
		set(null, detail);
		set(dc);
	}

	/**
	 * Constructor of the class
	 * 
	 * @param message error message
	 * @param detail detailed error message
	 * @param sqle
	 * @param sql
	 * @param dc
	 */
	private DatabaseException(String message, String detail, SQLException sqle, SQL sql, DatasourceConnection dc) {
		super(message != null ? message : "", "database");

		set(sql);
		set(sqle, detail);
		set(sqle);
		set(dc);
	}

	private void set(SQL sql) {
		this.sql = sql;
		if (sql != null) {
			setAdditional(KeyConstants._SQL, sql.toString());
		}
	}

	private void set(SQLException sqle, String detail) {
		String sqleMessage = sqle != null ? sqle.getMessage() : "";
		if (detail != null) {
			if (!StringUtil.isEmpty(sqleMessage)) setDetail(detail + "\n" + sqleMessage);
			else setDetail(detail);
		}
		else {
			if (!StringUtil.isEmpty(sqleMessage)) setDetail(sqleMessage);
		}
	}

	private void set(SQLException sqle) {
		if (sqle != null) {
			sqlstate = sqle.getSQLState();
			errorcode = sqle.getErrorCode();

			this.setStackTrace(sqle.getStackTrace());
		}
	}

	private void set(DatasourceConnection dc) {
		if (dc != null) {
			datasource = dc.getDatasource();
			try {
				DatabaseMetaData md = dc.getConnection().getMetaData();
				md.getDatabaseProductName();
				setAdditional(KeyConstants._DatabaseName, md.getDatabaseProductName());
				setAdditional(KeyConstants._DatabaseVersion, md.getDatabaseProductVersion());
				setAdditional(KeyConstants._DriverName, md.getDriverName());
				setAdditional(KeyConstants._DriverVersion, md.getDriverVersion());
				// setAdditional("url",md.getURL());

				if (!"__default__".equals(dc.getDatasource().getName())) setAdditional(KeyConstants._Datasource, dc.getDatasource().getName());

			}
			catch (SQLException e) {}
		}
	}

	/**
	 * Constructor of the class
	 * 
	 * @param message
	 * @param sqle
	 * @param sql
	 * 
	 *            public DatabaseException(String message, SQLException sqle, SQL
	 *            sql,DatasourceConnection dc) { this(message,null,sqle,sql,dc); }
	 */

	/**
	 * Constructor of the class
	 * 
	 * @param sqle
	 * @param sql
	 */
	public DatabaseException(SQLException sqle, SQL sql, DatasourceConnection dc) {
		this(sqle != null ? sqle.getMessage() : null, null, sqle, sql, dc);
	}

	/**
	 * Constructor of the class
	 * 
	 * @param sqle
	 */

	@Override
	public CatchBlock getCatchBlock(Config config) {
		String strSQL = sql == null ? "" : sql.toString();
		if (StringUtil.isEmpty(strSQL)) strSQL = Caster.toString(getAdditional().get("SQL", ""), "");

		String datasourceName = datasource == null ? "" : datasource.getName();
		if (StringUtil.isEmpty(datasourceName)) datasourceName = Caster.toString(getAdditional().get("DataSource", ""), "");

		CatchBlock sct = super.getCatchBlock(config);
		sct.setEL("NativeErrorCode", new Double(errorcode));
		sct.setEL("DataSource", datasourceName);
		sct.setEL("SQLState", sqlstate);
		sct.setEL("Sql", strSQL);
		sct.setEL("queryError", strSQL);
		sct.setEL("where", "");
		return sct;
	}

	public static DatabaseException notFoundException(PageContext pc, String datasource) {

		List<String> list = new ArrayList<String>();

		// application based datasources
		DataSource[] datasources = pc.getApplicationContext().getDataSources();
		if (datasources != null) for (int i = 0; i < datasources.length; i++) {
			list.add(datasources[i].getName());
		}

		// config based datasources
		datasources = pc.getConfig().getDataSources();
		if (datasources != null) for (int i = 0; i < datasources.length; i++) {
			list.add(datasources[i].getName());
		}

		// create error detail
		DatabaseException de = new DatabaseException("Datasource [" + datasource + "] doesn't exist", null, null, null);
		de.setDetail(ExceptionUtil.createSoundexDetail(datasource, list.iterator(), "datasource names"));
		de.setAdditional(KeyConstants._Datasource, datasource);
		return de;
	}
}