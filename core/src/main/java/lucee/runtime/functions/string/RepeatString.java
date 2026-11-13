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

	// the maximum array size of the jvm (Integer.MAX_VALUE - 8)
	private static final int MAX_LENGTH = Integer.MAX_VALUE - 8;

	public static String call(PageContext pc, String str, Number count) throws ExpressionException {
		int cnt = Caster.toIntValue(count);
		if (cnt < 0) throw new ExpressionException("Parameter 2 of function repeatString which is now [" + Caster.toString(cnt) + "] must be a non-negative integer");
		checkLength(str, cnt);
		return StringUtil.repeatString(str, cnt);
	}

	/**
	 * a java String can not hold more than Integer.MAX_VALUE characters, without this check the length
	 * calculation (length of str * count) overflows and fails with a NegativeArraySizeException (LDEV-4277)
	 */
	private static void checkLength(String str, int count) throws ExpressionException {
		long total = (long) str.length() * count;
		if (total > MAX_LENGTH) throw new ExpressionException("The result of function repeatString would be too large: repeating a string of [" + str.length() + "] characters ["
				+ count + "] times gives [" + total + "] characters, the maximum length of a string is [" + MAX_LENGTH + "] characters");
	}

	public static String _call(PageContext pc, String str, Number count) throws ExpressionException {
		int len = Caster.toIntValue(count);
		if (len < 0) throw new ExpressionException("Parameter 2 of function repeatString which is now [" + len + "] must be a non-negative integer");
		checkLength(str, len);
		char[] chars = str.toCharArray();
		StringBuilder cb = new StringBuilder(chars.length * len);
		for (int i = 0; i < len; i++)
			cb.append(chars);
		return cb.toString();
	}

	public static StringBuilder call(StringBuilder sb, String str, Number count) throws ExpressionException {
		int len = Caster.toIntValue(count);
		if (len < 0) throw new ExpressionException("Parameter 1 of function repeatString which is now [" + len + "] must be a non-negative integer");

		for (int i = 0; i < len; i++)
			sb.append(str);
		return sb;
	}

	public static StringBuilder call(StringBuilder sb, char c, Number count) throws ExpressionException {
		int len = Caster.toIntValue(count);
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