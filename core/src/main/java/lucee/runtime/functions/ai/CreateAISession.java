package lucee.runtime.functions.ai;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

/**
 * implementation of the Function arrayAppend
 */
public final class CreateAISession extends BIF {

	private static final long serialVersionUID = 5632258425708603692L;

	public static Object call(PageContext pc, String nameAI) throws PageException {
		return call(pc, nameAI, null);
	}

	public static Object call(PageContext pc, String nameAI, String initalMessage) throws PageException {
		if (nameAI.startsWith("default:")) nameAI = ((PageContextImpl) pc).getNameFromDefault(nameAI.substring(8));
		return ((PageContextImpl) pc).createAISession(nameAI, initalMessage);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		throw new FunctionException(pc, "CreateAISession", 1, 2, args.length);
	}
}