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
import lucee.transformer.bytecode.op.OpBigDecimal;
import lucee.transformer.bytecode.op.OpNumber;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.FunctionEvaluator;
import lucee.transformer.expression.Expression;
import lucee.transformer.library.function.FunctionLibFunction;

public class PrecisionEvaluate implements FunctionEvaluator {

	@Override
	public void execute(BIF bif, FunctionLibFunction flf) throws TemplateException {

		Argument[] args = bif.getArguments();

		for (Argument arg: args) {
			Expression value = arg.getValue();
			if (value instanceof OpNumber) {
				arg.setValue(value.getFactory().toExprString(toOpBigDecimal(((OpNumber) value))), "any");
			}
		}
	}

	private OpBigDecimal toOpBigDecimal(OpNumber op) {
		Expression left = op.getLeft();
		Expression right = op.getRight();
		if (left instanceof OpNumber) left = toOpBigDecimal((OpNumber) left);
		if (right instanceof OpNumber) right = toOpBigDecimal((OpNumber) right);
		return new OpBigDecimal(left, right, op.getOperation());
	}

	@Override
	public void evaluate(BIF bif, FunctionLibFunction flf) throws EvaluatorException {
	}

	@Override
	public FunctionLibFunction pre(BIF bif, FunctionLibFunction flf) throws TemplateException {
		return null;
	}
}