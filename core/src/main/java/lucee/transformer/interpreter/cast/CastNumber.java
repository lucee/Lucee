package lucee.transformer.interpreter.cast;

import java.math.BigDecimal;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.cast.Cast;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * cast an Expression to a Double
 */
public final class CastNumber extends ExpressionBase implements ExprNumber, Cast {

	private Expression expr;

	private CastNumber(Expression expr) {
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
	public static ExprNumber toExprNumber(Expression expr) {
		if (expr instanceof ExprDouble) return (ExprDouble) expr;
		if (expr instanceof Literal) {
			Number n = ((Literal) expr).getNumber(null);
			if (n != null) {
				if (n instanceof BigDecimal) return expr.getFactory().createLitNumber(((BigDecimal) n), expr.getStart(), expr.getEnd());
				return expr.getFactory().createLitNumber(BigDecimal.valueOf(n.doubleValue()), expr.getStart(), expr.getEnd());
			}
		}
		return new CastNumber(expr);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		ic.stack(ic.getValueAsNumber(expr));
		return Number.class;
	}

	@Override
	public Expression getExpr() {
		return expr;
	}
}