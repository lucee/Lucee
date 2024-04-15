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
 * Implements the CFML Function bitshrn
 */
package lucee.runtime.functions.math;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.op.Caster;

public final class BitSHRN extends BIF implements Function {

	private static final long serialVersionUID = -3188551575162886748L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {

		int count = Caster.toIntValue(args[1]);
		if (count < 0) throw new FunctionException(pc, "bitSHRN", 2, "count", "Invalid shift value [" + count + "], value must be a positive integer");

		if (AppListenerUtil.getPreciseMath(pc, null)) {
			return Caster.toBigDecimal(Caster.toBigInteger(args[0]).shiftRight(count));
		}
		return Caster.toDouble(Caster.toLongValue(args[0]) >>> count);
	}
}