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
package lucee.runtime.type;

import java.util.Date;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.Operator;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.dt.DateTime;

public final class CastableStruct extends StructImpl {

	private Object value;

	public CastableStruct() {
	}

	public CastableStruct(int type) {
		super(type);
	}

	public CastableStruct(Object value) {
		this.value = value;
	}

	public CastableStruct(Object value, int type) {
		super(type);
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		if (value == null) return super.castToBooleanValue();
		return Caster.toBooleanValue(value);

	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		if (value == null) return super.castToBoolean(defaultValue);
		return Caster.toBoolean(value, defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		if (value == null) return super.castToDateTime();
		return Caster.toDate(value, null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		if (value == null) return super.castToDateTime(defaultValue);
		return DateCaster.toDateAdvanced(value, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		if (value == null) return super.castToDoubleValue();
		return Caster.toDoubleValue(value);
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		if (value == null) return super.castToDoubleValue(defaultValue);
		return Caster.toDoubleValue(value, true, defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		if (value == null) return super.castToString();
		return Caster.toString(value);
	}

	@Override
	public String castToString(String defaultValue) {
		if (value == null) return super.castToString(defaultValue);
		return Caster.toString(value, defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		if (value == null) return super.compareTo(b);
		return Operator.compare(value, b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		if (value == null) return super.compareTo(dt);
		return Operator.compare(value, (Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		if (value == null) return super.compareTo(d);
		return Operator.compare(value, d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		if (value == null) return super.compareTo(str);
		return Operator.compare(value, str);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		if (value == null) return super.duplicate(deepCopy);
		Struct sct = new CastableStruct(deepCopy ? Duplicator.duplicate(value, deepCopy) : value);
		copy(this, sct, deepCopy);
		return sct;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		if (value == null) return super.toDumpData(pageContext, maxlevel, dp);
		DumpTable table = new DumpTable("struct", "#9999ff", "#ccccff", "#000000");
		table.setTitle("Value Struct");
		maxlevel--;
		table.appendRow(1, new SimpleDumpData("value"), DumpUtil.toDumpData(value, pageContext, maxlevel, dp));
		table.appendRow(1, new SimpleDumpData("struct"), super.toDumpData(pageContext, maxlevel, dp));

		return table;
	}

}