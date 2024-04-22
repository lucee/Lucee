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
 * Implements the CFML Function acos
 */
package lucee.runtime.functions.math;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class ACos implements Function {
	private static final long serialVersionUID = -7453640128079472952L;

	public static double call(PageContext pc, double number) throws ExpressionException {
		if (number >= -1d && number <= 1d) return StrictMath.acos(number);
		throw new ExpressionException("invalid range of argument for function aCos, argument range must be between -1 and 1, now is [" + number + "]");
	}

	public static Number call(PageContext pc, Number number) throws ExpressionException {
		if (number.doubleValue() >= -1d && number.doubleValue() <= 1d) return Caster.toBigDecimal(StrictMath.acos(Caster.toDoubleValue(number)));
		throw new ExpressionException("invalid range of argument for function aCos, argument range must be between -1 and 1, now is [" + number + "]");
	}
}