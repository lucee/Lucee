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
 * Implements the CFML Function right
 */
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class Right extends BIF {

	private static final long serialVersionUID = 2270997683293984478L;

	public static String call(PageContext pc, String str, double number) throws ExpressionException {
		int len = (int) number;
		if (len == 0) throw new ExpressionException("parameter 2 of the function right can not be 0");
		int l = str.length();
		if (Math.abs(len) >= l) return str;
		if (len < 0) len = l + len;
		return str.substring(l - len, l);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]));
		throw new FunctionException(pc, "Right", 2, 2, args.length);
	}
}