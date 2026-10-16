package lucee.runtime.functions.ai;

import java.util.List;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.PageContext;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AISessionMultipart;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.ai.ComplexAnswer;
import lucee.runtime.ai.Part;
import lucee.runtime.ai.PartImpl;
import lucee.runtime.ai.Response;
import lucee.runtime.ai.UDFAIResponseListener;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.UDF;

/**
 * implementation of the Function arrayAppend
 */
public final class InquiryAISession extends BIF {

	private static final long serialVersionUID = 4034033693139930644L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 2 || args.length > 3) throw new FunctionException(pc, "InquiryAISession", 2, 3, args.length);

		Object oSession = args[0];
		if (!(oSession instanceof AISession)) {
			throw new CasterException(oSession, AISession.class);
		}
		AISession ais = (AISession) oSession;

		// listener
		UDF listener = args.length > 2 ? Caster.toFunction(args[2]) : null;

		Response rsp = null;

		Object oQuestion = args[1];
		// simple string question
		if (Decision.isString(oQuestion)) {
			String question = Caster.toString(oQuestion);

			LogUtil.logx(pc.getConfig(), Log.LEVEL_INFO, "ai", "Submitting question to AI endpoint [" + ais.getEngine().getName() + "] from type [" + ais.getEngine().getLabel()
					+ "] with the following content: [" + question + "]", "ai", "application");

			if (listener != null) rsp = ais.inquiry(question, new UDFAIResponseListener(pc, listener));
			else rsp = ais.inquiry(question);
		}
		// multipart questions
		else if (Decision.isArray(oQuestion)) {
			List<Part> parts = PartImpl.toParts(pc, Caster.toNativeArray(oQuestion));

			LogUtil.logx(pc.getConfig(), Log.LEVEL_INFO, "ai", "Submitting question to AI endpoint [" + ais.getEngine().getName() + "] from type [" + ais.getEngine().getLabel()
					+ "] with the following texts: [" + AIUtil.extractTextFromParts(parts) + "]", "ai", "application");

			if (listener != null) rsp = AISessionMultipart.toAISessionMultipart(ais).inquiry(parts, new UDFAIResponseListener(pc, listener));
			else rsp = AISessionMultipart.toAISessionMultipart(ais).inquiry(parts);
		}

		return extractAnswer(rsp);
	}

	private Object extractAnswer(Response rsp) {

		if (!rsp.isMultiPart()) return rsp.getAnswer();

		List<Part> answers = rsp.getAnswers();
		if (AIUtil.isTextOnly(answers)) {
			StringBuilder sb = new StringBuilder();
			String a;
			for (Part rp: rsp.getAnswers()) {
				a = rp.getAsString();
				if (a != null) sb.append(a);
			}
			return sb.toString();
		}

		return new ComplexAnswer(rsp);
	}

	public static String extractStringAnswer(Response rsp) {
		if (rsp.isMultiPart()) {
			StringBuilder sb = new StringBuilder();
			String a;
			for (Part rp: rsp.getAnswers()) {
				a = rp.getAsString();
				if (a != null) sb.append(a);
			}
			return sb.toString();
		}
		return rsp.getAnswer();

	}
}