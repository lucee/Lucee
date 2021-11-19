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
package lucee.commons.math;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Math Util
 */
public final class MathUtil {

	/**
	 * abs
	 * 
	 * @param number
	 * @return abs value
	 */
	public static double abs(double number) {
		return (number <= 0.0D) ? 0.0D - number : number;
	}

	public static double sgn(double number) {
		return number != 0.0d ? number >= 0.0d ? 1 : -1 : 0;
	}

	public static int nextPowerOf2(int value) {

		int result = 1;
		while (result < value)
			result = result << 1;

		return result;
	}

	public static BigDecimal divide(BigDecimal left, BigDecimal right) {
		try {
			return left.divide(right, BigDecimal.ROUND_UNNECESSARY);
		}
		catch (ArithmeticException ex) {
			return left.divide(right, MathContext.DECIMAL128);
		}
	}

	public static BigDecimal add(BigDecimal left, BigDecimal right) {
		try {
			return left.add(right, MathContext.UNLIMITED);
		}
		catch (ArithmeticException ex) {
			return left.add(right, MathContext.DECIMAL128);
		}
	}

	public static BigDecimal subtract(BigDecimal left, BigDecimal right) {
		try {
			return left.subtract(right, MathContext.UNLIMITED);
		}
		catch (ArithmeticException ex) {
			return left.subtract(right, MathContext.DECIMAL128);
		}
	}

	public static BigDecimal multiply(BigDecimal left, BigDecimal right) {
		try {
			return left.multiply(right, MathContext.UNLIMITED);
		}
		catch (ArithmeticException ex) {
			return left.multiply(right, MathContext.DECIMAL128);
		}
	}
}