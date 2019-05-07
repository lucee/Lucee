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
package lucee.runtime.query.caster;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import lucee.commons.lang.ClassUtil;

public class _OracleOpaqueCast {

	private static final Object[] ZERO_ARGS = new Object[0];

	public static Object toCFType(ResultSet rst, int columnIndex) throws SQLException, IOException {
		validateClasses();

		Object o = rst.getObject(columnIndex);
		if (o == null) return null;

		// we do not have oracle.sql.OPAQUE in the core, so we need reflection for this
		/*
		 * try{ String typeName=Caster.toString(Reflector.callMethod(o, "getSQLTypeName", ZERO_ARGS),null);
		 * 
		 * //OPAQUE opaque = ((oracle.sql.OPAQUE)o); if("SYS.XMLTYPE".equals(typeName)) {
		 * 
		 * // first we need to load the class in question Class
		 * clazz=ClassUtil.loadClass(o.getClass().getClassLoader(),"oracle.xdb.XMLType"); return
		 * Reflector.callStaticMethod(clazz, "createXML", new Object[]{o}); } } catch(PageException pe){
		 * throw ExceptionUtil.toIOException(pe); }
		 */
		return o;
	}

	private static void validateClasses() throws IOException {
		Class<?> clazz1 = ClassUtil.loadClass("oracle.xdb.XMLType", null);
		Class<?> clazz2 = ClassUtil.loadClass("oracle.xml.parser.v2.XMLParseException", null);
		if (clazz1 == null || clazz2 == null) throw new IOException("the xdb.jar/xmlparserv2.jar is missing, please download at "
				+ "http://www.oracle.com/technology/tech/xml/xdk/xdk_java.html and copy it into the Lucee lib directory");

	}
}