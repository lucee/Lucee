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

import java.util.HashMap;
import java.util.Map;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.PropertyFactory;
import lucee.runtime.type.util.UDFUtil;

public final class UDFAddProperty extends UDFGSProperty {

	private static final long serialVersionUID = 94007529373807331L;

	private final Property prop;
	private final Key propName;

	public UDFAddProperty(Component component, Property prop) {
		super(component, "add" + StringUtil.ucFirst(PropertyFactory.getSingularName(prop)), getFunctionArgument(prop), CFTypes.TYPE_ANY);
		this.prop = prop;
		this.propName = KeyImpl.init(prop.getName());
	}

	private static FunctionArgument[] getFunctionArgument(Property prop) {
		String t = PropertyFactory.getType(prop);
		FunctionArgument value = new FunctionArgumentLight(KeyImpl.init(PropertyFactory.getSingularName(prop)), "any", CFTypes.TYPE_ANY, true);
		if ("struct".equalsIgnoreCase(t)) {
			FunctionArgument key = new FunctionArgumentLight(KeyConstants._key, "string", CFTypes.TYPE_STRING, true);
			return new FunctionArgument[] { key, value };
		}
		return new FunctionArgument[] { value };
	}

	@Override
	public UDF duplicate() {
		return new UDFAddProperty(srcComponent, prop);
	}

	@Override
	public Object _call(PageContext pageContext, Object[] args, boolean doIncludePath) throws PageException {
		Component c = getComponent(pageContext);
		// struct
		if (this.arguments.length == 2) {
			if (args.length < 2) throw new ExpressionException(
					"The function [" + getFunctionName() + "] needs 2 arguments, only " + args.length + " argument" + (args.length == 1 ? " is" : "s are") + " passed in.");
			return _call(pageContext, c, args[0], args[1]);
		}
		// array
		else if (this.arguments.length == 1) {
			if (args.length < 1)
				throw new ExpressionException("The parameter [" + this.arguments[0].getName() + "] to function [" + getFunctionName() + "] is required but was not passed in.");
			return _call(pageContext, c, null, args[0]);
		}

		// never reached
		return c;

	}

	@Override
	public Object _callWithNamedValues(PageContext pageContext, Struct values, boolean doIncludePath) throws PageException {
		UDFUtil.argumentCollection(values, getFunctionArguments());
		Component c = getComponent(pageContext);

		// struct
		if (this.arguments.length == 2) {
			Key keyName = arguments[0].getName();
			Key valueName = arguments[1].getName();
			Object key = values.get(keyName, null);
			Object value = values.get(valueName, null);
			if (key == null) throw new ExpressionException("The parameter [" + keyName + "] to function [" + getFunctionName() + "] is required but was not passed in.");
			if (value == null) throw new ExpressionException("The parameter [" + valueName + "] to function [" + getFunctionName() + "] is required but was not passed in.");

			return _call(pageContext, c, key, value);
		}
		// array
		else if (this.arguments.length == 1) {
			Key valueName = arguments[0].getName();
			Object value = values.get(valueName, null);
			if (value == null) {
				Key[] keys = CollectionUtil.keys(values);
				if (keys.length == 1) {
					value = values.get(keys[0]);
				}
				else throw new ExpressionException("The parameter [" + valueName + "] to function [" + getFunctionName() + "] is required but was not passed in.");
			}
			return _call(pageContext, c, null, value);
		}

		// never reached
		return getComponent(pageContext);
	}

	private Object _call(PageContext pageContext, Component c, Object key, Object value) throws PageException {

		Object propValue = c.getComponentScope().get(propName, null);

		// struct
		if (this.arguments.length == 2) {
			key = cast(pageContext, arguments[0], key, 1);
			value = cast(pageContext, arguments[1], value, 2);
			if (propValue == null) {
				HashMap map = new HashMap();
				c.getComponentScope().setEL(propName, map);
				propValue = map;
			}
			if (propValue instanceof Struct) {
				((Struct) propValue).set(KeyImpl.toKey(key), value);
			}
			else if (propValue instanceof Map) {
				((Map) propValue).put(key, value);
			}
		}
		else {
			value = cast(pageContext, arguments[0], value, 1);
			if (propValue == null) {
				/*
				 * jira2049 PageContext pc = ThreadLocalPageContext.get(); ORMSession sess = ORMUtil.getSession(pc);
				 * SessionImpl s=(SessionImpl) sess.getRawSession(); propValue=new PersistentList(s);
				 * component.getComponentScope().setEL(propName,propValue);
				 */
				Array arr = new ArrayImpl();
				c.getComponentScope().setEL(propName, arr);
				propValue = arr;
			}
			if (propValue instanceof Array) {
				((Array) propValue).appendEL(value);
			}
			else if (propValue instanceof java.util.List) {
				((java.util.List) propValue).add(value);
			}
		}
		return c;
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
		return "any";
	}
}
