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
package lucee.runtime.functions.displayFormatting;

import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.date.TimeZoneUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTime;

/**
 * Implements the CFML Function dateformat
 */
public final class DateTimeFormatClassic implements Function {

	private static final long serialVersionUID = 134840879454373440L;

	/**
	 * @param pc
	 * @param object
	 * @return Formated Time Object as String
	 * @throws ExpressionException
	 */
	public static String call(PageContext pc, Object object) throws ExpressionException {
		return _call(pc, object, "dd-mmm-yy hh:nn tt", ThreadLocalPageContext.getTimeZone(pc));
	}

	/**
	 * @param pc
	 * @param object
	 * @param mask Characters that show how CFML displays a date:
	 * @return Formated Time Object as String
	 * @throws ExpressionException
	 */
	public static String call(PageContext pc, Object object, String mask) throws ExpressionException {
		return _call(pc, object, mask, ThreadLocalPageContext.getTimeZone(pc));
	}

	public static String call(PageContext pc, Object object, String mask, String strTimezone) throws ExpressionException {
		return _call(pc, object, mask, strTimezone == null ? ThreadLocalPageContext.getTimeZone(pc) : TimeZoneUtil.toTimeZone(strTimezone));
	}

	private static String _call(PageContext pc, Object object, String mask, TimeZone tz) throws ExpressionException {
		Locale locale = Locale.US;// :pc.getConfig().getLocale();
		DateTime datetime = Caster.toDate(object, true, tz, null);
		if (datetime == null) {
			if (object.toString().trim().length() == 0) return "";
			throw new ExpressionException("Can't convert value [" + object + "] to a datetime value");
		}

		return new lucee.runtime.format.DateTimeFormat(locale).format(datetime, mask, tz);
		// return new lucee.runtime.text.TimeFormat(locale).format(datetime,mask);
	}
}
