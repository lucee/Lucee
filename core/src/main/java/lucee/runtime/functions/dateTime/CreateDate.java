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
 * Implements the CFML Function createdate
 */
package lucee.runtime.functions.dateTime;

import java.util.TimeZone;

import lucee.commons.date.DateTimeUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.dt.DateTime;

public final class CreateDate implements Function {

	private static final long serialVersionUID = -8116641467358905335L;

	public static DateTime call(PageContext pc, double year) throws ExpressionException {
		return _call(pc, year, 1, 1, pc.getTimeZone());
	}

	public static DateTime call(PageContext pc, double year, double month) throws ExpressionException {
		return _call(pc, year, month, 1, pc.getTimeZone());
	}

	public static DateTime call(PageContext pc, double year, double month, double day) throws ExpressionException {
		return _call(pc, year, month, day, pc.getTimeZone());
	}

	public static DateTime call(PageContext pc, double year, double month, double day, TimeZone tz) throws ExpressionException {
		return _call(pc, year, month, day, tz == null ? pc.getTimeZone() : tz);
	}

	private static DateTime _call(PageContext pc, double year, double month, double day, TimeZone tz) throws ExpressionException {
		return DateTimeUtil.getInstance().toDateTime(tz, (int) year, (int) month, (int) day, 0, 0, 0, 0);
	}
}