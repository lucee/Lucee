package lucee.transformer.interpreter.op;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.Position;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public final class OpNegate extends ExpressionBase implements ExprBoolean {

	private ExprBoolean expr;

	private OpNegate(Expression expr, Position start, Position end) {
		super(expr.getFactory(), start, end);
		this.expr = expr.getFactory().toExprBoolean(expr);
	}

	/**
	 * Create a String expression from an Expression
	 * 
	 * @param left
	 * @param right
	 * 
	 * @return String expression
	 * @throws TemplateException
	 */
	public static ExprBoolean toExprBoolean(Expression expr, Position start, Position end) {
		if (expr instanceof Literal) {
			Boolean b = ((Literal) expr).getBoolean(null);
			if (b != null) {
				return expr.getFactory().createLitBoolean(!b.booleanValue(), start, end);
			}
		}
		return new OpNegate(expr, start, end);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		if (mode == MODE_VALUE) {
			ic.stack(!ic.getValueAsBooleanValue(expr));
			return boolean.class;
		}

		ic.stack(ic.getValueAsBooleanValue(expr) ? Boolean.FALSE : Boolean.TRUE);
		return Boolean.class;
	}
}