package lucee.transformer.interpreter.op;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public final class OpNegateNumber extends ExpressionBase implements ExprDouble {

	private ExprDouble expr;

	private OpNegateNumber(Expression expr, Position start, Position end) {
		super(expr.getFactory(), start, end);
		this.expr = expr.getFactory().toExprDouble(expr);
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
	public static ExprDouble toExprDouble(Expression expr, Position start, Position end) {
		if (expr instanceof Literal) {
			Double d = ((Literal) expr).getDouble(null);
			if (d != null) {
				return expr.getFactory().createLitDouble(-d.doubleValue(), start, end);
			}
		}
		return new OpNegateNumber(expr, start, end);
	}

	public static ExprDouble toExprDouble(Expression expr, int operation, Position start, Position end) {
		if (operation == Factory.OP_NEG_NBR_MINUS) return toExprDouble(expr, start, end);
		return expr.getFactory().toExprDouble(expr);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		if (mode == MODE_VALUE) {
			ic.stack(-ic.getValueAsDouble(expr));
			return double.class;
		}

		ic.stack(Double.valueOf(-ic.getValueAsDouble(expr)));
		return Double.class;
	}
}