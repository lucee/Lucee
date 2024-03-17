package lucee.transformer.cfml.evaluator.func.impl;

import java.util.List;

import lucee.runtime.exp.TemplateException;
import lucee.runtime.type.scope.Scope;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.expression.var.Argument;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.bytecode.expression.var.NamedArgument;
import lucee.transformer.bytecode.expression.var.VariableString;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.FunctionEvaluator;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Member;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.library.function.FunctionLibFunction;

public class LiteralStruct implements FunctionEvaluator {
	private static final String GENERAL_MSG = " Invalid arguments for literal struct. "
			+ "For full specifications, only named arguments are allowed, like {name:\"value\", name2:\"value2\"}."
			+ " Shorthand notation is supported where the variable name and the key are identical (e.g., {name} as a shorthand for {name: name})."
			+ "Ensure each item inside the curly braces is either a single valid identifier for shorthand notation or follows the 'key: value' format for named arguments.";

	private static final String GENERAL_SHORTHAND_MSG = "Invalid struct shorthand syntax%s. "
			+ "Shorthand notation requires single, unqualified variable names without any dots or property accessors. "
			+ "For example, [susi] is valid, but [susi.sorglos] or [url.susi] is not.";

	@Override
	public FunctionLibFunction pre(BIF bif, FunctionLibFunction flf) throws TemplateException {

		Argument[] args = bif.getArguments();
		if (args == null || args.length == 0) return null;

		Expression val;
		List<Member> members;
		Argument arg;
		boolean modified = false, failed = false;
		Member m = null;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (!(arg instanceof NamedArgument)) {
				val = arg.getValue();
				if (val instanceof Variable) {
					Variable var = (Variable) val;
					do {
						// scope
						if (var.getScope() != Scope.SCOPE_UNDEFINED) {
							failed = true;
							break;
						}
						members = var.getMembers();
						// member count
						if (members.size() != 1) {
							failed = true;
							break;
						}
						// member type
						m = members.get(0);
						if (!(m instanceof DataMember)) {
							failed = true;
							break;
						}
					}
					while (false);

					if (failed) {
						throw new TemplateException(bif.getData().srcCode, arg.getEnd().line, arg.getEnd().column, String.format(GENERAL_SHORTHAND_MSG, variableToString(var)));
					}

					modified = true;
					args[i] = new NamedArgument(((DataMember) m).getName(), val, arg.getStringType(), false);

				}
				else {
					throw new TemplateException(GENERAL_MSG);
				}

				if (modified) {
					bif.setArguments(args);
				}
			}
		}

		return null;
	}

	private String variableToString(Variable var) {

		try {
			return " [ " + VariableString.variableToString(null, var, false) + " ] ";
		}
		catch (TransformerException e) {
			return "";
		}
	}

	@Override
	public void execute(BIF bif, FunctionLibFunction flf) throws TemplateException {

	}

	@Override
	public void evaluate(BIF bif, FunctionLibFunction flf) throws EvaluatorException {
	}
}
