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

import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.expression.var.Argument;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.FunctionEvaluator;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.library.function.FunctionLibFunction;

public class IsNull implements FunctionEvaluator {

	@Override
	public void execute(BIF bif, FunctionLibFunction flf) throws TemplateException {
		Argument arg = bif.getArguments()[0];
		Expression value = arg.getValue();

		// set all member to safe navigated
		if (value instanceof Variable) {
			Variable var = ((Variable) value);
			/*
			 * LDEV-1201 List<Member> members = var.getMembers(); for(Member m:members) {
			 * m.setSafeNavigated(true); }
			 */
			var.setDefaultValue(value.getFactory().createNull());
		}
	}

	@Override
	public void evaluate(BIF bif, FunctionLibFunction flf) throws EvaluatorException {
	}

	@Override
	public FunctionLibFunction pre(BIF bif, FunctionLibFunction flf) throws TemplateException {
		return null;
	}

}