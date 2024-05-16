package lucee.transformer.interpreter.op;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.interpreter.InterpreterException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.transformer.Factory;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public final class OpBool extends ExpressionBase implements ExprBoolean {

	private ExprBoolean left;
	private ExprBoolean right;
	private int operation;

	private OpBool(Expression left, Expression right, int operation) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left.getFactory().toExprBoolean(left);
		this.right = left.getFactory().toExprBoolean(right);
		this.operation = operation;
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		boolean res;
		// AND
		if (operation == Factory.OP_BOOL_AND) {
			res = (ic.getValueAsBooleanValue(left) && ic.getValueAsBooleanValue(right));
		}
		// OR
		else if (operation == Factory.OP_BOOL_OR) {
			res = (ic.getValueAsBooleanValue(left) || ic.getValueAsBooleanValue(right));
		}
		// XOR ^
		else if (operation == Factory.OP_BOOL_XOR) {
			res = (ic.getValueAsBooleanValue(left) ^ ic.getValueAsBooleanValue(right));
		}
		// EQV
		else if (operation == Factory.OP_BOOL_EQV) {
			res = (OpUtil.eqv(ic.getPageContext(), ic.getValueAsBooleanValue(left), ic.getValueAsBooleanValue(right)));
		}
		// IMP
		else if (operation == Factory.OP_BOOL_IMP) {
			res = (OpUtil.imp(ic.getPageContext(), ic.getValueAsBooleanValue(left), ic.getValueAsBooleanValue(right)));
		}
		else throw new InterpreterException("invalid operatior:" + operation);

		if (mode == MODE_REF) {
			ic.stack(Caster.toBoolean(res));
			return Boolean.class;
		}

		ic.stack(res);
		return boolean.class;

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
	public static ExprBoolean toExprBoolean(Expression left, Expression right, int operation) {
		if (left instanceof Literal && right instanceof Literal) {
			Boolean l = ((Literal) left).getBoolean(null);
			Boolean r = ((Literal) right).getBoolean(null);

			if (l != null && r != null) {
				switch (operation) {
				case Factory.OP_BOOL_AND:
					return left.getFactory().createLitBoolean(l.booleanValue() && r.booleanValue(), left.getStart(), right.getEnd());
				case Factory.OP_BOOL_OR:
					return left.getFactory().createLitBoolean(l.booleanValue() || r.booleanValue(), left.getStart(), right.getEnd());
				case Factory.OP_BOOL_XOR:
					return left.getFactory().createLitBoolean(l.booleanValue() ^ r.booleanValue(), left.getStart(), right.getEnd());
				}
			}
		}
		return new OpBool(left, right, operation);
	}

	@Override
	public String toString() {
		return left + " " + toStringOperation() + " " + right;
	}

	private String toStringOperation() {
		if (Factory.OP_BOOL_AND == operation) return "and";
		if (Factory.OP_BOOL_OR == operation) return "or";
		if (Factory.OP_BOOL_XOR == operation) return "xor";
		if (Factory.OP_BOOL_EQV == operation) return "eqv";
		if (Factory.OP_BOOL_IMP == operation) return "imp";
		return operation + "";
	}
}