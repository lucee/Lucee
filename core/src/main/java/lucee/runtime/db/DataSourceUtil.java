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
package lucee.runtime.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;

public class DataSourceUtil {

	public static boolean isHSQLDB(DatasourceConnection dc) {
		return is(dc, "HSQL", false, "org.hsqldb.jdbcDriver");
	}

	public static boolean isOracle(DatasourceConnection dc) {
		return is(dc, "Oracle", true, "OracleDriver");
	}

	public static boolean isPostgres(DatasourceConnection dc) {
		return is(dc, "PostgreSQL", true, "postgresql");
	}

	public static boolean isMySQL(DatasourceConnection dc) {
		return is(dc, "MySQL", false, "org.gjt.mm.mysql.Driver");
	}

	public static boolean isMSSQL(DatasourceConnection dc) {
		return is(dc, "Microsoft", false, "com.microsoft.jdbc.sqlserver.SQLServerDriver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "net.sourceforge.jtds.jdbc.Driver");
	}

	public static boolean isMSSQLDriver(DatasourceConnection dc) {
		if (dc == null) return false;
		DataSourcePro dsp = (DataSourcePro) dc.getDatasource();

		if (dsp.isMSSQL() == null) {
			try {
				if (dc.getConnection().getMetaData().getDriverName().indexOf("Microsoft SQL Server JDBC Driver") != -1) {
					dsp.setMSSQL(true);
					return true;
				}
			}
			catch (SQLException e) {
			}

			String className = dc.getDatasource().getClassDefinition().getClassName();
			boolean isMSSQL = className.equals("com.microsoft.jdbc.sqlserver.SQLServerDriver") || className.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			dsp.setMSSQL(isMSSQL);
			return isMSSQL;
		}
		else return dsp.isMSSQL();
	}

	private static boolean is(DatasourceConnection dc, String keyword, boolean doIndexOf, String... classNames) {
		if (dc == null) return false;
		try {
			if (dc.getConnection().getMetaData().getDatabaseProductName().indexOf(keyword) != -1) return true;
		}
		catch (Exception e) {
			String className = dc.getDatasource().getClassDefinition().getClassName();
			if (doIndexOf) {
				for (String cn: classNames) {
					if (className.indexOf(cn) != -1) return true;
				}
			}
			else {
				for (String cn: classNames) {
					if (className.equals(cn)) return true;
				}
			}

		}
		return false;
	}

	public static boolean isValid(DatasourceConnection dc, int timeout) throws SQLException {
		return dc.getConnection().isValid(timeout);
	}

	public static boolean isValid(DatasourceConnection dc, int timeout, boolean defaultValue) {
		try {
			return dc.getConnection().isValid(timeout);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static boolean isClosed(PreparedStatement ps, boolean defaultValue) {
		try {
			return ps.isClosed();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static String getDatabaseName(DatasourceConnection dc) throws SQLException {
		String dbName = null;
		try {
			dbName = dc.getDatasource().getDatabase();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		if (StringUtil.isEmpty(dbName)) dbName = dc.getConnection().getCatalog(); // works on most JDBC drivers (except Oracle )
		return dbName;
	}

	public static void setQueryTimeoutSilent(Statement stat, int seconds) {
		// some jdbc driver multiply the value by 1000 to get milli second what can end in a negative value,
		// so we have to make sure the given timeout can be
		// multiply by 1000
		int max = Integer.MAX_VALUE / 1000;
		if (max < seconds) seconds = max;
		try {
			if (seconds > 0) stat.setQueryTimeout(seconds);
		}
		catch (SQLException e) {
		}
	}

	public static String getLargeTextSqlTypeName(DatasourceConnection dc) {
		if (DataSourceUtil.isHSQLDB(dc)) return "VARCHAR";
		if (DataSourceUtil.isMySQL(dc)) return "LONGTEXT";
		if (DataSourceUtil.isOracle(dc)) return "CLOB";
		if (DataSourceUtil.isPostgres(dc)) return "TEXT";

		// default
		return "NTEXT";
	}

}