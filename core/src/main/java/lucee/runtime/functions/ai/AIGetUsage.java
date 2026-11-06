package lucee.runtime.functions.ai;

import lucee.runtime.PageContext;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AISessionSupport;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public final class AIGetUsage extends BIF {

	private static final long serialVersionUID = 6847126505873340813L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 1) throw new FunctionException(pc, "AIGetUsage", 1, 1, args.length);

		Object oSession = args[0];
		if (!(oSession instanceof AISession)) throw new CasterException(oSession, AISession.class);

		AISessionSupport support = AIUtil.toSessionSupport((AISession) oSession);
		if (support != null) return support.getUsage();

		// a session from a third party engine does not track the usage
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		sct.setEL("requests", 0L);
		sct.setEL("unreportedRequests", 0L);
		sct.setEL("failedRequests", 0L);
		sct.setEL("input", 0L);
		sct.setEL("output", 0L);
		sct.setEL("total", 0L);
		sct.setEL("cached", 0L);
		sct.setEL("reasoning", 0L);
		return sct;
	}
}
