/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
import lucee.runtime.PageSource;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.ScriptConverter;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructUtil;
import lucee.transformer.bytecode.util.ASMProperty;
import lucee.transformer.bytecode.util.ASMUtil;

public final class PropertyImpl extends MemberSupport implements Property, ASMProperty {

	private static final long serialVersionUID = 3206074213415946902L;

	// Reference fields (8 bytes each) - group together to minimize padding
	private String type = "any";
	private String name;
	private Collection.Key nameAsKey; // Cached key for property name
	private Collection.Key getterKey; // Cached key for "getName"
	private Collection.Key setterKey; // Cached key for "setName"
	private String getterName; // Cached "getName" string
	private String setterName; // Cached "setName" string
	private Object _default;
	private String displayname = "";
	private String hint = "";
	private Struct dynAttrs; // lazy-init to avoid allocating ConcurrentHashMap(32) per property
	private Struct metadata;
	private String ownerName;
	private PageSource ownerPageSource;

	// Boolean fields (1 byte each) - group at end to minimize padding
	private boolean required;
	private boolean setter = true;
	private boolean getter = true;
	private boolean axisType;

	public PropertyImpl() {
		this(true);
	}

	public PropertyImpl(boolean axisType) {
		super(Component.ACCESS_REMOTE);
		this.axisType = axisType;
	}

	@Override
	protected final int hash() {
		return java.util.Objects.hash(name);
	}

	@Override
	public String getDefault() {
		if (_default == null) return null;
		try {
			return Caster.toString(_default);
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public Object getDefaultAsObject() {
		return _default;
	}

	/**
	 * @param _default the _default to set
	 */
	public void setDefault(Object _default) {
		this._default = _default;
	}

	// FUTURE remove, exists for archives point to this
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
		this.nameAsKey = null; // Clear cached key when name changes
		this.getterKey = null; // Clear cached getter key
		this.setterKey = null; // Clear cached setter key
		this.getterName = null; // Clear cached getter name
		this.setterName = null; // Clear cached setter name
	}

	public Collection.Key getNameAsKey() {
		if (nameAsKey == null && name != null) {
			nameAsKey = lucee.runtime.type.KeyImpl.init(name);
		}
		return nameAsKey;
	}

	public void setNameAsKey(Collection.Key key) {
		this.nameAsKey = key;
	}

	public Collection.Key getGetterKey() {
		if (getterKey == null && name != null) {
			getterKey = lucee.runtime.type.KeyImpl.init("get" + name);
		}
		return getterKey;
	}

	public void setGetterKey(Collection.Key key) {
		this.getterKey = key;
	}

	public Collection.Key getSetterKey() {
		if (setterKey == null && name != null) {
			setterKey = lucee.runtime.type.KeyImpl.init("set" + name);
		}
		return setterKey;
	}

	public void setSetterKey(Collection.Key key) {
		this.setterKey = key;
	}

	public String getGetterName() {
		if (getterName == null && name != null) {
			getterName = "get" + StringUtil.ucFirst(name);
		}
		return getterName;
	}

	public String getSetterName() {
		if (setterName == null && name != null) {
			setterName = "set" + StringUtil.ucFirst(name);
		}
		return setterName;
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
		return ASMUtil.toType(ThreadLocalPageContext.get(), getType(), axisType);
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

	/**
	 * Lazy-init helper for dynAttrs - creates HashMap with minimal capacity only when needed
	 */
	private Struct ensureDynAttrs() {
		if (dynAttrs == null) {
			dynAttrs = new StructImpl(StructImpl.TYPE_REGULAR, 8);
		}
		return dynAttrs;
	}

	@Override
	public Object getMetaData() {
		// Typical size: name + hint + displayname + type + default + dynAttrs + metadata = ~16
		Struct sct = new StructImpl(StructImpl.TYPE_REGULAR, 16);

		// meta
		if (metadata != null) StructUtil.copy(metadata, sct, true);

		sct.setEL(KeyConstants._name, name);
		if (!StringUtil.isEmpty(hint, true)) sct.setEL(KeyConstants._hint, hint);
		if (!StringUtil.isEmpty(displayname, true)) sct.setEL(KeyConstants._displayname, displayname);
		if (!StringUtil.isEmpty(type, true)) sct.setEL(KeyConstants._type, type);
		if (_default != null) sct.setEL(KeyConstants._default, _default);

		// dyn attributes (includes 'required' when explicitly set)
		if (dynAttrs != null) StructUtil.copy(dynAttrs, sct, true);

		return sct;
	}

	public void setDynamicAttributes(Struct dynAttrs) {
		this.dynAttrs = dynAttrs;
	}

	@Override
	public Struct getDynamicAttributes() {
		return ensureDynAttrs();
	}

	public void setMetaData(Struct metadata) {
		this.metadata = metadata;
	}

	@Override
	public Struct getMeta() {
		if (metadata == null) metadata = new StructImpl(StructImpl.TYPE_REGULAR, 8);
		return metadata;
	}

	@Override
	public Class<?> getClazz() {
		return null;
	}

	@Override
	public boolean isPeristent() {
		return dynAttrs == null ? true : Caster.toBooleanValue(dynAttrs.get(KeyConstants._persistent, Boolean.TRUE), true);
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public void setOwnerName(String ownerName, PageSource ownerPageSource) {
		this.ownerName = ownerName;
		this.ownerPageSource = ownerPageSource;
	}

	@Override
	public String getOwnerName() {
		return ownerName;
	}

	public PageSource getOwnerPageSource() {
		return ownerPageSource;
	}

	@Override
	public String toString() {
		String strDynAttrs = "";
		if (dynAttrs != null) {
			try {
				strDynAttrs = new ScriptConverter().serialize(dynAttrs);
			}
			catch (ConverterException ce) {
			}
		}

		return "default:" + this._default + ";displayname:" + this.displayname + ";hint:" + this.hint + ";name:" + this.name + ";type:" + this.type + ";ownerName:" + ownerName
				+ ";attrs:" + strDynAttrs + ";";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PropertyImpl)) return false;
		PropertyImpl other = (PropertyImpl) obj;

		// Compare actual fields instead of allocating strings via toString()
		if (!StringUtil.emptyIfNull(name).equals(StringUtil.emptyIfNull(other.name))) return false;
		if (!StringUtil.emptyIfNull(type).equals(StringUtil.emptyIfNull(other.type))) return false;
		if (!StringUtil.emptyIfNull(displayname).equals(StringUtil.emptyIfNull(other.displayname))) return false;
		if (!StringUtil.emptyIfNull(hint).equals(StringUtil.emptyIfNull(other.hint))) return false;
		if (!StringUtil.emptyIfNull(ownerName).equals(StringUtil.emptyIfNull(other.ownerName))) return false;
		if (required != other.required) return false;
		if (getter != other.getter) return false;
		if (setter != other.setter) return false;

		// Compare default value
		if (_default == null) {
			if (other._default != null) return false;
		}
		else if (!_default.equals(other._default)) return false;

		// Compare dynamic attributes
		if (dynAttrs == null) {
			if (other.dynAttrs != null && other.dynAttrs.size() > 0) return false;
		}
		else if (other.dynAttrs == null) {
			if (dynAttrs.size() > 0) return false;
		}
		else {
			// Both have dynAttrs - use toString() comparison as fallback for Struct comparison
			try {
				String thisDynAttrs = new ScriptConverter().serialize(dynAttrs);
				String otherDynAttrs = new ScriptConverter().serialize(other.dynAttrs);
				if (!thisDynAttrs.equals(otherDynAttrs)) return false;
			}
			catch (ConverterException ce) {
				// If serialization fails, fall back to reference equality
				if (dynAttrs != other.dynAttrs) return false;
			}
		}

		return true;
	}

	@Override
	public Object duplicate(boolean deepCopy) {
		PropertyImpl other = new PropertyImpl();
		other._default = _default;
		other.displayname = displayname;
		other.getter = getter;
		other.hint = hint;
		other.dynAttrs = dynAttrs == null ? null : (deepCopy ? (Struct) Duplicator.duplicate(dynAttrs, deepCopy) : dynAttrs);
		other.name = name;
		other.ownerName = ownerName;
		other.ownerPageSource = ownerPageSource;
		other.required = required;
		other.setter = setter;
		other.type = type;
		// Copy cached keys and names to avoid recalculation
		other.nameAsKey = nameAsKey;
		other.getterKey = getterKey;
		other.setterKey = setterKey;
		other.getterName = getterName;
		other.setterName = setterName;

		return other;
	}}