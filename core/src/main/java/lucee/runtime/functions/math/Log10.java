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
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class Log10 implements Function {

	private static final long serialVersionUID = 5661273846487684766L;
	private static final double LOG10_CONVERSION = 0.43429448190325182D;

	public static Number call(PageContext pc, Number number) throws ExpressionException {

		double numValue = Caster.toDoubleValue(number);

		if (numValue <= 0.0D) {
			throw new ExpressionException("invalid argument at function log10, value must be a positive number, now " + Caster.toString(number) + "");
		}

		double result = LOG10_CONVERSION * StrictMath.log(numValue);

		if (ThreadLocalPageContext.preciseMath(pc)) {
			return Caster.toBigDecimal(result);
		}
		return result;
	}
}
