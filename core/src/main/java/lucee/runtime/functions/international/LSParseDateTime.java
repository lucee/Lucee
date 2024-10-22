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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lucee.print;
import lucee.commons.date.TimeZoneConstants;
import lucee.commons.date.TimeZoneUtil;
import lucee.commons.i18n.FormatUtil;
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
		return _call(pc, oDate, ThreadLocalPageContext.getLocale(pc), ThreadLocalPageContext.getTimeZone(pc), null);
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

		String strDate = StringUtil.replaceSpecialWhiteSpace(Caster.toString(oDate));

		// regular parse date time
		if (StringUtil.isEmpty(format, true)) return DateCaster.toDateTime(locale, strDate, tz, locale.equals(Locale.US));

		// with java based format
		tz = ThreadLocalPageContext.getTimeZone(tz);
		if (locale == null) locale = pc.getLocale();
		DateFormat df = FormatUtil.getDateTimeFormat(locale, tz, format);
		try {
			return new DateTimeImpl(df.parse(strDate));
		}
		catch (ParseException e) {
			return new DateTimeImpl(FormatUtil.parse(FormatUtil.getDateTimeFormatter(locale, format), strDate, tz));
			// throw Caster.toPageException(e);
		}
		// return new DateTimeImpl(FormatUtil.parse(FormatUtil.getDateTimeFormatter(locale, format),
		// strDate, tz));
	}

	public static void main(String[] args) throws PageException {
		print.e(_call(null, "2022-01-02T11:22:33+01:00", Locale.ENGLISH, TimeZoneConstants.UTC, null));
		print.e(_call(null, "2022-01-02T11:22:33.444+01:00", Locale.ENGLISH, TimeZoneConstants.UTC, null));
		// print.e(_call(null, "1/30/02 7:02:33", Locale.ENGLISH, TimeZoneConstants.UTC, "m/dd/yy
		// h:nn:ss"));
	}
}