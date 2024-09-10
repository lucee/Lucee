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
package lucee.runtime.functions.math;

import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class InputBaseN implements Function {

	private static final long serialVersionUID = 951439327862024318L;

	public static Number call(PageContext pc, String string, Number radix) throws ExpressionException {

		int radixValue = Caster.toInteger(radix);

		if (radixValue < 2 || radixValue > 36) {
			throw new FunctionException(pc, "inputBaseN", 2, "radix", "radix must be between 2 and 36");
		}

		string = string.trim().toLowerCase();
		if (string.startsWith("0x")) {
			string = string.substring(2); // Remove '0x' prefix
		}

		if (string.length() > 32) {
			throw new FunctionException(pc, "inputBaseN", 1, "string", "argument is too large, it can only be a maximum of 32 digits (-0x at start)");
		}

		long result = Long.parseLong(string, radixValue);

		if (ThreadLocalPageContext.preciseMath(pc)) {
			return Caster.toBigDecimal(result);
		}
		return result;
	}
}
