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
package lucee.runtime.functions.decision;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.string.Len;
import lucee.runtime.op.Caster;

public class IsEmpty implements Function {

	private static final long serialVersionUID = -2839407878650099024L;

	public static boolean call(PageContext pc, Object value) throws PageException {
		if (value == null) return true;
		if (value instanceof Boolean || value instanceof Number) return false;

		double len = Len.invoke(value, -1);
		if (len == -1) throw new FunctionException(pc, "isEmpty", 1, "variable", "this type  [" + Caster.toTypeName(value) + "] is not supported");
		return len == 0;
	}
}