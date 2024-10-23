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
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import lucee.commons.date.TimeZoneConstants;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;

public class FormatUtil {

	public static final short FORMAT_TYPE_DATE = 1;
	public static final short FORMAT_TYPE_TIME = 2;
	public static final short FORMAT_TYPE_DATE_TIME = 3;
	public static final short FORMAT_TYPE_DATE_ALL = 4;

	private static final LocalTime DEFAULT_TIME = LocalTime.of(0, 0, 0);
	private static final LocalDate DEFAULT_DATE = LocalDate.of(1899, 12, 30);

	private final static Map<String, SoftReference<DateFormat[]>> formats = new ConcurrentHashMap<String, SoftReference<DateFormat[]>>();

	private final static Map<String, SoftReference<List<FormatterWrapper>>> cfmlFormats = new ConcurrentHashMap<>();
	// "EEEE, MMMM d, yyyy, h:mm:ss a 'Coordinated Universal Time'"
	private final static Pattern[] strCfmlFormats = new Pattern[] {

			// new Pattern("M/d/yyyy", FORMAT_TYPE_DATE),

			// new Pattern("MM/dd/yyyy", FORMAT_TYPE_DATE),

			new Pattern("dd-MMM-yyyy", FORMAT_TYPE_DATE),

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

			new Pattern("EE, dd-MMM-yyyy HH:mm:ss zz", FORMAT_TYPE_DATE_TIME),

			new Pattern("EE, dd MMM yyyy HH:mm:ss zz", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE d, MMM yyyy HH:mm:ss zz", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE, dd MMM yyyy HH:mm:ss Z", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE, MMM dd, yyyy HH:mm:ssZ", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE, dd MMM yyyy HH:mm:ss Z", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEEE, MMMM d, yyyy, h:mm:ss a z", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEEE, MMMM d, yyyy, h:mm:ss a zzzz", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE MMM dd yyyy HH:mm:ss 'GMT'ZZ (z)", FORMAT_TYPE_DATE_TIME),

			new Pattern("EEE MMM dd yyyy HH:mm:ss 'GMT'ZZ (zzzz)", FORMAT_TYPE_DATE_TIME),

			new Pattern("yyyy/MM/dd HH:mm:ss zz", FORMAT_TYPE_DATE_TIME),

			new Pattern("yyyy-MM-dd HH:mm:ss zz", FORMAT_TYPE_DATE_TIME),

			new Pattern("yyyy-MM-dd'T'HH:mm:ssXXX", FORMAT_TYPE_DATE_TIME),

			new Pattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", FORMAT_TYPE_DATE_TIME)

	};

	private static final Map<String, SoftReference<FormatterWrapper>> dateTimeFormatter = new ConcurrentHashMap<>();
	public static final boolean debug = true;

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
						builder = new DateTimeFormatterBuilder().appendPattern(p.pattern);
						if (lenient) builder.parseCaseInsensitive();
						else builder.parseCaseSensitive();

						DateTimeFormatter dtf;
						if (p.type == FORMAT_TYPE_DATE_TIME) dtf = builder.toFormatter(locale).withZone(zone);
						else dtf = builder.toFormatter(locale);
						formatter.add(new FormatterWrapper(dtf, p.pattern, p.type, zone, true));
					}
					cfmlFormats.put(key, new SoftReference(formatter));
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
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.FULL).withLocale(locale).withZone(zone), "FULL_FULL",
							FORMAT_TYPE_DATE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.LONG).withLocale(locale).withZone(zone), "LONG_LONG",
							FORMAT_TYPE_DATE_TIME, zone));
					df.add(new FormatterWrapper(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).withLocale(locale).withZone(zone), "MEDIUM_MEDIUM",
							FORMAT_TYPE_DATE_TIME, zone));
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

					cfmlFormats.put(key, new SoftReference<List<FormatterWrapper>>(df));
				}
			}
		}
		return df;
	}

	@Deprecated
	public static DateFormat[] getDateTimeFormatsOld(Locale locale, TimeZone tz, boolean lenient) {

		String id = "dt-" + locale.toString() + "-" + tz.getID() + "-" + lenient;
		SoftReference<DateFormat[]> tmp = formats.get(id);
		DateFormat[] df = tmp == null ? null : tmp.get();
		if (df == null) {
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
			add24AndRemoveComma(list, locale, true, true);
			addCustom(list, locale, FORMAT_TYPE_DATE_TIME);

			df = list.toArray(new DateFormat[list.size()]);

			for (int i = 0; i < df.length; i++) {
				df[i].setLenient(lenient);
				df[i].setTimeZone(tz);
			}

			formats.put(id, new SoftReference<DateFormat[]>(df));
		}
		return clone(df);
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

					cfmlFormats.put(key, new SoftReference<List<FormatterWrapper>>(df));
				}
			}
		}
		return df;
	}

	@Deprecated
	public static DateFormat[] getDateFormatsOld(Locale locale, TimeZone tz, boolean lenient) {
		String id = "d-" + locale.toString() + "-" + tz.getID() + "-" + lenient;
		SoftReference<DateFormat[]> tmp = formats.get(id);
		DateFormat[] df = tmp == null ? null : tmp.get();

		if (df == null) {
			List<DateFormat> list = new ArrayList<DateFormat>();
			list.add(DateFormat.getDateInstance(DateFormat.FULL, locale));
			list.add(DateFormat.getDateInstance(DateFormat.LONG, locale));
			list.add(DateFormat.getDateInstance(DateFormat.MEDIUM, locale));
			list.add(DateFormat.getDateInstance(DateFormat.SHORT, locale));
			add24AndRemoveComma(list, locale, true, false);
			addCustom(list, locale, FORMAT_TYPE_DATE);
			df = list.toArray(new DateFormat[list.size()]);

			for (int i = 0; i < df.length; i++) {
				df[i].setLenient(lenient);
				df[i].setTimeZone(tz);
			}
			formats.put(id, new SoftReference<DateFormat[]>(df));
		}
		return clone(df);
	}

	private static DateFormat[] clone(DateFormat[] src) {
		DateFormat[] trg = new DateFormat[src.length];
		for (int i = 0; i < src.length; i++) {
			trg[i] = (DateFormat) ((SimpleDateFormat) src[i]).clone();
		}
		return trg;
	}

	@Deprecated
	public static DateFormat[] getTimeFormatsOld(Locale locale, TimeZone tz, boolean lenient) {
		String id = "t-" + locale.toString() + "-" + tz.getID() + "-" + lenient;
		SoftReference<DateFormat[]> tmp = formats.get(id);
		DateFormat[] df = tmp == null ? null : tmp.get();

		if (df == null) {
			List<DateFormat> list = new ArrayList<DateFormat>();
			list.add(DateFormat.getTimeInstance(DateFormat.FULL, locale));
			list.add(DateFormat.getTimeInstance(DateFormat.LONG, locale));
			list.add(DateFormat.getTimeInstance(DateFormat.MEDIUM, locale));
			list.add(DateFormat.getTimeInstance(DateFormat.SHORT, locale));
			add24AndRemoveComma(list, locale, false, true);
			addCustom(list, locale, FORMAT_TYPE_TIME);
			df = list.toArray(new DateFormat[list.size()]);

			for (int i = 0; i < df.length; i++) {
				df[i].setLenient(lenient);
				df[i].setTimeZone(tz);
			}
			formats.put(id, new SoftReference<DateFormat[]>(df));
		}
		return clone(df);
	}

	@Deprecated
	private static void add24AndRemoveComma(List<DateFormat> list, Locale locale, boolean isDate, boolean isTime) {

		DateFormat[] df = list.toArray(new DateFormat[list.size()]);
		for (int i = 0; i < df.length; i++) {
			if (df[i] instanceof SimpleDateFormat) {
				add24AndRemoveComma(list, df[i], locale, isDate, isTime);
			}
		}
	}

	@Deprecated
	private static void add24AndRemoveComma(List<DateFormat> list, DateFormat sdf, Locale locale, boolean isDate, boolean isTime) {
		String p;

		List<DateFormat> results = new ArrayList<>();
		p = ((SimpleDateFormat) sdf).toPattern() + "";
		// print.e("----- "+p);
		if (isDate && isTime) {
			if ((check(results, p, locale, " 'um' ", " "))) {
			}
			if ((check(results, p, locale, " 'à' ", " "))) {
			}
			if ((check(results, p, locale, " 'at' ", " "))) {
			}
			if ((check(results, p, locale, " 'de' ", " "))) {
			}

		}
		if (isTime) {
			if ((check(results, p, locale, "hh:mm:ss a", "HH:mm:ss"))) {
			}
			else if ((check(results, p, locale, "h:mm:ss a", "H:mm:ss"))) {
			}
			else if ((check(results, p, locale, "hh:mm a", "HH:mm"))) {
			}
			else if ((check(results, p, locale, "h:mm a", "H:mm"))) {
			}
			else if ((check(results, p, locale, "hh:mm:ssa", "HH:mm:ss"))) {
			}
			else if ((check(results, p, locale, "h:mm:ssa", "H:mm:ss"))) {
			}
			else if ((check(results, p, locale, "hh:mma", "HH:mm"))) {
			}
			else if ((check(results, p, locale, "h:mma", "H:mm"))) {
			}
		}
		if (isDate) {
			if ((check(results, p, locale, "y,", "y"))) {
			}
			if ((check(results, p, locale, "d MMMM ", "d. MMMM "))) {
			}
			if ((check(results, p, locale, "d MMM y", "d-MMM-y"))) {
			}
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

	@Deprecated
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

	private static void addCustom(List<DateFormat> list, Locale locale, short formatType) {
		// get custom formats from file
		Config config = ThreadLocalPageContext.getConfig();
		Resource dir = config != null ? config.getConfigDir().getRealResource("locales") : null;
		if (dir != null && dir.isDirectory()) {
			String appendix = "-datetime";
			if (formatType == FORMAT_TYPE_DATE) appendix = "-date";
			if (formatType == FORMAT_TYPE_TIME) appendix = "-time";

			Resource file = dir.getRealResource(locale.getLanguage() + "-" + locale.getCountry() + appendix + ".df");
			if (file.isFile()) {
				try {
					String content = IOUtil.toString(file, (Charset) null);
					String[] arr = lucee.runtime.type.util.ListUtil.listToStringArray(content, '\n');
					String line;
					DateFormat sdf;
					for (int i = 0; i < arr.length; i++) {
						line = arr[i].trim();
						if (StringUtil.isEmpty(line)) continue;
						sdf = FormatUtil.getDateTimeFormat(locale, null, line);
						if (!list.contains(sdf)) list.add(sdf);
					}

				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
		}
	}

	public static DateFormat[] getFormats(Locale locale, TimeZone tz, boolean lenient, short formatType) {
		if (FORMAT_TYPE_DATE_TIME == formatType) return getDateTimeFormatsOld(locale, TimeZoneConstants.GMT, true);
		if (FORMAT_TYPE_DATE == formatType) return getDateFormatsOld(locale, TimeZoneConstants.GMT, true);
		if (FORMAT_TYPE_TIME == formatType) return getTimeFormatsOld(locale, TimeZoneConstants.GMT, true);

		DateFormat[] dt = getDateTimeFormatsOld(locale, TimeZoneConstants.GMT, true);
		DateFormat[] d = getDateFormatsOld(locale, TimeZoneConstants.GMT, true);
		DateFormat[] t = getTimeFormatsOld(locale, TimeZoneConstants.GMT, true);

		DateFormat[] all = new DateFormat[dt.length + d.length + t.length];
		for (int i = 0; i < dt.length; i++) {
			all[i] = dt[i];
		}
		for (int i = 0; i < d.length; i++) {
			all[i + dt.length] = d[i];
		}
		for (int i = 0; i < t.length; i++) {
			all[i + dt.length + d.length] = t[i];
		}
		return getDateTimeFormatsOld(locale, TimeZoneConstants.GMT, true);
	}

	public static String[] getSupportedPatterns(Locale locale, short formatType) {
		DateFormat[] _formats = getFormats(locale, TimeZoneConstants.GMT, true, formatType);
		String[] patterns = new String[_formats.length];
		for (int i = 0; i < _formats.length; i++) {
			if (!(_formats[i] instanceof SimpleDateFormat)) return null; // all or nothing
			patterns[i] = ((SimpleDateFormat) _formats[i]).toPattern();
		}

		return patterns;
	}

	@Deprecated
	public static DateFormat getDateFormat(Locale locale, TimeZone tz, String mask) {
		DateFormat df;
		if (mask.equalsIgnoreCase("short")) df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		else if (mask.equalsIgnoreCase("medium")) df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		else if (mask.equalsIgnoreCase("long")) df = DateFormat.getDateInstance(DateFormat.LONG, locale);
		else if (mask.equalsIgnoreCase("full")) df = DateFormat.getDateInstance(DateFormat.FULL, locale);
		else {
			df = FormatUtil.getDateTimeFormat(locale, null, mask);
		}
		df.setTimeZone(tz);
		return df;
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

					cfmlFormats.put(key, new SoftReference<List<FormatterWrapper>>(df));
				}
			}
		}
		return df;
	}

	@Deprecated
	public static DateFormat getTimeFormat(Locale locale, TimeZone tz, String mask) {
		DateFormat df;
		if (mask.equalsIgnoreCase("short")) df = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		else if (mask.equalsIgnoreCase("medium")) df = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
		else if (mask.equalsIgnoreCase("long")) df = DateFormat.getTimeInstance(DateFormat.LONG, locale);
		else if (mask.equalsIgnoreCase("full")) df = DateFormat.getTimeInstance(DateFormat.FULL, locale);
		else {
			df = locale == null ? new SimpleDateFormat(mask) : new SimpleDateFormat(mask, locale);
		}
		if (tz != null) df.setTimeZone(tz);
		return df;
	}

	@Deprecated
	public static DateFormat getDateTimeFormat(Locale locale, TimeZone tz, String mask) {
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
					else if (mask.equalsIgnoreCase("iso8601") || mask.equalsIgnoreCase("iso")) {
						formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
					}
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
		return date.toInstant().atZone(timeZone != null ? timeZone.toZoneId() : ZoneId.systemDefault()).format(formatter);
	}

	public static String format(DateTimeFormatter formatter, long millis, TimeZone timeZone) {
		return Instant.ofEpochMilli(millis).atZone(timeZone != null ? timeZone.toZoneId() : ZoneId.systemDefault()).format(formatter);
	}

	public static long parseSimple(DateTimeFormatter formatter, String date, TimeZone timeZone) throws DateTimeParseException {
		return ZonedDateTime.parse(date, formatter).withZoneSameInstant(timeZone != null ? timeZone.toZoneId() : ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public static long parseX(DateTimeFormatter formatter, String date, TimeZone timeZone) throws DateTimeParseException {
		// Parse the date using the formatter (no time zone assumption yet)
		ZonedDateTime zonedDateTime = null;

		try {
			// Parse the date string into a ZonedDateTime
			zonedDateTime = ZonedDateTime.parse(date, formatter);
		}
		catch (DateTimeParseException e) {
			// If no time zone is provided in the input, handle it with the passed TimeZone
			LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);
			zonedDateTime = localDateTime.atZone(timeZone != null ? timeZone.toZoneId() : ZoneId.systemDefault());
		}

		// Convert the parsed ZonedDateTime to the desired time zone and return epoch milliseconds
		return zonedDateTime.withZoneSameInstant(timeZone != null ? timeZone.toZoneId() : ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public static long parse(FormatterWrapper fw, String date, ZoneId zone) {

		if (fw.type == FormatUtil.FORMAT_TYPE_DATE_TIME) {
			ZonedDateTime zdt = ZonedDateTime.parse(date, fw.formatter);
			if (zdt.getYear() < 100) {
				// TODO handle this here
				throw new RuntimeException();
			}
			return zdt.toInstant().toEpochMilli();
		}
		else if (fw.type == FormatUtil.FORMAT_TYPE_DATE) {
			LocalDate ld = LocalDate.parse(date, fw.formatter);
			if (ld.getYear() < 100) {
				// TODO handle this here
				throw new RuntimeException();
			}
			return getEpochMillis(LocalDate.parse(date, fw.formatter), DEFAULT_TIME, zone);

		}
		return getEpochMillis(DEFAULT_DATE, LocalTime.parse(date, fw.formatter), zone);
	}

	private static long getEpochMillis(LocalDate localDate, LocalTime localTime, ZoneId zoneId) {
		// Combine LocalDate and LocalTime into LocalDateTime
		LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

		// Convert LocalDateTime to ZonedDateTime with the specified time zone
		ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

		// Convert to Instant and get epoch millis
		return zonedDateTime.toInstant().toEpochMilli();
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