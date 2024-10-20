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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.SystemUtil;
import lucee.runtime.engine.ThreadLocalPageContext;

public final class DateFormatPool {

	private final static Map<String, SoftReference<SimpleDateFormat>> datax = new ConcurrentHashMap<>();

	public static String format(Locale locale, TimeZone timeZone, String pattern, Date date) {
		return getSimpleDateFormat(locale, timeZone, pattern).format(date);
	}

	public static String format(Locale locale, String pattern, Date date) {
		return getSimpleDateFormat(locale, null, pattern).format(date);
	}

	public static String format(String pattern, Date date) {
		return getSimpleDateFormat(null, null, pattern).format(date);
	}

	private static SimpleDateFormat getSimpleDateFormat(Locale locale, TimeZone timeZone, String pattern) {
		if (locale == null) locale = ThreadLocalPageContext.getLocale();
		if (timeZone == null) timeZone = ThreadLocalPageContext.getTimeZone();

		String key = locale.toString() + '-' + timeZone.getID() + '-' + pattern;
		SoftReference<SimpleDateFormat> ref = datax.get(key);
		SimpleDateFormat sdf = ref == null ? null : ref.get();

		if (sdf == null) {
			synchronized (SystemUtil.createToken("DateFormatPool", key)) {
				ref = datax.get(key);
				sdf = ref == null ? null : ref.get();
				if (sdf == null) {
					sdf = new SimpleDateFormat(pattern, locale);
					sdf.setTimeZone(timeZone);
					datax.put(key, new SoftReference<>(sdf));
				}
			}
		}
		return sdf;
	}
}