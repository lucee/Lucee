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
 * Implements the CFML Function dateadd
 */
package lucee.runtime.functions.dateTime;

import java.util.Calendar;
import java.util.TimeZone;

import lucee.commons.date.JREDateTimeUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;

public final class DateAdd extends BIF {
	// do not change this is used in the chart extension
	private static final long serialVersionUID = -5827644560609841341L;

	public static DateTime call(PageContext pc, String datepart, double number, DateTime date) throws ExpressionException {
		return _call(pc, pc.getTimeZone(), datepart, number, date);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 3) throw new FunctionException(pc, "DateAdd", 3, 3, args.length);
		return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toDate(args[2], pc.getTimeZone()));
	}

	public static DateTime _call(PageContext pc, TimeZone tz, String datepart, double number, DateTime date) throws ExpressionException {
		datepart = datepart.toLowerCase();
		long l = (long) number;
		int n = (int) l;
		char first = datepart.length() == 1 ? datepart.charAt(0) : (char) 0;

		if (first == 'l') return new DateTimeImpl(pc, date.getTime() + l, false);
		else if (first == 's') return new DateTimeImpl(pc, date.getTime() + (l * 1000), false);
		else if (first == 'n') return new DateTimeImpl(pc, date.getTime() + (l * 60000), false);
		else if (first == 'h') return new DateTimeImpl(pc, date.getTime() + (l * 3600000), false);

		Calendar c = JREDateTimeUtil.getThreadCalendar();
		// if (c == null)c=JREDateTimeUtil.newInstance();
		// synchronized (c) {
		// c.clear();
		c.setTimeZone(tz);
		c.setTimeInMillis(date.getTime());

		if (datepart.equals("yyyy")) {
			c.set(Calendar.YEAR, c.get(Calendar.YEAR) + n);
		}
		else if (datepart.equals("ww")) c.add(Calendar.WEEK_OF_YEAR, n);
		else if (first == 'q') c.add(Calendar.MONTH, (n * 3));
		else if (first == 'm') c.add(Calendar.MONTH, n);
		else if (first == 'y') c.add(Calendar.DAY_OF_YEAR, n);
		else if (first == 'd') c.add(Calendar.DATE, n);
		else if (first == 'w') {
			int dow = c.get(Calendar.DAY_OF_WEEK);
			int offset;
			// -
			if (n < 0) {
				if (Calendar.SUNDAY == dow) offset = 2;
				else offset = -(6 - dow);
			}
			// +
			else {
				if (Calendar.SATURDAY == dow) offset = -2;
				else offset = dow - 2;
			}
			c.add(Calendar.DAY_OF_WEEK, -offset);

			if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) {
				if (n > 0) n--;
				else if (n < 0) n++;
			}
			else n += offset;
			c.add(Calendar.DAY_OF_WEEK, (n / 5) * 7 + n % 5);

		}

		else {
			throw new ExpressionException("invalid datepart identifier [" + datepart + "] for function dateAdd");
		}
		return new DateTimeImpl(pc, c.getTimeInMillis(), false);
		// }
	}
}