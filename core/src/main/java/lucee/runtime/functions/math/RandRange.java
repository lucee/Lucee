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
 */
package lucee.runtime.functions.math;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class RandRange implements Function {
	private static final long serialVersionUID = 2380288324240209290L;

	public static Number call(PageContext pc, Number number1, Number number2) throws ExpressionException {
		return call(pc, number1, number2, "cfmx_compat");
	}

	public static Number call(PageContext pc, Number number1, Number number2, String algo) throws ExpressionException {

		int min = Caster.toIntValue(number1);
		int max = Caster.toIntValue(number2);

		if (min > max) {
			int tmp = min;
			min = max;
			max = tmp;
		}
		int diff = max - min;
		return (Caster.toIntValue(Rand.call(pc, algo)) * (diff + 1)) + min;
	}

	public static int invoke(int min, int max) throws ExpressionException {

		if (min > max) {
			int tmp = min;
			min = max;
			max = tmp;
		}
		int diff = max - min;
		return (Caster.toIntValue(Rand.call(null, "cfmx_compat")) * (diff + 1)) + min;
	}

}