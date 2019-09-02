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
		this.id = StringUtil.isEmpty(id) ? null : id.trim();
		this.connStr = StringUtil.isEmpty(connStr) ? null : connStr.trim();
		this.cd = cd;
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