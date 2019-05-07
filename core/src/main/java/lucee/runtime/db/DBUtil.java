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
import lucee.transformer.library.ClassDefinitionImpl;

public class DBUtil {

	private static DataSourceDefintion DB2 = new DataSourceDefintion("com.ddtek.jdbc.db2.DB2Driver", "jdbc:datadirect:db2://{host}:{port};DatabaseName={database}", 50000);
	private static DataSourceDefintion FIREBIRD = new DataSourceDefintion("org.firebirdsql.jdbc.FBDriver", "jdbc:firebirdsql://{host}:{port}/{path}{database}", 3050);
	private static DataSourceDefintion H2 = new DataSourceDefintion("org.h2.Driver", "jdbc:h2:{path}{database};MODE={mode}", -1);
	private static DataSourceDefintion MSSQL = new DataSourceDefintion("net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://{host}:{port}/{database}", 1433);
	private static DataSourceDefintion MYSQL = new DataSourceDefintion("org.gjt.mm.mysql.Driver", "jdbc:mysql://{host}:{port}/{database}", 3306);
	private static DataSourceDefintion ORACLE = new DataSourceDefintion("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:{drivertype}:@{host}:{port}:{database}", 1521);
	private static DataSourceDefintion POSTGRESQL = new DataSourceDefintion("org.postgresql.Driver", "jdbc:postgresql://{host}:{port}/{database}", 5432);
	private static DataSourceDefintion SYBASE = new DataSourceDefintion("net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sybase://{host}:{port}/{database}", 7100);

	public static DataSourceDefintion getDataSourceDefintionForType(String type, DataSourceDefintion defaultValue) {
		if (StringUtil.isEmpty(type)) return defaultValue;
		type = type.trim().toLowerCase();
		// TODO this needs to be loaded dynamically from
		if ("db2".equals(type)) return DB2;
		if ("firebird".equals(type)) return FIREBIRD;
		if ("h2".equals(type)) return H2;
		if ("mssql".equals(type)) return MSSQL;
		if ("mysql".equals(type)) return MYSQL;
		if ("oracle".equals(type)) return ORACLE;
		if ("postgresql".equals(type) || "postgre".equals(type)) return POSTGRESQL;
		if ("sybase".equals(type)) return SYBASE;
		return defaultValue;
	}

	public static class DataSourceDefintion {

		public ClassDefinitionImpl classDefinition;
		public final String connectionString;
		public final int port;

		DataSourceDefintion(String className, String connectionString, int port) {
			this(new ClassDefinitionImpl(className), connectionString, port);
		}

		DataSourceDefintion(ClassDefinitionImpl cd, String connectionString, int port) {
			this.classDefinition = cd;
			this.connectionString = connectionString;
			this.port = port;
		}
	}
}