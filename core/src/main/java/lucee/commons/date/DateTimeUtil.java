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
package lucee.commons.date;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.functions.displayFormatting.DateTimeFormat;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;

public abstract class DateTimeUtil {

	private final static SimpleDateFormat HTTP_TIME_STRING_FORMAT_OLD;
	private final static SimpleDateFormat HTTP_TIME_STRING_FORMAT;

	// public final static SimpleDateFormat DATETIME_FORMAT_LOCAL;

	static {
		// DATETIME_FORMAT_LOCAL = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

		HTTP_TIME_STRING_FORMAT_OLD = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH);
		HTTP_TIME_STRING_FORMAT_OLD.setTimeZone(TimeZone.getTimeZone("GMT"));

		HTTP_TIME_STRING_FORMAT = new SimpleDateFormat("EE, dd-MMM-yyyy HH:mm:ss zz", Locale.ENGLISH);
		HTTP_TIME_STRING_FORMAT.setTimeZone(TimeZoneConstants.UTC);
	}

	private static final double DAY_MILLIS = 86400000D;
	private static final long CF_UNIX_OFFSET = 2209161600000L;

	public static final int SECOND = 0;
	public static final int MINUTE = 1;
	public static final int HOUR = 2;
	public static final int DAY = 3;
	public static final int YEAR = 10;
	public static final int MONTH = 11;
	public static final int WEEK = 12;
	public static final int QUARTER = 20;
	public static final int MILLISECOND = 30;

	private static DateTimeUtil instance;

	public static DateTimeUtil getInstance() {
		if (instance == null) {
			// try to load jar Date TimeUtil
			instance = new JREDateTimeUtil();
		}
		return instance;
	}

	public DateTime toDateTime(TimeZone tz, int year, int month, int day, int hour, int minute, int second, int milliSecond) throws DateTimeException {
		return new DateTimeImpl(toTime(tz, year, month, day, hour, minute, second, milliSecond), false);
	}

	public DateTime toDateTime(TimeZone tz, int year, int month, int day, int hour, int minute, int second, int milliSecond, DateTime defaultValue) {
		long time = toTime(tz, year, month, day, hour, minute, second, milliSecond, Long.MIN_VALUE);
		if (time == Long.MIN_VALUE) return defaultValue;
		return new DateTimeImpl(time, false);
	}

	/**
	 * returns a date time instance by a number, the conversion from the double to date is o the base of
	 * the CFML rules.
	 * 
	 * @param days double value to convert to a number
	 * @return DateTime Instance
	 */
	public DateTime toDateTime(double days) {
		long utc = Math.round(days * DAY_MILLIS);
		utc -= CF_UNIX_OFFSET;
		utc -= getLocalTimeZoneOffset(utc);
		return new DateTimeImpl(utc, false);
	}

	public long toTime(TimeZone tz, int year, int month, int day, int hour, int minute, int second, int milliSecond, long defaultValue) {
		tz = ThreadLocalPageContext.getTimeZone(tz);
		year = toYear(year);

		if (month < 1) return defaultValue;
		if (month > 12) return defaultValue;
		if (day < 1) return defaultValue;
		if (hour < 0) return defaultValue;
		if (minute < 0) return defaultValue;
		if (second < 0) return defaultValue;
		if (milliSecond < 0) return defaultValue;
		if (hour > 24) return defaultValue;
		if (minute > 59) return defaultValue;
		if (second > 59) return defaultValue;

		if (daysInMonth(year, month) < day) return defaultValue;

		return _toTime(tz, year, month, day, hour, minute, second, milliSecond);
	}

	public long toTime(TimeZone tz, int year, int month, int day, int hour, int minute, int second, int milliSecond) throws DateTimeException {
		tz = ThreadLocalPageContext.getTimeZone(tz);
		year = toYear(year);

		if (month < 1) throw new DateTimeException("Month number [" + month + "] must be at least 1");
		if (month > 12) throw new DateTimeException("Month number [" + month + "] can not be greater than 12");
		if (day < 1) throw new DateTimeException("Day number [" + day + "] must be at least 1");
		if (hour < 0) throw new DateTimeException("Hour number [" + hour + "] must be at least 0");
		if (minute < 0) throw new DateTimeException("Minute number [" + minute + "] must be at least 0");
		if (second < 0) throw new DateTimeException("Second number [" + second + "] must be at least 0");
		if (milliSecond < 0) throw new DateTimeException("Milli second number [" + milliSecond + "] must be at least 0");

		if (hour > 24) throw new DateTimeException("Hour number [" + hour + "] can not be greater than 24");
		if (minute > 59) throw new DateTimeException("Minute number [" + minute + "] can not be greater than 59");
		if (second > 59) throw new DateTimeException("Second number [" + second + "] can not be greater than 59");

		if (daysInMonth(year, month) < day)
<<<<<<< HEAD
			throw new DateTimeException("Day number [" + day + "] can not be greater than " + daysInMonth(year, month) + " when month is " + month + " and year " + year);
=======
			throw new DateTimeException("Day number [" + day + "] can not be greater than [" + daysInMonth(year, month) + "] when month is [" + month + "] and year [" + year + "]");
>>>>>>> upstream/master

		return _toTime(tz, year, month, day, hour, minute, second, milliSecond);
	}

	/**
	 * return how much days given month in given year has
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public int daysInMonth(int year, int month) {
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			return 31;
		case 4:
		case 6:
		case 9:
		case 11:
			return 30;
		case 2:
			return isLeapYear(year) ? 29 : 28;
		}
		return -1;
	}

	/**
	 * translate 2 digit numbers to a year; for example 10 to 2010 or 50 to 1950
	 * 
	 * @param year
	 * @return year matching number
	 */
	public int toYear(int year) {
		if (year < 100) {
			if (year < 30) year = year += 2000;
			else year = year += 1900;
		}
		return year;
	}

	/**
	 * return if given is is a leap year or not
	 * 
	 * @param year
	 * @return is leap year
	 */
	public boolean isLeapYear(int year) {
		return ((year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0)));
	}

	/**
	 * cast boolean value
	 * 
	 * @param dateTime
	 * @return boolean value
	 * @throws ExpressionException
	 */
	public boolean toBooleanValue(DateTime dateTime) throws DateTimeException {
		throw new DateTimeException("Can't cast Date [" + DateTimeUtil.toHTTPTimeString(dateTime, false) + "] to boolean value");
	}

	public double toDoubleValue(DateTime dateTime) {
		return toDoubleValue(dateTime.getTime());
	}

	public double toDoubleValue(long time) {
		time += getLocalTimeZoneOffset(time);
		time += CF_UNIX_OFFSET;
		return time / DAY_MILLIS;
	}

	private static long getLocalTimeZoneOffset(long time) {
		return ThreadLocalPageContext.getTimeZone().getOffset(time);
	}

	public long getMilliSecondsAdMidnight(TimeZone timeZone, long time) {
		return time - getMilliSecondsInDay(timeZone, time);
	}

	abstract long _toTime(TimeZone tz, int year, int month, int day, int hour, int minute, int second, int milliSecond);

	public abstract int getYear(TimeZone tz, lucee.runtime.type.dt.DateTime dt);

	public abstract void setYear(TimeZone tz, lucee.runtime.type.dt.DateTime dt, int value);

	public abstract int getMonth(TimeZone tz, DateTime dt);

	public abstract void setMonth(TimeZone tz, DateTime dt, int value);

	public abstract int getDay(TimeZone tz, DateTime dt);

	public abstract void setDay(TimeZone tz, DateTime dt, int value);

	public abstract int getHour(TimeZone tz, DateTime dt);

	public abstract void setHour(TimeZone tz, DateTime dt, int value);

	public abstract int getMinute(TimeZone tz, DateTime dt);

	public abstract void setMinute(TimeZone tz, DateTime dt, int value);

	public abstract int getSecond(TimeZone tz, DateTime dt);

	public abstract void setSecond(TimeZone tz, DateTime dt, int value);

	public abstract int getMilliSecond(TimeZone tz, DateTime dt);

	public abstract void setMilliSecond(TimeZone tz, DateTime dt, int value);

	public abstract long getMilliSecondsInDay(TimeZone tz, long time);

	public abstract int getDaysInMonth(TimeZone tz, DateTime dt);

	public abstract int getDayOfYear(Locale locale, TimeZone tz, DateTime dt);

	public abstract int getDayOfWeek(Locale locale, TimeZone tz, DateTime dt);

	public abstract int getWeekOfYear(Locale locale, TimeZone tz, DateTime dt);

	public abstract int getFirstDayOfMonth(TimeZone tz, DateTime dt);

	public abstract String toString(PageContext pc, DateTime dt, TimeZone tz, Boolean addTimeZoneOffset);

	public static String toHTTPTimeString(long time, boolean oldFormat) {
		return toHTTPTimeString(new Date(time), oldFormat);
	}

	/**
	 * converts a date to a http time String
	 * 
	 * @param date date to convert
	 * @param oldFormat "old" in that context means the format support the existing functionality in
	 *            CFML like the function getHTTPTimeString, in that format the date parts are separated
	 *            by a space (like "EE, dd MMM yyyy HH:mm:ss zz"), in the "new" format, the date part is
	 *            separated by "-" (like "EE, dd-MMM-yyyy HH:mm:ss zz")
	 * @return
	 */
	public static String toHTTPTimeString(Date date, boolean oldFormat) {
		if (oldFormat) {
			synchronized (HTTP_TIME_STRING_FORMAT_OLD) {
				return StringUtil.replace(HTTP_TIME_STRING_FORMAT_OLD.format(date), "+00:00", "", true);
			}
		}
		synchronized (HTTP_TIME_STRING_FORMAT) {
			return StringUtil.replace(HTTP_TIME_STRING_FORMAT.format(date), "+00:00", "", true);
		}
	}

	public static String format(long time, Locale l, TimeZone tz) {
		return DateTimeFormat.invoke(new DateTimeImpl(time, false), null, ThreadLocalPageContext.getLocale(l), ThreadLocalPageContext.getTimeZone(tz));
	}

}
