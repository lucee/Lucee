package lucee.transformer.interpreter.cast;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.cast.Cast;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * Cast to a Boolean
 */
public final class CastBoolean extends ExpressionBase implements ExprBoolean, Cast {

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(boolean)" + expr;
	}

	private Expression expr;

	/**
	 * constructor of the class
	 * 
	 * @param expr
	 */
	private CastBoolean(Expression expr) {
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
	public static ExprBoolean toExprBoolean(Expression expr) {
		if (expr instanceof ExprBoolean) return (ExprBoolean) expr;
		if (expr instanceof Literal) {
			Boolean bool = ((Literal) expr).getBoolean(null);
			if (bool != null) return expr.getFactory().createLitBoolean(bool.booleanValue(), expr.getStart(), expr.getEnd());
			// TODO throw new TemplateException("can't cast value to a boolean value");
		}
		return new CastBoolean(expr);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		if (mode == MODE_VALUE) {
			ic.stack(ic.getValueAsBooleanValue(expr));
			return boolean.class;
		}
		ic.stack(ic.getValueAsBoolean(expr));
		return Boolean.class;
	}

	@Override
	public Expression getExpr() {
		return expr;
	}
}