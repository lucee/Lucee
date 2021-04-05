package lucee.transformer.interpreter.cast;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.cast.Cast;
import lucee.transformer.expression.ExprFloat;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * cast an Expression to a Double
 */
public final class CastFloat extends ExpressionBase implements ExprFloat, Cast {

	private Expression expr;

	private CastFloat(Expression expr) {
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
	public static ExprFloat toExprFloat(Expression expr) {
		if (expr instanceof ExprFloat) return (ExprFloat) expr;
		if (expr instanceof Literal) {
			Double dbl = ((Literal) expr).getDouble(null);
			if (dbl != null) return expr.getFactory().createLitFloat((float) dbl.doubleValue(), expr.getStart(), expr.getEnd());
		}
		return new CastFloat(expr);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		if (mode == MODE_VALUE) {
			ic.stack(ic.getValueAsFloatValue(expr));
			return float.class;
		}
		ic.stack(ic.getValueAsFloat(expr));
		return Float.class;
	}

	@Override
	public Expression getExpr() {
		return expr;
	}
}