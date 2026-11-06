package lucee.runtime.functions.ai;

import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AISessionSupport;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.ai.Conversation;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public final class LoadAISession extends BIF {

	private static final long serialVersionUID = 2099884812627584898L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 2) throw new FunctionException(pc, "LoadAISession", 2, 2, args.length);

		Struct connection = AIUtil.toConnection(args[0]);
		String nameAI = connection == null ? Caster.toString(args[0]) : null;
		Object objData = args[1];
		Struct data = (objData instanceof String) ? CFMLEngineFactory.getInstance().getCastUtil().fromJsonStringToStruct(Caster.toString(objData)) : Caster.toStruct(objData);

		double temp = Caster.toDoubleValue(data.get(KeyConstants._temperature, null), false, -1D);
		int limit = Caster.toIntValue(data.get(KeyConstants._limit, null), -1);
		int connTimeout = Caster.toIntValue(data.get(KeyConstants._connectionTimeout, null), -1);
		int sockTimeout = Caster.toIntValue(data.get("socketTimeout", null), -1);
		String systemMessage = StringUtil.emptyAsNull(Caster.toString(data.get("systemMessage", null), null), true);

		Conversation[] history = AIUtil.toConversations(Caster.toArray(data.get(KeyConstants._history)));

		AISession session;
		if (connection != null) session = ((PageContextImpl) pc).createAISession(connection, systemMessage, history, limit, temp, connTimeout, sockTimeout);
		else if (nameAI.startsWith("id:")) session = ((PageContextImpl) pc).createAISessionById(nameAI.substring(3), systemMessage, history, limit, temp, connTimeout, sockTimeout);
		else {
			if (nameAI.startsWith("default:")) nameAI = ((PageContextImpl) pc).getNameFromDefault(nameAI.substring(8));
			session = ((PageContextImpl) pc).createAISession(nameAI, systemMessage, history, limit, temp, connTimeout, sockTimeout);
		}

		AISessionSupport support = AIUtil.toSessionSupport(session);
		if (support != null) support.restoreUsage(Caster.toStruct(data.get("usage", null), null));

		return session;
	}
}