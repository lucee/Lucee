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
 * Implements the CFML Function createdatetime
 */
package lucee.runtime.functions.dateTime;

import java.util.TimeZone;

import lucee.commons.date.DateTimeUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.dt.DateTime;

public final class CreateDateTime implements Function {

	public static DateTime call(PageContext pc , double year, double month, double day, double hour, double minute, double second) throws ExpressionException {
		return _call(pc,year,month,day,hour,minute,second,0,pc.getTimeZone());
	}
	public static DateTime call(PageContext pc , double year, double month, double day, double hour, double minute, double second,double millis) throws ExpressionException {
		return _call(pc,year,month,day,hour,minute,second,millis,pc.getTimeZone());
	}
	public static DateTime call(PageContext pc , double year, double month, double day, double hour, double minute, double second,double millis,TimeZone tz) throws ExpressionException {
		return _call(pc,year,month,day,hour,minute,second,millis,tz==null?pc.getTimeZone():tz);
	}
	private static DateTime _call(PageContext pc , double year, double month, double day, double hour, double minute, double second,double millis,TimeZone tz) throws ExpressionException {
		return DateTimeUtil.getInstance().toDateTime(tz,(int)year,(int)month,(int)day,(int)hour,(int)minute,(int)second,(int)millis);
	}
}