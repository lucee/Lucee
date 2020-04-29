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
 * Implements the CFML Function numberformat
 */
package lucee.runtime.functions.displayFormatting;

import java.util.Locale;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.util.InvalidMaskException;
import lucee.runtime.util.NumberFormat.Mask;

/**
 * Formats a Number by given pattern
 */
public final class NumberFormat implements Function {

	/**
	 * @param pc
	 * @param object
	 * @return formated number
	 * @throws ExpressionException
	 */
	public static String call(PageContext pc, Object object) throws PageException {
		return new lucee.runtime.util.NumberFormat().format(Locale.US, toNumber(pc, object, 0)).replace('\'', ',');
	}

	/**
	 * @param pc
	 * @param object
	 * @param mask
	 * @return formated number
	 * @throws ExpressionException
	 */
	public static String call(PageContext pc, Object object, String mask) throws PageException {
		if (mask == null) return call(pc, object);
		if (mask.equalsIgnoreCase("roman")) {
			return intToRoman(pc, (int) toNumber(pc, object, 0));
		}
		else if (mask.equalsIgnoreCase("hex")) {
			return Integer.toHexString((int) toNumber(pc, object, 0));
		}
		else if (mask.equalsIgnoreCase(",")) {
			return call(pc, object);
		}

		try {
			Mask _mask = lucee.runtime.util.NumberFormat.convertMask(mask);
			return new lucee.runtime.util.NumberFormat().format(Locale.US, toNumber(pc, object, _mask.right), _mask);
		}
		catch (InvalidMaskException e) {
			throw new FunctionException(pc, "numberFormat", 2, "mask", e.getMessage());
		}
	}

	public static double toNumber(PageContext pc, Object object, int digits) throws PageException {
		double d = Caster.toDoubleValue(object, true, Double.NaN);
		if (Decision.isValid(d)) {
			return d;
		}
		String str = Caster.toString(object);
		if (str.length() == 0) return 0;
		throw new FunctionException(pc, "numberFormat", 1, "number", "can't cast value [" + str + "] to a number");
	}

	private static String intToRoman(PageContext pc, int value) throws FunctionException {
		if (value == 0) throw new FunctionException(pc, "numberFormat", 1, "number", "a roman value can't be 0");
		if (value < 0) throw new FunctionException(pc, "numberFormat", 1, "number", "a roman value can't be less than 0");
		if (value > 3999) throw new FunctionException(pc, "numberFormat", 1, "number", "a roman value can't be greater than 3999");

		StringBuilder roman = new StringBuilder();

		while (value / 1000 >= 1) {
			roman.append('M');
			value = value - 1000;
		}
		if (value / 900 >= 1) {
			roman.append("CM");
			value = value - 900;
		}
		if (value / 500 >= 1) {
			roman.append("D");
			value = value - 500;
		}
		if (value / 400 >= 1) {
			roman.append("CD");
			value = value - 400;
		}
		while (value / 100 >= 1) {
			roman.append("C");
			value = value - 100;
		}
		if (value / 90 >= 1) {
			roman.append("XC");
			value = value - 90;
		}
		if (value / 50 >= 1) {
			roman.append("L");
			value = value - 50;
		}
		if (value / 40 >= 1) {
			roman.append("XL");
			value = value - 40;
		}
		while (value / 10 >= 1) {
			roman.append("X");
			value = value - 10;
		}
		if (value / 9 >= 1) {
			roman.append("IX");
			value = value - 9;
		}
		if (value / 5 >= 1) {
			roman.append("V");
			value = value - 5;
		}
		if (value / 4 >= 1) {
			roman.append("IV");
			value = value - 4;
		}
		while (value >= 1) {
			roman.append("I");
			value = value - 1;
		}
		return roman.toString();
	}

}