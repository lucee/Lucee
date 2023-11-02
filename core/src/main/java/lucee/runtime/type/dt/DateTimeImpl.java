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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import lucee.commons.date.DateTimeUtil;
import lucee.commons.lang.CFTypes;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Operator;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Objects;
import lucee.runtime.type.SimpleValue;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.MemberUtil;

/**
 * Printable and Castable DateTime Object
 */
public final class DateTimeImpl extends DateTime implements SimpleValue, Objects {

	public DateTimeImpl(PageContext pc) {
		this(pc, System.currentTimeMillis(), true);
	}

	public DateTimeImpl(Config config) {
		this(config, System.currentTimeMillis(), true);
	}

	public DateTimeImpl() {
		this(System.currentTimeMillis(), true);
	}

	public DateTimeImpl(PageContext pc, long utcTime, boolean doOffset) {
		super(doOffset ? addOffset(ThreadLocalPageContext.getConfig(pc), utcTime) : utcTime);
	}

	public DateTimeImpl(Config config, long utcTime, boolean doOffset) {
		super(doOffset ? addOffset(ThreadLocalPageContext.getConfig(config), utcTime) : utcTime);
	}

	public DateTimeImpl(long utcTime, boolean doOffset) {
		super(doOffset ? addOffset(ThreadLocalPageContext.getConfig(), utcTime) : utcTime);
	}

	/*
	 * public DateTimeImpl(Config config, long utcTime) {
	 * super(addOffset(ThreadLocalPageContext.getConfig(config),utcTime)); }
	 */

	public DateTimeImpl(Date date) {
		this(date.getTime(), false);
	}

	public DateTimeImpl(Calendar calendar) {
		super(calendar.getTimeInMillis());
		// this.timezone=ThreadLocalPageContext.getTimeZone(calendar.getTimeZone());
	}

	public static long addOffset(Config config, long utcTime) {
		if (config != null) return utcTime + config.getTimeServerOffset();
		return utcTime;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		String str = castToString(pageContext.getTimeZone());
		DumpTable table = new DumpTable("date", "#ff6600", "#ffcc99", "#000000");
		if (dp.getMetainfo()) table.appendRow(1, new SimpleDumpData("Date Time (" + pageContext.getTimeZone().getID() + ")"));
		else table.appendRow(1, new SimpleDumpData("Date Time"));
		table.appendRow(0, new SimpleDumpData(str));
		return table;
	}

	@Override
	public String castToString() {
		return castToString((TimeZone) null);
	}

	@Override
	public String castToString(String defaultValue) {
		return castToString((TimeZone) null);
	}

	public String castToString(TimeZone tz) {// MUST move to DateTimeUtil
		return DateTimeUtil.getInstance().toString(ThreadLocalPageContext.get(), this, tz, null);

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

	@Override
	public String toString() {
		return castToString();
		/*
		 * synchronized (javaFormatter) { javaFormatter.setTimeZone(timezone); return
		 * javaFormatter.format(this); }
		 */
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		return Reflector.getField(this, key.getString(), defaultValue);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		return Reflector.getField(this, key.getString());
	}

	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		return Reflector.setField(this, propertyName.getString(), value);
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		try {
			return Reflector.setField(this, propertyName.getString(), value);
		}
		catch (PageException e) {
			return value;
		}
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] args) throws PageException {
		return MemberUtil.call(pc, this, methodName, args, new short[] { CFTypes.TYPE_DATETIME }, new String[] { "datetime" });
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		return MemberUtil.callWithNamedValues(pc, this, methodName, args, CFTypes.TYPE_DATETIME, "datetime");
	}

}