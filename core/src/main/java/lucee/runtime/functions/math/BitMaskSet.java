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
 * Implements the CFML Function bitmaskset
 */
package lucee.runtime.functions.math;

import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public final class BitMaskSet implements Function {

	private static final long serialVersionUID = -6113955054602192041L;

	public static Number call(PageContext pc, Number number, Number mask, Number start, Number length) throws FunctionException {

		// Convert the input numbers to integers for bitwise operations
		int numValue = Caster.toInteger(number);
		int maskValue = Caster.toInteger(mask);
		int startValue = Caster.toInteger(start);
		int lengthValue = Caster.toInteger(length);

		// Validation checks
		if (!Decision.isInteger(number)) {
			throw new FunctionException(pc, "bitMaskSet", 1, "number", "value [" + number + "] must be an integer.");
		}
		if (startValue > 31 || startValue < 0) {
			throw new FunctionException(pc, "bitMaskSet", 2, "start", "must be between 0 and 31, now " + startValue);
		}
		if (lengthValue > 31 || lengthValue < 0) {
			throw new FunctionException(pc, "bitMaskSet", 3, "length", "must be between 0 and 31, now " + lengthValue);
		}

		// Perform bitwise set operation
		int tmp = (1 << lengthValue) - 1 << startValue;
		maskValue &= (1 << lengthValue) - 1;
		int result = numValue & ~tmp | maskValue << startValue;

		if (ThreadLocalPageContext.preciseMath(pc)) {
			return Caster.toBigDecimal(result);
		}
		return result;
	}
}
