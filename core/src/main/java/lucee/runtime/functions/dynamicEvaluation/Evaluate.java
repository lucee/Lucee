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
package lucee.runtime.functions.dynamicEvaluation;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.interpreter.CFMLExpressionInterpreter;
import lucee.runtime.op.Caster;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.CallerImpl;
import lucee.runtime.type.scope.Local;
import lucee.runtime.type.scope.LocalNotSupportedScope;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.scope.Variables;

/**
 * Implements the CFML Function evaluate
 */
public final class Evaluate implements Function {

	private static final long serialVersionUID = 2259041678381553989L;

	public static Object call(PageContext pc, Object[] objs) throws PageException {
		return call(pc, objs, false);
	}

	public static Object call(PageContext pc, Object[] objs, boolean preciseMath) throws PageException {
		// define another environment for the function
		if (objs.length > 1 && objs[objs.length - 1] instanceof Scope) {

			// Variables Scope
			Variables var = null;
			Local lcl = null, cLcl = null;
			Argument arg = null, cArg = null;
			if (objs[objs.length - 1] instanceof Variables) {
				var = (Variables) objs[objs.length - 1];
			}
			else if (objs[objs.length - 1] instanceof CallerImpl) {
				CallerImpl ci = ((CallerImpl) objs[objs.length - 1]);
				var = ci.getVariablesScope();
				lcl = ci.getLocalScope();
				arg = ci.getArgumentsScope();
			}

			if (var != null) {
				Variables cVar = pc.variablesScope();
				pc.setVariablesScope(var);
				if (lcl != null && !(lcl instanceof LocalNotSupportedScope)) {
					cLcl = pc.localScope();
					cArg = pc.argumentsScope();
					pc.setFunctionScopes(lcl, arg);
				}
				try {
					return _call(pc, objs, objs.length - 1, preciseMath);
				}
				finally {
					pc.setVariablesScope(cVar);
					if (cLcl != null) pc.setFunctionScopes(cLcl, cArg);
				}
			}

			// Undefined Scope
			else if (objs[objs.length - 1] instanceof Undefined) {
				PageContextImpl pci = (PageContextImpl) pc;
				Undefined undefined = (Undefined) objs[objs.length - 1];

				boolean check = undefined.getCheckArguments();
				Variables orgVar = pc.variablesScope();
				Argument orgArgs = pc.argumentsScope();
				Local orgLocal = pc.localScope();

				pci.setVariablesScope(undefined.variablesScope());
				if (check) pci.setFunctionScopes(undefined.localScope(), undefined.argumentsScope());
				try {
					return _call(pc, objs, objs.length - 1, preciseMath);
				}
				finally {
					pc.setVariablesScope(orgVar);
					if (check) pci.setFunctionScopes(orgLocal, orgArgs);
				}

			}
		}
		return _call(pc, objs, objs.length, preciseMath);
	}

	private static Object _call(PageContext pc, Object[] objs, int len, boolean preciseMath) throws PageException {
		Object rst = null;
		for (int i = 0; i < len; i++) {
			if (objs[i] instanceof Number) rst = objs[i];
			else rst = new CFMLExpressionInterpreter(false).interpret(pc, Caster.toString(objs[i]), preciseMath);
		}
		return rst;
	}
}
