package lucee.transformer.interpreter.op;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.interpreter.InterpreterException;
import lucee.runtime.op.Operator;
import lucee.transformer.Factory;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.Expression;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public final class OpDouble extends ExpressionBase implements ExprDouble {

	private int op;
	private Expression left;
	private Expression right;

	OpDouble(Expression left, Expression right, int operation) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left;
		this.right = right;
		this.op = operation;
	}

	public Expression getLeft() {
		return left;
	}

	public Expression getRight() {
		return right;
	}

	public int getOperation() {
		return op;
	}

	/**
	 * Create a String expression from an Expression
	 * 
	 * @param left
	 * @param right
	 * @param operation
	 * 
	 * @return String expression
	 * @throws TemplateException
	 */
	public static ExprDouble toExprDouble(Expression left, Expression right, int operation) {
		return new OpDouble(left, right, operation);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.expression.ExpressionBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter,
	 *      int)
	 */
	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		return writeOutDouble(ic, mode);
	}

	public Class<?> writeOutDouble(InterpreterContext ic, int mode) throws PageException {

		double d;
		if (op == Factory.OP_DBL_EXP) {
			d = Operator.exponent(ic.getValueAsDoubleValue(left), ic.getValueAsDoubleValue(right));
		}
		else if (op == Factory.OP_DBL_DIVIDE) {
			d = Operator.div(ic.getValueAsDoubleValue(left), ic.getValueAsDoubleValue(right));
		}
		else if (op == Factory.OP_DBL_INTDIV) {
			d = Operator.intdiv(ic.getValueAsDoubleValue(left), ic.getValueAsDoubleValue(right));
		}
		else if (op == Factory.OP_DBL_PLUS) {
			d = Operator.plus(ic.getValueAsDoubleValue(left), ic.getValueAsDoubleValue(right));
		}
		else if (op == Factory.OP_DBL_MINUS) {
			d = Operator.minus(ic.getValueAsDoubleValue(left), ic.getValueAsDoubleValue(right));
		}
		else if (op == Factory.OP_DBL_MODULUS) {
			d = Operator.modulus(ic.getValueAsDoubleValue(left), ic.getValueAsDoubleValue(right));
		}
		else if (op == Factory.OP_DBL_DIVIDE) {
			d = Operator.divide(ic.getValueAsDoubleValue(left), ic.getValueAsDoubleValue(right));
		}
		else if (op == Factory.OP_DBL_MULTIPLY) {
			d = Operator.multiply(ic.getValueAsDoubleValue(left), ic.getValueAsDoubleValue(right));
		}
		else throw new InterpreterException("invalid operation: " + op);
		if (mode == MODE_VALUE) {
			ic.stack(d);
			return double.class;
		}
		ic.stack(Double.valueOf(d));
		return Double.class;
	}

}