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
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.SimpleValue;

/**
 * Printable and Castable Time Object (at the moment, same as DateTime)
 */
public final class TimeImpl extends Time implements SimpleValue {

	private static SimpleDateFormat luceeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.US);

	// private TimeZone timezone;
	public TimeImpl(long utcTime) {
		this(null, utcTime, false);
	}

	public TimeImpl(boolean addOffset) {
		this(null, System.currentTimeMillis(), addOffset);
	}

	public TimeImpl(long utcTime, boolean addOffset) {
		this(null, utcTime, addOffset);
	}

	public TimeImpl(PageContext pc, boolean addOffset) {
		this(pc, System.currentTimeMillis(), addOffset);
	}

	public TimeImpl(PageContext pc, long utcTime, boolean addOffset) {
		super(addOffset ? DateTimeImpl.addOffset(ThreadLocalPageContext.getConfig(pc), utcTime) : utcTime);
	}

	public TimeImpl(java.util.Date date) {
		this(date.getTime(), false);
	}

	@Override
	public String castToString() {
		synchronized (luceeFormatter) {
			luceeFormatter.setTimeZone(ThreadLocalPageContext.getTimeZone());
			return "{t '" + luceeFormatter.format(this) + "'}";
		}
	}

	@Override
	public String castToString(String defaultValue) {
		synchronized (luceeFormatter) {
			luceeFormatter.setTimeZone(ThreadLocalPageContext.getTimeZone());
			return "{t '" + luceeFormatter.format(this) + "'}";
		}
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		String str = castToString("");
		DumpTable table = new DumpTable("date", "#ff9900", "#ffcc00", "#000000");
		table.appendRow(1, new SimpleDumpData("Time"), new SimpleDumpData(str));
		return table;
	}

	@Override
	public boolean castToBooleanValue() throws ExpressionException {
		return DateTimeUtil.getInstance().toBooleanValue(this);
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() {
		return toDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return toDoubleValue();
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
	public double toDoubleValue() {
		return DateTimeUtil.getInstance().toDoubleValue(this);
	}

	@Override
	public int compareTo(boolean b) {
		return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), b ? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), (java.util.Date) this, (java.util.Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(d));
	}

	@Override
	public int compareTo(String str) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), str);
	}
}