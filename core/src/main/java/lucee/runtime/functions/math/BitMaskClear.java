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
 * Implements the CFML Function bitmaskclear
 */
package lucee.runtime.functions.math;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;

public final class BitMaskClear implements Function {
	public static double call(PageContext pc, double dnumber, double dstart, double dlength) throws FunctionException {

		int number = (int) dnumber, start = (int) dstart, length = (int) dlength;

		if (start > 31 || start < 0) throw new FunctionException(pc, "bitMaskClear", 2, "start", "must be beetween 0 and 31 now " + start);
		if (length > 31 || length < 0) throw new FunctionException(pc, "bitMaskClear", 3, "length", "must be beetween 0 and 31 now " + length);

		return number & ~((1 << length) - 1 << start);
	}
}