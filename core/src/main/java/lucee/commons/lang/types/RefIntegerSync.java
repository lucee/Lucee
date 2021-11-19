/**
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland
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

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.dt.DateTime;

/**
 * Integer Type that can be modified
 */
public class RefIntegerSync implements RefInteger, Castable {

	private int value;

	/**
	 * @param value
	 */
	public RefIntegerSync(int value) {
		this.value = value;
	}

	public RefIntegerSync() {
	}

	/**
	 * @param value
	 */
	@Override
	public synchronized void setValue(int value) {
		this.value = value;
	}

	/**
	 * operation plus
	 * 
	 * @param value
	 */
	@Override
	public synchronized void plus(int value) {
		this.value += value;
	}

	/**
	 * operation minus
	 * 
	 * @param value
	 */
	@Override
	public synchronized void minus(int value) {
		this.value -= value;
	}

	/**
	 * @return returns value as integer
	 */
	@Override
	public synchronized Integer toInteger() {
		return Integer.valueOf(value);
	}

	/**
	 * @return returns value as integer
	 */
	@Override
	public synchronized Double toDouble() {
		return new Double(value);
	}

	@Override
	public synchronized double toDoubleValue() {
		return value;
	}

	@Override
	public synchronized int toInt() {
		return value;
	}

	@Override
	public synchronized String toString() {
		return String.valueOf(value);
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return Caster.toBoolean(value);
	}

	@Override
	public boolean castToBooleanValue() {
		return Caster.toBooleanValue(value);
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
		return toString();
	}

	@Override
	public String castToString(String defaultValue) {
		return toString();
	}

	@Override
	public int compareTo(String other) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), other);
	}

	@Override
	public int compareTo(boolean other) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), castToBooleanValue() ? Boolean.TRUE : Boolean.FALSE, other ? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public int compareTo(double other) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(other));
	}

	@Override
	public int compareTo(DateTime other) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), (Date) castToDateTime(), (Date) other);
	}
}