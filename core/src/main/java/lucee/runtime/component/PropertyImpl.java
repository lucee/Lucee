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
package lucee.runtime.component;

import org.objectweb.asm.Type;

import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.ScriptConverter;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructUtil;
import lucee.transformer.bytecode.util.ASMProperty;
import lucee.transformer.bytecode.util.ASMUtil;

/**
 */
public final class PropertyImpl extends MemberSupport implements Property, ASMProperty {

	private static final long serialVersionUID = 3206074213415946902L;

	private String type = "any";
	private String name;
	private boolean required;
	private boolean setter = true;
	private boolean getter = true;

	private String _default;
	private String displayname = "";
	private String hint = "";
	private Struct dynAttrs = new StructImpl();
	private Struct metadata;

	private String ownerName;

	public PropertyImpl() {
		super(Component.ACCESS_REMOTE);
	}

	/**
	 * @return the _default
	 */
	@Override
	public String getDefault() {
		return _default;
	}

	/**
	 * @param _default the _default to set
	 */
	public void setDefault(String _default) {
		this._default = _default;
	}

	/**
	 * @return the displayname
	 */
	@Override
	public String getDisplayname() {
		return displayname;
	}

	/**
	 * @param displayname the displayname to set
	 */
	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}

	/**
	 * @return the hint
	 */
	@Override
	public String getHint() {
		return hint;
	}

	/**
	 * @param hint the hint to set
	 */
	public void setHint(String hint) {
		this.hint = hint;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the required
	 */
	@Override
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * @return the type
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Object getValue() {
		return _default;
	}

	@Override
	public Type getASMType() throws PageException {
		return ASMUtil.toType(getType(), true);
	}

	/**
	 * @return the setter
	 */
	@Override
	public boolean getSetter() {
		return setter;
	}

	/**
	 * @param setter the setter to set
	 */
	public void setSetter(boolean setter) {
		this.setter = setter;
	}

	/**
	 * @return the getter
	 */
	@Override
	public boolean getGetter() {
		return getter;
	}

	/**
	 * @param getter the getter to set
	 */
	public void setGetter(boolean getter) {
		this.getter = getter;
	}

	@Override
	public Object getMetaData() {
		Struct sct = new StructImpl();

		// meta
		if (metadata != null) StructUtil.copy(metadata, sct, true);

		sct.setEL(KeyConstants._name, name);
		if (!StringUtil.isEmpty(hint, true)) sct.setEL(KeyConstants._hint, hint);
		if (!StringUtil.isEmpty(displayname, true)) sct.setEL(KeyConstants._displayname, displayname);
		if (!StringUtil.isEmpty(type, true)) sct.setEL(KeyConstants._type, type);

		// dyn attributes

		StructUtil.copy(dynAttrs, sct, true);

		return sct;
	}

	@Override
	public Struct getDynamicAttributes() {
		return dynAttrs;
	}

	@Override
	public Struct getMeta() {
		if (metadata == null) metadata = new StructImpl();
		return metadata;
	}

	@Override
	public Class getClazz() {
		return null;
	}

	@Override
	public boolean isPeristent() {
		return Caster.toBooleanValue(dynAttrs.get(KeyConstants._persistent, Boolean.TRUE), true);
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	@Override
	public String getOwnerName() {
		return ownerName;
	}

	@Override
	public String toString() {
		String strDynAttrs = "";
		try {
			strDynAttrs = new ScriptConverter().serialize(dynAttrs);
		}
		catch (ConverterException ce) {
		}

		return "default:" + this._default + ";displayname:" + this.displayname + ";hint:" + this.hint + ";name:" + this.name + ";type:" + this.type + ";ownerName:" + ownerName
				+ ";attrs:" + strDynAttrs + ";";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Property)) return false;
		Property other = (Property) obj;

		return toString().equals(other.toString());
	}

	@Override
	public Object duplicate(boolean deepCopy) {
		PropertyImpl other = new PropertyImpl();
		other._default = _default;
		other.displayname = displayname;
		other.getter = getter;
		other.hint = hint;
		other.dynAttrs = deepCopy ? (Struct) Duplicator.duplicate(dynAttrs, deepCopy) : dynAttrs;
		other.name = name;
		other.ownerName = ownerName;
		other.required = required;
		other.setter = setter;
		other.type = type;

		return other;
	}
}