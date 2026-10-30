package lucee.transformer.cfml.evaluator.func.impl;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.bytecode.expression.var.SpreadUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.FunctionEvaluator;
import lucee.transformer.expression.var.Argument;
import lucee.transformer.expression.var.NamedArgument;
import lucee.transformer.library.function.FunctionLibFunction;

public final class LiteralArray implements FunctionEvaluator {

	@Override
	public FunctionLibFunction pre(BIF bif, FunctionLibFunction flf) throws TemplateException {
		Argument[] args = bif.getArguments();
		if (args == null || args.length == 0) return null;

		// spread operator
		if (SpreadUtil.hasSpread(args)) {
			boolean hasNamed = false;
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof NamedArgument) hasNamed = true;
			}
			if (!hasNamed) return flf.getFunctionLib().getFunction("_literalSpreadArray");
			for (int i = 0; i < args.length; i++) {
				if (!(args[i] instanceof NamedArgument) && !SpreadUtil.isSpread(args[i]))
					throw new TemplateException("invalid argument for literal ordered struct, only named arguments and the spread operator are allowed like [name:\"value\", ...other]");
			}
			return flf.getFunctionLib().getFunction("_literalSpreadOrderedStruct");
		}

		// named arguments
		if (args[0] instanceof NamedArgument) {
			for (int i = 1; i < args.length; i++) {
				if (!(args[i] instanceof NamedArgument))
					throw new TemplateException("invalid argument for literal ordered struct, only named arguments are allowed like {name:\"value\",name2:\"value2\"}");
			}
			return flf.getFunctionLib().getFunction("_literalOrderedStruct");
		}

		for (int i = 1; i < args.length; i++) {
			if (args[i] instanceof NamedArgument) throw new TemplateException("invalid argument for literal array, no named arguments are allowed");
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
