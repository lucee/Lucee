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
package lucee.commons.sql;

import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.Connection;

import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.op.Caster;

public class OracleClob {

	private static Integer duration;
	private static Integer mode;
	private static Method createTemporary;
	private static Method open;
	private static Method setString;

	public static Clob createClob(Connection conn, String value, Clob defaultValue) {
		try {
			Class clazz = ClassUtil.loadClass("oracle.sql.CLOB");

			// CLOB.DURATION_SESSION;
			if (duration == null) duration = Caster.toInteger(clazz.getField("DURATION_SESSION").getInt(null));
			// CLOB.MODE_READWRITE
			if (mode == null) mode = Caster.toInteger(clazz.getField("MODE_READWRITE").getInt(null));

			// CLOB c = CLOB.createTemporary(conn, false, CLOB.DURATION_SESSION);
			if (createTemporary == null || createTemporary.getDeclaringClass() != clazz)
				createTemporary = clazz.getMethod("createTemporary", new Class[] { Connection.class, boolean.class, int.class });
			Object clob = createTemporary.invoke(null, new Object[] { conn, Boolean.FALSE, duration });

			// c.open(CLOB.MODE_READWRITE);
			if (open == null || open.getDeclaringClass() != clazz) open = clazz.getMethod("open", new Class[] { int.class });
			open.invoke(clob, new Object[] { mode });

			// c.setString(1,value);
			if (setString == null || setString.getDeclaringClass() != clazz) setString = clazz.getMethod("setString", new Class[] { long.class, String.class });
			setString.invoke(clob, new Object[] { Long.valueOf(1), value });

			return (Clob) clob;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			// print.printST(t);
		}
		return defaultValue;
	}

}