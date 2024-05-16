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
package lucee.runtime.db;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.sql.Types;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

/**
 * 
 */
public class SQLItemImpl implements SQLItem, Serializable {

	/**
	 * Yes or No. Indicates whether the parameter is passed as a null. If Yes, the tag ignores the value
	 * attribute. The default is No.
	 */
	private boolean nulls;

	/**
	 * Specifies the actual value that get passed to the right of the comparison operator in a where
	 * clause.
	 */
	private Object value;
	private Object cfValue;

	/** Number of decimal places of the parameter. The default value is zero. */
	private int scale = 0;

	/** The SQL type that the parameter (any type) will be bound to. */
	private int type = Types.CHAR;

	private boolean isValueSet;

	private Charset charset;

	private int maxlength = -1;

	/**
	 * constructor of the class
	 */
	public SQLItemImpl() {
	}

	/**
	 * constructor of the class
	 * 
	 * @param value
	 */
	public SQLItemImpl(Object value) {
		this.value = value;
	}

	/**
	 * constructor of the class
	 * 
	 * @param value
	 */
	public SQLItemImpl(Object value, int type) {
		this.value = value;
		this.type = type;
	}

	public SQLItemImpl(Object value, int type, int maxlength, Charset charset) {
		this.value = value;
		this.type = type;
		this.charset = charset;
		this.maxlength = maxlength;
	}

	@Override
	public boolean isNulls() {
		return nulls;
	}

	@Override
	public void setNulls(boolean nulls) {
		this.nulls = nulls;
	}

	@Override
	public int getScale() {
		return scale;
	}

	@Override
	public void setScale(int scale) {
		this.scale = scale;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		isValueSet = true;
		this.value = value;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public SQLItem clone(Object object) {

		SQLItemImpl item = new SQLItemImpl();
		item.nulls = nulls;
		item.scale = scale;
		item.type = type;
		item.value = object;
		return item;
	}

	@Override
	public Object getValueForCF() throws PageException {
		if (cfValue == null) {
			cfValue = SQLCaster.toCFTypex(this);
		}
		return cfValue;
	}

	@Override
	public boolean isValueSet() {
		return isValueSet;
	}

	public Charset getCharset() {
		return charset;
	}

	public int getMaxlength() {
		return maxlength;
	}

	@Override
	public String toString() {
		try {
			return Caster.toString(getValueForCF(), "");
		}
		catch (PageException e) {
			return Caster.toString(getValue(), "");
		}
	}

	public static SQLItem duplicate(SQLItem item) {
		if (!(item instanceof SQLItemImpl)) return item;
		return ((SQLItemImpl) item).duplicate();
	}

	public SQLItem duplicate() {
		SQLItemImpl rtn = new SQLItemImpl(value, type, maxlength, charset);
		rtn.nulls = nulls;
		rtn.cfValue = cfValue;
		rtn.isValueSet = isValueSet;
		rtn.scale = scale;
		return rtn;
	}
}