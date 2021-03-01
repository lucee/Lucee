/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.type.util;

import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentImpl;
import lucee.runtime.component.Member;
import lucee.runtime.component.Property;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFAddProperty;
import lucee.runtime.type.UDFGetterProperty;
import lucee.runtime.type.UDFHasProperty;
import lucee.runtime.type.UDFRemoveProperty;
import lucee.runtime.type.UDFSetterProperty;

public class PropertyFactory {

	public static final Collection.Key SINGULAR_NAME = KeyImpl.getInstance("singularName");
	public static final Key FIELD_TYPE = KeyConstants._fieldtype;

	public static void createPropertyUDFs(ComponentImpl comp, Property property) throws PageException {
		// getter
		if (property.getGetter()) {
			PropertyFactory.addGet(comp, property);
		}
		// setter
		if (property.getSetter()) {
			PropertyFactory.addSet(comp, property);
		}

		String fieldType = Caster.toString(property.getDynamicAttributes().get(PropertyFactory.FIELD_TYPE, null), null);

		// add
		if (fieldType != null) {
			if ("one-to-many".equalsIgnoreCase(fieldType) || "many-to-many".equalsIgnoreCase(fieldType)) {
				PropertyFactory.addHas(comp, property);
				PropertyFactory.addAdd(comp, property);
				PropertyFactory.addRemove(comp, property);
			}
			else if ("one-to-one".equalsIgnoreCase(fieldType) || "many-to-one".equalsIgnoreCase(fieldType)) {
				PropertyFactory.addHas(comp, property);
			}
		}
	}

	public static void addGet(ComponentImpl comp, Property prop) throws ApplicationException {
		Member m = comp.getMember(Component.ACCESS_PRIVATE, KeyImpl.init("get" + prop.getName()), true, false);
		if (!(m instanceof UDF)) {
			UDF udf = new UDFGetterProperty(comp, prop);
			comp.registerUDF(KeyImpl.init(udf.getFunctionName()), udf);
		}
	}

	public static void addSet(ComponentImpl comp, Property prop) throws PageException {
		Member m = comp.getMember(Component.ACCESS_PRIVATE, KeyImpl.init("set" + prop.getName()), true, false);
		if (!(m instanceof UDF)) {
			UDF udf = new UDFSetterProperty(comp, prop);
			comp.registerUDF(KeyImpl.init(udf.getFunctionName()), udf);
		}
	}

	public static void addHas(ComponentImpl comp, Property prop) throws ApplicationException {
		Member m = comp.getMember(Component.ACCESS_PRIVATE, KeyImpl.init("has" + getSingularName(prop)), true, false);
		if (!(m instanceof UDF)) {
			UDF udf = new UDFHasProperty(comp, prop);
			comp.registerUDF(KeyImpl.init(udf.getFunctionName()), udf);
		}
	}

	public static void addAdd(ComponentImpl comp, Property prop) throws ApplicationException {
		Member m = comp.getMember(Component.ACCESS_PRIVATE, KeyImpl.init("add" + getSingularName(prop)), true, false);
		if (!(m instanceof UDF)) {
			UDF udf = new UDFAddProperty(comp, prop);
			comp.registerUDF(KeyImpl.init(udf.getFunctionName()), udf);
		}
	}

	public static void addRemove(ComponentImpl comp, Property prop) throws ApplicationException {
		Member m = comp.getMember(Component.ACCESS_PRIVATE, KeyImpl.init("remove" + getSingularName(prop)), true, false);
		if (!(m instanceof UDF)) {
			UDF udf = new UDFRemoveProperty(comp, prop);
			comp.registerUDF(KeyImpl.init(udf.getFunctionName()), udf);
		}
	}

	public static String getSingularName(Property prop) {
		String singularName = Caster.toString(prop.getDynamicAttributes().get(SINGULAR_NAME, null), null);
		if (!StringUtil.isEmpty(singularName)) return singularName;
		return prop.getName();
	}

	public static String getType(Property prop) {
		String type = prop.getType();
		if (StringUtil.isEmpty(type) || "any".equalsIgnoreCase(type) || "object".equalsIgnoreCase(type)) {
			String fieldType = Caster.toString(prop.getDynamicAttributes().get(FIELD_TYPE, null), null);
			if ("one-to-many".equalsIgnoreCase(fieldType) || "many-to-many".equalsIgnoreCase(fieldType)) {
				return "array";
			}
			return "any";
		}
		return type;
	}

}