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
 * Implements the CFML Function gettickcount
 */
package lucee.runtime.functions.other;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;

public final class GetTickCount implements Function {

	private static final long serialVersionUID = 678332662578928144L;

	public static double UNIT_NANO = 1;
	public static double UNIT_MILLI = 2;
	public static double UNIT_MICRO = 4;
	public static double UNIT_SECOND = 8;

	public static double call(PageContext pc) {
		return System.currentTimeMillis();
	}

	public static double call(PageContext pc, String unit) throws FunctionException {
		if (!StringUtil.isEmpty(unit, true)) {
			unit = unit.trim();
			char c = unit.charAt(0);

			if (c == 'n' || c == 'N') return System.nanoTime();
			else if (c == 'm' || c == 'M') {
				if ("micro".equalsIgnoreCase(unit)) return System.nanoTime() / 1000;
				return System.currentTimeMillis();
			}
			else if (c == 's' || c == 'S') return System.currentTimeMillis() / 1000;
		}

		throw new FunctionException(pc, "GetTickCount", 1, "unit", "invalid value [" + unit + "], valid values are (nano, micro, milli, second)");
	}

	// this function is only called when the evaluator validates the unit definition on compilation time
	public static double call(PageContext pc, double unit) {
		if (UNIT_NANO == unit) return System.nanoTime();
		if (UNIT_MICRO == unit) return System.nanoTime() / 1000;
		if (UNIT_MILLI == unit) return System.currentTimeMillis();
		return System.currentTimeMillis() / 1000;
	}
}