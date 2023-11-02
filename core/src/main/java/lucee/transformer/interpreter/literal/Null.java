package lucee.transformer.interpreter.literal;

import lucee.runtime.type.scope.Scope;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public class Null extends ExpressionBase {

	public Null(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		ic.stack((Object) null);
		return Object.class;
	}

	public Variable toVariable() {
		Variable v = getFactory().createVariable(Scope.SCOPE_UNDEFINED, getStart(), getEnd());
		v.addMember(getFactory().createDataMember(getFactory().createLitString("null")));
		return v;
	}
}