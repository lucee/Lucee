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
package lucee.runtime.type;

import java.util.Iterator;
import java.util.Map;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.PropertyFactory;
import lucee.runtime.type.util.UDFUtil;

public final class UDFHasProperty extends UDFGSProperty {

	private final Property prop;
	// private ComponentScope scope;

	private final Key propName;

	// private static final String NULL="sdsdsdfsfsfjkln fsdfsa";

	public UDFHasProperty(Component component, Property prop) {
		super(component, "has" + StringUtil.ucFirst(PropertyFactory.getSingularName(prop)), getFunctionArgument(prop), CFTypes.TYPE_BOOLEAN);
		this.prop = prop;
		this.propName = KeyImpl.init(prop.getName());
	}

	private static FunctionArgument[] getFunctionArgument(Property prop) {
		String t = PropertyFactory.getType(prop);

		if ("struct".equalsIgnoreCase(t)) {
			FunctionArgument key = new FunctionArgumentLight(KeyConstants._key, "string", CFTypes.TYPE_STRING, false);
			return new FunctionArgument[] { key };
		}
		FunctionArgument value = new FunctionArgumentLight(KeyImpl.init(PropertyFactory.getSingularName(prop)), "any", CFTypes.TYPE_ANY, false);
		return new FunctionArgument[] { value };
	}

	private boolean isStruct() {
		String t = PropertyFactory.getType(prop);
		return "struct".equalsIgnoreCase(t);
	}

	@Override
	public UDF duplicate() {
		return new UDFHasProperty(srcComponent, prop);
	}

	@Override
	public Object _call(PageContext pageContext, Object[] args, boolean doIncludePath) throws PageException {
		if (args.length < 1) return has(pageContext);
		return has(pageContext, args[0]);
	}

	@Override
	public Object _callWithNamedValues(PageContext pageContext, Struct values, boolean doIncludePath) throws PageException {
		UDFUtil.argumentCollection(values, getFunctionArguments());
		Key key = arguments[0].getName();
		Object value = values.get(key, null);
		if (value == null) {
			Key[] keys = CollectionUtil.keys(values);
			if (keys.length > 0) {
				value = values.get(keys[0]);
			}
			else return has(pageContext);
		}

		return has(pageContext, value);
	}

	private boolean has(PageContext pageContext) {
		Object propValue = getComponent(pageContext).getComponentScope().get(propName, null);

		// struct
		if (isStruct()) {
			if (propValue instanceof Map) {
				return !((Map) propValue).isEmpty();
			}
			return false;
		}

		// Object o;
		if (propValue instanceof Array) {
			Array arr = ((Array) propValue);
			return arr.size() > 0;
		}
		else if (propValue instanceof java.util.List) {

			return ((java.util.List) propValue).size() > 0;
		}
		return propValue instanceof Component;

	}

	private boolean has(PageContext pageContext, Object value) throws PageException {
		Object propValue = getComponent(pageContext).getComponentScope().get(propName, null);

		// struct
		if (isStruct()) {
			String strKey = Caster.toString(value);
			// if(strKey==NULL) throw new ;

			if (propValue instanceof Struct) {
				return ((Struct) propValue).containsKey(KeyImpl.getInstance(strKey));
			}
			else if (propValue instanceof Map) {
				return ((Map) propValue).containsKey(strKey);
			}
			return false;
		}

		Object o;

		if (propValue instanceof Array) {
			Array arr = ((Array) propValue);
			Iterator<Object> it = arr.valueIterator();
			while (it.hasNext()) {
				if (ORMUtil.equals(value, it.next())) return true;
			}
		}
		else if (propValue instanceof java.util.List) {
			Iterator it = ((java.util.List) propValue).iterator();
			while (it.hasNext()) {
				o = it.next();
				if (ORMUtil.equals(value, o)) return true;
			}
		}
		return false;

	}

	@Override
	public Object implementation(PageContext pageContext) throws Throwable {
		return null;
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index) throws PageException {
		return prop.getDefault();
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index, Object defaultValue) throws PageException {
		return prop.getDefault();
	}

	@Override
	public String getReturnTypeAsString() {
		return "boolean";
	}
}