package lucee.transformer.interpreter.literal;

import lucee.runtime.config.NullSupportHelper;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public class Empty extends ExpressionBase {

	public Empty(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		ic.stack(NullSupportHelper.empty(ic.getPageContext()));
		return Object.class;
	}
}