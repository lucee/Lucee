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
package lucee.commons.lang.types;

import java.util.Date;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.type.dt.DateTime;

/**
 * Integer Type that can be modified
 */
public final class RefBooleanImpl implements RefBoolean, Castable {// MUST add interface Castable

	private boolean value;

	public RefBooleanImpl() {
	}

	/**
	 * @param value
	 */
	public RefBooleanImpl(boolean value) {
		this.value = value;
	}

	/**
	 * @param value
	 */
	@Override
	public void setValue(boolean value) {
		this.value = value;
	}

	/**
	 * @return returns value as Boolean Object
	 */
	@Override
	public Boolean toBoolean() {
		return value ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * @return returns value as boolean value
	 */
	@Override
	public boolean toBooleanValue() {
		return value;
	}

	@Override
	public String toString() {
		return value ? "true" : "false";
	}

	@Override
	public Boolean castToBoolean(Boolean arg0) {
		return toBoolean();
	}

	@Override
	public boolean castToBooleanValue() {
		return toBooleanValue();
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDatetime(value, null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return Caster.toDate(value, false, null, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(value);
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return Caster.toDoubleValue(value);
	}

	@Override
	public String castToString() throws PageException {
		return Caster.toString(value);
	}

	@Override
	public String castToString(String defaultValue) {
		return toString();
	}

	@Override
	public int compareTo(String other) throws PageException {
		return Operator.compare(castToString(), other);
	}

	@Override
	public int compareTo(boolean other) throws PageException {
		return Operator.compare(castToBooleanValue(), other);
	}

	@Override
	public int compareTo(double other) throws PageException {
		return Operator.compare(castToDoubleValue(), other);
	}

	@Override
	public int compareTo(DateTime other) throws PageException {
		return Operator.compare((Date) castToDateTime(), (Date) other);
	}
}