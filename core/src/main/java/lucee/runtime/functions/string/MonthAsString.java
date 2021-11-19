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
 * Implements the CFML Function monthasstring
 */
package lucee.runtime.functions.string;

import java.text.DateFormatSymbols;
import java.util.Locale;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;

public final class MonthAsString implements Function {

	public static String call(PageContext pc, double month) throws ExpressionException {
		return call(month, pc.getLocale(), false);
	}

	public static String call(PageContext pc, double month, Locale locale) throws ExpressionException {
		return call(month, locale == null ? pc.getLocale() : locale, false);
	}

	protected static String call(double month, Locale locale, boolean _short) throws ExpressionException {
		int m = (int) month;
		if (m >= 1 && m <= 12) {
			DateFormatSymbols dfs = new DateFormatSymbols(locale);
			String[] months = _short ? dfs.getShortMonths() : dfs.getMonths();
			return months[m - 1];
		}
		throw new ExpressionException("invalid month definition in function monthAsString, must be between 1 and 12 now [" + month + "]");

	}
}