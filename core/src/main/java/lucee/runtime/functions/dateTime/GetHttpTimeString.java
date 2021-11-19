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
 * Implements the CFML Function gethttptimestring
 */
package lucee.runtime.functions.dateTime;

import lucee.commons.date.DateTimeUtil;
import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;

public final class GetHttpTimeString implements Function {

	public static String call(PageContext pc) {
		return DateTimeUtil.toHTTPTimeString(new DateTimeImpl(pc), true);
	}

	public static String call(PageContext pc, DateTime datetime) {
		return DateTimeUtil.toHTTPTimeString(datetime == null ? new DateTimeImpl(pc) : datetime, true);
	}

	public static String invoke(DateTime datetime) {
		return DateTimeUtil.toHTTPTimeString(datetime, true);
	}

	public static String invoke() {
		return call(null);
	}
}