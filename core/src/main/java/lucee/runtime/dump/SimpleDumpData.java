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
package lucee.runtime.dump;

import java.util.Date;

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.dt.DateTime;

public class SimpleDumpData implements DumpData, Castable {

	private String data;

	public SimpleDumpData(String data) {
		this.data = data;
	}

	public SimpleDumpData(double data) {
		this.data = Caster.toString(data);
	}

	public SimpleDumpData(boolean data) {
		this.data = Caster.toString(data);
	}

	@Override
	public String toString() {
		return data;
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBooleanValue(data);
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return Caster.toBoolean(data, defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDatetime(data, null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return DateCaster.toDateAdvanced(data, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(data);
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return Caster.toDoubleValue(data, defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return Caster.toString(data);
	}

	@Override
	public String castToString(String defaultValue) {
		return Caster.toString(data, defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws ExpressionException {
		return Operator.compare(data, b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return Operator.compare(data, (Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return Operator.compare(data, d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return Operator.compare(data, str);
	}
}