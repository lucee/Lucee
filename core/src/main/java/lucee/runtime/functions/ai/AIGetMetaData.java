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
import lucee.runtime.type.Struct;

public final class AIGetMetaData extends BIF {

	private static final long serialVersionUID = 6532201888958323478L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 2) throw new FunctionException(pc, "AIGetMetaData", 1, 2, args.length);
		boolean detailed = args.length > 1 ? Caster.toBooleanValue(args[1]) : false;

		// AIEEngine as input
		if (args[0] instanceof AIEngine) {
			return AIUtil.getMetaData((AIEngine) args[0], detailed, detailed);
		}

		// AIEEngine as input
		if (args[0] instanceof AISession) {
			return AIUtil.getMetaData(((AISession) args[0]).getEngine(), detailed, detailed);
		}

		// connection definition as input
		Struct connection = AIUtil.toConnection(args[0]);
		if (connection != null) return AIUtil.getMetaData(((PageContextImpl) pc).getAIEngine(connection), detailed, detailed);

		// string as input
		String nameAI = Caster.toString(args[0]);

		if (nameAI.startsWith("default:")) nameAI = ((PageContextImpl) pc).getNameFromDefault(nameAI.substring(8));
		AIEngine aie = ((PageContextImpl) pc).getAIEngine(nameAI);
		return AIUtil.getMetaData(aie, detailed, detailed);
	}

}