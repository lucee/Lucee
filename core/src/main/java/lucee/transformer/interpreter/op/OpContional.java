package lucee.transformer.interpreter.op;

import lucee.runtime.exp.PageException;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public final class OpContional extends ExpressionBase {

	private ExprBoolean cont;
	private Expression left;
	private Expression right;

	private OpContional(Expression cont, Expression left, Expression right) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.cont = left.getFactory().toExprBoolean(cont);
		this.left = left;
		this.right = right;
	}

	public static Expression toExpr(Expression cont, Expression left, Expression right) {
		return new OpContional(cont, left, right);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		ic.stack(ic.getValueAsBooleanValue(cont) ? left : right);
		return Object.class;
	}
}