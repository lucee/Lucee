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
// TODO Time constructor muss auch noch entfernt werden und durch DateUtil methode ersetzen
package lucee.runtime.op.date;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.date.DateTimeUtil;
import lucee.commons.date.JREDateTimeUtil;
import lucee.commons.date.TimeZoneConstants;
import lucee.commons.i18n.FormatUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.dt.Time;
import lucee.runtime.type.dt.TimeImpl;
import lucee.runtime.type.util.CollectionUtil;

/**
 * Class to cast Strings to Date Objects
 */
public final class DateCaster {

	public static final short CONVERTING_TYPE_NONE = 0;
	public static final short CONVERTING_TYPE_YEAR = 1;
	public static final short CONVERTING_TYPE_OFFSET = 2;

	// private static short MODE_DAY_STR=1;
	// private static short MODE_MONTH_STR=2;
	// private static short MODE_NONE=4;
	private static long DEFAULT_VALUE = Long.MIN_VALUE;

	private static DateTimeUtil util = DateTimeUtil.getInstance();
	public static boolean classicStyle = false;

	/**
	 * converts an Object to a DateTime Object (Advanced but slower)
	 * 
	 * @param o Object to Convert
	 * @param timezone
	 * @return Date Time Object
	 * @throws PageException
	 */
	public static DateTime toDateAdvanced(Object o, TimeZone timezone) throws PageException {
		if (o instanceof Date) {
			if (o instanceof DateTime) return (DateTime) o;
			return new DateTimeImpl((Date) o);
		}
		else if (o instanceof Castable) return ((Castable) o).castToDateTime();
		else if (o instanceof String) {
			DateTime dt = toDateAdvanced((String) o, timezone, null);
			if (dt == null) throw new ExpressionException("can't cast [" + o + "] to date value");
			return dt;
		}
		else if (o instanceof Number) return util.toDateTime(((Number) o).doubleValue());
		else if (o instanceof ObjectWrap) return toDateAdvanced(((ObjectWrap) o).getEmbededObject(), timezone);
		else if (o instanceof Calendar) {

			return new DateTimeImpl((Calendar) o);
		}
		throw new ExpressionException("can't cast [" + Caster.toClassName(o) + "] to date value");
	}

	/**
	 * converts an Object to a DateTime Object (Advanced but slower)
	 * 
	 * @param str String to Convert
	 * @param timezone
	 * @return Date Time Object
	 * @throws PageException
	 */
	public static DateTime toDateAdvanced(String str, TimeZone timezone) throws PageException {
		DateTime dt = toDateAdvanced(str, timezone, null);
		if (dt == null) throw new ExpressionException("can't cast [" + str + "] to date value");
		return dt;
	}

	/**
	 * converts an Object to a DateTime Object (Advanced but slower), returns null if invalid string
	 * 
	 * @param o Object to Convert
	 * @param timeZone
	 * @param defaultValue
	 * @return Date Time Object
	 */
	public static DateTime toDateAdvanced(Object o, TimeZone timeZone, DateTime defaultValue) {
		if (o instanceof DateTime) return (DateTime) o;
		else if (o instanceof Date) return new DateTimeImpl((Date) o);
		else if (o instanceof Castable) {
			return ((Castable) o).castToDateTime(defaultValue);
		}
		else if (o instanceof String) return toDateAdvanced(o.toString(), timeZone, defaultValue);
		else if (o instanceof Number) return util.toDateTime(((Number) o).doubleValue());
		else if (o instanceof Calendar) {
			return new DateTimeImpl((Calendar) o);
		}
		else if (o instanceof ObjectWrap) return toDateAdvanced(((ObjectWrap) o).getEmbededObject(defaultValue), timeZone, defaultValue);
		return defaultValue;
	}

	/**
	 * converts a String to a DateTime Object (Advanced but slower), returns null if invalid string
	 * 
	 * @param str String to convert
	 * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
	 *            converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
	 *            CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
	 * @param timeZone
	 * @param defaultValue
	 * @return Date Time Object
	 */
	public static DateTime toDateAdvanced(String str, short convertingType, TimeZone timeZone, DateTime defaultValue) {
		str = str.trim();
		if (StringUtil.isEmpty(str)) return defaultValue;
		if (!hasDigits(str)) return defaultValue; // every format has digits
		timeZone = ThreadLocalPageContext.getTimeZone(timeZone);
		DateTime dt = toDateSimple(str, convertingType, true, timeZone, defaultValue);
		if (dt == null) {
			final DateFormat[] formats = FormatUtil.getCFMLFormats(timeZone, true);
			synchronized (formats) {
				Date d;
				ParsePosition pp = new ParsePosition(0);
				for (int i = 0; i < formats.length; i++) {
					// try {
					pp.setErrorIndex(-1);
					pp.setIndex(0);
					d = formats[i].parse(str, pp);
					if (pp.getIndex() == 0 || d == null || pp.getIndex() < str.length()) continue;
					dt = new DateTimeImpl(d.getTime(), false);
					return dt;
				}
			}
			dt = toDateTime(Locale.US, str, timeZone, defaultValue, false);
		}
		return dt;
	}

	private static boolean hasDigits(String str) {
		for (int i = str.length() - 1; i >= 0; i--) {
			if (Character.isDigit(str.charAt(i))) return true;
		}
		return false;
	}

	/**
	 * parse a string to a Datetime Object
	 * 
	 * @param locale
	 * @param str String representation of a locale Date
	 * @param tz
	 * @return DateTime Object
	 * @throws PageException
	 */
	public static DateTime toDateTime(Locale locale, String str, TimeZone tz, boolean useCommomDateParserAsWell) throws PageException {

		DateTime dt = toDateTime(locale, str, tz, null, useCommomDateParserAsWell);
		if (dt == null) {
			String prefix = locale.getLanguage() + "-" + locale.getCountry() + "-";
			throw new ExpressionException("can't cast [" + str + "] to date value",
					"to add custom formats for " + LocaleFactory.toString(locale) + ", create/extend on of the following files [" + prefix + "datetime.df (for date time formats), "
							+ prefix + "date.df (for date formats) or " + prefix + "time.df (for time formats)] in the following directory [<context>/lucee/locales]." + "");

			// throw new ExpressionException("can't cast ["+str+"] to date value");
		}
		return dt;
	}

	/*
	 * public static void main(String[] args) throws PageException {
	 * 
	 * Locale[] locales = Locale.getAvailableLocales(); Iterator<Locale> it =
	 * LocaleFactory.getLocales().values().iterator();
	 * 
	 * //print.e(toDateTime(new Locale("de","CH"), "06.02.2008 01:02:01 MEZ",
	 * TimeZone.getDefault(),null, false)); String str="dimanche, 6. avril 2008 01:02:03";
	 * str="06.02.2008, 01:02:01 MEZ"; str="01.02. h CEST"; str="6-apr-2008";
	 * str="Sunday, April 6, 2008 1:02:03 AM CEST"; str="Sunday, April 6, 2008 1:02:03 AM CEST";
	 * str="01:02:03 o'clock CEST"; str="1:02 Uhr MEZ"; Locale l=new Locale("fr","CH"); l=new
	 * Locale("it","CH"); l=new Locale("en","US"); l=new Locale("en","UK"); l=new Locale("de","CH");
	 * //l=new Locale("es","ES"); //l=LocaleConstant.PORTUGUESE_BRASIL;
	 * //l=LocaleConstant.DUTCH_NETHERLANDS; //l=LocaleConstant.ARABIC_ALGERIA; //l=Locale.CHINA;
	 * print.e(str); print.e(toDateTime(l, str, TimeZone.getDefault(),null, false));
	 * 
	 * 
	 * }
	 */

	/**
	 * parse a string to a Datetime Object, returns null if can't convert
	 * 
	 * @param locale
	 * @param str String representation of a locale Date
	 * @param tz
	 * @param defaultValue
	 * @return datetime object
	 */
	public static DateTime toDateTime(Locale locale, String str, TimeZone tz, DateTime defaultValue, boolean useCommomDateParserAsWell) {
		str = str.trim();
		tz = ThreadLocalPageContext.getTimeZone(tz);
		DateFormat[] df;

		// get Calendar
		Calendar c = JREDateTimeUtil.getThreadCalendar(locale, tz);

		// datetime
		ParsePosition pp = new ParsePosition(0);
		df = FormatUtil.getDateTimeFormats(locale, tz, false);// dfc[FORMATS_DATE_TIME];
		Date d;
		for (int i = 0; i < df.length; i++) {
			SimpleDateFormat sdf = (SimpleDateFormat) df[i];
			// print.e(sdf.format(new Date(108,3,6,1,2,1)) + " : "+sdf.toPattern());
			pp.setErrorIndex(-1);
			pp.setIndex(0);
			sdf.setTimeZone(tz);
			d = sdf.parse(str, pp);
			if (pp.getIndex() == 0 || d == null || pp.getIndex() < str.length()) continue;

			optimzeDate(c, tz, d);
			return new DateTimeImpl(c.getTime());
		}

		// date
		df = FormatUtil.getDateFormats(locale, tz, false);
		for (int i = 0; i < df.length; i++) {
			SimpleDateFormat sdf = (SimpleDateFormat) df[i];
			// print.e(sdf.format(new Date(108,3,6,1,2,1)) + " : "+sdf.toPattern());
			pp.setErrorIndex(-1);
			pp.setIndex(0);
			sdf.setTimeZone(tz);
			d = sdf.parse(str, pp);
			if (pp.getIndex() == 0 || d == null || pp.getIndex() < str.length()) continue;
			optimzeDate(c, tz, d);
			return new DateTimeImpl(c.getTime());
		}

		// time
		df = FormatUtil.getTimeFormats(locale, tz, false);// dfc[FORMATS_TIME];
		for (int i = 0; i < df.length; i++) {
			SimpleDateFormat sdf = (SimpleDateFormat) df[i];
			// print.e(sdf.format(new Date(108,3,6,1,2,1))+ " : "+sdf.toPattern());
			pp.setErrorIndex(-1);
			pp.setIndex(0);
			sdf.setTimeZone(tz);
			d = sdf.parse(str, pp);
			if (pp.getIndex() == 0 || d == null || pp.getIndex() < str.length()) continue;
			c.setTimeZone(tz);
			c.setTime(d);
			c.set(Calendar.YEAR, 1899);
			c.set(Calendar.MONTH, 11);
			c.set(Calendar.DAY_OF_MONTH, 30);
			c.setTimeZone(tz);
			return new DateTimeImpl(c.getTime());
		}

		if (useCommomDateParserAsWell) return DateCaster.toDateSimple(str, CONVERTING_TYPE_NONE, true, tz, defaultValue);
		return defaultValue;
	}

	private static void optimzeDate(Calendar c, TimeZone tz, Date d) {
		c.setTimeZone(tz);
		c.setTime(d);
		int year = c.get(Calendar.YEAR);
		if (year < 40) c.set(Calendar.YEAR, 2000 + year);
		else if (year < 100) c.set(Calendar.YEAR, 1900 + year);
	}

	public static DateTime toDateAdvanced(String str, TimeZone timeZone, DateTime defaultValue) {
		return toDateAdvanced(str, CONVERTING_TYPE_OFFSET, timeZone, defaultValue);
	}

	/**
	 * converts a boolean to a DateTime Object
	 * 
	 * @param b boolean to Convert
	 * @param timeZone
	 * @return coverted Date Time Object
	 */
	public static DateTime toDateSimple(boolean b, TimeZone timeZone) {
		return toDateSimple(b ? 1L : 0L, timeZone);
	}

	/**
	 * converts a char to a DateTime Object
	 * 
	 * @param c char to Convert
	 * @param timeZone
	 * @return coverted Date Time Object
	 */
	public static DateTime toDateSimple(char c, TimeZone timeZone) {
		return toDateSimple((long) c, timeZone);
	}

	/**
	 * converts a double to a DateTime Object
	 * 
	 * @param d double to Convert
	 * @param timeZone
	 * @return coverted Date Time Object
	 */
	public static DateTime toDateSimple(double d, TimeZone timeZone) {
		return util.toDateTime(d);
	}

	/**
	 * converts a double to a DateTime Object
	 * 
	 * @param d double to Convert
	 * @param timeZone
	 * @return coverted Date Time Object
	 */
	public static DateTime toDateSimple(long l, TimeZone timeZone) {
		return util.toDateTime(l);
	}

	/*
	 * * converts a double to a DateTime Object
	 * 
	 * @param d double to Convert
	 * 
	 * @param timeZone
	 * 
	 * @return coverted Date Time Object / public static DateTime toDateSimple(long l, TimeZone
	 * timezone) { return new DateTimeImpl(l,false); }
	 */

	/**
	 * converts an Object to a DateTime Object, returns null if invalid string
	 * 
	 * @param o Object to Convert
	 * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
	 *            converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
	 *            CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
	 * @param timeZone
	 * @return coverted Date Time Object
	 * @throws PageException
	 */
	public static DateTime toDateSimple(Object o, short convertingType, boolean alsoMonthString, TimeZone timeZone) throws PageException {
		if (o instanceof DateTime) return (DateTime) o;
		else if (o instanceof Date) return new DateTimeImpl((Date) o);
		else if (o instanceof Castable) return ((Castable) o).castToDateTime();
		else if (o instanceof String) return toDateSimple(o.toString(), convertingType, alsoMonthString, timeZone);
		else if (o instanceof Number) return util.toDateTime(((Number) o).doubleValue());
		else if (o instanceof Calendar) return new DateTimeImpl((Calendar) o);
		else if (o instanceof ObjectWrap) return toDateSimple(((ObjectWrap) o).getEmbededObject(), convertingType, alsoMonthString, timeZone);
		else if (o instanceof Calendar) {
			return new DateTimeImpl((Calendar) o);
		}

		if (o instanceof Component) throw new ExpressionException("can't cast component [" + ((Component) o).getAbsName() + "] to date value");

		throw new ExpressionException("can't cast [" + Caster.toTypeName(o) + "] to date value");
	}

	/**
	 * 
	 * @param o
	 * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
	 *            converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
	 *            CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
	 * @param alsoMonthString
	 * @param timeZone
	 * @param defaultValue
	 * @return
	 */
	public static DateTime toDateSimple(Object o, short convertingType, boolean alsoMonthString, TimeZone timeZone, DateTime defaultValue) {
		if (o instanceof DateTime) return (DateTime) o;
		else if (o instanceof Date) return new DateTimeImpl((Date) o);
		else if (o instanceof Castable) return ((Castable) o).castToDateTime(defaultValue);
		else if (o instanceof String) return toDateSimple(o.toString(), convertingType, alsoMonthString, timeZone, defaultValue);
		else if (o instanceof Number) return util.toDateTime(((Number) o).doubleValue());
		else if (o instanceof Calendar) return new DateTimeImpl((Calendar) o);
		else if (o instanceof ObjectWrap) {
			Object eo = ((ObjectWrap) o).getEmbededObject(CollectionUtil.NULL);
			if (eo == CollectionUtil.NULL) return defaultValue;
			return toDateSimple(eo, convertingType, alsoMonthString, timeZone, defaultValue);
		}
		else if (o instanceof Calendar) {
			return new DateTimeImpl((Calendar) o);
		}
		return defaultValue;
	}

	/**
	 * converts an Object to a DateTime Object, returns null if invalid string
	 * 
	 * @param str String to Convert
	 * @param timeZone
	 * @return coverted Date Time Object
	 * @throws PageException
	 */
	public static DateTime toDateSimple(String str, TimeZone timeZone) throws PageException {
		DateTime dt = toDateSimple(str, CONVERTING_TYPE_OFFSET, true, timeZone, null);
		if (dt == null) throw new ExpressionException("can't cast [" + str + "] to date value");
		return dt;
	}

	/**
	 * converts an Object to a Time Object, returns null if invalid string
	 * 
	 * @param o Object to Convert
	 * @return coverted Date Time Object
	 * @throws PageException
	 */
	public static Time toTime(TimeZone timeZone, Object o) throws PageException {
		if (o instanceof Time) return (Time) o;
		else if (o instanceof Date) return new TimeImpl((Date) o);
		else if (o instanceof Castable) return new TimeImpl(((Castable) o).castToDateTime());
		else if (o instanceof String) {
			Time dt = toTime(timeZone, o.toString(), null);
			if (dt == null) throw new ExpressionException("can't cast [" + o + "] to time value");
			return dt;
		}
		else if (o instanceof ObjectWrap) return toTime(timeZone, ((ObjectWrap) o).getEmbededObject());
		else if (o instanceof Calendar) {
			// TODO check timezone offset
			return new TimeImpl(((Calendar) o).getTimeInMillis(), false);
		}
		throw new ExpressionException("can't cast [" + Caster.toClassName(o) + "] to time value");
	}

	/**
	 * 
	 * @param o
	 * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
	 *            converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
	 *            CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
	 * @param timeZone
	 * @param defaultValue
	 * @return
	 */
	public static DateTime toDateAdvanced(Object o, short convertingType, TimeZone timeZone, DateTime defaultValue) {
		return _toDateAdvanced(o, convertingType, timeZone, defaultValue, true);
	}

	private static DateTime _toDateAdvanced(Object o, short convertingType, TimeZone timeZone, DateTime defaultValue, boolean advanced) {
		if (o instanceof DateTime) return (DateTime) o;
		else if (o instanceof Date) return new DateTimeImpl((Date) o);
		else if (o instanceof Castable) {
			return ((Castable) o).castToDateTime(defaultValue);
		}
		else if (o instanceof String) {
			if (advanced) return toDateAdvanced(o.toString(), convertingType, timeZone, defaultValue);

			return toDateSimple(o.toString(), convertingType, true, timeZone, defaultValue);
		}
		else if (o instanceof Number) {
			return numberToDate(timeZone, ((Number) o).doubleValue(), convertingType, defaultValue);
		}
		else if (o instanceof ObjectWrap) {
			return _toDateAdvanced(((ObjectWrap) o).getEmbededObject(defaultValue), convertingType, timeZone, defaultValue, advanced);
		}
		else if (o instanceof Calendar) {
			return new DateTimeImpl((Calendar) o);
		}
		return defaultValue;
	}

	/**
	 * converts an Object to a DateTime Object, returns null if invalid string
	 * 
	 * @param str Stringt to Convert
	 * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
	 *            converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
	 *            CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
	 * @param timeZone
	 * @return coverted Date Time Object
	 * @throws PageException
	 */
	public static DateTime toDateSimple(String str, short convertingType, boolean alsoMonthString, TimeZone timeZone) throws PageException {
		DateTime dt = toDateSimple(str, convertingType, alsoMonthString, timeZone, null);
		if (dt == null) throw new ExpressionException("can't cast value to a Date Object");
		return dt;
	}

	public static DateTime toDateAdvanced(Object o, short convertingType, TimeZone timeZone) throws PageException {
		DateTime dt = toDateAdvanced(o, convertingType, timeZone, null);
		if (dt == null) {
			if (o instanceof CharSequence) throw new ExpressionException("can't cast value [" + o + "] to a Date Object");
			throw new ExpressionException("can't cast value to a Date Object");
		}
		return dt;
	}

	/**
	 * converts a String to a Time Object, returns null if invalid string @param str String to
	 * convert @param defaultValue @return Time Object @throws
	 */
	public static Time toTime(TimeZone timeZone, String str, Time defaultValue) {

		if (str == null || str.length() < 3) {
			return defaultValue;
		}
		DateString ds = new DateString(str);
		// Timestamp
		if (ds.isCurrent('{') && ds.isLast('}')) {

			// Time
			// "^\\{t '([0-9]{1,2}):([0-9]{1,2}):([0-9]{2})'\\}$"
			if (ds.fwIfNext('t')) {

				// Time
				if (!(ds.fwIfNext(' ') && ds.fwIfNext('\''))) return defaultValue;
				ds.next();
				// hour
				int hour = ds.readDigits();
				if (hour == -1) return defaultValue;

				if (!ds.fwIfCurrent(':')) return defaultValue;

				// minute
				int minute = ds.readDigits();
				if (minute == -1) return defaultValue;

				if (!ds.fwIfCurrent(':')) return defaultValue;

				// second
				int second = ds.readDigits();
				if (second == -1) return defaultValue;

				if (!(ds.fwIfCurrent('\'') && ds.fwIfCurrent('}'))) return defaultValue;

				if (ds.isAfterLast()) {
					long time = util.toTime(timeZone, 1899, 12, 30, hour, minute, second, 0, DEFAULT_VALUE);
					if (time == DEFAULT_VALUE) return defaultValue;
					return new TimeImpl(time, false);
				}
				return defaultValue;

			}
			return defaultValue;
		}
		// Time start with int
		/*
		 * else if(ds.isDigit()) { char sec=ds.charAt(1); char third=ds.charAt(2); // 16.10.2004 (02:15)?
		 * if(sec==':' || third==':') { // hour int hour=ds.readDigits(); if(hour==-1) return defaultValue;
		 * 
		 * if(!ds.fwIfCurrent(':'))return defaultValue;
		 * 
		 * // minutes int minutes=ds.readDigits(); if(minutes==-1) return defaultValue;
		 * 
		 * if(ds.isAfterLast()) { long time=util.toTime(timeZone,1899,12,30,hour,minutes,0,0,DEFAULT_VALUE);
		 * if(time==DEFAULT_VALUE) return defaultValue;
		 * 
		 * return new TimeImpl(time,false); } //else if(!ds.fwIfCurrent(':'))return null; else
		 * if(!ds.fwIfCurrent(':')) { if(!ds.fwIfCurrent(' '))return defaultValue;
		 * 
		 * if(ds.fwIfCurrent('a') || ds.fwIfCurrent('A')) { if(ds.fwIfCurrent('m') || ds.fwIfCurrent('M')) {
		 * if(ds.isAfterLast()) { long time=util.toTime(timeZone,1899,12,30,hour,minutes,0,0,DEFAULT_VALUE);
		 * if(time==DEFAULT_VALUE) return defaultValue; return new TimeImpl(time,false); } } return
		 * defaultValue; } else if(ds.fwIfCurrent('p') || ds.fwIfCurrent('P')) { if(ds.fwIfCurrent('m') ||
		 * ds.fwIfCurrent('M')) { if(ds.isAfterLast()) { long
		 * time=util.toTime(timeZone,1899,12,30,hour<13?hour+12:hour,minutes,0,0,DEFAULT_VALUE);
		 * if(time==DEFAULT_VALUE) return defaultValue; return new TimeImpl(time,false); } } return
		 * defaultValue; } return defaultValue; }
		 * 
		 * 
		 * // seconds int seconds=ds.readDigits(); if(seconds==-1) return defaultValue;
		 * 
		 * if(ds.isAfterLast()) { long
		 * time=util.toTime(timeZone,1899,12,30,hour,minutes,seconds,0,DEFAULT_VALUE);
		 * if(time==DEFAULT_VALUE) return defaultValue; return new TimeImpl(time,false); }
		 * 
		 * } }
		 */

		// TODO bessere impl
		ds.reset();
		DateTime rtn = parseTime(timeZone, new int[] { 1899, 12, 30 }, ds, defaultValue, -1);
		if (rtn == defaultValue) return defaultValue;
		return new TimeImpl(rtn);

		// return defaultValue;
	}

	/**
	 * converts a String to a DateTime Object, returns null if invalid string
	 * 
	 * @param str String to convert
	 * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
	 *            converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
	 *            CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
	 * @param alsoMonthString allow that the month is an English name
	 * @param timeZone
	 * @param defaultValue
	 * @return Date Time Object
	 */
	private static DateTime parseDateTime(String str, DateString ds, short convertingType, boolean alsoMonthString, TimeZone timeZone, DateTime defaultValue) {
		int month = 0;
		int first = ds.readDigits();
		// first
		if (first == -1) {
			if (!alsoMonthString) return defaultValue;
			first = ds.readMonthString();
			if (first == -1) return defaultValue;
			month = 1;
		}

		if (ds.isAfterLast()) return month == 1 ? defaultValue : numberToDate(timeZone, Caster.toDoubleValue(str, Double.NaN), convertingType, defaultValue);

		char del = ds.current();
		if (del != '.' && del != '/' && del != '-' && del != ' ' && del != '\t') {
			if (ds.fwIfCurrent(':')) {
				return parseTime(timeZone, new int[] { 1899, 12, 30 }, ds, defaultValue, first);
			}
			return defaultValue;
		}
		ds.next();
		ds.removeWhitespace();

		// second
		int second = ds.readDigits();
		if (second == -1) {
			if (!alsoMonthString || month != 0) return defaultValue;
			second = ds.readMonthString();
			if (second == -1) return defaultValue;
			month = 2;
		}

		if (ds.isAfterLast()) {
			return toDate(month, timeZone, first, second, defaultValue);
		}

		char del2 = ds.current();
		if (del != del2) {
			ds.fwIfCurrent(' ');
			ds.fwIfCurrent('T');
			ds.fwIfCurrent(' ');
			return parseTime(timeZone, _toDate(timeZone, month, first, second), ds, defaultValue, -1);
		}
		ds.next();
		ds.removeWhitespace();

		int third = ds.readDigits();
		if (third == -1) {
			return defaultValue;
		}

		if (ds.isAfterLast()) {
			if (classicStyle() && del == '.') return toDate(month, timeZone, second, first, third, defaultValue);
			return toDate(month, timeZone, first, second, third, defaultValue);
		}
		ds.fwIfCurrent(' ');
		ds.fwIfCurrent('T');
		ds.fwIfCurrent(' ');
		if (classicStyle() && del == '.') return parseTime(timeZone, _toDate(month, second, first, third), ds, defaultValue, -1);
		return parseTime(timeZone, _toDate(month, first, second, third), ds, defaultValue, -1);

	}

	private static boolean classicStyle() {
		return classicStyle;
	}

	private static DateTime parseTime(TimeZone timeZone, int[] date, DateString ds, DateTime defaultValue, int hours) {
		if (date == null) return defaultValue;

		ds.removeWhitespace();

		// hour
		boolean next = false;
		if (hours == -1) {
			ds.removeWhitespace();
			hours = ds.readDigits();
			ds.removeWhitespace();
			if (hours == -1) {
				return parseOffset(ds, timeZone, date, 0, 0, 0, 0, true, defaultValue);
			}
		}
		else next = true;

		int minutes = 0;
		if (next || ds.fwIfCurrent(':')) {
			ds.removeWhitespace();
			minutes = ds.readDigits();
			ds.removeWhitespace();
			if (minutes == -1) return defaultValue;
		}

		int seconds = 0;
		if (ds.fwIfCurrent(':')) {
			ds.removeWhitespace();
			seconds = ds.readDigits();
			ds.removeWhitespace();
			if (seconds == -1) return defaultValue;
		}

		int msSeconds = 0;
		if (ds.fwIfCurrent('.')) {
			ds.removeWhitespace();
			msSeconds = ds.readDigits();
			ds.removeWhitespace();
			if (msSeconds == -1) return defaultValue;
		}

		if (ds.isAfterLast()) {
			return DateTimeUtil.getInstance().toDateTime(timeZone, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, defaultValue);
		}
		ds.fwIfCurrent(' ');

		if (ds.fwIfCurrent('a') || ds.fwIfCurrent('A')) {
			if (!ds.fwIfCurrent('m')) ds.fwIfCurrent('M');
			if (ds.isAfterLast())
				return DateTimeUtil.getInstance().toDateTime(timeZone, date[0], date[1], date[2], hours < 12 ? hours : hours - 12, minutes, seconds, msSeconds, defaultValue);
			return defaultValue;
		}
		else if (ds.fwIfCurrent('p') || ds.fwIfCurrent('P')) {
			if (!ds.fwIfCurrent('m')) ds.fwIfCurrent('M');
			if (hours > 24) return defaultValue;
			if (ds.isAfterLast())
				return DateTimeUtil.getInstance().toDateTime(timeZone, date[0], date[1], date[2], hours < 12 ? hours + 12 : hours, minutes, seconds, msSeconds, defaultValue);
			return defaultValue;
		}

		ds.fwIfCurrent(' ');
		return parseOffset(ds, timeZone, date, hours, minutes, seconds, msSeconds, true, defaultValue);

	}

	private static DateTime parseOffset(DateString ds, TimeZone timeZone, int[] date, int hours, int minutes, int seconds, int msSeconds, boolean checkAfterLast,
			DateTime defaultValue) {
		if (ds.isLast() && (ds.fwIfCurrent('Z') || ds.fwIfCurrent('z'))) {
			return util.toDateTime(TimeZoneConstants.UTC, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, defaultValue);
		}
		else if (ds.fwIfCurrent('+')) {
			DateTime rtn = util.toDateTime(timeZone, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, defaultValue);
			if (rtn == defaultValue) return rtn;
			return readOffset(true, timeZone, rtn, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, ds, checkAfterLast, defaultValue);
		}
		else if (ds.fwIfCurrent('-')) {
			DateTime rtn = util.toDateTime(timeZone, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, defaultValue);
			if (rtn == defaultValue) return rtn;
			return readOffset(false, timeZone, rtn, date[0], date[1], date[2], hours, minutes, seconds, msSeconds, ds, checkAfterLast, defaultValue);
		}
		return defaultValue;
	}

	private static DateTime toDate(int month, TimeZone timeZone, int first, int second, DateTime defaultValue) {
		int[] d = _toDate(timeZone, month, first, second);
		if (d == null) return defaultValue;
		return util.toDateTime(timeZone, d[0], d[1], d[2], 0, 0, 0, 0, defaultValue);
	}

	private static int[] _toDate(TimeZone tz, int month, int first, int second) {
		int YEAR = year(tz);
		if (first <= 12 && month < 2) {
			if (util.daysInMonth(YEAR, first) >= second) return new int[] { YEAR, first, second };
			return new int[] { util.toYear(second), first, 1 };
		}
		// first>12
		if (second <= 12) {
			if (util.daysInMonth(YEAR, second) >= first) return new int[] { YEAR, second, first };
			return new int[] { util.toYear(first), second, 1 };
		}
		return null;
	}

	private static int year(TimeZone tz) {
		return util.getYear(ThreadLocalPageContext.getTimeZone(tz), new DateTimeImpl());
	}

	private static DateTime toDate(int month, TimeZone timeZone, int first, int second, int third, DateTime defaultValue) {
		int[] d = _toDate(month, first, second, third);
		if (d == null) return defaultValue;
		return util.toDateTime(timeZone, d[0], d[1], d[2], 0, 0, 0, 0, defaultValue);
	}

	private static int[] _toDate(int month, int first, int second, int third) {
		if (first <= 12) {
			if (month == 2) return new int[] { util.toYear(third), second, first };
			if (util.daysInMonth(util.toYear(third), first) >= second) return new int[] { util.toYear(third), first, second };
			return null;
		}
		if (second > 12) return null;
		if (month == 2) {
			int tmp = first;
			first = third;
			third = tmp;
		}
		if (util.daysInMonth(util.toYear(first), second) < third) {
			if (util.daysInMonth(util.toYear(third), second) >= first) return new int[] { util.toYear(third), second, first };
			return null;
		}
		return new int[] { util.toYear(first), second, third };
	}

	/**
	 * converts the given string to a date following simple and fast parsing rules (no international
	 * formats)
	 * 
	 * @param str
	 * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
	 *            converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
	 *            CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
	 * @param alsoMonthString allow that the month is defined as english word (jan,janauary ...)
	 * @param timeZone
	 * @param defaultValue
	 * @return
	 */
	public static DateTime toDateSimple(String str, short convertingType, boolean alsoMonthString, TimeZone timeZone, DateTime defaultValue) {
		str = StringUtil.trim(str, "");
		DateString ds = new DateString(str);

		// Timestamp
		if (ds.isCurrent('{') && ds.isLast('}')) {
			return _toDateSimpleTS(ds, timeZone, defaultValue);
		}
		DateTime res = parseDateTime(str, ds, convertingType, alsoMonthString, timeZone, defaultValue);
		if (res == defaultValue && Decision.isNumber(str)) {
			return numberToDate(timeZone, Caster.toDoubleValue(str, Double.NaN), convertingType, defaultValue);
		}
		return res;
	}

	/**
	 * 
	 * @param timeZone
	 * @param d
	 * @param convertingType one of the following values: - CONVERTING_TYPE_NONE: number are not
	 *            converted at all - CONVERTING_TYPE_YEAR: integers are handled as years -
	 *            CONVERTING_TYPE_OFFSET: numbers are handled as offset from 1899-12-30 00:00:00 UTC
	 * @return
	 */
	private static DateTime numberToDate(TimeZone timeZone, double d, short convertingType, DateTime defaultValue) {
		if (!Decision.isValid(d)) return defaultValue;

		if (convertingType == CONVERTING_TYPE_YEAR) {
			int i = (int) d;
			if (i == d) {
				timeZone = ThreadLocalPageContext.getTimeZone(timeZone);
				Calendar c = Calendar.getInstance(timeZone);
				c.set(Calendar.MILLISECOND, 0);
				c.set(i, 0, 1, 0, 0, 0);
				return new DateTimeImpl(c);
			}
		}
		if (convertingType == CONVERTING_TYPE_OFFSET) return util.toDateTime(d);

		return defaultValue;
	}

	private static DateTime _toDateSimpleTS(DateString ds, TimeZone timeZone, DateTime defaultValue) {
		// Date
		// "^\\{d '([0-9]{2,4})-([0-9]{1,2})-([0-9]{1,2})'\\}$"
		if (ds.fwIfNext('d')) {
			if (!(ds.fwIfNext(' ') && ds.fwIfNext('\''))) return defaultValue;
			ds.next();
			// year
			int year = ds.readDigits();
			if (year == -1) return defaultValue;

			if (!ds.fwIfCurrent('-')) return defaultValue;

			// month
			int month = ds.readDigits();
			if (month == -1) return defaultValue;

			if (!ds.fwIfCurrent('-')) return defaultValue;

			// day
			int day = ds.readDigits();
			if (day == -1) return defaultValue;

			if (!(ds.fwIfCurrent('\'') && ds.fwIfCurrent('}'))) return defaultValue;

			if (ds.isAfterLast()) return util.toDateTime(timeZone, year, month, day, 0, 0, 0, 0, defaultValue);// new DateTimeImpl(year,month,day);

			return defaultValue;

		}

		// DateTime
		// "^\\{ts '([0-9]{1,4})-([0-9]{1,2})-([0-9]{1,2}) ([0-9]{2}):([0-9]{1,2}):([0-9]{2})'\\}$"
		else if (ds.fwIfNext('t')) {
			if (!(ds.fwIfNext('s') && ds.fwIfNext(' ') && ds.fwIfNext('\''))) {

				// Time
				if (!(ds.fwIfNext(' ') && ds.fwIfNext('\''))) return defaultValue;
				ds.next();
				// hour
				int hour = ds.readDigits();
				if (hour == -1) return defaultValue;

				if (!ds.fwIfCurrent(':')) return defaultValue;

				// minute
				int minute = ds.readDigits();
				if (minute == -1) return defaultValue;

				if (!ds.fwIfCurrent(':')) return defaultValue;

				// second
				int second = ds.readDigits();
				if (second == -1) return defaultValue;

				// Milli Second
				int millis = 0;

				if (ds.fwIfCurrent('.')) {
					millis = ds.readDigits();
				}

				int before = ds.getPos();
				DateTime tmp = parseOffset(ds, timeZone, new int[] { 1899, 12, 30 }, hour, minute, second, millis, false, defaultValue);
				if (tmp == null && before != ds.getPos()) return defaultValue;

				if (!(ds.fwIfCurrent('\'') && ds.fwIfCurrent('}'))) return defaultValue;

				if (ds.isAfterLast()) {
					if (tmp != null) {
						return new TimeImpl(tmp.getTime(), false);
					}
					long time = util.toTime(timeZone, 1899, 12, 30, hour, minute, second, millis, DEFAULT_VALUE);
					if (time == DEFAULT_VALUE) return defaultValue;
					return new TimeImpl(time, false);
				}
				return defaultValue;
			}
			ds.next();
			// year
			int year = ds.readDigits();
			if (year == -1) return defaultValue;

			if (!ds.fwIfCurrent('-')) return defaultValue;

			// month
			int month = ds.readDigits();
			if (month == -1) return defaultValue;

			if (!ds.fwIfCurrent('-')) return defaultValue;

			// day
			int day = ds.readDigits();
			if (day == -1) return defaultValue;

			if (!ds.fwIfCurrent(' ')) return defaultValue;

			// hour
			int hour = ds.readDigits();
			if (hour == -1) return defaultValue;

			if (!ds.fwIfCurrent(':')) return defaultValue;

			// minute
			int minute = ds.readDigits();
			if (minute == -1) return defaultValue;

			if (!ds.fwIfCurrent(':')) return defaultValue;

			// second
			int second = ds.readDigits();
			if (second == -1) return defaultValue;

			// Milli Second
			int millis = 0;

			if (ds.fwIfCurrent('.')) {
				millis = ds.readDigits();
			}

			int before = ds.getPos();
			DateTime tmp = parseOffset(ds, timeZone, new int[] { year, month, day }, hour, minute, second, millis, false, defaultValue);
			if (tmp == null && before != ds.getPos()) return defaultValue;

			if (!(ds.fwIfCurrent('\'') && ds.fwIfCurrent('}'))) return defaultValue;

			if (ds.isAfterLast()) {
				if (tmp != null) return tmp;
				return util.toDateTime(timeZone, year, month, day, hour, minute, second, millis, defaultValue);
			}

			return defaultValue;

		}
		else return defaultValue;
	}

	/**
	 * reads an offset definition at the end of a date string
	 * 
	 * @param timeZone
	 * @param dt previous parsed date Object
	 * @param ds DateString to parse
	 * @param defaultValue
	 * @return date Object with offset
	 */
	private static DateTime readOffset(boolean isPlus, TimeZone timeZone, DateTime dt, int years, int months, int days, int hours, int minutes, int seconds, int milliSeconds,
			DateString ds, boolean checkAfterLast, DateTime defaultValue) {
		// timeZone=ThreadLocalPageContext.getTimeZone(timeZone);
		if (timeZone == null) return defaultValue;
		// HOUR
		int hourLength = ds.getPos();
		int hour = ds.readDigits();
		hourLength = ds.getPos() - hourLength;
		if (hour == -1) return defaultValue;

		// MINUTE
		int minute = 0;
		if (!ds.isAfterLast()) {
			if (!(ds.fwIfCurrent(':') || ds.fwIfCurrent('.'))) return defaultValue;
			minute = ds.readDigits();
			if (minute == -1) return defaultValue;

		}
		else if (hourLength > 2) {
			int h = hour / 100;
			minute = hour - (h * 100);
			hour = h;
		}

		if (minute > 59) return defaultValue;
		if (hour > 14 || (hour == 14 && minute > 0)) return defaultValue;

		long offset = hour * 60L * 60L * 1000L;
		offset += minute * 60 * 1000;

		if (!checkAfterLast || ds.isAfterLast()) {
			long time = util.toTime(TimeZoneConstants.UTC, years, months, days, hours, minutes, seconds, milliSeconds, 0);

			if (isPlus) time -= offset;
			else time += offset;
			return new DateTimeImpl(time, false);
		}
		return defaultValue;
	}

	public static String toUSDate(Object o, TimeZone timeZone) throws PageException {
		if (Decision.isUSDate(o)) return Caster.toString(o);
		DateTime date = DateCaster.toDateAdvanced(o, timeZone);
		return new lucee.runtime.format.DateFormat(Locale.US).format(date, "mm/dd/yyyy");
	}

	public static String toEuroDate(Object o, TimeZone timeZone) throws PageException {
		if (Decision.isEuroDate(o)) return Caster.toString(o);
		DateTime date = DateCaster.toDateAdvanced(o, timeZone);
		return new lucee.runtime.format.DateFormat(Locale.US).format(date, "dd.mm.yyyy");
	}

	public static String toShortTime(long time) {
		return Long.toString(time / 1000, 36);
	}

	public static long fromShortTime(String str) {
		return Long.parseLong(str, 36) * 1000;
	}
}