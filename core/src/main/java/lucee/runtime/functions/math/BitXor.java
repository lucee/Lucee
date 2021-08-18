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
 * Implements the CFML Function bitxor
 */
package lucee.runtime.functions.math;

import lucee.runtime.PageContext;
import lucee.runtime.op.Decision;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;

public final class BitXor implements Function {
	public static double call(PageContext pc, double number, double number2) throws FunctionException {
		if (!Decision.isInteger(number)) throw new FunctionException(pc, "bitXor", 1, "number1", "value [" + number + "] must be between the integer range");
		if (!Decision.isInteger(number2)) throw new FunctionException(pc, "bitXor", 1, "number2", "value [" + number + "] must be between the integer range");
		return (int) number ^ (int) number2;
	}
}