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
package lucee.runtime.functions.dateTime;

import java.util.TimeZone;

import lucee.commons.date.DateTimeUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;

public final class Beat implements Function {
	private static final double day = 86400000;
	private static final TimeZone BMD = TimeZone.getTimeZone("GMT+1");

	public static double call(PageContext pc) throws PageException {
		return call(pc, null);
	}

	public static double call(PageContext pc, Object obj) throws PageException {
		if (obj == null) obj = new DateTimeImpl(pc);

		TimeZone tz = ThreadLocalPageContext.getTimeZone(pc);
		DateTime date = DateCaster.toDateAdvanced(obj, tz);
		return format(date.getTime());
	}

	public static double format(long time) {

		long millisInDay = DateTimeUtil.getInstance().getMilliSecondsInDay(BMD, time);
		double res = (millisInDay / day) * 1000;
		return ((int) (res * 1000)) / 1000D;
	}
}