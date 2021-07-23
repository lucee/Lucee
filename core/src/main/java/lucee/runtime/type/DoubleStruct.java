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

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.type.dt.DateTime;

public final class DoubleStruct extends StructImpl {

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		try {
			return Caster.toBoolean(castToBooleanValue());
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		try {
			return castToDateTime();
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		try {
			return castToDoubleValue();
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public String castToString(String defaultValue) {
		try {
			return castToString();
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBooleanValue(castToDoubleValue());

	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDate(castToDateTime(), null);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		Iterator it = valueIterator();
		double value = 0;
		while (it.hasNext()) {
			value += Caster.toDoubleValue(it.next());
		}
		return value;
	}

	@Override
	public String castToString() throws PageException {
		return Caster.toString(castToDoubleValue());
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return Operator.compare(castToDoubleValue(), b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return Operator.compare(castToDoubleValue(), dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return Operator.compare(castToDoubleValue(), d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return Operator.compare(castToDoubleValue(), str);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		Struct sct = new DoubleStruct();
		copy(this, sct, deepCopy);
		return sct;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable table = (DumpTable) super.toDumpData(pageContext, maxlevel, dp);
		try {
			table.setTitle("Double Struct (" + castToString() + ")");
		}
		catch (PageException pe) {
		}
		return table;
	}
}