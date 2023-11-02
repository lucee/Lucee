package lucee.transformer.interpreter.literal;

import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public class NullConstant extends ExpressionBase {

	public NullConstant(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {
		if (NullSupportHelper.full(ic.getPageContext())) {
			ic.stack((Object) null);
		}
		else {
			ic.stack(ic.getPageContext().undefinedScope().get(KeyConstants._NULL));
		}
		return Object.class;
	}

	public Variable toVariable() {
		Variable v = getFactory().createVariable(Scope.SCOPE_UNDEFINED, getStart(), getEnd());
		v.addMember(getFactory().createDataMember(getFactory().createLitString("null")));
		return v;
	}
}