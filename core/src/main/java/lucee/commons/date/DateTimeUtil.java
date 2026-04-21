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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import lucee.commons.i18n.FormatUtil;
import lucee.commons.i18n.FormatterWrapper;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.functions.displayFormatting.DateTimeFormat;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.dt.Time;

public abstract class DateTimeUtil {

	private final static FormatterWrapper HTTP_TIME_STRING_FORMAT_OLD = FormatUtil.getDateTimeFormatter(Locale.ENGLISH, "EE, dd MMM yyyy HH:mm:ss zz");
	private final static FormatterWrapper HTTP_TIME_STRING_FORMAT = FormatUtil.getDateTimeFormatter(Locale.ENGLISH, "EE, dd-MMM-yyyy HH:mm:ss zz");

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

	private static final Map<String, ZoneId> zones = new HashMap<>();

	/**
	 * Unix timestamp representing the last moment when it was 1899-12-30 23:59:59 anywhere on Earth.
	 * This corresponds to 1899-12-31 11:59:59 UTC (in the UTC-12 timezone).
	 * 
	 * Used to detect synthetic old dates created by Lucee's createTime() function, which uses
	 * 1899-12-30 as its base date. Dates before this timestamp are considered synthetic and should use
	 * fixed offsets to avoid confusing historical timezone behavior.
	 */
	private static final long LAST_1899_MOMENT_UTC = -2208859201000L;

	/**
	 * Converts a ZoneId to a fixed ZoneOffset for old synthetic dates to avoid historical timezone
	 * issues.
	 * 
	 * When Lucee's createTime() function creates a Time object, it uses 1899-12-30 as the base date.
	 * This can trigger historical timezone rules that are confusing to users who expect modern timezone
	 * behavior. For dates before the last moment of 1899, this method returns a fixed offset based on
	 * the standard time (winter time, no DST) of the timezone.
	 * 
	 * @param zone The original ZoneId
	 * @param date The date to check (must be instanceof Time for conversion)
	 * @return Fixed ZoneOffset for old synthetic dates, or original ZoneId for normal dates
	 */
	public static ZoneId toOffsetIfNeeded(ZoneId zone, Date date) {

		if (date instanceof Time && date.getTime() <= LAST_1899_MOMENT_UTC) {
			String cacheKey = zone.toString();
			ZoneId cachedOffset = zones.get(cacheKey);

			if (cachedOffset == null) {
				synchronized (zones) {
					cachedOffset = zones.get(cacheKey);
					if (cachedOffset == null) {
						// Use standard offset (winter time, no DST) for synthetic old dates
						// DST didn't exist in 1899, and using a fixed offset avoids confusion
						int rawOffsetSeconds = zone.getRules().getStandardOffset(Instant.now()).getTotalSeconds();
						cachedOffset = ZoneOffset.ofTotalSeconds(rawOffsetSeconds);
						zones.put(cacheKey, cachedOffset);
					}
				}
			}
			return cachedOffset;
		}

		// Return original zone for normal (non-synthetic) dates
		return zone;
	}

	public static ZoneId toOffsetIfNeeded(ZoneId zone, long time) {

		if (time <= LAST_1899_MOMENT_UTC) {
			String cacheKey = zone.toString();
			ZoneId cachedOffset = zones.get(cacheKey);

			if (cachedOffset == null) {
				synchronized (zones) {
					cachedOffset = zones.get(cacheKey);
					if (cachedOffset == null) {
						// Use standard offset (winter time, no DST) for synthetic old dates
						// DST didn't exist in 1899, and using a fixed offset avoids confusion
						int rawOffsetSeconds = zone.getRules().getStandardOffset(Instant.now()).getTotalSeconds();
						cachedOffset = ZoneOffset.ofTotalSeconds(rawOffsetSeconds);
						zones.put(cacheKey, cachedOffset);
					}
				}
			}
			return cachedOffset;
		}

		// Return original zone for normal (non-synthetic) dates
		return zone;
	}

	private static DateTimeUtil instance;

	public static DateTimeUtil getInstance() {
		if (instance == null) {
			String type = SystemUtil.getSystemPropOrEnvVar("lucee.datetime.util", "jre");
			if ("jre".equals(type)) {
				instance = new JREDateTimeUtil();
			}
			else {
				instance = new JavaTimeDateTimeUtil();
			}
		}
		return instance;
	}

	public DateTime toDateTime(TimeZone tz, int year, int month, int day, int hour, int minute, int second, int milliSecond) throws DateTimeException {
		return new DateTimeImpl(toTime(tz, year, month, day, hour, minute, second, milliSecond));
	}

	public DateTime toDateTime(TimeZone tz, int year, int month, int day, int hour, int minute, int second, int milliSecond, DateTime defaultValue) {
		long time = toTime(tz, year, month, day, hour, minute, second, milliSecond, Long.MIN_VALUE);
		if (time == Long.MIN_VALUE) return defaultValue;
		return new DateTimeImpl(time);
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
		return new DateTimeImpl(utc);
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
		return toTime(tz == null ? ThreadLocalPageContext.getTimeZone().toZoneId() : tz.toZoneId(),

				year, month, day, hour, minute, second, milliSecond);
	}

	public long toTime(ZoneId zone, int year, int month, int day, int hour, int minute, int second, int milliSecond) throws DateTimeException {
		if (zone == null) {
			zone = ThreadLocalPageContext.getTimeZone().toZoneId();
		}
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
			throw new DateTimeException("Day number [" + day + "] can not be greater than " + daysInMonth(year, month) + " when month is " + month + " and year " + year);

		return _toTime(zone, year, month, day, hour, minute, second, milliSecond);
	}

	public Long toTime(ZoneId zone, int year, int month, int day, int hour, int minute, int second, int milliSecond, Long defaultValue) {
		if (zone == null) {
			zone = ThreadLocalPageContext.getTimeZone().toZoneId();
		}
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

		return _toTime(zone, year, month, day, hour, minute, second, milliSecond);
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
	 * @throws DateTimeException
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

	abstract long _toTime(ZoneId zone, int year, int month, int day, int hour, int minute, int second, int milliSecond);

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
			return StringUtil.replace(FormatUtil.format(HTTP_TIME_STRING_FORMAT_OLD.formatter, date, TimeZoneConstants.GMT), "+00:00", "", true);
		}
		return StringUtil.replace(FormatUtil.format(HTTP_TIME_STRING_FORMAT.formatter, date, TimeZoneConstants.GMT), "+00:00", "", true);
	}

	public static String format(long time, Locale l, TimeZone tz) {
		return DateTimeFormat.invoke(new DateTimeImpl(time), null, ThreadLocalPageContext.getLocale(l), ThreadLocalPageContext.getTimeZone(tz));
	}

	static void toString(StringBuilder sb, int i, int amount) {
		String str = Caster.toString(i);

		amount = amount - str.length();
		while (amount-- > 0) {
			sb.append('0');
		}
		sb.append(str);
	}
}
