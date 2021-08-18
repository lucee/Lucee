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
import lucee.runtime.op.Decision;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;

public final class BitMaskSet implements Function {

	public static double call(PageContext pc, double dnumber, double dmask, double dstart, double dlength) throws FunctionException {

		int number = (int) dnumber, mask = (int) dmask, start = (int) dstart, length = (int) dlength;

		if(!Decision.isInteger(dnumber)) throw new FunctionException(pc, "bitMaskSet", 1, "number", "value [" + dnumber + "] must be between the integer range");
		if (start > 31 || start < 0) throw new FunctionException(pc, "bitMaskSet", 2, "start", "must be between 0 and 31 now " + start);
		if (length > 31 || length < 0) throw new FunctionException(pc, "bitMaskSet", 3, "length", "must be between 0 and 31 now " + length);

		int tmp = (1 << length) - 1 << start;
		mask &= (1 << length) - 1;
		return number & ~tmp | mask << start;
	}
}