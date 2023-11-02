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
 * Implements the CFML Function insert
 */
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class Insert extends BIF {

	private static final long serialVersionUID = 5926183314989306282L;

	public static String call(PageContext pc, String sub, String str, double pos) throws ExpressionException {
		int p = (int) pos;
		if (p < 0 || p > str.length()) throw new ExpressionException("third parameter of the function insert, must be between 0 and " + str.length() + " now [" + (p) + "]");
		StringBuilder sb = new StringBuilder(str.length() + sub.length());

		return sb.append(str.substring(0, p)).append(sub).append(str.substring(p)).toString();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]));

		throw new FunctionException(pc, "Insert", 2, 3, args.length);
	}
}