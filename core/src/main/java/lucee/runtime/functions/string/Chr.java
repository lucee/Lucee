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
 * Implements the CFML Function chr
 */
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class Chr extends BIF {

	private static final long serialVersionUID = -4941617303146860984L;

	public static String call(PageContext pc, double number) throws ExpressionException {
		int value = (int) number;
		if (value < 1) {
			if (value == 0) return "";
			// else {
			throw new ExpressionException("Parameter 1 of function chr which is now [" + value + "] must be a non-negative integer");
			// }
		}
		return "" + (char) value;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toDoubleValue(args[0]));
		throw new FunctionException(pc, "chr", 1, 1, args.length);
	}
}