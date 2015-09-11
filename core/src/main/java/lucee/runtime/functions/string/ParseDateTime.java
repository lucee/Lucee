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
package lucee.runtime.functions.string;

import java.util.TimeZone;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.date.DateCaster;

/**
 * Implements the CFML Function parsedatetime
 */
public final class ParseDateTime implements Function {

	private static final long serialVersionUID = -2623323893206022437L;
	
	public static lucee.runtime.type.dt.DateTime call(PageContext pc , Object oDate) throws PageException {
		return _call(oDate,pc.getTimeZone());
	}
	public static lucee.runtime.type.dt.DateTime call(PageContext pc , Object oDate, String popConversion) throws PageException {
		return _call(oDate,pc.getTimeZone());
	}
	public static lucee.runtime.type.dt.DateTime call(PageContext pc , Object oDate, String popConversion,TimeZone tz) throws PageException {
		return _call(oDate,tz==null?pc.getTimeZone():tz);
	}
	private static lucee.runtime.type.dt.DateTime _call( Object oDate,TimeZone tz) throws PageException {
		return DateCaster.toDateAdvanced(oDate,DateCaster.CONVERTING_TYPE_YEAR,tz);
	}
}