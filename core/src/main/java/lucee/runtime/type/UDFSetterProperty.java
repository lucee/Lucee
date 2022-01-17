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
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.UDFUtil;

public final class UDFSetterProperty extends UDFGSProperty {

	/**
	 * 
	 */
	private static final long serialVersionUID = 378348754607851563L;

	private static final Collection.Key VALIDATE_PARAMS = KeyImpl.getInstance("validateParams");
	private final Property prop;
	private final Key propName;
	private String validate;
	private Struct validateParams;

	private UDFSetterProperty(Component component, Property prop, String validate, Struct validateParams) {
		super(component, "set" + StringUtil.ucFirst(prop.getName()),
				new FunctionArgument[] {
						new FunctionArgumentLight(KeyImpl.init(prop.getName()), prop.getType(), CFTypes.toShortStrict(prop.getType(), CFTypes.TYPE_UNKNOW), true) },
				CFTypes.TYPE_ANY);
		this.prop = prop;
		this.propName = KeyImpl.init(prop.getName());
		this.validate = validate;
		this.validateParams = validateParams;
	}

	public UDFSetterProperty(Component component, Property prop) throws PageException {
		super(component, "set" + StringUtil.ucFirst(prop.getName()),
				new FunctionArgument[] {
						new FunctionArgumentLight(KeyImpl.init(prop.getName()), prop.getType(), CFTypes.toShortStrict(prop.getType(), CFTypes.TYPE_UNKNOW), true) },
				CFTypes.TYPE_ANY);

		this.prop = prop;
		this.propName = KeyImpl.init(prop.getName());

		this.validate = Caster.toString(prop.getDynamicAttributes().get(KeyConstants._validate, null), null);
		if (!StringUtil.isEmpty(validate, true)) {
			validate = validate.trim().toLowerCase();
			Struct da = prop.getDynamicAttributes();
			if (da != null) {
				Object o = da.get(VALIDATE_PARAMS, null);
				if (o != null) {
					if (Decision.isStruct(o)) validateParams = Caster.toStruct(o);
					else {
						String str = Caster.toString(o);
						if (!StringUtil.isEmpty(str, true)) {
							validateParams = ORMUtil.convertToSimpleMap(str);
							if (validateParams == null) throw new ExpressionException("cannot parse string [" + str + "] as struct");
						}
					}
				}
			}
		}
	}

	@Override
	public UDF duplicate() {
		return new UDFSetterProperty(srcComponent, prop, validate, validateParams);
	}

	@Override
	public Object _call(PageContext pageContext, Object[] args, boolean doIncludePath) throws PageException {
		if (args.length < 1) throw new ExpressionException("The parameter " + prop.getName() + " to function " + getFunctionName() + " is required but was not passed in.");
		validate(validate, validateParams, args[0]);
		Component c = getComponent(pageContext);
		c.getComponentScope().set(propName, cast(pageContext, this.arguments[0], args[0], 1));

		// make sure it is reconized that set is called by hibernate
		// if(component.isPersistent())ORMUtil.getSession(pageContext);
		ApplicationContext appContext = pageContext.getApplicationContext();
		if (appContext.isORMEnabled() && c.isPersistent()) ORMUtil.getSession(pageContext);

		return c;
	}

	@Override
	public Object _callWithNamedValues(PageContext pageContext, Struct values, boolean doIncludePath) throws PageException {
		UDFUtil.argumentCollection(values, getFunctionArguments());
		Object value = values.get(propName, null);
		Component c = getComponent(pageContext);

		if (value == null) {
			Key[] keys = CollectionUtil.keys(values);
			if (keys.length == 1) {
				value = values.get(keys[0]);
			}
			else throw new ExpressionException("The parameter " + prop.getName() + " to function " + getFunctionName() + " is required but was not passed in.");
		}
		c.getComponentScope().set(propName, cast(pageContext, arguments[0], value, 1));

		// make sure it is reconized that set is called by hibernate
		// if(component.isPersistent())ORMUtil.getSession(pageContext);
		ApplicationContext appContext = pageContext.getApplicationContext();
		if (appContext.isORMEnabled() && c.isPersistent()) ORMUtil.getSession(pageContext);

		return c;
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

	@Override
	public Object implementation(PageContext pageContext) throws Throwable {
		return null;
	}

}