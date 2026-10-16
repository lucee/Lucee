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
package lucee.runtime.tag;

import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.DynamicAttributes;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * Defines components as complex types that are used for web services authoring. The attributes of
 * this tag are exposed as component metadata and are subject to inheritance rules.
 *
 *
 *
 **/
public final class Property extends TagImpl implements DynamicAttributes {

	private lucee.runtime.component.PropertyImpl property = new lucee.runtime.component.PropertyImpl();

	@Override
	public void release() {
		super.release();
		property = new lucee.runtime.component.PropertyImpl();
	}

	@Override
	public void setDynamicAttribute(String uri, String name, Object value) {
		property.getDynamicAttributes().setEL(KeyImpl.init(name), value);
	}

	@Override
	public void setDynamicAttribute(String uri, Collection.Key name, Object value) {
		property.getDynamicAttributes().setEL(name, value);
	}

	public void setMetaData(String name, Object value) {
		property.getMeta().setEL(KeyImpl.init(name), value);
	}

	/**
	 * set the value type A string; a property type name; data type.
	 * 
	 * @param type value to set
	 **/
	public void setType(String type) {
		property.setType(type);
		setDynamicAttribute(null, KeyConstants._type, type);
	}

	/**
	 * set the value name A string; a property name. Must be a static value.
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		// Fix for axis 1.4, axis can not handle when first char is upper case
		// name=StringUtil.lcFirst(name.toLowerCase());

		property.setName(name);
		setDynamicAttribute(null, KeyConstants._name, name);
	}

	/**
	 * FUTURE exists for backward compatibility with archives
	 */
	public void setDefault(String _default) {
		property.setDefault(_default);
		setDynamicAttribute(null, "default", _default);
	}

	/**
	 * @param _default The _default to set.
	 */
	public void setDefault(Object _default) {
		property.setDefault(_default);
		setDynamicAttribute(null, "default", _default);

	}

	/**
	 * @param access The access to set.
	 * @throws ApplicationException
	 */
	public void setAccess(String access) throws ApplicationException {
		setDynamicAttribute(null, "access", access);
		property.setAccess(access);
	}

	/**
	 * @param displayname The displayname to set.
	 */
	public void setDisplayname(String displayname) {
		property.setDisplayname(displayname);
		setDynamicAttribute(null, "displayname", displayname);
	}

	/**
	 * @param hint The hint to set.
	 */
	public void setHint(String hint) {
		property.setHint(hint);
		setDynamicAttribute(null, "hint", hint);
	}

	/**
	 * @param required The required to set.
	 */
	public void setRequired(boolean required) {
		property.setRequired(required);
		setDynamicAttribute(null, "required", required ? "yes" : "no");
	}

	public void setSetter(boolean setter) {
		property.setSetter(setter);
		setDynamicAttribute(null, "setter", setter ? "yes" : "no");
	}

	public void setGetter(boolean setter) {
		property.setGetter(setter);
		setDynamicAttribute(null, "getter", setter ? "yes" : "no");
	}

	@Override
	public int doStartTag() throws PageException {
		if (pageContext.variablesScope() instanceof ComponentScope) {
			Component comp = ((ComponentScope) pageContext.variablesScope()).getComponent();
			comp.setProperty(property);
			// LDEV-3335: Only set owner if not already set (e.g., from parent component)
			if (property.getOwnerPageSource() == null) {
				property.setOwnerName(comp.getAbsName(), comp.getPageSource());
			}
		}

		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
	/*
	 * unfortunately, this validation breaks frameworks that set default to a value that does not match
	 * the type
	 * 
	 * public int doEndTag() throws PageException { // Validate default value against type if type is
	 * not 'any' String type = property.getType(); Object defaultValue = property.getDefaultAsObject();
	 * 
	 * if (!StringUtil.isEmpty(type, true) && !"any".equalsIgnoreCase(type) && defaultValue != null) {
	 * if (!Decision.isCastableTo(type, defaultValue, true, true, -1)) { throw new ApplicationException(
	 * "Default value for property [" + property.getName() + "] is not compatible with type [" + type +
	 * "]" ); } }
	 * 
	 * return EVAL_PAGE; }
	 */
}