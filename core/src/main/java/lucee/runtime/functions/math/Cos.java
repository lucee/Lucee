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
 * Implements the CFML Function cos
 */
package lucee.runtime.functions.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.op.Caster;

public final class Cos implements Function {
	private static final long serialVersionUID = -6746800530182386158L;
	// MathContext to specify the precision and rounding of big decimal calculations
	private static final MathContext mc = new MathContext(30, RoundingMode.HALF_UP);

	public static double call(PageContext pc, double number) {
		return StrictMath.cos(number);
	}

	public static Number call(PageContext pc, Number number) {
		if (AppListenerUtil.getPreciseMath(pc, null)) {
			return cosine(Caster.toBigDecimal(number));
		}
		return StrictMath.cos(Caster.toDoubleValue(number));
	}

	public static BigDecimal cosine(BigDecimal x) {
		BigDecimal sum = BigDecimal.ZERO;
		BigDecimal term;
		int maxIterations = 100; // More iterations, more precision

		for (int n = 0; n <= maxIterations; n++) {
			BigDecimal numerator = pow(x, 2 * n).multiply(BigDecimal.valueOf(Math.pow(-1, n)), mc);
			BigDecimal denominator = factorial(2 * n);
			term = numerator.divide(denominator, mc);

			sum = sum.add(term, mc);

			// Check if the term is negligible
			if (term.abs().compareTo(new BigDecimal("1E-29", mc)) < 0) {
				break;
			}
		}

		return sum;
	}

	private static BigDecimal pow(BigDecimal base, int exponent) {
		BigDecimal result = BigDecimal.ONE;
		for (int i = 1; i <= exponent; i++) {
			result = result.multiply(base, mc);
		}
		return result;
	}

	private static BigDecimal factorial(int n) {
		BigDecimal result = BigDecimal.ONE;
		for (int i = 2; i <= n; i++) {
			result = result.multiply(BigDecimal.valueOf(i), mc);
		}
		return result;
	}
}