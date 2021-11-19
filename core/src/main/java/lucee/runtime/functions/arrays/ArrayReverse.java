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
 * Implements the CFML Function ArrayReverse
 */
package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ArrayUtil;

public final class ArrayReverse extends BIF {

	private static final long serialVersionUID = 5418304787535992180L;

	public static Array call(PageContext pc, Array array) throws ExpressionException {
		Array rev = ArrayUtil.getInstance(array.getDimension());
		int len = array.size();
		for (int i = 0; i < len; i++) {
			try {
				rev.setE(len - i, array.getE(i + 1));
			}
			catch (PageException e) {
			}
		}
		return rev;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toArray(args[0]));
		else throw new FunctionException(pc, "ArrayReverse", 1, 1, args.length);
	}
}