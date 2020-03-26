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
import java.util.concurrent.atomic.AtomicInteger;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.type.dt.DateTime;

/**
 * Integer Type that can be modified
 */
public class RefIntegerSync implements RefInteger, Castable {

	private volatile AtomicInteger value;

	/**
	 * @param value
	 */
	public RefIntegerSync(int value) {
		this.value = new AtomicInteger(value);
	}

	public RefIntegerSync() {
		this.value = new AtomicInteger(0);
	}

	/**
	 * @param value
	 */
	@Override
	public void setValue(int value) {
		this.value = new AtomicInteger(value);
	}

	/**
	 * operation plus
	 * 
	 * @param value
	 */
	@Override
	public void plus(int value) {
		this.value.addAndGet(value);
	}

	/**
	 * operation minus
	 * 
	 * @param value
	 */
	@Override
	public void minus(int value) {
		this.value.getAndAdd(-value);
	}

	/**
	 * @return returns value as integer
	 */
	@Override
	public Integer toInteger() {
		return Integer.valueOf(value.get());
	}

	/**
	 * @return returns value as integer
	 */
	@Override
	public Double toDouble() {
		return new Double(value.doubleValue());
	}

	@Override
	public double toDoubleValue() {
		return value.doubleValue();
	}

	@Override
	public int toInt() {
		return value.get();
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return Caster.toBoolean(value.get());
	}

	@Override
	public boolean castToBooleanValue() {
		return Caster.toBooleanValue(value.get());
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
		return Caster.toDoubleValue(value.get());
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