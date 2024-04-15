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
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

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

	public static BigDecimal pow(BigDecimal left, int right) {
		if (right < 0) return left.pow(right, MathContext.DECIMAL128); // negative exponent always throws
		try {
			return left.pow(right, MathContext.UNLIMITED);
		}
		catch (ArithmeticException ex) {
			return left.pow(right, MathContext.DECIMAL128);
		}
	}

	/**
	 * Converts a BigDecimal to a BigInteger based on a specified threshold for rounding. The method
	 * rounds the given BigDecimal to the nearest whole number and checks if the rounded value is within
	 * a certain precision threshold of the original number. If it is not, an exception is thrown.
	 *
	 * @param decimal The BigDecimal to convert to BigInteger.
	 * @param threshold The negative power of ten that defines the precision threshold for rounding. For
	 *            example, a threshold of 10 means the difference must be less than 1E-10.
	 * @return BigInteger The rounded BigInteger if the original decimal is within the specified
	 *         threshold.
	 * @throws ArithmeticException If the decimal is not close enough to an integer within the specified
	 *             threshold.
	 * @throws CasterException If the threshold is negative, as a negative threshold does not make
	 *             sense.
	 */
	public static BigInteger roundToBigInteger(BigDecimal decimal, int threshold) throws ArithmeticException, CasterException {
		if (threshold < 0) {
			throw new IllegalArgumentException("Threshold must be a non-negative value.");
		}

		// Round to the nearest whole number
		BigDecimal rounded = decimal.setScale(0, RoundingMode.HALF_UP);

		// If the difference is less than the threshold, it is close enough to round
		if (decimal.subtract(rounded).abs().compareTo(new BigDecimal("1E-" + threshold)) < 0) {
			return rounded.toBigInteger();
		}
		else {
			// Otherwise, throw an exception as the number is not close enough to an integer
			throw new CasterException(String.format("The value [" + Caster.toString(decimal)
					+ "] cannot be converted to long without significant data loss. The decimal value %s is not close enough to any integer. Values must be within %s of an integer to round.",
					decimal.toPlainString(), new BigDecimal("1E-" + threshold).toPlainString()));
		}
	}

	public static BigInteger roundToBigInteger(Object val) throws ArithmeticException, PageException {
		return roundToBigInteger(Caster.toBigDecimal(val), 12);
	}
}