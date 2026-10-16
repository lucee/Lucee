package lucee.transformer.bytecode.expression.var;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Argument;
import lucee.transformer.expression.var.Func;
import lucee.transformer.expression.var.NamedArgument;

public final class Call extends ExpressionBase implements Func {

	// Object getFunction (PageContext,Object,Object[])
	private final static Method GET_FUNCTION_KEY = new Method("getFunction", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT_ARRAY });

	// Object getFunctionWithNamedValues (PageContext,Object,Object[])
	private final static Method GET_FUNCTION_WITH_NAMED_ARGS_KEY = new Method("getFunctionWithNamedValues", Types.OBJECT,
			new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT_ARRAY });

	private Expression expr;
	private List<Argument> args = new ArrayList<Argument>();

	public Call(Expression expr) {
		super(expr.getFactory(), expr.getStart(), expr.getEnd());
		this.expr = expr;
	}

	@Override
	public void addArgument(Argument argument) {
		args.add(argument);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter ga = bc.getAdapter();

		ga.loadArg(0);

		expr.writeOut(bc, MODE_REF);

		ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, args.toArray(new Expression[args.size()]));

		ga.invokeStatic(Types.PAGE_CONTEXT_UTIL, namedArgs(bc) ? GET_FUNCTION_WITH_NAMED_ARGS_KEY : GET_FUNCTION_KEY);

		return Types.OBJECT;
	}

	private boolean namedArgs(BytecodeContext bc) throws TransformerException {
		if (args.isEmpty()) return false;
		Iterator<Argument> it = args.iterator();
		boolean named = it.next() instanceof NamedArgument;
		while (it.hasNext()) {
			if (named != (it.next() instanceof NamedArgument)) throw new TransformerException(bc, "You cannot mix named and unnamed arguments in function calls", getEnd());
		}

		return named;
	}

	@Override
	public void dump(Struct sct) {
		super.dump(sct);
		sct.setEL(KeyConstants._type, "CallExpression");

		// Check if this is a built-in function call
		// The expression could be a Variable with a BIF as its first member
		if (expr instanceof VariableImpl) {
			VariableImpl var = (VariableImpl) expr;
			if (var.getFirstMember() instanceof BIF) {
				sct.setEL("isBuiltIn", Boolean.TRUE);
			}
		}

		Struct callee = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
		sct.setEL("callee", callee);
		expr.dump(callee);

		// arguments
		Array arrArgs = new ArrayImpl(8, false);
		sct.setEL(KeyConstants._arguments, arrArgs);
		for (Argument arg: args) {
			Struct sctArg = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			arrArgs.appendEL(sctArg);
			arg.dump(sctArg);
		}
	}
}
