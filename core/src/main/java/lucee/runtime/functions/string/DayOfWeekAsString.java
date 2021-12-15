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
 * Implements the CFML Function dayofweekasstring
 */
package lucee.runtime.functions.string;

import java.util.Date;
import java.util.Locale;

import lucee.commons.date.TimeZoneConstants;
import lucee.commons.i18n.DateFormatPool;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class DayOfWeekAsString extends BIF {

	private static final long serialVersionUID = 4067032942689404733L;
	private static final int DAY = 1000 * 60 * 60 * 24;

	private static Date[] dates = new Date[] { new Date(0 + (3 * DAY)), new Date(0 + (4 * DAY)), new Date(0 + (5 * DAY)), new Date(0 + (6 * DAY)), new Date(0),
			new Date(0 + (1 * DAY)), new Date(0 + (2 * DAY)) };

	public static String call(PageContext pc, double dow) throws ExpressionException {
		return call(pc, dow, pc.getLocale(), true);
	}

	public static String call(PageContext pc, double dow, Locale locale) throws ExpressionException {
		return call(pc, dow, locale == null ? pc.getLocale() : locale, true);
	}

	protected static String call(PageContext pc, double dow, Locale locale, boolean _long) throws ExpressionException {

		int dayOfWeek = (int) dow;
		if (dayOfWeek >= 1 && dayOfWeek <= 7) {
			return DateFormatPool.format(locale, TimeZoneConstants.GMT0, _long ? "EEEE" : "EEE", dates[dayOfWeek - 1]);
		}
		throw new FunctionException(pc, _long ? "DayOfWeekAsString" : "DayOfWeekShortAsString", 1, "dayOfWeek", "must be between 1 and 7 now [" + dayOfWeek + "]");
		// throw new ExpressionException("invalid dayOfWeek definition in function DayOfWeekAsString, must
		// be between 1 and 7 now ["+dayOfWeek+"]");
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toDoubleValue(args[0]));
		if (args.length == 2) return call(pc, Caster.toDoubleValue(args[0]), Caster.toLocale(args[1]));
		throw new FunctionException(pc, "DayOfWeekAsString", 1, 2, args.length);
	}
}