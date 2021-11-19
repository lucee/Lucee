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
 * Implements the CFML Function dateconvert
 */
package lucee.runtime.functions.dateTime;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;

public final class DateConvert implements Function {
	public static DateTime call(PageContext pc, String conversionType, DateTime date) throws FunctionException {

		// throw new ApplicationException("This function is no longer supported, because it gives you the
		// wrong impression that the timezone is part of the date object, what is wrong!" +
		// "When you wanna convert a Date to String based on the UTC timezone, do for example
		// [DateTimeFormat(date:now(),timezone:'UTC')].");

		int offset = pc.getTimeZone().getOffset(date.getTime());
		conversionType = conversionType.toLowerCase();

		if (conversionType.equals("local2utc")) {
			return new DateTimeImpl(pc, date.getTime() - offset, false);
		}
		else if (conversionType.equals("utc2local")) {
			return new DateTimeImpl(pc, date.getTime() + offset, false);
		}
		throw new FunctionException(pc, "DateConvert", 1, "conversionType", "invalid conversion-type [" + conversionType + "] for function dateConvert");
	}
}