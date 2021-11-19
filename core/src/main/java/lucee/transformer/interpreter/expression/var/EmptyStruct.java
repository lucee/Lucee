package lucee.transformer.interpreter.expression.var;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.transformer.Factory;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

public class EmptyStruct extends ExpressionBase {

	public EmptyStruct(Factory factory) {
		super(factory, null, null);
	}

	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		ic.stack(new StructImpl());
		return Struct.class;
	}

}
