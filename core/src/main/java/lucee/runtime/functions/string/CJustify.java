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
 * Implements the CFML Function cjustify
 */
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class CJustify extends BIF {

	private static final long serialVersionUID = -4145093552477680411L;

	public static String call(PageContext pc, String string, double length) throws ExpressionException {
		int len = (int) length;
		if (len < 1) throw new ExpressionException("Parameter 2 of function cJustify which is now [" + len + "] must be a non-negative integer");
		else if ((len -= string.length()) <= 0) return string;
		else {

			char[] chrs = new char[string.length() + len];
			int part = len / 2;

			for (int i = 0; i < part; i++)
				chrs[i] = ' ';
			for (int i = string.length() - 1; i >= 0; i--)
				chrs[part + i] = string.charAt(i);
			for (int i = part + string.length(); i < chrs.length; i++)
				chrs[i] = ' ';

			return new String(chrs);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]));
		throw new FunctionException(pc, "CJustify", 2, 2, args.length);
	}
}