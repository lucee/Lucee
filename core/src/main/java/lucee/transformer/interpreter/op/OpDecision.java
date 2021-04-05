package lucee.transformer.interpreter.op;

import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.InterpreterException;
import lucee.runtime.op.Operator;
import lucee.transformer.Factory;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public final class OpDecision extends ExpressionBase implements ExprBoolean {

	private final Expression left;
	private final Expression right;
	private final int op;

	private OpDecision(Expression left, Expression right, int operation) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left;
		this.right = right;
		this.op = operation;
	}

	/**
	 * Create a String expression from an operation
	 * 
	 * @param left
	 * @param right
	 * 
	 * @return String expression
	 */
	public static ExprBoolean toExprBoolean(Expression left, Expression right, int operation) {
		return new OpDecision(left, right, operation);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		Boolean b;

		if (op == Factory.OP_DEC_CT) {
			b = Operator.ct(ic.getValue(left), ic.getValue(right));
		}
		else if (op == Factory.OP_DEC_NCT) {
			b = Operator.nct(ic.getValue(left), ic.getValue(right));
		}
		else if (op == Factory.OP_DEC_EEQ) {
			b = Operator.eeq(ic.getValue(left), ic.getValue(right));
		}
		else if (op == Factory.OP_DEC_NEEQ) {
			b = Operator.neeq(ic.getValue(left), ic.getValue(right));
		}

		else {
			int i = Operator.compare(ic.getValue(left), ic.getValue(right));
			if (Factory.OP_DEC_LT == op) b = i < 0;
			else if (Factory.OP_DEC_LTE == op) b = i <= 0;
			else if (Factory.OP_DEC_GT == op) b = i > 0;
			else if (Factory.OP_DEC_GTE == op) b = i >= 0;
			else if (Factory.OP_DEC_EQ == op) b = i == 0;
			else if (Factory.OP_DEC_NEQ == op) b = i != 0;
			else throw new InterpreterException("invalid operation: " + op);
		}

		if (mode == MODE_VALUE) {
			ic.stack(b);
			return boolean.class;
		}
		ic.stack(Boolean.valueOf(b));
		return Boolean.class;
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
}