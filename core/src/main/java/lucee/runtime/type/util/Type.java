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
package lucee.runtime.type.util;

import lucee.runtime.Component;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.text.xml.struct.XMLStruct;
import lucee.runtime.type.Array;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.Scope;

public final class Type {

	public static String getName(Object o) {
		if (o == null) return "null";
		if (o instanceof UDF) return "user defined function (" + (((UDF) o).getFunctionName()) + ")";
		else if (o instanceof Boolean) return "Boolean";
		else if (o instanceof Number) return "Number";
		else if (o instanceof TimeSpan) return "TimeSpan";
		else if (o instanceof Array) return "Array";
		else if (o instanceof Component) return "Component " + ((Component) o).getAbsName();
		else if (o instanceof Scope) return ((Scope) o).getTypeAsString();
		else if (o instanceof Struct) {
			if (o instanceof XMLStruct) return "XML";
			return "Struct";
		}
		else if (o instanceof Query) return "Query";
		else if (o instanceof DateTime) return "DateTime";
		else if (o instanceof byte[]) return "Binary";
		else {
			String className = o.getClass().getName();
			if (className.startsWith("java.lang.")) {
				return className.substring(10);
			}
			return className;
		}

	}

	public static String getName(Class clazz) {
		if (clazz == null) return "null";
		// String name=clazz.getName();
		// if(Reflector.isInstaneOf(clazz,String.class)) return "String";
		if (Reflector.isInstaneOf(clazz, UDF.class, false)) return "user defined function";
		// else if(Reflector.isInstaneOf(clazz,Boolean.class)) return "Boolean";
		// else if(Reflector.isInstaneOf(clazz,Number.class)) return "Number";
		else if (Reflector.isInstaneOf(clazz, Array.class, false)) return "Array";
		else if (Reflector.isInstaneOf(clazz, Struct.class, false)) return "Struct";
		else if (Reflector.isInstaneOf(clazz, Query.class, false)) return "Query";
		else if (Reflector.isInstaneOf(clazz, DateTime.class, false)) return "DateTime";
		else if (Reflector.isInstaneOf(clazz, Component.class, false)) return "Component";
		else if (Reflector.isInstaneOf(clazz, byte[].class, false)) return "Binary";
		else {
			String className = clazz.getName();
			if (className.startsWith("java.lang.")) {
				return className.substring(10);
			}
			return className;
		}

	}

}