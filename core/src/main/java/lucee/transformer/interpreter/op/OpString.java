package lucee.transformer.interpreter.op;

import lucee.runtime.exp.PageException;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public final class OpString extends ExpressionBase implements ExprString {

	private ExprString right;
	private ExprString left;

	private static final int MAX_SIZE = 65535;

	private OpString(Expression left, Expression right) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left.getFactory().toExprString(left);
		this.right = left.getFactory().toExprString(right);
	}

	public static ExprString toExprString(Expression left, Expression right, boolean concatStatic) {
		if (concatStatic && left instanceof Literal && right instanceof Literal) {
			String l = ((Literal) left).getString();
			String r = ((Literal) right).getString();
			if ((l.length() + r.length()) <= MAX_SIZE) return left.getFactory().createLitString(l.concat(r), left.getStart(), right.getEnd());
		}
		return new OpString(left, right);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		ic.stack(ic.getValueAsString(left).concat(ic.getValueAsString(right)));
		return String.class;
	}
}