package lucee.transformer.interpreter.expression.var;

import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.transformer.Factory;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public class EmptyArray extends ExpressionBase {

	public EmptyArray(Factory factory) {
		super(factory, null, null);
	}

	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		ic.stack(new ArrayImpl());
		return Array.class;
	}

}