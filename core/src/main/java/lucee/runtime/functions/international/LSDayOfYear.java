/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
/**
 * Implements the CFML Function dayofyear
 */
package lucee.runtime.functions.international;

import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.date.DateTimeUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTime;

public final class LSDayOfYear extends BIF {

	private static final long serialVersionUID = 8136302798735384757L;

	public static double call(PageContext pc, DateTime date) {
		return _call(pc, date, null, null);
	}

	public static double call(PageContext pc, DateTime date, Locale locale) {
		return _call(pc, date, locale, null);
	}

	public static double call(PageContext pc, DateTime date, Locale locale, TimeZone tz) {
		return _call(pc, date, locale == null ? pc.getLocale() : locale, tz == null ? pc.getTimeZone() : tz);
	}

	private static double _call(PageContext pc, DateTime date, Locale l, TimeZone tz) {
		return DateTimeUtil.getInstance().getDayOfYear(l, tz, date);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 3) throw new FunctionException(pc, "LSDayOfYear", 1, 3, args.length);

		DateTime dt = Caster.toDatetime(args[0], pc.getTimeZone());

		if (args.length == 1) return call(pc, dt);
		if (args.length == 2) return call(pc, dt, Caster.toLocale(args[1]));
		return call(pc, dt, Caster.toLocale(args[1]), Caster.toTimeZone(args[2]));

	}
}