/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.functions.international;

import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.date.DateTimeUtil;
import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.dt.DateTime;

public final class LSWeek implements Function {

	private static final long serialVersionUID = -4184509921145613454L;

	public static double call(PageContext pc, DateTime date) {
		return _call(pc, date, pc.getLocale(), pc.getTimeZone());
	}

	public static double call(PageContext pc, DateTime date, Locale locale) {
		return _call(pc, date, locale == null ? pc.getLocale() : locale, pc.getTimeZone());
	}

	public static double call(PageContext pc, DateTime date, Locale locale, TimeZone tz) {
		return _call(pc, date, locale == null ? pc.getLocale() : locale, tz == null ? pc.getTimeZone() : tz);
	}

	private static double _call(PageContext pc, DateTime date, Locale locale, TimeZone tz) {
		return DateTimeUtil.getInstance().getWeekOfYear(locale, tz, date);
	}
}