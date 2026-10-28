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
import lucee.runtime.crypt.Cryptor;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class RandRange implements Function {
	private static final long serialVersionUID = 2380288324240209290L;

	public static Number call(PageContext pc, Number number1, Number number2) throws ExpressionException {
		return call(pc, number1, number2, Cryptor.DEFAULT_ALGORITHM);
	}

	public static Number call(PageContext pc, Number number1, Number number2, String algo) throws ExpressionException {

		// LDEV-25 calculate with long, an int overflows for ranges like 0 to 2^31-1
		long min = Caster.toLongValue(number1);
		long max = Caster.toLongValue(number2);

		if (min > max) {
			long tmp = min;
			min = max;
			max = tmp;
		}
		double span = ((double) max - (double) min) + 1D;
		long result = min + (long) (Rand.getRandom(pc, algo, Double.NaN).nextDouble() * span);
		// double precision can round up for very large ranges
		if (result > max) result = max;
		else if (result < min) result = min;

		// keep returning an int where it fits
		if (result >= Integer.MIN_VALUE && result <= Integer.MAX_VALUE) return Integer.valueOf((int) result);
		return Long.valueOf(result);
	}

	public static int invoke(int min, int max) throws ExpressionException {

		if (min > max) {
			int tmp = min;
			min = max;
			max = tmp;
		}
		int diff = max - min;
		return (Caster.toIntValue(Rand.call(null, Cryptor.DEFAULT_ALGORITHM)) * (diff + 1)) + min;
	}

}