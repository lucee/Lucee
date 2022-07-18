package lucee.transformer.interpreter.op;

import java.math.BigDecimal;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public final class OpNegateNumber extends ExpressionBase implements ExprNumber {

	private ExprNumber expr;

	private OpNegateNumber(Expression expr, Position start, Position end) {
		super(expr.getFactory(), start, end);
		this.expr = expr.getFactory().toExprNumber(expr);
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
	public static ExprNumber toExprNumber(Expression expr, Position start, Position end) {
		if (expr instanceof Literal) {
			Number n = ((Literal) expr).getNumber(null);
			if (n != null) {
				if (n instanceof BigDecimal) return expr.getFactory().createLitNumber(((BigDecimal) n).negate(), start, end);
				return expr.getFactory().createLitNumber(BigDecimal.valueOf(-n.doubleValue()), start, end);
			}
		}
		return new OpNegateNumber(expr, start, end);
	}

	public static ExprNumber toExprNumber(Expression expr, int operation, Position start, Position end) {
		if (operation == Factory.OP_NEG_NBR_MINUS) return toExprNumber(expr, start, end);
		return expr.getFactory().toExprNumber(expr);
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