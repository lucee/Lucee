package lucee.transformer.bytecode.expression;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.component.ComponentLoader;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.statement.tag.TagComponent;
import lucee.transformer.bytecode.util.Types;

public class ComponentAsExpression extends ExpressionBase {
	private static final Type COMPONENT_LOADER = Type.getType(ComponentLoader.class);
	// ComponentImpl loadInline(PageContext pc, CIPage page)
	private static final Method LOAD_INLINE1 = new Method("loadInline", Types.COMPONENT_IMPL, new Type[] { Types.CI_PAGE });
	private static final Method LOAD_INLINE2 = new Method("loadInline", Types.COMPONENT_IMPL, new Type[] { Types.CI_PAGE, Types.PAGE_CONTEXT });

	private TagComponent tc;

	public ComponentAsExpression(TagComponent tc) {
		super(tc.getFactory(), tc.getStart(), tc.getEnd());
		this.tc = tc;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		// creates the component class file, but creates no output
		tc._writeOut(bc);

		// load the component
		GeneratorAdapter adapter = bc.getAdapter();

		String pageClassName = bc.getPage().getClassName();
		String inlineClassName = tc.getSubClassName(bc.getPage());
		// ASMConstants.NULL(adapter);
		adapter.visitTypeInsn(Opcodes.NEW, inlineClassName);
		adapter.visitInsn(Opcodes.DUP);

		/////// init class ///////
		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, pageClassName, "getPageSource", "()Llucee/runtime/PageSource;");

		adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, inlineClassName, "<init>", "(Llucee/runtime/PageSource;)V");
		adapter.checkCast(Types.CI_PAGE);
		/////// init class ///////

		adapter.loadArg(0);
		adapter.invokeStatic(COMPONENT_LOADER, LOAD_INLINE2);

		return Types.COMPONENT;
	}

	/**
	 * @return the closure
	 */
	public TagComponent getTagComponent() {
		return tc;
	}
}