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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.WeakHashMap;

/**
 * 
 */
public final class DateFormatPool {

	private final static Map<String, SimpleDateFormat> data = new WeakHashMap();

	/**
	 * pool for formated dates
	 * 
	 * @param locale
	 * @param timeZone
	 * @param pattern
	 * @param date
	 * @return date matching given values
	 */
	public static String format(Locale locale, TimeZone timeZone, String pattern, Date date) {
		synchronized (data) {
			String key = locale.toString() + '-' + timeZone.getID() + '-' + pattern;
			Object obj = data.get(key);
			if (obj != null) {
				return ((SimpleDateFormat) obj).format(date);
			}
			SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
			sdf.setTimeZone(timeZone);
			data.put(key, sdf);
			return sdf.format(date);
		}
	}

	/**
	 * pool for formated dates
	 * 
	 * @param locale
	 * @param pattern
	 * @param date
	 * @return date matching given values
	 */
	public static String format(Locale locale, String pattern, Date date) {
		synchronized (data) {
			String key = locale.toString() + '-' + pattern;
			Object obj = data.get(key);
			if (obj != null) {
				return ((SimpleDateFormat) obj).format(date);
			} // print.ln(key);
			SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
			data.put(key, sdf);
			return sdf.format(date);
		}
	}

	/**
	 * pool for formated dates
	 * 
	 * @param pattern
	 * @param date
	 * @return date matching given values
	 */
	public static String format(String pattern, Date date) {
		synchronized (data) {
			Object obj = data.get(pattern);
			if (obj != null) {
				return ((SimpleDateFormat) obj).format(date);
			} // print.ln(pattern);
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			data.put(pattern, sdf);
			return sdf.format(date);
		}
	}

}