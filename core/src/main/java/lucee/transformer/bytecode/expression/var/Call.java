package lucee.transformer.bytecode.expression.var;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;

public class Call extends ExpressionBase implements Func {

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

		ga.invokeStatic(Types.PAGE_CONTEXT_UTIL, namedArgs() ? GET_FUNCTION_WITH_NAMED_ARGS_KEY : GET_FUNCTION_KEY);

		return Types.OBJECT;
	}

	private boolean namedArgs() throws TransformerException {
		if (args.isEmpty()) return false;
		Iterator<Argument> it = args.iterator();
		boolean named = it.next() instanceof NamedArgument;
		while (it.hasNext()) {
			if (named != (it.next() instanceof NamedArgument)) throw new TransformerException("You cannot mix named and unnamed arguments in function calls", getEnd());
		}

		return named;
	}
}
