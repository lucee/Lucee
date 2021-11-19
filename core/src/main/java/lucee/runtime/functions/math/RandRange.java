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
 * Implements the CFML Function randrange
 */
package lucee.runtime.functions.math;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;

public final class RandRange implements Function {
	public static double call(PageContext pc, double number1, double number2) throws ExpressionException {
		return call(pc, number1, number2, "cfmx_compat");
	}

	public static double call(PageContext pc, double number1, double number2, String algo) throws ExpressionException {

		int min = (int) number1;
		int max = (int) number2;

		if (number1 > number2) {
			int tmp = min;
			min = max;
			max = tmp;
		}
		int diff = max - min;
		return ((int) (Rand.call(pc, algo) * (diff + 1))) + min;
	}

	public static int invoke(int min, int max) throws ExpressionException {

		if (min > max) {
			int tmp = min;
			min = max;
			max = tmp;
		}
		int diff = max - min;
		return ((int) (Rand.call(null, "cfmx_compat") * (diff + 1))) + min;
	}

}