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
package lucee.runtime.functions.math;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;

/**
 * Implements the CFML Function inputbasen
 */
public final class InputBaseN implements Function {
	public static double call(PageContext pc, String string, double radix) throws ExpressionException {
		if (radix < 2 || radix > 36) throw new FunctionException(pc, "inputBaseN", 2, "radix", "radix must be between 2 an 36");

		string = string.trim().toLowerCase();
		if (string.startsWith("0x")) string = string.substring(2, string.length());

		if (string.length() > 32) throw new FunctionException(pc, "inputBaseN", 1, "string", "argument is too large, it can only be a maximum of 32 digits (-0x at start)");

		// print.ln(string+"-"+radix);
		return (double) Long.parseLong(string, (int) radix);

	}

}
