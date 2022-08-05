package lucee.transformer.bytecode.op;

import lucee.transformer.Position;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Variable;

public class OpUnaryString extends AbsOpUnary implements ExprString {

	public OpUnaryString(Variable var, Expression value, short type, int op, Position start, Position end) {
		super(var, value, type, op, start, end);
	}

}