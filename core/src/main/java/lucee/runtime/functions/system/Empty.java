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
package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.string.Len;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.op.Caster;

public class Empty implements Function {

	private static final long serialVersionUID = 3780957672985941192L;

	public static boolean call(PageContext pc, String variableName) throws FunctionException {
		Object res = VariableInterpreter.getVariableEL(pc, variableName, null);

		if (res == null) return true;
		double len = Len.invoke(res, -1);
		if (len == -1) throw new FunctionException(pc, "empty", 1, "variable", "this type  [" + Caster.toTypeName(res) + "] is not supported");
		return len == 0;
	}
}