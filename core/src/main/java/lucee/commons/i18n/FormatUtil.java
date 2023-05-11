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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.date.TimeZoneConstants;
import lucee.commons.io.IOUtil;
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

	private final static Map<String, SoftReference<DateFormat[]>> formats = new ConcurrentHashMap<String, SoftReference<DateFormat[]>>();

	public static DateFormat[] getDateTimeFormats(Locale locale, TimeZone tz, boolean lenient) {

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

	public static DateFormat[] getDateFormats(Locale locale, TimeZone tz, boolean lenient) {
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

	public static DateFormat[] getTimeFormats(Locale locale, TimeZone tz, boolean lenient) {
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

	private static void add24AndRemoveComma(List<DateFormat> list, Locale locale, boolean isDate, boolean isTime) {

		DateFormat[] df = list.toArray(new DateFormat[list.size()]);
		for (int i = 0; i < df.length; i++) {
			if (df[i] instanceof SimpleDateFormat) {
				add24AndRemoveComma(list, (SimpleDateFormat) df[i], locale, isDate, isTime);
			}
		}
	}

	private static void add24AndRemoveComma(List<DateFormat> list, SimpleDateFormat sdf, Locale locale, boolean isDate, boolean isTime) {
		String p;

		List<SimpleDateFormat> results = new ArrayList<SimpleDateFormat>();
		p = sdf.toPattern() + "";
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
			Iterator<SimpleDateFormat> it = results.iterator();
			SimpleDateFormat _sdf;
			while (it.hasNext()) {
				_sdf = it.next();
				if (!list.contains(_sdf)) {
					list.add(_sdf);
					add24AndRemoveComma(list, _sdf, locale, isDate, isTime);
				}
			}
		}

	}

	private static boolean check(List<SimpleDateFormat> results, String orgPattern, Locale locale, String from, String to) {
		int index = orgPattern.indexOf(from);
		if (index != -1) {
			String p = StringUtil.replace(orgPattern, from, to, true);
			SimpleDateFormat sdf = new SimpleDateFormat(p, locale);
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
					SimpleDateFormat sdf;
					for (int i = 0; i < arr.length; i++) {
						line = arr[i].trim();
						if (StringUtil.isEmpty(line)) continue;
						sdf = new SimpleDateFormat(line, locale);
						if (!list.contains(sdf)) list.add(sdf);
					}

				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
		}
	}

	/**
	 * CFML Supported LS Formats
	 * 
	 * @param locale
	 * @param tz
	 * @param lenient
	 * @return
	 */
	public static DateFormat[] getCFMLFormats(TimeZone tz, boolean lenient) {
		String id = "cfml-" + Locale.ENGLISH.toString() + "-" + tz.getID() + "-" + lenient;
		SoftReference<DateFormat[]> tmp = formats.get(id);
		DateFormat[] df = tmp == null ? null : tmp.get();

		if (df == null) {
			df = new SimpleDateFormat[] { new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH), new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss a zzz", Locale.ENGLISH),
					new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a", Locale.ENGLISH), new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.ENGLISH),
					new SimpleDateFormat("MMMM d yyyy HH:mm:ssZ", Locale.ENGLISH), new SimpleDateFormat("MMMM d yyyy HH:mm:ss", Locale.ENGLISH),
					new SimpleDateFormat("MMMM d yyyy HH:mm", Locale.ENGLISH), new SimpleDateFormat("EEE, MMM dd, yyyy HH:mm:ssZ", Locale.ENGLISH),
					new SimpleDateFormat("EEE, MMM dd, yyyy HH:mm:ss", Locale.ENGLISH), new SimpleDateFormat("EEEE, MMMM dd, yyyy H:mm:ss a zzz", Locale.ENGLISH),
					new SimpleDateFormat("dd-MMM-yy HH:mm a", Locale.ENGLISH), new SimpleDateFormat("dd-MMMM-yy HH:mm a", Locale.ENGLISH),
					new SimpleDateFormat("EE, dd-MMM-yyyy HH:mm:ss zz", Locale.ENGLISH), new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH),
					new SimpleDateFormat("EEE d, MMM yyyy HH:mm:ss zz", Locale.ENGLISH), new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH),
					new SimpleDateFormat("MMMM, dd yyyy HH:mm:ssZ", Locale.ENGLISH), new SimpleDateFormat("MMMM, dd yyyy HH:mm:ss", Locale.ENGLISH),
					new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zz", Locale.ENGLISH), new SimpleDateFormat("dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH),
					new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'ZZ (z)", Locale.ENGLISH), new SimpleDateFormat("dd MMM, yyyy HH:mm:ss", Locale.ENGLISH)
					// ,new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.ENGLISH)
			};

			for (int i = 0; i < df.length; i++) {
				df[i].setLenient(lenient);
				df[i].setTimeZone(tz);
			}
			formats.put(id, new SoftReference<DateFormat[]>(df));
		}
		return clone(df);
	}

	public static DateFormat[] getFormats(Locale locale, TimeZone tz, boolean lenient, short formatType) {
		if (FORMAT_TYPE_DATE_TIME == formatType) return getDateTimeFormats(locale, TimeZoneConstants.GMT, true);
		if (FORMAT_TYPE_DATE == formatType) return getDateFormats(locale, TimeZoneConstants.GMT, true);
		if (FORMAT_TYPE_TIME == formatType) return getTimeFormats(locale, TimeZoneConstants.GMT, true);

		DateFormat[] dt = getDateTimeFormats(locale, TimeZoneConstants.GMT, true);
		DateFormat[] d = getDateFormats(locale, TimeZoneConstants.GMT, true);
		DateFormat[] t = getTimeFormats(locale, TimeZoneConstants.GMT, true);

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
		return getDateTimeFormats(locale, TimeZoneConstants.GMT, true);
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

	public static DateFormat getDateFormat(Locale locale, TimeZone tz, String mask) {
		DateFormat df;
		if (mask.equalsIgnoreCase("short")) df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		else if (mask.equalsIgnoreCase("medium")) df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		else if (mask.equalsIgnoreCase("long")) df = DateFormat.getDateInstance(DateFormat.LONG, locale);
		else if (mask.equalsIgnoreCase("full")) df = DateFormat.getDateInstance(DateFormat.FULL, locale);
		else {
			df = new SimpleDateFormat(mask, locale);
		}
		df.setTimeZone(tz);
		return df;
	}

	public static DateFormat getTimeFormat(Locale locale, TimeZone tz, String mask) {
		DateFormat df;
		if (mask.equalsIgnoreCase("short")) df = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		else if (mask.equalsIgnoreCase("medium")) df = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
		else if (mask.equalsIgnoreCase("long")) df = DateFormat.getTimeInstance(DateFormat.LONG, locale);
		else if (mask.equalsIgnoreCase("full")) df = DateFormat.getTimeInstance(DateFormat.FULL, locale);
		else {
			df = new SimpleDateFormat(mask, locale);
		}
		df.setTimeZone(tz);
		return df;
	}

	public static DateFormat getDateTimeFormat(Locale locale, TimeZone tz, String mask) {
		DateFormat df;
		if (mask.equalsIgnoreCase("short")) df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
		else if (mask.equalsIgnoreCase("medium")) df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
		else if (mask.equalsIgnoreCase("long")) df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		else if (mask.equalsIgnoreCase("full")) df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale);
		else if (mask.equalsIgnoreCase("iso8601")) df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		else {
			df = new SimpleDateFormat(mask, locale);
		}
		df.setTimeZone(tz);
		return df;

		/*
		 * if(mask!=null && StringUtil.indexOfIgnoreCase(mask, "tt")==-1 &&
		 * StringUtil.indexOfIgnoreCase(mask, "t")!=-1) { DateFormatSymbols dfs = new
		 * DateFormatSymbols(locale); dfs.setAmPmStrings(AP); sdf.setDateFormatSymbols(dfs); }
		 */
	}

}