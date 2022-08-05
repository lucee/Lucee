package lucee.transformer.interpreter.op;

import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.InterpreterException;
import lucee.runtime.op.OpUtil;
import lucee.transformer.Factory;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.Expression;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public final class OpNumber extends ExpressionBase implements ExprNumber {

	private int op;
	private Expression left;
	private Expression right;

	OpNumber(Expression left, Expression right, int operation) {
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

	public static ExprNumber toExprNumber(Expression left, Expression right, int operation) {
		return new OpNumber(left, right, operation);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		return writeOutNumber(ic, mode);
	}

	public Class<?> writeOutNumber(InterpreterContext ic, int mode) throws PageException {
		// TODOX all as Number
		Number n;
		if (op == Factory.OP_DBL_EXP) {
			n = OpUtil.exponentRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right));
		}
		else if (op == Factory.OP_DBL_DIVIDE) {
			n = OpUtil.divideRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right));
		}
		else if (op == Factory.OP_DBL_INTDIV) {
			n = OpUtil.intdivRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right));
		}
		else if (op == Factory.OP_DBL_PLUS) {
			n = OpUtil.plusRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right));
		}
		else if (op == Factory.OP_DBL_MINUS) {
			n = OpUtil.minusRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right));
		}
		else if (op == Factory.OP_DBL_MODULUS) {
			n = OpUtil.modulusRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right));
		}
		else if (op == Factory.OP_DBL_DIVIDE) {
			n = OpUtil.divideRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right));
		}
		else if (op == Factory.OP_DBL_MULTIPLY) {
			n = OpUtil.multiplyRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right));
		}
		else throw new InterpreterException("invalid operation: " + op);
		/*
		 * if (mode == MODE_VALUE) { ic.stack(d); return double.class; }
		 */
		ic.stack(n);
		return Number.class;
	}

}