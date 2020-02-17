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
 * Implements the CFML Function arraynew
 */
package lucee.runtime.functions.arrays;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayTyped;
import lucee.runtime.type.util.ArrayUtil;

public final class ArrayNew extends BIF {

	private static final long serialVersionUID = -5923269433550568279L;

	public static Array call(PageContext pc, double dimension, String type, boolean _synchronized) throws PageException {
		Array a;
		if (StringUtil.isEmpty(type, true) || Decision.isBoolean(type)) {
			a = ArrayUtil.getInstance((int) dimension, _synchronized);
		}
		else {
			if (dimension > 1) {
				throw new ApplicationException("multi dimensional arrays are not supported with typed arrays");
			}
			a = new ArrayTyped(type.trim());
		}
		return a;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc, 1, null, false);
		if (args.length == 1) return call(pc, Caster.toDoubleValue(args[0]), null, false);
		if (args.length == 2) return call(pc, Caster.toDoubleValue(args[0]), Caster.toString(args[1]), false);
		if (args.length == 3) return call(pc, Caster.toDoubleValue(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]));
		if (args.length == 4) return call(pc, Caster.toDoubleValue(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]));
		else throw new FunctionException(pc, "ArrayNew", 0, 3, args.length);
	}

}