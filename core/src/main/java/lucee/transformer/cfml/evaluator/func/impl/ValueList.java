package lucee.transformer.cfml.evaluator.func.impl;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.expression.var.Argument;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.FunctionEvaluator;
import lucee.transformer.library.function.FunctionLibFunction;

public class ValueList implements FunctionEvaluator {

	@Override
	public FunctionLibFunction pre(BIF bif, FunctionLibFunction flf) throws TemplateException {
		Argument[] args = bif.getArguments();
		// if we have 3 arguments, we switch to _valueList
		if (args.length == 3) {
			return flf.getFunctionLib().getFunction("_ValueList");

		}
		return null;
	}

	@Override
	public void execute(BIF bif, FunctionLibFunction flf) throws TemplateException {

	}

	@Override
	public void evaluate(BIF bif, FunctionLibFunction flf) throws EvaluatorException {
	}
}
