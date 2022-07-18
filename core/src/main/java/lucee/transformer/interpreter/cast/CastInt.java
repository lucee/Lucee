package lucee.transformer.interpreter.cast;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.cast.Cast;
import lucee.transformer.expression.ExprInt;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * cast an Expression to a Double
 */
public final class CastInt extends ExpressionBase implements ExprInt, Cast {

	private Expression expr;

	private CastInt(Expression expr) {
		super(expr.getFactory(), expr.getStart(), expr.getEnd());
		this.expr = expr;
	}

	/**
	 * Create a String expression from an Expression
	 * 
	 * @param expr
	 * @return String expression
	 * @throws TemplateException
	 */
	public static ExprInt toExprInt(Expression expr) {
		if (expr instanceof ExprInt) return (ExprInt) expr;
		if (expr instanceof Literal) {
			Number n = ((Literal) expr).getNumber(null);
			if (n != null) return expr.getFactory().createLitInteger(n.intValue(), expr.getStart(), expr.getEnd());
		}
		return new CastInt(expr);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		if (mode == MODE_VALUE) {
			ic.stack(ic.getValueAsIntValue(expr));
			return int.class;
		}
		ic.stack(ic.getValueAsInteger(expr));
		return Integer.class;
	}

	@Override
	public Expression getExpr() {
		return expr;
	}
}