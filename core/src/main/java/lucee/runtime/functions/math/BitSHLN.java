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
 * Implements the CFML Function bitshln
 */
package lucee.runtime.functions.math;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public final class BitSHLN implements Function {
	public static double call(PageContext pc, double dnumber, double dcount) throws PageException {
		int count = (int) dcount;
		if (count > 31 || count < 0) throw new FunctionException(pc, "bitSHLN", 2, "count", "must be between 0 and 31 now " + Caster.toString(dcount));
		if (!Decision.isInteger(dnumber)) {
			return Caster.toLongValueLossless(dnumber) << count;
		}
		return Caster.toIntValueLossless(dnumber) << count;
	}
}