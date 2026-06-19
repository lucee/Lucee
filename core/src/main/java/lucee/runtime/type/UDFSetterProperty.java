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
import lucee.runtime.component.PropertyImpl;
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

	private static final Collection.Key VALIDATE_PARAMS = KeyConstants._validateParams;
	private final Key propName;
	private String validate;
	private Struct validateParams;

	private UDFSetterProperty(Component component, Property prop, String validate, Struct validateParams) {
		super(component, ((PropertyImpl) prop).getSetterName(),
				new FunctionArgument[] { new FuncArgLite(((PropertyImpl) prop).getNameAsKey(), prop.getType(), CFTypes.toShortStrict(prop.getType(), CFTypes.TYPE_UNKNOW), true) },
				CFTypes.TYPE_VOID);
		this.prop = prop;
		this.propName = ((PropertyImpl) prop).getNameAsKey();
		this.validate = validate;
		this.validateParams = validateParams;
	}

	public UDFSetterProperty(Component component, Property prop) throws PageException {
		super(component, ((PropertyImpl) prop).getSetterName(),
				new FunctionArgument[] { new FuncArgLite(((PropertyImpl) prop).getNameAsKey(), prop.getType(), CFTypes.toShortStrict(prop.getType(), CFTypes.TYPE_UNKNOW), true) },
				CFTypes.TYPE_VOID);

		this.prop = prop;
		this.propName = ((PropertyImpl) prop).getNameAsKey();

		// Cache getDynamicAttributes() call to avoid multiple lookups
		Struct da = prop.getDynamicAttributes();
		this.validate = Caster.toString(da.get(KeyConstants._validate, null), null);
		if (!StringUtil.isEmpty(this.validate, true)) {
			this.validate = this.validate.trim().toLowerCase();
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

	@Override
	public UDF duplicate() {
		return new UDFSetterProperty(srcComponent, prop, validate, validateParams);
	}

	@Override
	public Object _call(PageContext pageContext, Component comp, Object[] args) throws PageException {
		if (args == null || args.length < 1)
			throw new ExpressionException("The parameter " + prop.getName() + " to function " + getFunctionName() + " is required but was not passed in.");
		validate(validate, validateParams, args[0]);
		comp.getComponentScope().set(propName, cast(pageContext, this.arguments[0], args[0], 1));

		ApplicationContext appContext = pageContext.getApplicationContext();
		if (appContext.isORMEnabled() && comp.isPersistent()) ORMUtil.getSession(pageContext);

		return comp;
	}

	@Override
	public Object _callWithNamedValues(PageContext pageContext, Component comp, Struct values) throws PageException {
		UDFUtil.argumentCollection(values, getFunctionArguments());
		Object value = values.get(propName, null);

		if (value == null) {
			Key[] keys = CollectionUtil.keys(values);
			if (keys.length == 1) {
				value = values.get(keys[0]);
			}
			else throw new ExpressionException("The parameter " + prop.getName() + " to function " + getFunctionName() + " is required but was not passed in.");
		}
		comp.getComponentScope().set(propName, cast(pageContext, arguments[0], value, 1));

		ApplicationContext appContext = pageContext.getApplicationContext();
		if (appContext.isORMEnabled() && comp.isPersistent()) ORMUtil.getSession(pageContext);

		return comp;
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index) throws PageException {
		// FUTURE getDefaultAsObject was added in Beta pahse of Lucee 7, so we keep the checkcast in place
		return ((PropertyImpl) prop).getDefaultAsObject();
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index, Object defaultValue) throws PageException {
		// FUTURE getDefaultAsObject was added in Beta pahse of Lucee 7, so we keep the checkcast in place
		return ((PropertyImpl) prop).getDefaultAsObject();
	}

	@Override
	public String getReturnTypeAsString() {
		return "void";
	}

	@Override
	public Object implementation(PageContext pageContext) throws Throwable {
		return null;
	}

	public Property getProperty() {
		return prop;
	}

}