package lucee.transformer.bytecode.expression;

import org.objectweb.asm.Type;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.statement.tag.TagComponent;
import lucee.transformer.bytecode.util.Types;

public class TagComponentAsExpression extends ExpressionBase {

	private TagComponent cfc;

	public TagComponentAsExpression(TagComponent cfc) {
		super(cfc.getFactory(), cfc.getStart(), cfc.getEnd());
		this.cfc = cfc;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		cfc._writeOut(bc);
		return Types.COMPONENT;
	}

	/**
	 * @return the closure
	 */
	public TagComponent getTagComponent() {
		return cfc;
	}
}