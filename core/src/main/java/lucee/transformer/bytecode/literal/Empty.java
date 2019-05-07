package lucee.transformer.bytecode.literal;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Types;

public class Empty extends ExpressionBase {

	private static final Method EMPTY = new Method("empty", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT });

	public Empty(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		bc.getAdapter().loadArg(0);
		bc.getAdapter().invokeStatic(Types.NULL_SUPPORT_HELPER, EMPTY);
		return Types.OBJECT;
	}
}