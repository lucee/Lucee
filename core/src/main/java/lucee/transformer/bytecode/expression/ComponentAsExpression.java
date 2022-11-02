package lucee.transformer.bytecode.expression;

import org.objectweb.asm.Type;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.statement.tag.TagComponent;
import lucee.transformer.bytecode.util.Types;

public class ComponentAsExpression extends ExpressionBase {

	private TagComponent tc;

	public ComponentAsExpression(TagComponent tc) {
		super(tc.getFactory(), tc.getStart(), tc.getEnd());
		this.tc = tc;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		tc._writeOut(bc);
		return Types.COMPONENT;
	}

	/**
	 * @return the closure
	 */
	public TagComponent getTagComponent() {
		return tc;
	}
}