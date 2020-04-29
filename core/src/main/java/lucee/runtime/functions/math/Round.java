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
 * Implements the CFML Function round
 */
package lucee.runtime.functions.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;

public final class Round implements Function {

	private static final long serialVersionUID = 3955271203445975609L;

	public static double call(PageContext pc, double number) {
		return call(pc, number, 0);
	}

	public static double call(PageContext pc, double number, double precision) {
		if (precision <= 0) return StrictMath.round(number);

		BigDecimal bd = new BigDecimal(Double.toString(number));
		bd = bd.setScale((int) precision, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}