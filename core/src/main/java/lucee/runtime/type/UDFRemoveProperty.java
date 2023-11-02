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
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Caster;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.PropertyFactory;
import lucee.runtime.type.util.UDFUtil;

public final class UDFRemoveProperty extends UDFGSProperty {

	private static final long serialVersionUID = -7030615729484825208L;

	private final Property prop;
	private final Key propName;

	public UDFRemoveProperty(Component component, Property prop) {
		super(component, "remove" + StringUtil.ucFirst(PropertyFactory.getSingularName(prop)), getFunctionArgument(prop), CFTypes.TYPE_BOOLEAN);
		this.prop = prop;
		this.propName = KeyImpl.getInstance(prop.getName());
	}

	private static FunctionArgument[] getFunctionArgument(Property prop) {
		String t = PropertyFactory.getType(prop);

		if ("struct".equalsIgnoreCase(t)) {
			FunctionArgument key = new FunctionArgumentLight(KeyConstants._key, "string", CFTypes.TYPE_STRING, true);
			return new FunctionArgument[] { key };
		}
		FunctionArgument value = new FunctionArgumentLight(KeyImpl.init(PropertyFactory.getSingularName(prop)), "any", CFTypes.TYPE_ANY, true);
		return new FunctionArgument[] { value };
	}

	private boolean isStruct() {
		String t = PropertyFactory.getType(prop);
		return "struct".equalsIgnoreCase(t);
	}

	@Override
	public UDF duplicate() {
		return new UDFRemoveProperty(srcComponent, prop);
	}

	@Override
	public Object _call(PageContext pageContext, Object[] args, boolean doIncludePath) throws PageException {
		if (args.length < 1)
			throw new ExpressionException("The parameter [" + this.arguments[0].getName() + "] to function [" + getFunctionName() + "] is required but was not passed in.");

		return remove(pageContext, args[0]);
	}

	@Override
	public Object _callWithNamedValues(PageContext pageContext, Struct values, boolean doIncludePath) throws PageException {
		UDFUtil.argumentCollection(values, getFunctionArguments());
		Key key = arguments[0].getName();
		Object value = values.get(key, null);
		if (value == null) {
			Key[] keys = CollectionUtil.keys(values);
			if (keys.length == 1) {
				value = values.get(keys[0]);
			}
			else throw new ExpressionException("The parameter [" + key + "] to function [" + getFunctionName() + "] is required but was not passed in.");
		}

		return remove(pageContext, value);
	}

	private boolean remove(PageContext pageContext, Object value) throws PageException {
		Component c = getComponent(pageContext);
		Object propValue = c.getComponentScope().get(propName, null);
		value = cast(pageContext, arguments[0], value, 1);

		// make sure it is reconized that set is called by hibernate
		// if(component.isPersistent())ORMUtil.getSession(pageContext);
		ApplicationContext appContext = pageContext.getApplicationContext();
		if (appContext.isORMEnabled() && c.isPersistent()) ORMUtil.getSession(pageContext);

		// struct
		if (isStruct()) {
			String strKey = Caster.toString(value, null);
			if (strKey == null) return false;

			if (propValue instanceof Struct) {
				return ((Struct) propValue).removeEL(KeyImpl.getInstance(strKey)) != null;
			}
			else if (propValue instanceof Map) {
				return ((Map) propValue).remove(strKey) != null;
			}
			return false;
		}

		Object o;
		boolean has = false;
		if (propValue instanceof Array) {
			Array arr = ((Array) propValue);
			Key[] keys = CollectionUtil.keys(arr);
			for (int i = 0; i < keys.length; i++) {
				o = arr.get(keys[i], null);
				if (ORMUtil.equals(value, o)) {
					arr.removeEL(keys[i]);
					has = true;
				}
			}
		}
		else if (propValue instanceof java.util.List) {
			Iterator it = ((java.util.List) propValue).iterator();
			while (it.hasNext()) {
				o = it.next();
				if (ORMUtil.equals(value, o)) {
					it.remove();
					has = true;
				}
			}
		}
		return has;

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
