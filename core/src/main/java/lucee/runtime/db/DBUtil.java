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

import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.osgi.OSGiUtil;
import lucee.transformer.library.ClassDefinitionImpl;

public class DBUtil {

	private static DataSourceDefintion DB2;
	private static DataSourceDefintion FIREBIRD;
	private static DataSourceDefintion H2;
	private static DataSourceDefintion MSSQL;
	private static DataSourceDefintion MYSQL;
	private static DataSourceDefintion ORACLE;
	private static DataSourceDefintion POSTGRESQL;
	private static DataSourceDefintion SYBASE;

	public static DataSourceDefintion getDataSourceDefintionForType(Config config, String type, DataSourceDefintion defaultValue) {
		if (StringUtil.isEmpty(type)) return defaultValue;

		type = type.trim().toLowerCase();
		// TODO extract data from JDBC config
		if ("db2".equals(type)) {

			if (DB2 == null) {
				DB2 = new DataSourceDefintion("com.ddtek.jdbc.db2.DB2Driver", "jdbc:datadirect:db2://{host}:{port};DatabaseName={database}", 50000);
			}
			return DB2;
		}
		if ("firebird".equals(type)) {
			if (FIREBIRD == null) {
				FIREBIRD = new DataSourceDefintion("org.firebirdsql.jdbc.FBDriver", "jdbc:firebirdsql://{host}:{port}/{path}{database}", 3050);
			}
			return FIREBIRD;
		}
		if ("h2".equals(type)) {
			if (H2 == null) {
				JDBCDriver jdbc = getJDBCDriver(config, "h2", "org.h2.Driver", "org.h2", "1.3.172", "jdbc:h2:{path}{database};MODE={mode}");
				H2 = new DataSourceDefintion(jdbc.cd, jdbc.connStr, -1);
			}
			return H2;
		}
		if ("mssql".equals(type)) {
			if (MSSQL == null) {
				JDBCDriver jdbc = getJDBCDriver(config, "mssql", "net.sourceforge.jtds.jdbc.Driver", "jtds", "1.3.1", "jdbc:jtds:sqlserver://{host}:{port}/{database}");
				MSSQL = new DataSourceDefintion(jdbc.cd, jdbc.connStr, 1433);
			}
			return MSSQL;
		}
		if ("mysql".equals(type)) {
			if (MYSQL == null) {
				JDBCDriver jdbc = getJDBCDriver(config, "mysql", "com.mysql.cj.jdbc.Driver", "com.mysql.cj", "8.0.15", "jdbc:mysql://{host}:{port}/{database}");
				MYSQL = new DataSourceDefintion(jdbc.cd, jdbc.connStr, 3306);
			}
			return MYSQL;
		}
		if ("oracle".equals(type)) {
			if (ORACLE == null) {
				JDBCDriver jdbc = getJDBCDriver(config, "oracle", "oracle.jdbc.driver.OracleDriver", "ojdbc7", "12.1.0.2L0001",
						"jdbc:oracle:{drivertype}:@{host}:{port}:{database}");
				ORACLE = new DataSourceDefintion(jdbc.cd, jdbc.connStr, 1521);
			}
			return ORACLE;
		}
		if ("postgresql".equals(type) || "postgre".equals(type)) {
			if (POSTGRESQL == null) {
				JDBCDriver jdbc = getJDBCDriver(config, "postgresql", "org.postgresql.Driver", "org.postgresql.jdbc42", "9.4.1212", "jdbc:postgresql://{host}:{port}/{database}");
				POSTGRESQL = new DataSourceDefintion(jdbc.cd, jdbc.connStr, 5432);
			}
			return POSTGRESQL;
		}
		if ("sybase".equals(type)) {
			if (SYBASE == null) {
				JDBCDriver jdbc = getJDBCDriver(config, "sybase", "net.sourceforge.jtds.jdbc.Driver", "jtds", "1.3.1", "jdbc:jtds:sybase://{host}:{port}/{database}");
				SYBASE = new DataSourceDefintion(jdbc.cd, jdbc.connStr, 7100);
			}
			return SYBASE;
		}
		return defaultValue;
	}

	private static JDBCDriver getJDBCDriver(Config config, String id, String className, String bundleName, String bundleVersion, String connStr) {
		// FUTURE remove the hardcoded fallback
		ConfigPro ci = (ConfigPro) config;
		JDBCDriver jdbc = ci.getJDBCDriverById(id, null);
		if (jdbc != null) return improve(jdbc, connStr);

		jdbc = ci.getJDBCDriverByClassName(className, null);
		if (jdbc != null) return improve(jdbc, connStr);

		jdbc = ci.getJDBCDriverByBundle(bundleName, OSGiUtil.toVersion(bundleVersion, null), null);
		if (jdbc != null) return improve(jdbc, connStr);

		return new JDBCDriver(id, id, connStr, new ClassDefinitionImpl(className, bundleName, bundleVersion, config.getIdentification()));
	}

	private static JDBCDriver improve(JDBCDriver jdbc, String connStr) {
		if (StringUtil.isEmpty(jdbc.connStr)) jdbc.connStr = connStr;
		return jdbc;
	}

	public static class DataSourceDefintion {

		public ClassDefinition classDefinition;
		public final String connectionString;
		public final int port;

		DataSourceDefintion(String className, String connectionString, int port) {
			this(new ClassDefinitionImpl(className), connectionString, port);
		}

		DataSourceDefintion(ClassDefinition cd, String connectionString, int port) {
			this.classDefinition = cd;
			this.connectionString = connectionString;
			this.port = port;
		}
	}
}