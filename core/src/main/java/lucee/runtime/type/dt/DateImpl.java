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
package lucee.runtime.type.dt;

import java.text.SimpleDateFormat;
import java.util.Locale;

import lucee.commons.date.DateTimeUtil;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Operator;
import lucee.runtime.type.SimpleValue;

/**
 * Printable and Castable Date Object (no visible time)
 */
public final class DateImpl extends Date implements SimpleValue {

	private static SimpleDateFormat luceeFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

	// private TimeZone timezone;

	public DateImpl() {
		this(null, System.currentTimeMillis());
	}

	public DateImpl(long utcTime) {
		this(null, utcTime);
	}

	public DateImpl(PageContext pc) {
		this(pc, System.currentTimeMillis());
	}

	public DateImpl(PageContext pc, long utcTime) {
		super(DateTimeImpl.addOffset(ThreadLocalPageContext.getConfig(pc), utcTime));
		// this.timezone=ThreadLocalPageContext.getTimeZone(pc);
	}

	public DateImpl(java.util.Date date) {
		super(date.getTime());
	}

	@Override
	public String castToString() {
		synchronized (luceeFormatter) {
			luceeFormatter.setTimeZone(ThreadLocalPageContext.getTimeZone());
			return "{d '" + luceeFormatter.format(this) + "'}";
		}
	}

	@Override
	public String toString() {
		return castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return castToString();
	}

	@Override
	public double toDoubleValue() {
		return DateTimeUtil.getInstance().toDoubleValue(this);
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return DateTimeUtil.getInstance().toDoubleValue(this);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		String str = castToString("");
		DumpTable table = new DumpTable("date", "#ff9900", "#ffcc00", "#000000");
		table.appendRow(1, new SimpleDumpData("Date"), new SimpleDumpData(str));
		return table;
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return DateTimeUtil.getInstance().toBooleanValue(this);
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() {
		return DateTimeUtil.getInstance().toDoubleValue(this);
	}

	@Override
	public DateTime castToDateTime() {
		return this;
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return this;
	}

	@Override
	public int compareTo(boolean b) {
		return Operator.compare(castToDoubleValue(), b ? 1D : 0D);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return Operator.compare((java.util.Date) this, (java.util.Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return Operator.compare(castToDoubleValue(), d);
	}

	@Override
	public int compareTo(String str) {
		return Operator.compare(castToString(), str);
	}
}