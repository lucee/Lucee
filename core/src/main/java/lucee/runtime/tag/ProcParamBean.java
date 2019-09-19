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
package lucee.runtime.tag;

import java.sql.Types;

import lucee.commons.lang.StringUtil;
import lucee.runtime.db.SQLCaster;
import lucee.runtime.db.SQLItem;
import lucee.runtime.exp.PageException;

public class ProcParamBean implements SQLItem {

	public static final int DIRECTION_IN = 0;
	public static final int DIRECTION_OUT = 1;
	public static final int DIRECTION_INOUT = 3;

	private int direction = DIRECTION_IN;
	private String variable = null;
	private Object value = null;
	private int sqlType = Types.VARCHAR;
	private int maxLength = 0;
	private int scale = 0;
	private boolean _null = false;
	private int index = -1;

	/**
	 * @return Returns the cfsqltype.
	 */
	@Override
	public int getType() {
		return sqlType;
	}

	/**
	 * @param cfsqltype The cfsqltype to set.
	 */
	@Override
	public void setType(int sqlType) {
		this.sqlType = sqlType;
	}

	/**
	 * @return Returns the ignoreNull.
	 */
	public boolean getNull() {
		return _null;
	}

	/**
	 * @param ignoreNull The ignoreNull to set.
	 */
	public void setNull(boolean _null) {
		this._null = _null;
	}

	/**
	 * @return Returns the maxLength.
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * @param maxLength The maxLength to set.
	 */
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 * @return Returns the scale.
	 */
	@Override
	public int getScale() {
		return scale;
	}

	/**
	 * @param scale The scale to set.
	 */
	@Override
	public void setScale(int scale) {
		this.scale = scale;
	}

	/**
	 * @return Returns the type.
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * @param type The type to set.
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

	/**
	 * @return Returns the value.
	 */
	@Override
	public Object getValue() {
		if (_null) return null;
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	@Override
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return Returns the variable.
	 */
	public String getVariable() {
		return variable;
	}

	/**
	 * @param variable The variable to set.
	 */
	public void setVariable(String variable) {
		this.variable = variable;
	}

	/**
	 * @return Returns the index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index The index to set.
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public SQLItem clone(Object object) {
		ProcParamBean ppb = new ProcParamBean();
		ppb.direction = direction;
		ppb.variable = variable;
		ppb.value = value;
		ppb.sqlType = sqlType;
		ppb.maxLength = maxLength;
		ppb.scale = scale;
		ppb._null = _null;
		ppb.index = index;
		return ppb;
	}

	@Override
	public Object getValueForCF() throws PageException {
		return SQLCaster.toCFTypex(this);
	}

	@Override
	public boolean isNulls() {
		return getValue() == null
				|| (sqlType != Types.VARCHAR && sqlType != Types.LONGVARCHAR && sqlType != Types.NVARCHAR && getValue() instanceof String && StringUtil.isEmpty(getValue()));
	}

	@Override
	public boolean isValueSet() {
		return value != null || _null;// TODO impl
	}

	@Override
	public void setNulls(boolean nulls) {
		// TODO impl
	}
}