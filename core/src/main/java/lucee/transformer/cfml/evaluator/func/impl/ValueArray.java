package lucee.transformer.cfml.evaluator.func.impl;

import lucee.print;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.expression.var.Argument;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.FunctionEvaluator;
import lucee.transformer.library.function.FunctionLibFunction;

public class ValueArray implements FunctionEvaluator {

	@Override
	public FunctionLibFunction pre(BIF bif, FunctionLibFunction flf) throws TemplateException {
		print.e("ValueArray EV");
		Argument[] args = bif.getArguments();
		print.e("ValueArray:"+args.length);
		// if we have to argument, we switch to QueryColumnData
		if(args.length==2) {
			print.e("changed to QueryColumnData");
			return flf.getFunctionLib().getFunction("QueryColumnData");
			
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
