/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
/**
 * Implements the CFML Function datepart
 */
package lucee.runtime.functions.international;

import java.util.Locale;
import java.util.TimeZone;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.dateTime.Day;
import lucee.runtime.functions.dateTime.Hour;
import lucee.runtime.functions.dateTime.MilliSecond;
import lucee.runtime.functions.dateTime.Minute;
import lucee.runtime.functions.dateTime.Month;
import lucee.runtime.functions.dateTime.Quarter;
import lucee.runtime.functions.dateTime.Second;
import lucee.runtime.functions.dateTime.Week;
import lucee.runtime.functions.dateTime.Year;
import lucee.runtime.type.dt.DateTime;

public final class LSDatePart implements Function {

	private static final long serialVersionUID = -4203375459570986511L;

	public static double call(PageContext pc, String datepart, DateTime date) throws ExpressionException {
		return call(pc, datepart, date, null, null);
	}

	public static double call(PageContext pc, String datepart, DateTime date, Locale locale) throws ExpressionException {
		return call(pc, datepart, date, locale, null);
	}

	public static double call(PageContext pc, String datepart, DateTime date, Locale locale, TimeZone tz) throws ExpressionException {
		datepart = datepart.toLowerCase();
		char first = datepart.length() == 1 ? datepart.charAt(0) : (char) 0;

		if (datepart.equals("yyyy")) return Year.call(pc, date, tz);
		else if (datepart.equals("ww")) return Week.call(pc, date, tz);
		else if (first == 'w') return LSDayOfWeek.call(pc, date, locale, tz);
		else if (first == 'q') return Quarter.call(pc, date, tz);
		else if (first == 'm') return Month.call(pc, date, tz);
		else if (first == 'y') return LSDayOfYear.call(pc, date, locale, tz);
		else if (first == 'd') return Day.call(pc, date, tz);
		else if (first == 'h') return Hour.call(pc, date, tz);
		else if (first == 'n') return Minute.call(pc, date, tz);
		else if (first == 's') return Second.call(pc, date, tz);
		else if (first == 'l') return MilliSecond.call(pc, date, tz);
		throw new ExpressionException("invalid datepart type [" + datepart + "] for function datePart");
	}
}