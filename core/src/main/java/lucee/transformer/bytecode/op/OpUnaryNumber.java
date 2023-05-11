package lucee.transformer.bytecode.op;

import lucee.transformer.Position;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Variable;

public class OpUnaryNumber extends AbsOpUnary implements ExprNumber {

	public OpUnaryNumber(Variable var, Expression value, short type, int operation, Position start, Position end) {
		super(var, value, type, operation, start, end);
	}
}
