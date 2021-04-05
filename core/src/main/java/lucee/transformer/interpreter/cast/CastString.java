package lucee.transformer.interpreter.cast;

import lucee.runtime.exp.PageException;
import lucee.transformer.cast.Cast;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * Cast to a String
 */
public final class CastString extends ExpressionBase implements ExprString, Cast {

	private Expression expr;

	/**
	 * constructor of the class
	 * 
	 * @param expr
	 */
	private CastString(Expression expr) {
		super(expr.getFactory(), expr.getStart(), expr.getEnd());
		this.expr = expr;
	}

	/**
	 * Create a String expression from an Expression
	 * 
	 * @param expr
	 * @param pos
	 * @return String expression
	 */
	public static ExprString toExprString(Expression expr) {
		if (expr instanceof ExprString) return (ExprString) expr;
		if (expr instanceof Literal) return expr.getFactory().createLitString(((Literal) expr).getString(), expr.getStart(), expr.getEnd());
		return new CastString(expr);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		ic.stack(ic.getValueAsString(expr));
		return String.class;
	}

	@Override
	public Expression getExpr() {
		return expr;
	}

}