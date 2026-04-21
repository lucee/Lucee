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
package lucee.commons.i18n;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import lucee.commons.date.DateTimeException;
import lucee.commons.date.DateTimeUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;

public final class FormatUtil {

	private static final short FORMAT_TYPE_DATE = 1;
	private static final short FORMAT_TYPE_TIME = 2;
	private static final short FORMAT_TYPE_DATE_TIME = 3;

	private static final LocalTime DEFAULT_TIME = LocalTime.of(0, 0, 0);
	private static final LocalDate DEFAULT_DATE = LocalDate.of(1899, 12, 30);

	private static final int DEFAULT_YEAR;
	private static final int DEFAULT_MONTH;
	private static final int DEFAULT_DAY;
	private static final int DEFAULT_HOUR;
	private static final int DEFAULT_MINUTE;
	private static final int DEFAULT_SECOND;
	private static final int DEFAULT_MILLISECOND;

	static {
		DEFAULT_YEAR = year(DEFAULT_DATE);
		DEFAULT_MONTH = DEFAULT_DATE.get(ChronoField.MONTH_OF_YEAR);
		DEFAULT_DAY = DEFAULT_DATE.get(ChronoField.DAY_OF_MONTH);

		DEFAULT_HOUR = hour(DEFAULT_TIME);
		DEFAULT_MINUTE = DEFAULT_TIME.isSupported(ChronoField.MINUTE_OF_HOUR) ? DEFAULT_TIME.get(ChronoField.MINUTE_OF_HOUR) : 0;
		DEFAULT_SECOND = DEFAULT_TIME.isSupported(ChronoField.SECOND_OF_MINUTE) ? DEFAULT_TIME.get(ChronoField.SECOND_OF_MINUTE) : 0;
		DEFAULT_MILLISECOND = DEFAULT_TIME.isSupported(ChronoField.MILLI_OF_SECOND) ? DEFAULT_TIME.get(ChronoField.MILLI_OF_SECOND) : 0;
	}

	private final static Map<String, SoftReference<List<FormatterWrapper>>> cfmlFormats = new ConcurrentHashMap<>();
	// "EEEE, MMMM d, yyyy, h:mm:ss a 'Coordinated Universal Time'"
	private final static Pattern[] strCfmlFormats = new Pattern[] {

			// new Pattern("M/d/yyyy", FORMAT_TYPE_DATE),

			// new Pattern("MM/dd/yyyy", FORMAT_TYPE_DATE),

			new Pattern("dd-MMM-yyyy", FORMAT_TYPE_DATE),

			new Pattern("dd-MMMM-yyyy", FORMAT_TYPE_DATE),

			new Pattern("dd-MMM-yy HH:mm a", FORMAT_TYPE_DATE_TIME),

			new Pattern("dd-MMMM-yy HH:mm a", FORMAT_TYPE_DATE_TIME),

			new Pattern("dd MMM, yyyy HH:mm:ss", FORMAT_TYPE_DATE_TIME),

			new Pattern("dd MMM yyyy HH:mm:ss zz", FORMAT_TYPE_DATE_TIME),

			new Pattern("MMMM d yyyy HH:mm", FORMAT_TYPE_DATE_TIME),

			new Pattern("MMMM d yyyy HH:mm:ss", FORMAT_TYPE_DATE_TIME),

			new Pattern("MMM dd, yyyy HH:mm:ss", FORMAT_TYPE_DATE_TIME),

			new Pattern("MMMM, dd yyyy HH:mm:ss", FORMAT_TYPE_DATE_TIME),

			new Pattern("MMMM d yyyy HH:mm:ssZ", FORMAT_TYPE_DATE_TIME),

			new Pattern("MMM dd, yyyy HH:mm:ss a", FORMAT_TYPE_DATE_TIME),

			new Pattern("MMMM, dd yyyy HH:mm:ssZ", FORMAT_TYPE_DATE_TIME),

			new Pattern("MMMM, dd yyyy HH:mm:ss Z", FORMAT_TYPE_DATE_TIME),

			new Pattern("MMMM dd, yyyy HH:mm:ss a zzz", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE, MMM dd, yyyy HH:mm:ss", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE MMM dd HH:mm:ss z yyyy", FORMAT_TYPE_DATE_TIME),

			new Pattern("EE, dd-MMM-yyyy H:mm:ss zz", FORMAT_TYPE_DATE_TIME),

			new Pattern("EE, dd MMM yyyy H:mm:ss zz", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE d, MMM yyyy H:mm:ss zz", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE, d MMM yyyy H:mm:ss Z", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE, MMM dd, yyyy H:mm:ssZ", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEEE, MMMM d, yyyy, h:mm:ss a z", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEEE, MMMM d, yyyy, h:mm:ss a zzzz", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE MMM dd yyyy HH:mm:ss 'GMT'ZZ (z)", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE MMM dd yyyy HH:mm:ss 'GMT'ZZ (zzzz)", FORMAT_TYPE_DATE_TIME),

			new Pattern("yyyy/MM/dd HH:mm:ss zz", FORMAT_TYPE_DATE_TIME),

			new Pattern("yyyy-MM-dd HH:mm:ss zz", FORMAT_TYPE_DATE_TIME),

			new Pattern("yyyy-MM-dd'T'HH:mm:ssXXX", FORMAT_TYPE_DATE_TIME),

			new Pattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", FORMAT_TYPE_DATE_TIME),

			new Pattern("H:mm:ss a z", FORMAT_TYPE_TIME)

	};

	private static final Map<String, SoftReference<FormatterWrapper>> dateTimeFormatter = new ConcurrentHashMap<>();
	public static final boolean debug = false;
	private static final char NON_BREAKING_SPACE = '\u202F';

	public static List<FormatterWrapper> getAllFormats(Locale locale, TimeZone timeZone, boolean lenient) {
		String key = "all:" + locale.toString() + "-" + timeZone.getID() + ":" + lenient;
		SoftReference<List<FormatterWrapper>> sr = cfmlFormats.get(key);
		List<FormatterWrapper> formatter = null;
		if (sr == null || (formatter = sr.get()) == null) {
			synchronized (SystemUtil.createToken("all", key)) {
				sr = cfmlFormats.get(key);
				if (sr == null || (formatter = sr.get()) == null) {

					formatter = new CopyOnWriteArrayList<>();
					for (FormatterWrapper dtf: getCFMLFormats(locale, timeZone, lenient)) {
						formatter.add(dtf);
					}
					for (FormatterWrapper dtf: getDateTimeFormats(locale, timeZone, lenient)) {
						formatter.add(dtf);
					}
					for (FormatterWrapper dtf: getDateFormats(locale, timeZone, lenient)) {
						formatter.add(dtf);
					}
					for (FormatterWrapper dtf: getTimeFormats(locale, timeZone, lenient)) {
						formatter.add(dtf);
					}

					cfmlFormats.put(key, new SoftReference(formatter));
				}
			}
		}
		return formatter;
	}

	public static List<FormatterWrapper> getCFMLFormats(Locale locale, TimeZone timeZone, boolean lenient) {
		String key = "cfml:" + locale.toString() + "-" + timeZone.getID() + ":" + lenient;

		SoftReference<List<FormatterWrapper>> sr = cfmlFormats.get(key);
		List<FormatterWrapper> formatter = null;
		if (sr == null || (formatter = sr.get()) == null) {
			synchronized (SystemUtil.createToken("cfml", key)) {
				sr = cfmlFormats.get(key);
				if (sr == null || (formatter = sr.get()) == null) {
					ZoneId zone = timeZone.toZoneId();
					formatter = new ArrayList<>();
					DateTimeFormatterBuilder builder;
					for (Pattern p: strCfmlFormats) {
						formatter.add(getFormatterWrapper(p.pattern, zone, locale, p.type, lenient));
					}
					cfmlFormats.put(key, new SoftReference<>(formatter));
				}
			}
		}
		return formatter;
	}

	public static List<FormatterWrapper> getDateTimeFormats(Locale locale, TimeZone tz, boolean lenient) {

		String key = "dt-" + locale.toString() + "-" + tz.getID() + "-" + lenient;
		SoftReference<List<FormatterWrapper>> tmp = cfmlFormats.get(key);
		List<FormatterWrapper> df = tmp == null ? null : tmp.get();
		if (df == null) {
			synchronized (SystemUtil.createToken("dt", key)) {
				df = tmp == null ? null : tmp.get();
				if (df == null) {
					ZoneId zone = tz.toZoneId();
					df = new ArrayList<>();
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).withLocale(locale).withZone(zone), "FULL_FULL", FORMAT_TYPE_DATE_TIME,
							zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withLocale(locale).withZone(zone), "LONG_LONG", FORMAT_TYPE_DATE_TIME,
							zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale).withZone(zone), "MEDIUM_MEDIUM", FORMAT_TYPE_DATE_TIME,
							zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT).withLocale(locale).withZone(zone), "SHORT_SHORT",
							FORMAT_TYPE_DATE_TIME, zone));

					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.LONG).withLocale(locale).withZone(zone), "FULL_LONG",
							FORMAT_TYPE_DATE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.MEDIUM).withLocale(locale).withZone(zone), "FULL_MEDIUM",
							FORMAT_TYPE_DATE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT).withLocale(locale).withZone(zone), "FULL_SHORT",
							FORMAT_TYPE_DATE_TIME, zone));

					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.FULL).withLocale(locale).withZone(zone), "LONG_FULL",
							FORMAT_TYPE_DATE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.MEDIUM).withLocale(locale).withZone(zone), "LONG_MEDIUM",
							FORMAT_TYPE_DATE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT).withLocale(locale).withZone(zone), "LONG_SHORT",
							FORMAT_TYPE_DATE_TIME, zone));

					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.FULL).withLocale(locale).withZone(zone), "MEDIUM_FULL",
							FORMAT_TYPE_DATE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.LONG).withLocale(locale).withZone(zone), "MEDIUM_LONG",
							FORMAT_TYPE_DATE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(locale).withZone(zone), "MEDIUM_SHORT",
							FORMAT_TYPE_DATE_TIME, zone));

					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.FULL).withLocale(locale).withZone(zone), "SHORT_FULL",
							FORMAT_TYPE_DATE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.LONG).withLocale(locale).withZone(zone), "SHORT_LONG",
							FORMAT_TYPE_DATE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM).withLocale(locale).withZone(zone), "SHORT_MEDIUM",
							FORMAT_TYPE_DATE_TIME, zone));

					// ISO8601
					df.add(getFormatterWrapper("yyyy-MM-dd'T'HH:mm:ssXXX", zone, locale, FORMAT_TYPE_DATE_TIME, lenient));
					df.add(getFormatterWrapper("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", zone, locale, FORMAT_TYPE_DATE_TIME, lenient));
					// custom
					if (Locale.ENGLISH.equals(locale)) {
						df.add(getFormatterWrapper("M/d/yy H:mm:ss", zone, locale, FORMAT_TYPE_DATE_TIME, lenient));
					}

					extractLegacyDateTimePatterns(df, locale, tz, lenient);

					cfmlFormats.put(key, new SoftReference<List<FormatterWrapper>>(df));
				}
			}
		}
		return df;
	}

	private static lucee.commons.i18n.FormatterWrapper getFormatterWrapper(String pattern, ZoneId zone, Locale locale, short type, boolean lenient) {
		DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()

				.parseCaseInsensitive()

				.appendPattern(pattern);
		if (lenient) builder.parseLenient();
		else builder.parseStrict();
		DateTimeFormatter dtf = builder.toFormatter(locale).withZone(zone);
		return new FormatterWrapper(dtf, pattern, type, zone, true);
	}

	public static FormatterWrapper fromFormatToFormatter(DateFormat format, short type, Locale locale, TimeZone tz, boolean lenient) {
		ZoneId zone = tz.toZoneId();
		String p;

		DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder().appendPattern(p = ((SimpleDateFormat) format).toPattern());
		if (lenient) builder.parseLenient();
		else builder.parseStrict();
		DateTimeFormatter dtf = builder.toFormatter(locale).withZone(zone);

		// add year flexibility yy -> y
		if (p.indexOf("yy") != -1 && p.indexOf("yyyy") == -1) {
			// old version handles yy as yyyy
			p = StringUtil.replace(p, "yy", "y", true, true);
			builder = new DateTimeFormatterBuilder().appendPattern(p);
			if (lenient) builder.parseLenient();
			else builder.parseStrict();
			dtf = builder.toFormatter(locale).withZone(zone);
			return new FormatterWrapper(dtf, p, type, zone);
		}
		return new FormatterWrapper(dtf, p, type, zone);

	}

	public static List<FormatterWrapper> getDateFormats(Locale locale, TimeZone tz, boolean lenient) {
		String key = "d-" + locale.toString() + "-" + tz.getID() + "-" + lenient;
		SoftReference<List<FormatterWrapper>> tmp = cfmlFormats.get(key);
		List<FormatterWrapper> df = tmp == null ? null : tmp.get();
		if (df == null) {
			synchronized (SystemUtil.createToken("dt", key)) {
				df = tmp == null ? null : tmp.get();
				if (df == null) {
					ZoneId zone = tz.toZoneId();
					df = new ArrayList<>();

					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale).withZone(zone), "FULL", FORMAT_TYPE_DATE, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale).withZone(zone), "LONG", FORMAT_TYPE_DATE, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).withZone(zone), "MEDIUM", FORMAT_TYPE_DATE, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale).withZone(zone), "SHORT", FORMAT_TYPE_DATE, zone));

					extractLegacyDatePatterns(df, locale, tz, lenient);

					cfmlFormats.put(key, new SoftReference<List<FormatterWrapper>>(df));
				}
			}
		}
		return df;
	}

	private static void extractLegacyDatePatterns(List<FormatterWrapper> wrappers, Locale locale, TimeZone tz, boolean lenient) {
		List<DateFormat> list = new ArrayList<DateFormat>();
		list.add(DateFormat.getDateInstance(DateFormat.FULL, locale));
		list.add(DateFormat.getDateInstance(DateFormat.LONG, locale));
		list.add(DateFormat.getDateInstance(DateFormat.MEDIUM, locale));
		list.add(DateFormat.getDateInstance(DateFormat.SHORT, locale));

		extractLegacy(wrappers, list, locale, tz, lenient, FORMAT_TYPE_DATE);
	}

	private static void extractLegacyTimePatterns(List<FormatterWrapper> wrappers, Locale locale, TimeZone tz, boolean lenient) {
		List<DateFormat> list = new ArrayList<DateFormat>();
		list.add(DateFormat.getTimeInstance(DateFormat.FULL, locale));
		list.add(DateFormat.getTimeInstance(DateFormat.LONG, locale));
		list.add(DateFormat.getTimeInstance(DateFormat.MEDIUM, locale));
		list.add(DateFormat.getTimeInstance(DateFormat.SHORT, locale));

		extractLegacy(wrappers, list, locale, tz, lenient, FORMAT_TYPE_TIME);
	}

	private static void extractLegacyDateTimePatterns(List<FormatterWrapper> wrappers, Locale locale, TimeZone tz, boolean lenient) {
		List<DateFormat> list = new ArrayList<DateFormat>();
		list.add(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT, locale));

		list.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.FULL, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale));

		list.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale));

		list.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale));
		list.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale));

		extractLegacy(wrappers, list, locale, tz, lenient, FORMAT_TYPE_DATE_TIME);
	}

	private static void extractLegacy(List<FormatterWrapper> wrappers, List<DateFormat> list, Locale locale, TimeZone tz, boolean lenient, short type) {
		// add 24 and remove comma
		DateFormat[] formats = list.toArray(new DateFormat[list.size()]);
		for (int i = 0; i < formats.length; i++) {
			if (formats[i] instanceof SimpleDateFormat) {
				add24AndRemoveComma(list, formats[i], locale, type != FORMAT_TYPE_TIME, type != FORMAT_TYPE_DATE);
			}
		}

		// lenient and timezone
		for (DateFormat df: list) {
			df.setLenient(lenient);
			df.setTimeZone(tz);
			wrappers.add(fromFormatToFormatter(df, type, locale, tz, lenient));
		}
	}

	private static void add24AndRemoveComma(List<DateFormat> list, DateFormat sdf, Locale locale, boolean isDate, boolean isTime) {
		String p = ((SimpleDateFormat) sdf).toPattern() + "";
		add24AndRemoveComma(list, p, locale, isDate, isTime);
		String pp = p.replace(NON_BREAKING_SPACE, ' ');
		if (!p.equals(pp)) add24AndRemoveComma(list, pp, locale, isDate, isTime);

	}

	@Deprecated
	private static void add24AndRemoveComma(List<DateFormat> list, String p, Locale locale, boolean isDate, boolean isTime) {
		List<DateFormat> results = new ArrayList<>();
		// print.e("----- "+p);
		if (isDate && isTime) {
			if ((check(results, p, locale, " 'um' ", " "))) {}
			if ((check(results, p, locale, " 'à' ", " "))) {}
			if ((check(results, p, locale, " 'at' ", " "))) {}
			if ((check(results, p, locale, " 'de' ", " "))) {}

		}
		if (isTime) {
			if ((check(results, p, locale, "hh:mm:ss a", "HH:mm:ss"))) {}
			else if ((check(results, p, locale, "h:mm:ss a", "H:mm:ss"))) {}
			else if ((check(results, p, locale, "hh:mm a", "HH:mm"))) {}
			else if ((check(results, p, locale, "h:mm a", "H:mm"))) {}
			else if ((check(results, p, locale, "hh:mm:ssa", "HH:mm:ss"))) {}
			else if ((check(results, p, locale, "h:mm:ssa", "H:mm:ss"))) {}
			else if ((check(results, p, locale, "hh:mma", "HH:mm"))) {}
			else if ((check(results, p, locale, "h:mma", "H:mm"))) {}
		}
		if (isDate) {
			if ((check(results, p, locale, "y,", "y"))) {}
			if ((check(results, p, locale, "d MMMM ", "d. MMMM "))) {}
			if ((check(results, p, locale, "d MMM y", "d-MMM-y"))) {}
		}
		if (results.size() > 0) {
			Iterator<DateFormat> it = results.iterator();
			DateFormat _sdf;
			while (it.hasNext()) {
				_sdf = it.next();
				if (!list.contains(_sdf)) {
					list.add(_sdf);
					add24AndRemoveComma(list, _sdf, locale, isDate, isTime);
				}
			}
		}

	}

	private static boolean check(List<DateFormat> results, String orgPattern, Locale locale, String from, String to) {
		int index = orgPattern.indexOf(from);
		if (index != -1) {
			String p = StringUtil.replace(orgPattern, from, to, true);

			DateFormat sdf = FormatUtil.getDateTimeFormat(locale, null, p);
			results.add(sdf);
			return true;
		}
		return false;
	}

	public static List<FormatterWrapper> getTimeFormats(Locale locale, TimeZone tz, boolean lenient) {

		String key = "t-" + locale.toString() + "-" + tz.getID() + "-" + lenient;
		SoftReference<List<FormatterWrapper>> tmp = cfmlFormats.get(key);
		List<FormatterWrapper> df = tmp == null ? null : tmp.get();
		if (df == null) {
			synchronized (SystemUtil.createToken("dt", key)) {
				df = tmp == null ? null : tmp.get();
				if (df == null) {
					ZoneId zone = tz.toZoneId();
					df = new ArrayList<>();
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL).withLocale(locale).withZone(zone), "FULL", FORMAT_TYPE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG).withLocale(locale).withZone(zone), "LONG", FORMAT_TYPE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale).withZone(zone), "MEDIUM", FORMAT_TYPE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale).withZone(zone), "SHORT", FORMAT_TYPE_TIME, zone));

					extractLegacyTimePatterns(df, locale, tz, lenient);

					cfmlFormats.put(key, new SoftReference<List<FormatterWrapper>>(df));
				}
			}
		}
		return df;
	}

	private static DateFormat getDateTimeFormat(Locale locale, TimeZone tz, String mask) {
		DateFormat df;
		if (mask.equalsIgnoreCase("short")) df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
		else if (mask.equalsIgnoreCase("medium")) df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
		else if (mask.equalsIgnoreCase("long")) df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		else if (mask.equalsIgnoreCase("full")) df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale);
		else if (mask.equalsIgnoreCase("iso8601") || mask.equalsIgnoreCase("iso")) df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		else if (mask.equalsIgnoreCase("isomillis") || mask.equalsIgnoreCase("isoms")) df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		else {
			df = locale == null ? new SimpleDateFormat(mask) : new SimpleDateFormat(mask, locale);
		}
		if (tz != null) df.setTimeZone(tz);
		return df;
	}

	public static FormatterWrapper getDateTimeFormatter(Locale locale, String mask) {
		return getDateTimeFormatter(locale, mask, (ZoneId) null);
	}

	public static FormatterWrapper getDateTimeFormatter(Locale locale, String mask, TimeZone tz) {
		return getDateTimeFormatter(locale, mask, tz == null ? null : tz.toZoneId());
	}

	public static FormatterWrapper getDateTimeFormatter(Locale locale, String mask, ZoneId zone) {
		String key = locale + ":" + mask;
		SoftReference<FormatterWrapper> ref = dateTimeFormatter.get(key);
		FormatterWrapper fw = ref == null ? null : ref.get();
		if (fw == null) {
			synchronized (SystemUtil.createToken("getDateTimeFormatter", key)) {
				ref = dateTimeFormatter.get(key);
				fw = ref == null ? null : ref.get();
				if (fw == null) {
					// TODO cache
					DateTimeFormatter formatter;
					if (mask.equalsIgnoreCase("short")) formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
					else if (mask.equalsIgnoreCase("medium")) formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
					else if (mask.equalsIgnoreCase("long")) formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG);
					else if (mask.equalsIgnoreCase("full")) formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
					else if (mask.equalsIgnoreCase("iso8601") || mask.equalsIgnoreCase("iso")) formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

					else if (mask.equalsIgnoreCase("isoms") || mask.equalsIgnoreCase("isoMillis") || mask.equalsIgnoreCase("javascript")) {
						formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
					}
					else formatter = DateTimeFormatter.ofPattern(mask);

					if (locale != null) formatter = formatter.withLocale(locale);

					fw = new FormatterWrapper(formatter, mask, FORMAT_TYPE_DATE_TIME, zone);
					dateTimeFormatter.put(key, new SoftReference<FormatterWrapper>(fw));
				}
			}
		}
		return fw;
	}

	public static String format(DateTimeFormatter formatter, Date date, TimeZone timeZone) {
		return date.toInstant().atZone(DateTimeUtil.toOffsetIfNeeded(timeZone != null ? timeZone.toZoneId() : ZoneId.systemDefault(), date)).format(formatter);
	}

	public static String format(DateTimeFormatter formatter, long millis, TimeZone timeZone) {
		return Instant.ofEpochMilli(millis).atZone(DateTimeUtil.toOffsetIfNeeded(timeZone != null ? timeZone.toZoneId() : ZoneId.systemDefault(), millis)).format(formatter);
	}

	public static Long parse(FormatterWrapper fw, String date, ZoneId zone) throws DateTimeException {
		ParsePosition position = new ParsePosition(0);
		TemporalAccessor accessor = fw.formatter.parseUnresolved(date, position);

		// Check if parsing was successful and consumed the entire string
		if (position.getErrorIndex() >= 0 || position.getIndex() < date.length()) {
			throw new DateTimeException("cannot parse the string [" + date + "] to a date using this pattern [" + fw.pattern + "] with the time zone [" + zone.toString() + "] (" +

					position.getErrorIndex() + ":" + position.getIndex() + ":" + date.length() + ")");
		}
		try {
			ZoneId tmp;
			if (accessor.isSupported(ChronoField.OFFSET_SECONDS)) {
				zone = ZoneOffset.ofTotalSeconds(accessor.get(ChronoField.OFFSET_SECONDS));
			}
			else if ((tmp = accessor.query(TemporalQueries.zone())) != null) {
				zone = tmp;
			}

			if (FormatUtil.FORMAT_TYPE_DATE_TIME == fw.type) {
				int year = year(accessor);
				int month = accessor.get(ChronoField.MONTH_OF_YEAR);
				int day = accessor.get(ChronoField.DAY_OF_MONTH);
				int hour = hour(accessor);
				int minute = accessor.isSupported(ChronoField.MINUTE_OF_HOUR) ? accessor.get(ChronoField.MINUTE_OF_HOUR) : 0;
				int second = accessor.isSupported(ChronoField.SECOND_OF_MINUTE) ? accessor.get(ChronoField.SECOND_OF_MINUTE) : 0;
				int millis = accessor.isSupported(ChronoField.MILLI_OF_SECOND) ? accessor.get(ChronoField.MILLI_OF_SECOND) : 0;

				return DateTimeUtil.getInstance().toTime(zone, year, month, day, hour, minute, second, millis);

			}
			else if (FormatUtil.FORMAT_TYPE_DATE == fw.type) {
				int year = year(accessor);
				int month = accessor.get(ChronoField.MONTH_OF_YEAR);
				int day = accessor.get(ChronoField.DAY_OF_MONTH);

				return DateTimeUtil.getInstance().toTime(zone, year, month, day, DEFAULT_HOUR, DEFAULT_MINUTE, DEFAULT_SECOND, DEFAULT_MILLISECOND);

			}
			else { // FormatUtil.FORMAT_TYPE_TIME
				int hour = hour(accessor);
				int minute = accessor.isSupported(ChronoField.MINUTE_OF_HOUR) ? accessor.get(ChronoField.MINUTE_OF_HOUR) : 0;
				int second = accessor.isSupported(ChronoField.SECOND_OF_MINUTE) ? accessor.get(ChronoField.SECOND_OF_MINUTE) : 0;
				int millis = accessor.isSupported(ChronoField.MILLI_OF_SECOND) ? accessor.get(ChronoField.MILLI_OF_SECOND) : 0;

				return DateTimeUtil.getInstance().toTime(zone, DEFAULT_YEAR, DEFAULT_MONTH, DEFAULT_DAY, hour, minute, second, millis);
			}
		}
		catch (DateTimeException cause) {
			DateTimeException dte = new DateTimeException(
					"cannot parse the string [" + date + "] to a date using this pattern [" + fw.pattern + "] with the time zone [" + zone.toString() + "]");
			ExceptionUtil.initCauseEL(dte, cause);
			throw dte;
		}
	}

	public static Long parse(FormatterWrapper fw, String date, ZoneId zone, Long defaultValue) {
		ParsePosition position = new ParsePosition(0);
		TemporalAccessor accessor = fw.formatter.parseUnresolved(date, position);
		try {
			// Check if parsing was successful and consumed the entire string
			if (position.getErrorIndex() >= 0 || position.getIndex() < date.length()) {
				return defaultValue;
			}
			ZoneId tmp;
			if (accessor.isSupported(ChronoField.OFFSET_SECONDS)) {
				zone = ZoneOffset.ofTotalSeconds(accessor.get(ChronoField.OFFSET_SECONDS));
			}
			else if ((tmp = accessor.query(TemporalQueries.zone())) != null) {
				zone = tmp;
			}

			if (FormatUtil.FORMAT_TYPE_DATE_TIME == fw.type) {
				return DateTimeUtil.getInstance().toTime(zone,

						year(accessor),

						accessor.get(ChronoField.MONTH_OF_YEAR),

						accessor.get(ChronoField.DAY_OF_MONTH),

						hour(accessor),

						accessor.isSupported(ChronoField.MINUTE_OF_HOUR) ? accessor.get(ChronoField.MINUTE_OF_HOUR) : 0,

						accessor.isSupported(ChronoField.SECOND_OF_MINUTE) ? accessor.get(ChronoField.SECOND_OF_MINUTE) : 0,

						accessor.isSupported(ChronoField.MILLI_OF_SECOND) ? accessor.get(ChronoField.MILLI_OF_SECOND) : 0,

						defaultValue);

			}
			else if (FormatUtil.FORMAT_TYPE_DATE == fw.type) {
				return DateTimeUtil.getInstance().toTime(zone,

						year(accessor),

						accessor.get(ChronoField.MONTH_OF_YEAR),

						accessor.get(ChronoField.DAY_OF_MONTH),

						DEFAULT_HOUR, DEFAULT_MINUTE, DEFAULT_SECOND, DEFAULT_MILLISECOND, defaultValue);

			}
			else { // FormatUtil.FORMAT_TYPE_TIME
				return DateTimeUtil.getInstance().toTime(zone, DEFAULT_YEAR, DEFAULT_MONTH, DEFAULT_DAY,

						hour(accessor),

						accessor.isSupported(ChronoField.MINUTE_OF_HOUR) ? accessor.get(ChronoField.MINUTE_OF_HOUR) : 0,

						accessor.isSupported(ChronoField.SECOND_OF_MINUTE) ? accessor.get(ChronoField.SECOND_OF_MINUTE) : 0,

						accessor.isSupported(ChronoField.MILLI_OF_SECOND) ? accessor.get(ChronoField.MILLI_OF_SECOND) : 0,

						defaultValue);
			}
		}
		catch (java.time.DateTimeException dte) {
			return defaultValue;
		}

	}

	private static final int hour(TemporalAccessor accessor) {
		// Calculate hour using 12-hour clock fields if 24-hour clock is not available
		if (accessor.isSupported(ChronoField.HOUR_OF_DAY)) {
			return accessor.get(ChronoField.HOUR_OF_DAY);
		}
		else if (accessor.isSupported(ChronoField.CLOCK_HOUR_OF_AMPM) && accessor.isSupported(ChronoField.AMPM_OF_DAY)) {
			// Convert 12-hour format to 24-hour
			int clockHour = accessor.get(ChronoField.CLOCK_HOUR_OF_AMPM); // 1-12
			int amPm = accessor.get(ChronoField.AMPM_OF_DAY); // 0=AM, 1=PM

			if (amPm == 1) { // PM
				return (clockHour == 12) ? 12 : clockHour + 12; // 12 PM -> 12, 1 PM -> 13, etc.
			}
			return (clockHour == 12) ? 0 : clockHour; // 12 AM -> 0, 1 AM -> 1, etc.
		}
		else if (accessor.isSupported(ChronoField.HOUR_OF_AMPM) && accessor.isSupported(ChronoField.AMPM_OF_DAY)) {
			// Alternative: use HOUR_OF_AMPM (0-11) instead of CLOCK_HOUR_OF_AMPM (1-12)
			int hourOfAmPm = accessor.get(ChronoField.HOUR_OF_AMPM); // 0-11
			int amPm = accessor.get(ChronoField.AMPM_OF_DAY); // 0=AM, 1=PM

			return (amPm == 1) ? hourOfAmPm + 12 : hourOfAmPm; // AM: 0-11, PM: 12-23
		}
		return 0;
	}

	private static final int year(TemporalAccessor accessor) {
		int year = accessor.isSupported(ChronoField.YEAR) ? accessor.get(ChronoField.YEAR) : accessor.get(ChronoField.YEAR_OF_ERA);
		if (year < 100) {
			if (year < 40) {
				return year + 2000;
			}
			return year + 1900;
		}
		return year;
	}

	private static class Pattern {
		public final String pattern;
		public final short type;

		Pattern(String pattern, short type) {
			this.pattern = pattern;
			this.type = type;
		}
	}
}