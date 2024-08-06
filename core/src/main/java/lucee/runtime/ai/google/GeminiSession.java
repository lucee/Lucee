package lucee.runtime.ai.google;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.commons.net.http.httpclient.HeaderImpl;
import lucee.loader.util.Util;
import lucee.runtime.ai.AIEngineSupport;
import lucee.runtime.ai.AISessionSupport;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.ai.Conversation;
import lucee.runtime.ai.ConversationImpl;
import lucee.runtime.ai.RequestSupport;
import lucee.runtime.ai.Response;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class GeminiSession extends AISessionSupport {
	private GeminiEngine geminiEngine;
	private String initalMessage;

	public GeminiSession(GeminiEngine engine, String initalMessage, long timeout) {
		super(engine, timeout);
		this.geminiEngine = engine;
		this.initalMessage = initalMessage;
	}

	@Override
	public Response inquiry(String message) throws PageException {
		try {

			Struct root = new StructImpl(StructImpl.TYPE_LINKED);

			// contents
			Array contents = new ArrayImpl();
			root.set(KeyConstants._contents, contents);

			// add system
			if (!StringUtil.isEmpty(initalMessage, true)) {
				contents.append(createParts("user", initalMessage));
			}

			// Add conversation history
			for (Conversation c: getHistoryAsList()) { // question msg = new StructImpl();
				contents.append(createParts("user", c.getRequest().getQuestion()));
				contents.append(createParts("model", c.getResponse().getAnswer()));
			}

			// https://ai.google.dev/gemini-api/docs/get-started/tutorial?lang=rest&hl=de
			// curl

			// parts
			contents.append(createParts("user", message));

			JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
			String str = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_COLUMN, null);
			HTTPResponse rsp = HTTPEngine4Impl.post(geminiEngine.url, null, null, getTimeout(), false, geminiEngine.mimetype, geminiEngine.charset,
					AIEngineSupport.DEFAULT_USERAGENT, geminiEngine.proxy, new Header[] {

							// new HeaderImpl("Authorization", "Bearer " + geminiEngine.apikey),

							new HeaderImpl("Content-Type", "application/json")

					}, geminiEngine.formfields, str);

			ContentType ct = rsp.getContentType();

			if ("application/json".equals(ct.getMimeType())) {
				String cs = ct.getCharset();
				if (Util.isEmpty(cs, true)) cs = geminiEngine.charset;

				Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, rsp.getContentAsString(cs)));
				Struct err = Caster.toStruct(raw.get(KeyConstants._error, null), null);
				if (err != null) {
					String msg = Caster.toString(err.get(KeyConstants._message, null), null);
					String code = Caster.toString(err.get(KeyConstants._code, null), null);
					String status = Caster.toString(err.get(KeyConstants._status, null), null);
					if (!StringUtil.isEmpty(msg, true)) throw AIUtil.toException(msg, status, code);
					throw AIUtil.toException(getEngine().getLabel() + " did reposne with status code [" + rsp.getStatusCode() + "]", null,
							Caster.toString(rsp.getStatusCode(), null));
				}

				GeminiResponse response = new GeminiResponse(raw, cs);
				getHistoryAsList().add(new ConversationImpl(new RequestSupport(message), response));
				return response;

			}
			else {
				String cs = ct.getCharset();
				if (Util.isEmpty(cs, true)) cs = geminiEngine.charset;
				throw new ApplicationException("Gemini did answer with the mime type [" + ct.getMimeType() + "] that is not supported, only [application/json] is supported");
			}
		}
		catch (

		Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private Struct createParts(String role, String msg) throws PageException {
		Struct sctContents = new StructImpl(StructImpl.TYPE_LINKED);

		Array parts = new ArrayImpl();
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		parts.append(sct);
		sct.set(KeyConstants._text, msg);

		if (role != null) sctContents.set(KeyConstants._role, role.trim());
		sctContents.set(KeyConstants._parts, parts);

		return sctContents;
	}

	@Override
	public void release() {
		// nothing to give up
	}

}
