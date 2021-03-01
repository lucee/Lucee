package lucee.transformer.interpreter.op;

import lucee.transformer.TransformerException;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public final class OpElvis extends ExpressionBase {

	private Variable left;
	private Expression right;

	private OpElvis(Variable left, Expression right) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left;
		this.right = right;
	}

	public static Expression toExpr(Variable left, Expression right) {
		return new OpElvis(left, right);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws TransformerException {
		try {
			ic.stack(left.writeOut(ic, mode));
		}
		catch (Exception e) {
			ic.stack(right.writeOut(ic, mode));
		}
		return Object.class;
	}
}