package lucee.transformer.bytecode.statement;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.var.VariableImpl;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;

public class TryCatch extends StatementBaseNoFinal {
	final static Method TO_PAGEEXCEPTION = new Method("toPageException", Types.PAGE_EXCEPTION, new Type[] { Types.THROWABLE });

	final static Method HANDLE_LISTENER = new Method("handleListener", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.PAGE_EXCEPTION, Types.OBJECT });

	private VariableImpl var;
	private Expression listener;
	private Boolean asCollection;

	public TryCatch(Factory factory, Position start, Position end, VariableImpl var, Expression listener, Boolean asCollection) {
		super(factory, end, end);
		this.var = var;
		this.listener = listener;
		this.asCollection = asCollection;
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter ga = bc.getAdapter();
		int objIndex = ga.newLocal(Type.getType(Object.class));
		int expIndex = ga.newLocal(Type.getType(Object.class));

		// Labels for try-catch block
		Label tryStart = new Label();
		Label tryEnd = new Label();
		Label catchBlock = new Label();
		Label end = new Label();

		// Start of the try block
		ga.visitLabel(tryStart);
		var.writeOutX(bc, Expression.MODE_REF, asCollection);
		ga.storeLocal(objIndex);
		ASMConstants.NULL(ga);
		ga.storeLocal(expIndex);
		ga.goTo(end);

		// End of the try block
		ga.visitLabel(tryEnd);

		// Start of the catch block
		ga.visitLabel(catchBlock);
		ga.invokeStatic(Types.CASTER, TO_PAGEEXCEPTION);
		ga.storeLocal(expIndex); // Store the result of toPageException

		ASMConstants.NULL(ga);
		ga.storeLocal(objIndex);

		// End of method, after catch block
		ga.visitLabel(end);

		ga.loadArg(0);
		ga.loadLocal(objIndex);
		ga.loadLocal(expIndex);
		listener.writeOut(bc, Expression.MODE_REF);
		ga.invokeStatic(Types.TAG_UTIL, HANDLE_LISTENER);

		// Specify the try-catch block details
		ga.visitTryCatchBlock(tryStart, tryEnd, catchBlock, Type.getType(Exception.class).getInternalName());
		// ASMConstants.NULL(ga);
	}

}
