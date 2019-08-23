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
/**
 * Implements the CFML Function lsparsedatetime
 */
package lucee.runtime.functions.international;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.date.TimeZoneUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.dt.DateTimeImpl;

public final class LSParseDateTime implements Function {

	private static final long serialVersionUID = 7808039073301229473L;

	public static lucee.runtime.type.dt.DateTime call(PageContext pc, Object oDate) throws PageException {
		return _call(pc, oDate, pc.getLocale(), pc.getTimeZone(), null);
	}

	public static lucee.runtime.type.dt.DateTime call(PageContext pc, Object oDate, Locale locale) throws PageException {
		return _call(pc, oDate, locale == null ? pc.getLocale() : locale, pc.getTimeZone(), null);
	}

	public static lucee.runtime.type.dt.DateTime call(PageContext pc, Object oDate, Locale locale, String strTimezoneOrFormat) throws PageException {
		if (locale == null) locale = pc.getLocale();
		if (strTimezoneOrFormat == null) {
			return _call(pc, oDate, locale, pc.getTimeZone(), null);
		}
		TimeZone tz = TimeZoneUtil.toTimeZone(strTimezoneOrFormat, null);
		if (tz != null) return _call(pc, oDate, locale, tz, null);
		return _call(pc, oDate, locale, pc.getTimeZone(), strTimezoneOrFormat);
	}

	public static lucee.runtime.type.dt.DateTime call(PageContext pc, Object oDate, Locale locale, String strTimezone, String strFormat) throws PageException {
		return _call(pc, oDate, locale == null ? pc.getLocale() : locale, strTimezone == null ? pc.getTimeZone() : TimeZoneUtil.toTimeZone(strTimezone), strFormat);
	}

	private static lucee.runtime.type.dt.DateTime _call(PageContext pc, Object oDate, Locale locale, TimeZone tz, String format) throws PageException {

		if (oDate instanceof Date) return Caster.toDate(oDate, tz);

		String strDate = Caster.toString(oDate);

		// regular parse date time
		if (StringUtil.isEmpty(format, true)) return DateCaster.toDateTime(locale, strDate, tz, locale.equals(Locale.US));

		// with java based format
		tz = ThreadLocalPageContext.getTimeZone(tz);
		if (locale == null) locale = pc.getLocale();
		SimpleDateFormat df = new SimpleDateFormat(format, locale);
		df.setTimeZone(tz);
		try {
			return new DateTimeImpl(df.parse(strDate));
		}
		catch (ParseException e) {
			throw Caster.toPageException(e);
		}
	}
}