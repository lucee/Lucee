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
 * Implements the CFML Function mid
 */
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class Mid extends BIF {
	public static String call(PageContext pc, String str, double start) throws ExpressionException {
		return call(pc, str, start, -1);
	}

	public static String call(PageContext pc, String str, double start, double count) throws ExpressionException {
		int s = (int) (start - 1);
		int c = (int) count;

		if (s < 0) throw new ExpressionException("Parameter 2 of function mid which is now [" + (s + 1) + "] must be a positive integer");
		if (c == -1) c = str.length();
		else if (c < -1) throw new ExpressionException("Parameter 3 of function mid which is now [" + c + "] must be a non-negative integer or -1 (for string length)");
		c += s;
		if (s > str.length()) return "";
		else if (c >= str.length()) return str.substring(s);
		else {
			return str.substring(s, c);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]));

		throw new FunctionException(pc, "Mid", 2, 3, args.length);
	}
}