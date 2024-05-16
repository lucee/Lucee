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
package lucee.runtime.type;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExternalizableUtil;

/**
 * a single argument of a function
 */
public final class FunctionArgumentImpl implements FunctionArgument, Externalizable {

	private static final long serialVersionUID = -7275048405949174352L; // do not change

	private String dspName;
	private String hint;
	private Collection.Key name;
	private short type;
	private String strType;
	private boolean required;
	private Struct meta;
	private int defaultType;
	private boolean passByReference;

	/** @deprecated use other constructor */
	@Deprecated
	public FunctionArgumentImpl(String name, String type, boolean required) {
		this(name, type, required, "", "");
	}

	/** @deprecated use other constructor */
	@Deprecated
	public FunctionArgumentImpl(String name, String type, boolean required, String dspName, String hint) {
		this(name, type, required, DEFAULT_TYPE_RUNTIME_EXPRESSION, true, dspName, hint, null);
	}

	/** @deprecated use other constructor */
	@Deprecated
	public FunctionArgumentImpl(String name, String type, boolean required, String dspName, String hint, StructImpl meta) {
		this(name, type, required, DEFAULT_TYPE_RUNTIME_EXPRESSION, true, dspName, hint, meta);
	}

	/** @deprecated use other constructor */
	@Deprecated
	public FunctionArgumentImpl(String name, String type, boolean required, int defaultType, String dspName, String hint, StructImpl meta) {
		this(name, type, required, defaultType, true, dspName, hint, meta);
	}

	/** @deprecated use other constructor */
	@Deprecated
	public FunctionArgumentImpl(String name, String type, boolean required, double defaultType, String dspName, String hint, StructImpl meta) {
		this(name, type, required, (int) defaultType, true, dspName, hint, meta);
	}

	/** @deprecated use other constructor */
	@Deprecated
	public FunctionArgumentImpl(String name, String type, boolean required, double defaultType, boolean passByReference, String dspName, String hint, StructImpl meta) {
		this(name, type, required, (int) defaultType, passByReference, dspName, hint, meta);
	}

	/** @deprecated use other constructor */
	@Deprecated
	public FunctionArgumentImpl(String name, String type, boolean required, int defaultType, boolean passByReference, String dspName, String hint, StructImpl meta) {
		this(KeyImpl.init(name), type, required, defaultType, passByReference, dspName, hint, meta);
	}

	/** @deprecated use other constructor */
	@Deprecated
	public FunctionArgumentImpl(String name, String strType, short type, boolean required, int defaultType, boolean passByReference, String dspName, String hint, StructImpl meta) {
		this(KeyImpl.init(name), strType, type, required, defaultType, passByReference, dspName, hint, meta);
	}

	/** @deprecated use other constructor */
	@Deprecated
	public FunctionArgumentImpl(Collection.Key name, String type, boolean required, int defaultType, boolean passByReference, String dspName, String hint, StructImpl meta) {
		this.name = name;
		this.strType = (type);
		this.type = CFTypes.toShortStrict(type, CFTypes.TYPE_UNKNOW);
		this.required = required;
		this.defaultType = defaultType;
		this.dspName = dspName;
		this.hint = hint;
		this.meta = meta;
		this.passByReference = passByReference;
	}

	/**
	 * NEVER USE THIS CONSTRUCTOR, this constructor is only for deserialize this object from stream
	 */
	public FunctionArgumentImpl() {
	}

	public FunctionArgumentImpl(Collection.Key name) {
		this(name, "any", CFTypes.TYPE_ANY, false, DEFAULT_TYPE_NULL, true, "", "", null);
	}

	public FunctionArgumentImpl(Collection.Key name, short type) {
		this(name, CFTypes.toString(type, "any"), type, false, DEFAULT_TYPE_NULL, true, "", "", null);
	}

	public FunctionArgumentImpl(Collection.Key name, String strType, short type) {
		this(name, strType, type, false, DEFAULT_TYPE_NULL, true, "", "", null);
	}

	public FunctionArgumentImpl(Collection.Key name, String strType, short type, boolean required) {
		this(name, strType, type, required, DEFAULT_TYPE_NULL, true, "", "", null);
	}

	public FunctionArgumentImpl(Collection.Key name, String strType, short type, boolean required, int defaultType) {
		this(name, strType, type, required, defaultType, true, "", "", null);
	}

	public FunctionArgumentImpl(Collection.Key name, String strType, short type, boolean required, int defaultType, boolean passByReference) {
		this(name, strType, type, required, defaultType, passByReference, "", "", null);
	}

	public FunctionArgumentImpl(Collection.Key name, String strType, short type, boolean required, int defaultType, boolean passByReference, String dspName) {
		this(name, strType, type, required, defaultType, passByReference, dspName, "", null);
	}

	public FunctionArgumentImpl(Collection.Key name, String strType, short type, boolean required, int defaultType, boolean passByReference, String dspName, String hint) {
		this(name, strType, type, required, defaultType, passByReference, dspName, hint, null);
	}

	public FunctionArgumentImpl(Collection.Key name, String strType, short type, boolean required, int defaultType, boolean passByReference, String dspName, String hint,
			StructImpl meta) {
		this.name = name;
		this.strType = strType;
		this.type = type;
		this.required = required;
		this.defaultType = defaultType;
		this.dspName = dspName;
		this.hint = hint;
		this.meta = meta;
		this.passByReference = passByReference;
	}

	// private static StructImpl sct=new StructImpl();

	/**
	 * @return the defaultType
	 */
	@Override
	public int getDefaultType() {
		return defaultType;
	}

	@Override
	public Collection.Key getName() {
		return name;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public short getType() {
		return type;
	}

	@Override
	public String getTypeAsString() {
		return strType;
	}

	@Override
	public String getHint() {
		return hint;
	}

	@Override
	public String getDisplayName() {
		return dspName;
	}

	@Override
	public Struct getMetaData() {
		return meta;
	}

	@Override
	public boolean isPassByReference() {
		return passByReference;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		dspName = ExternalizableUtil.readString(in);
		hint = ExternalizableUtil.readString(in);
		name = KeyImpl.init(ExternalizableUtil.readString(in));
		type = in.readShort();
		strType = ExternalizableUtil.readString(in);
		required = in.readBoolean();
		meta = (Struct) in.readObject();
		defaultType = in.readInt();
		passByReference = in.readBoolean();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ExternalizableUtil.writeString(out, dspName);
		ExternalizableUtil.writeString(out, hint);
		ExternalizableUtil.writeString(out, name.getString());
		out.writeShort(type);
		ExternalizableUtil.writeString(out, strType);
		out.writeBoolean(required);
		out.writeObject(meta);
		out.writeInt(defaultType);
		out.writeBoolean(passByReference);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FunctionArgument)) return false;
		return equals(this, (FunctionArgument) obj);
	}

	public static boolean equals(FunctionArgument left, FunctionArgument right) {
		if (left.getDefaultType() != right.getDefaultType() || left.getType() != right.getType() || !_eq(left.getName(), right.getName())
				|| !_eq(left.getTypeAsString(), right.getTypeAsString()) || left.isPassByReference() != right.isPassByReference() || left.isRequired() != right.isRequired())
			return false;

		return true;
	}

	private static boolean _eq(Object left, Object right) {
		if (left == null) return right == null;
		return left.equals(right);
	}
}