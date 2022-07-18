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
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.ListUtil;

public final class CastableArray extends ArrayImpl {

	private final Object value;

	/**
	 * Constructor of the class generates as string list of the array
	 */
	public CastableArray() {
		value = null;
	}

	public CastableArray(Object value) {
		this.value = value;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return duplicate(new CastableArray(value), deepCopy);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBooleanValue(getValue());

	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		try {
			return Caster.toBoolean(getValue(), defaultValue);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDate(getValue(), null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		try {
			return DateCaster.toDateAdvanced(getValue(), DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(getValue());
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		try {
			return Caster.toDoubleValue(getValue(), true, defaultValue);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public String castToString() throws PageException {
		return Caster.toString(getValue());
	}

	@Override
	public String castToString(String defaultValue) {
		try {
			return Caster.toString(getValue(), defaultValue);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), b ? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), (Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), Double.valueOf(d));
	}

	@Override
	public int compareTo(String str) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), str);
	}

	private Object getValue() throws PageException {
		if (value != null) return value;
		return ListUtil.arrayToList(this, ",");
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable dt = (DumpTable) super.toDumpData(pageContext, maxlevel, dp);
		dt.setTitle("Castable Array");
		return dt;
	}

}