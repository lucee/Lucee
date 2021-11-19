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
package lucee.runtime.functions.international;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.i18n.FormatUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;

/**
 * Implements the CFML Function dateformat
 */
public final class LSTimeFormat implements Function {

	private static final long serialVersionUID = -35357762883021790L;

	/**
	 * @param pc
	 * @param o
	 * @return
	 * @throws PageException
	 */
	public static String call(PageContext pc, Object o) throws PageException {
		return _call(pc, o, "short", pc.getLocale(), pc.getTimeZone());
	}

	public static String call(PageContext pc, Object o, String mask) throws PageException {
		return _call(pc, o, mask, pc.getLocale(), pc.getTimeZone());
	}

	public static String call(PageContext pc, Object o, String mask, Locale locale) throws PageException {
		return _call(pc, o, mask, locale == null ? pc.getLocale() : locale, pc.getTimeZone());
	}

	public static String call(PageContext pc, Object o, String mask, Locale locale, TimeZone tz) throws PageException {
		return _call(pc, o, mask, locale == null ? pc.getLocale() : locale, tz == null ? pc.getTimeZone() : tz);
	}

	private static String _call(PageContext pc, Object o, String mask, Locale locale, TimeZone tz) throws PageException {
		if (o instanceof String && StringUtil.isEmpty((String) o, true)) return "";
		return new lucee.runtime.format.TimeFormat(locale).format(toTimeLS(locale, tz, o), mask, tz);
	}

	private static DateTime toTimeLS(Locale locale, TimeZone timeZone, Object object) throws PageException {
		if (object instanceof DateTime) return (DateTime) object;
		if (object instanceof CharSequence) {
			String str = Caster.toString(object);

			DateFormat[] formats = FormatUtil.getTimeFormats(locale, timeZone, true);
			for (int i = 0; i < formats.length; i++) {
				try {
					return new DateTimeImpl(formats[i].parse(str).getTime(), false);
				}
				catch (ParseException e) {
				}
			}

		}
		return DateCaster.toDateAdvanced(object, timeZone);
	}

}