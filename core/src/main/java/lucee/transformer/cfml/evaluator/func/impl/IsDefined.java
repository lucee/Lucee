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
package lucee.transformer.cfml.evaluator.func.impl;

import lucee.commons.lang.StringList;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.type.Collection;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.bytecode.expression.type.CollectionKey;
import lucee.transformer.bytecode.expression.type.CollectionKeyArray;
import lucee.transformer.bytecode.expression.var.Argument;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.FunctionEvaluator;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.library.function.FunctionLibFunction;

public class IsDefined implements FunctionEvaluator {

	@Override
	public void execute(BIF bif, FunctionLibFunction flf) throws TemplateException {
		Argument arg = bif.getArguments()[0];
		Expression value = arg.getValue();
		if (value instanceof LitString) {
			String str = ((LitString) value).getString();
			StringList sl = VariableInterpreter.parse(str, false);
			if (sl != null) {
				// scope
				str = sl.next();

				int scope = VariableInterpreter.scopeString2Int(bif.ts.ignoreScopes, str);
				if (scope == Scope.SCOPE_UNDEFINED) sl.reset();

				// keys
				String[] arr = sl.toArray();
				ArrayUtil.trimItems(arr);

				// update first arg
				arg.setValue(bif.getFactory().createLitNumber(scope), "number");

				// add second argument

				if (arr.length == 1) {
					Expression expr = new CollectionKey(bif.getFactory(), arr[0]);// LitString.toExprString(str);
					arg = new Argument(expr, Collection.Key.class.getName());
					bif.addArgument(arg);
				}
				else {
					CollectionKeyArray expr = new CollectionKeyArray(bif.getFactory(), arr);
					// LiteralStringArray expr = new LiteralStringArray(arr);
					arg = new Argument(expr, Collection.Key[].class.getName());
					bif.addArgument(arg);
				}

			}

		}
		// print.out("bif:"+arg.getValue().getClass().getName());
	}

	@Override
	public void evaluate(BIF bif, FunctionLibFunction flf) throws EvaluatorException {
	}

	@Override
	public FunctionLibFunction pre(BIF bif, FunctionLibFunction flf) throws TemplateException {
		return null;
	}

}