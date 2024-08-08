package lucee.runtime.ai.openai;

import java.net.URL;

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

public class OpenAISession extends AISessionSupport {

	private OpenAIEngine openaiEngine;
	private String initalMessage;

	public OpenAISession(OpenAIEngine engine, String initalMessage, long timeout) {
		super(engine, timeout);
		this.openaiEngine = engine;
		this.initalMessage = initalMessage;
	}

	@Override
	public Response inquiry(String message) throws PageException {
		try {
			Struct msg;
			Array arr = new ArrayImpl();

			// add system
			if (!StringUtil.isEmpty(initalMessage)) {
				msg = new StructImpl();
				msg.set(KeyConstants._role, "system");
				msg.set(KeyConstants._content, initalMessage);
				arr.append(msg);
			}

			// Add conversation history
			for (Conversation c: getHistoryAsList()) {
				// question
				msg = new StructImpl();
				msg.set(KeyConstants._role, "user");
				msg.set(KeyConstants._content, c.getRequest().getQuestion());
				arr.append(msg);
				// answer
				msg = new StructImpl();
				msg.set(KeyConstants._role, "assistant");
				msg.set(KeyConstants._content, c.getResponse().getAnswer());
				arr.append(msg);

			}

			// Add new user messages
			msg = new StructImpl();
			msg.set(KeyConstants._role, "user");
			msg.set(KeyConstants._content, message);
			arr.append(msg);

			Struct sct = new StructImpl();
			sct.set(KeyConstants._model, openaiEngine.model);
			sct.set(KeyConstants._messages, arr);

			JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
			String str = json.serialize(null, sct, SerializationSettings.SERIALIZE_AS_COLUMN, null);

			URL url = new URL(openaiEngine.getBaseURL(), "chat/completions");
			HTTPResponse rsp = HTTPEngine4Impl.post(url, null, null, getTimeout(), false, openaiEngine.mimetype, openaiEngine.charset, AIEngineSupport.DEFAULT_USERAGENT,
					openaiEngine.proxy, new Header[] { new HeaderImpl("Authorization", "Bearer " + openaiEngine.secretKey), new HeaderImpl("Content-Type", "application/json") },
					openaiEngine.formfields, str);

			ContentType ct = rsp.getContentType();
			if ("application/json".equals(ct.getMimeType())) {
				String cs = ct.getCharset();
				if (Util.isEmpty(cs, true)) cs = openaiEngine.charset;
				// stream=rsp.getContentAsStream();
				Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, rsp.getContentAsString(cs)));

				Struct err = Caster.toStruct(raw.get(KeyConstants._error, null), null);
				if (err != null) {
					throw AIUtil.toException(this.getEngine(), Caster.toString(err.get(KeyConstants._message)), Caster.toString(err.get(KeyConstants._type, null), null),
							Caster.toString(err.get(KeyConstants._code, null), null));
				}

				OpenAIResponse response = new OpenAIResponse(raw, cs);
				getHistoryAsList().add(new ConversationImpl(new RequestSupport(message), response));
				return response;
			}
			else {
				throw new ApplicationException("The AI did answer with the mime type [" + ct.getMimeType() + "] that is not supported, only [application/json] is supported");
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public void release() {
		// nothing to give up
	}
}
