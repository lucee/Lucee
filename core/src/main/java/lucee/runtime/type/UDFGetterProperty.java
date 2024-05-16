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

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;

public final class UDFGetterProperty extends UDFGSProperty {

	private static final FunctionArgument[] EMPTY = new FunctionArgument[0];

	private final Property prop;
	// private ComponentScope scope;
	private final Key propName;

	public UDFGetterProperty(Component component, Property prop) {
		super(component, "get" + StringUtil.ucFirst(prop.getName()), EMPTY, CFTypes.TYPE_STRING);
		this.prop = prop;
		this.propName = KeyImpl.init(prop.getName());

	}

	@Override
	public UDF duplicate() {
		return new UDFGetterProperty(srcComponent, prop);
	}

	@Override
	public Object _call(PageContext pageContext, Object[] args, boolean doIncludePath) throws PageException {
		return getComponent(pageContext).getComponentScope().get(pageContext, propName, null);
	}

	@Override
	public Object _callWithNamedValues(PageContext pageContext, Struct values, boolean doIncludePath) throws PageException {
		return getComponent(pageContext).getComponentScope().get(pageContext, propName, null);
	}

	@Override
	public Object implementation(PageContext pageContext) throws Throwable {
		return getComponent(pageContext).getComponentScope().get(pageContext, propName, null);
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index) throws PageException {
		return null;
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index, Object defaultValue) throws PageException {
		return defaultValue;
	}

	@Override
	public String getReturnTypeAsString() {
		return prop.getType();
	}

}