package lucee.transformer.interpreter.cast;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.cast.Cast;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * cast an Expression to a Double
 */
public final class CastDouble extends ExpressionBase implements ExprDouble, Cast {

	private Expression expr;

	private CastDouble(Expression expr) {
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
	public static ExprDouble toExprDouble(Expression expr) {
		if (expr instanceof ExprDouble) return (ExprDouble) expr;
		if (expr instanceof Literal) {
			Double dbl = ((Literal) expr).getDouble(null);
			if (dbl != null) return expr.getFactory().createLitDouble(dbl.doubleValue(), expr.getStart(), expr.getEnd());
		}
		return new CastDouble(expr);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		if (mode == MODE_VALUE) {
			ic.stack(ic.getValueAsDoubleValue(expr));
			return double.class;
		}
		ic.stack(ic.getValueAsDouble(expr));
		return Double.class;
	}

	@Override
	public Expression getExpr() {
		return expr;
	}
}