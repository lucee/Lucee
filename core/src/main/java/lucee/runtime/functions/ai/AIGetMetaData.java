package lucee.runtime.functions.ai;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public final class AIGetMetaData extends BIF {

	private static final long serialVersionUID = 6532201888958323478L;

	public static Struct call(PageContext pc, String nameAI) throws PageException {
		if (nameAI.startsWith("default:")) nameAI = ((PageContextImpl) pc).getNameFromDefault(nameAI.substring(8));
		AIEngine aie = ((PageContextImpl) pc).getAIEngine(nameAI);
		return AIUtil.getMetaData(aie);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		throw new FunctionException(pc, "AIListModels", 1, 1, args.length);
	}

}