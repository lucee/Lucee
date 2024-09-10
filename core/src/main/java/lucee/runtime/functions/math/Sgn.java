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
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class Sgn implements Function {

	private static final long serialVersionUID = -6590806959925629603L;

	public static Number call(PageContext pc, Number number) {

		double numValue = Caster.toDoubleValue(number);

		int result = numValue != 0.0d ? (numValue >= 0.0d ? 1 : -1) : 0;

		if (ThreadLocalPageContext.preciseMath(pc)) {
			return Caster.toBigDecimal(result);
		}
		return result;
	}
}
