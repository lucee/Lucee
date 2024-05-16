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
 * Implements the CFML Function formatbasen
 */
package lucee.runtime.functions.displayFormatting;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class FormatBaseN implements Function {
	private static final long uint32_mask = 0x0000_0000_FFFF_FFFFL;

	public static String call(PageContext pc, double number, double radix) throws ExpressionException {
		if (radix < 2 || radix > 36) throw new FunctionException(pc, "formatBaseN", 2, "radix", "radix must be between 2 an 36");

		// LDEV-3776
		// Adobe compat - only support values in the range of a signed int32, and for base 2 and 16 mask away the high 32 bits
		// By masking away the most-significant digits we stringify the raw "unsigned" 2's complement bitwise representation of the number
		final long converted = Caster.toLongValue(number);
		if (converted < Integer.MIN_VALUE || converted > Integer.MAX_VALUE) {
			throw new FunctionException(pc, "formatBaseN", 1, "number", "number to formatted must be on or between Integer.MIN_VALUE and Integer.MAX_VALUE (" + Integer.MIN_VALUE + ", " + Integer.MAX_VALUE + ")");
		}
		return radix == 2 || radix == 16
			? Long.toString(converted & uint32_mask, (int) radix)
			: Long.toString(converted, (int) radix);
	}
}