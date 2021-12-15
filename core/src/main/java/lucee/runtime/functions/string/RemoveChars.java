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
 * Implements the CFML Function removechars
 */
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class RemoveChars extends BIF {
	public static String call(PageContext pc, String str, double s, double l) throws ExpressionException {
		int start = (int) s;
		int length = (int) l;
		int strLength = str.length();

		// check param 2
		if (start < 1 || start > strLength)
			throw new ExpressionException("Parameter 2 of function removeChars which is now [" + start + "] must be a greater 0 and less than the length of the first parameter");

		// check param 3
		if (length < 0) throw new ExpressionException("Parameter 3 of function removeChars which is now [" + length + "] must be a non-negative integer");

		if (strLength == 0) return "";

		String rtn = str.substring(0, start - 1);

		if (start + length <= strLength) rtn += str.substring(start + length - 1);
		return rtn;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]));

		throw new FunctionException(pc, "RemoveChars", 3, 3, args.length);
	}
}