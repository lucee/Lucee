package lucee.runtime.functions.ai;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

/**
 * returns the models available for a configured AI endpoint (LDEV-6618)
 */
public final class AIGetModels extends BIF {

	private static final long serialVersionUID = -2374628937157723019L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 1) throw new FunctionException(pc, "AIGetModels", 1, 1, args.length);
		return AIUtil.getModelsAsQuery(toEngine(pc, args[0]));
	}

	private static AIEngine toEngine(PageContext pc, Object obj) throws PageException {
		if (obj instanceof AIEngine) return (AIEngine) obj;
		if (obj instanceof AISession) return ((AISession) obj).getEngine();

		String nameAI = Caster.toString(obj);
		if (nameAI.startsWith("default:")) nameAI = ((PageContextImpl) pc).getNameFromDefault(nameAI.substring(8));
		return ((PageContextImpl) pc).getAIEngine(nameAI);
	}
}
