package lucee.runtime.functions.ai;

import lucee.runtime.PageContext;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.Response;
import lucee.runtime.ai.UDFAIResponseListener;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;

/**
 * implementation of the Function arrayAppend
 */
public final class InquiryAISession extends BIF {

	private static final long serialVersionUID = 4034033693139930644L;

	public static String call(PageContext pc, Object oSession, String question) throws PageException {
		return call(pc, oSession, question, null);
	}

	public static String call(PageContext pc, Object oSession, String question, UDF listener) throws PageException {
		if (!(oSession instanceof AISession)) {
			throw new CasterException(oSession, AISession.class);
		}
		AISession ais = (AISession) oSession;
		Response rsp;
		if (listener != null) rsp = ais.inquiry(question, new UDFAIResponseListener(pc, listener));
		else rsp = ais.inquiry(question);

		return rsp.getAnswer();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toFunction(args[2]));
		throw new FunctionException(pc, "InquiryAISession", 2, 3, args.length);
	}
}