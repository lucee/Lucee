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
 * Implements the CFML Function repeatstring
 */
package lucee.runtime.functions.string;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class RepeatString extends BIF {

	private static final long serialVersionUID = 6041471441971348584L;

	public static String call(PageContext pc, String str, double count) throws ExpressionException {
		if (count < 0) throw new ExpressionException("Parameter 2 of function repeatString which is now [" + Caster.toString(count) + "] must be a non-negative integer");
		return StringUtil.repeatString(str, (int) count);
	}

	public static String _call(PageContext pc, String str, double count) throws ExpressionException {
		int len = (int) count;
		if (len < 0) throw new ExpressionException("Parameter 2 of function repeatString which is now [" + len + "] must be a non-negative integer");
		char[] chars = str.toCharArray();
		StringBuilder cb = new StringBuilder(chars.length * len);
		for (int i = 0; i < len; i++)
			cb.append(chars);
		return cb.toString();
	}

	public static StringBuilder call(StringBuilder sb, String str, double count) throws ExpressionException {
		int len = (int) count;
		if (len < 0) throw new ExpressionException("Parameter 1 of function repeatString which is now [" + len + "] must be a non-negative integer");

		for (int i = 0; i < len; i++)
			sb.append(str);
		return sb;
	}

	public static StringBuilder call(StringBuilder sb, char c, double count) throws ExpressionException {
		int len = (int) count;
		if (len < 0) throw new ExpressionException("Parameter 1 of function repeatString which is now [" + len + "] must be a non-negative integer");

		for (int i = 0; i < len; i++)
			sb.append(c);
		return sb;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]));

		throw new FunctionException(pc, "RepeatString", 2, 2, args.length);
	}
}