/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
package lucee.runtime.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.osgi.framework.Bundle;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.type.util.ListUtil;

public class JDBCDriver {

	public final String label;
	public final String id;
	public String connStr;
	public final ClassDefinition cd;

	public JDBCDriver(String label, String id, String connStr, ClassDefinition cd) {
		this.label = label;
		this.id = StringUtil.isEmpty(id, true) ? null : id.trim();
		this.connStr = StringUtil.isEmpty(connStr, true) ? getById(id) : connStr.trim();
		this.cd = cd;
	}

	private static String getById(String id) {
		// FUTURE PATCH add to driver itself
		if ("hsqldb".equalsIgnoreCase(id)) return "jdbc:hsqldb:file:{path}{database}";
		if ("exasol".equalsIgnoreCase(id)) return "jdbc:exa:{host}:{port}";
		if ("teradata".equalsIgnoreCase(id)) return "jdbc:teradata://{host}";
		if ("jtds".equalsIgnoreCase(id)) return "jdbc:jtds:sqlserver://{host}:{port}/{database}";
		if ("h2".equalsIgnoreCase(id)) return "jdbc:h2:{path}{database};MODE={mode}";
		if ("mysql".equalsIgnoreCase(id)) return "jdbc:mysql://{host}:{port}/{database}";
		if ("postgresql".equalsIgnoreCase(id)) return "jdbc:postgresql://{host}:{port}/{database}";
		if ("mssql".equalsIgnoreCase(id)) return "jdbc:sqlserver://{host}:{port}";
		if ("oracle".equalsIgnoreCase(id)) return "jdbc:oracle:{drivertype}:@{host}:{port}:{database}";
		if ("derby".equalsIgnoreCase(id)) return "jdbc:derby:{mode}:{path}{database}";
		return null;
	}

	public static String extractClassName(Bundle bundle) throws IOException {
		URL url = bundle.getResource("/META-INF/services/java.sql.Driver");

		BufferedReader br = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
		String content = IOUtil.toString(br);
		return ListUtil.first(content, " \n\t");
	}

	public static String extractClassName(Bundle bundle, String defaultValue) {
		try {
			return extractClassName(bundle);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}
}