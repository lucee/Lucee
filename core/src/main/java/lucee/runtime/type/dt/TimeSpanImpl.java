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

import java.io.Serializable;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.op.date.DateCaster;

/**
 * TimeSpan Object, represent a timespan
 */
public final class TimeSpanImpl implements TimeSpan, Serializable {

	private double value;
	private long valueMillis;

	private long day;
	private int hour;
	private int minute;
	private int second;
	private int milli;

	public static TimeSpan fromDays(double value) {
		return new TimeSpanImpl(value);
	}

	public static TimeSpan fromMillis(long value) {
		return new TimeSpanImpl(value);
	}

	private TimeSpanImpl(double valueDays) {
		this((long) (valueDays * 86400000D));
	}

	private TimeSpanImpl(long valueMillis) {

		this.valueMillis = valueMillis;
		value = valueMillis / 86400000D;
		long tmp = valueMillis;
		day = valueMillis / 86400000L;
		tmp -= day * 86400000L;
		hour = (int) (tmp / 3600000L);
		tmp -= hour * 3600000L;
		minute = (int) (tmp / 60000L);
		tmp -= minute * 60000L;
		second = (int) (tmp / 1000L);
		tmp -= second * 1000L;
		milli = (int) tmp;
	}

	/**
	 * constructor of the timespan class
	 * 
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 */
	public TimeSpanImpl(int day, int hour, int minute, int second) {

		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		value = day + (((double) hour) / 24) + (((double) minute) / 24 / 60) + (((double) second) / 24 / 60 / 60);
		valueMillis = (second + (minute * 60L) + (hour * 3600L) + (day * 3600L * 24L)) * 1000;
	}

	/**
	 * constructor of the timespan class
	 * 
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 */
	public TimeSpanImpl(int day, int hour, int minute, int second, int millisecond) {
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.milli = millisecond;
		value = day + (((double) hour) / 24) + (((double) minute) / 24 / 60) + (((double) second) / 24 / 60 / 60) + (((double) millisecond) / 24 / 60 / 60 / 1000);
		valueMillis = ((second + (minute * 60L) + (hour * 3600L) + (day * 3600L * 24L)) * 1000) + millisecond;
	}

	@Override
	public String castToString() {
		return Caster.toString(value);
	}

	@Override
	public String castToString(String defaultValue) {
		return Caster.toString(value);
	}

	@Override
	public boolean castToBooleanValue() {
		return value != 0;
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return value != 0;
	}

	@Override
	public double castToDoubleValue() {
		return value;
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return value;
	}

	@Override
	public DateTime castToDateTime() throws ExpressionException {
		return DateCaster.toDateSimple(value, null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return DateCaster.toDateSimple(value, null);
	}

	@Override
	public int compareTo(boolean b) {
		return OpUtil.compare(ThreadLocalPageContext.get(), castToBooleanValue() ? Boolean.TRUE : Boolean.FALSE, b ? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), (java.util.Date) castToDateTime(), (java.util.Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(value), Double.valueOf(d));
	}

	@Override
	public int compareTo(String str) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(value), str);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable table = new DumpTable("timespan", "#ff9900", "#ffcc00", "#000000");
		if (milli > 0)
			table.appendRow(1, new SimpleDumpData("Timespan"), new SimpleDumpData("createTimeSpan(" + day + "," + hour + "," + minute + "," + second + "," + milli + ")"));
		else table.appendRow(1, new SimpleDumpData("Timespan"), new SimpleDumpData("createTimeSpan(" + day + "," + hour + "," + minute + "," + second + ")"));

		return table;
	}

	@Override
	public long getMillis() {
		return valueMillis;
	}

	public long getMilli() {
		return milli;
	}

	@Override
	public long getSeconds() {
		return valueMillis / 1000;
	}

	@Override
	public String toString() {
		if (milli > 0) return "createTimeSpan(" + day + "," + hour + "," + minute + "," + second + "," + milli + ")";
		return "createTimeSpan(" + day + "," + hour + "," + minute + "," + second + ")";
	}

	@Override
	public int getDay() {
		return Integer.MAX_VALUE > day ? (int) day : Integer.MAX_VALUE;
	}

	public long getDayAsLong() {
		return day;
	}

	@Override
	public int getHour() {
		return hour;
	}

	@Override
	public int getMinute() {
		return minute;
	}

	@Override
	public int getSecond() {
		return second;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TimeSpan)) return false;
		return getMillis() == ((TimeSpan) obj).getMillis();
	}
}