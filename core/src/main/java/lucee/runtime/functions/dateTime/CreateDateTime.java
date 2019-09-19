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
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTime;

public final class CreateDateTime extends BIF {

	private static final long serialVersionUID = 2158994510749730985L;

	public static DateTime call(PageContext pc, double year) throws ExpressionException {
		return _call(pc, (int) year, 1, 1, 0, 0, 0, 0, pc.getTimeZone());
	}

	public static DateTime call(PageContext pc, double year, double month) throws ExpressionException {
		return _call(pc, (int) year, (int) month, 1, 0, 0, 0, 0, pc.getTimeZone());
	}

	public static DateTime call(PageContext pc, double year, double month, double day) throws ExpressionException {
		return _call(pc, (int) year, (int) month, (int) day, 0, 0, 0, 0, pc.getTimeZone());
	}

	public static DateTime call(PageContext pc, double year, double month, double day, double hour) throws ExpressionException {
		return _call(pc, (int) year, (int) month, (int) day, (int) hour, 0, 0, 0, pc.getTimeZone());
	}

	public static DateTime call(PageContext pc, double year, double month, double day, double hour, double minute) throws ExpressionException {
		return _call(pc, (int) year, (int) month, (int) day, (int) hour, (int) minute, 0, 0, pc.getTimeZone());
	}

	public static DateTime call(PageContext pc, double year, double month, double day, double hour, double minute, double second) throws ExpressionException {
		return _call(pc, (int) year, (int) month, (int) day, (int) hour, (int) minute, (int) second, 0, pc.getTimeZone());
	}

	public static DateTime call(PageContext pc, double year, double month, double day, double hour, double minute, double second, double millis) throws ExpressionException {
		return _call(pc, (int) year, (int) month, (int) day, (int) hour, (int) minute, (int) second, (int) millis, pc.getTimeZone());
	}

	public static DateTime call(PageContext pc, double year, double month, double day, double hour, double minute, double second, double millis, TimeZone tz)
			throws ExpressionException {
		return _call(pc, (int) year, (int) month, (int) day, (int) hour, (int) minute, (int) second, (int) millis, tz == null ? pc.getTimeZone() : tz);
	}

	private static DateTime _call(PageContext pc, int year, int month, int day, int hour, int minute, int second, int millis, TimeZone tz) throws ExpressionException {
		return DateTimeUtil.getInstance().toDateTime(tz, year, month, day, hour, minute, second, millis);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 8) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), Caster.toIntValue(args[3]),
				Caster.toIntValue(args[4]), Caster.toIntValue(args[5]), Caster.toIntValue(args[6]), Caster.toTimeZone(args[7]));
		if (args.length == 7) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), Caster.toIntValue(args[3]),
				Caster.toIntValue(args[4]), Caster.toIntValue(args[5]), Caster.toIntValue(args[6]), pc.getTimeZone());
		if (args.length == 6) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), Caster.toIntValue(args[3]),
				Caster.toIntValue(args[4]), Caster.toIntValue(args[5]), 0, pc.getTimeZone());
		if (args.length == 5) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), Caster.toIntValue(args[3]),
				Caster.toIntValue(args[4]), 0, 0, pc.getTimeZone());
		if (args.length == 4)
			return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), Caster.toIntValue(args[3]), 0, 0, 0, pc.getTimeZone());
		if (args.length == 3) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), Caster.toIntValue(args[2]), 0, 0, 0, 0, pc.getTimeZone());
		if (args.length == 2) return _call(pc, Caster.toIntValue(args[0]), Caster.toIntValue(args[1]), 1, 0, 0, 0, 0, pc.getTimeZone());
		if (args.length == 1) return _call(pc, Caster.toIntValue(args[0]), 1, 1, 0, 0, 0, 0, pc.getTimeZone());
		throw new FunctionException(pc, "CreateDateTime", 1, 8, args.length);
	}
}