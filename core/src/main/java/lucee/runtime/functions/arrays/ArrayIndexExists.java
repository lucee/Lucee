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
 * Implements the CFML Function structkeyexists
 */
package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;

public final class ArrayIndexExists extends BIF {

	private static final long serialVersionUID = -4490011932571314711L;

	public static boolean call(PageContext pc, Array array, double index) {
		Object _null = NullSupportHelper.NULL(pc);
		return array.get((int) index, _null) != _null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]));
		else throw new FunctionException(pc, "ArrayIndexExists", 2, 2, args.length);
	}
}