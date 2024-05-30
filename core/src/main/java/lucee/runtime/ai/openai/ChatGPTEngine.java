package lucee.runtime.ai.openai;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lucee.print;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.commons.net.http.httpclient.HeaderImpl;
import lucee.loader.util.Util;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.Request;
import lucee.runtime.ai.Response;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class ChatGPTEngine implements AIEngine {
	private static final URL DEFAULT_URL;
	private static final int DEFAULT_TIMEOUT = 0;
	private static final String DEFAULT_CHARSET = null;
	private static final String DEFAULT_MIMETYPE = null;
	private static final String DEFAULT_USERAGENT = "Lucee (AI Request)";
	private static final String DEFAULT_MODEL = "gpt-3.5-turbo";

	static {
		URL tmp = null;
		try {
			tmp = new URL("https://api.openai.com/v1/chat/completions");
		}
		catch (MalformedURLException e) {
			log(e);
		}
		DEFAULT_URL = tmp;
	}

	private Struct properties;
	private URL url;
	private String secretKey;
	private int timeout;
	private String charset;
	private String mimetype;
	private ProxyData proxy = null;
	private Map<String, String> formfields = null;
	private String model;
	private List<Struct> conversationHistory = new ArrayList<>();

	@Override
	public AIEngine init(Struct properties) throws PageException {
		this.properties = properties;

		// URL
		String str = Caster.toString(properties.get(KeyConstants._URL, null), null);
		if (!Util.isEmpty(str, true)) {
			try {
				url = HTTPUtil.toURL(str.trim(), HTTPUtil.ENCODED_AUTO);
			}
			catch (Exception e) {
				url = DEFAULT_URL;
			}
		}
		else url = DEFAULT_URL;

		// secret key
		str = Caster.toString(properties.get(KeyConstants._secretKey, null), null);
		if (Util.isEmpty(str, true)) {
			throw new ApplicationException("the property [secretKey] is required for the AI Engine ChatGPT!");
		}
		secretKey = str.trim();

		// timeout
		timeout = Caster.toIntValue(properties.get(KeyConstants._timeout, null), DEFAULT_TIMEOUT);
		// charset
		charset = Caster.toString(properties.get(KeyConstants._charset, null), DEFAULT_CHARSET);
		if (Util.isEmpty(charset, true)) charset = null;
		// mimetype
		mimetype = Caster.toString(properties.get(KeyConstants._mimetype, null), DEFAULT_MIMETYPE);
		if (Util.isEmpty(mimetype, true)) mimetype = null;
		// model
		model = Caster.toString(properties.get(KeyConstants._model, DEFAULT_MODEL), DEFAULT_MODEL);
		return this;
	}

	@Override
	public Response invoke(Request req) throws PageException {
		try {
			Array arr = new ArrayImpl();
			// Add conversation history
			for (Struct msg: conversationHistory) {
				arr.append(msg);
			}
			// Add new user messages
			for (String q: req.getQuestions()) {
				Struct msg = new StructImpl();
				msg.set(KeyConstants._role, "user");
				msg.set(KeyConstants._content, q);
				arr.append(msg);
			}

			Struct sct = new StructImpl();
			sct.set(KeyConstants._model, model);
			sct.set(KeyConstants._messages, arr);

			JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
			String str = json.serialize(null, sct, SerializationSettings.SERIALIZE_AS_COLUMN, null);

			HTTPResponse rsp = HTTPEngine4Impl.post(url, null, null, timeout, false, mimetype, charset, DEFAULT_USERAGENT, proxy,
					new Header[] { new HeaderImpl("Authorization", "Bearer " + secretKey), new HeaderImpl("Content-Type", "application/json") }, formfields, str);

			ContentType ct = rsp.getContentType();
			if ("application/json".equals(ct.getMimeType())) {
				String cs = ct.getCharset();
				if (Util.isEmpty(cs, true)) cs = charset;

				Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, rsp.getContentAsString(cs)));
				Struct err = Caster.toStruct(raw.get(KeyConstants._error, null), null);
				if (err != null) {
					throw ChatGPTUtil.toException(Caster.toString(err.get(KeyConstants._message)), Caster.toString(err.get(KeyConstants._type, null), null),
							Caster.toString(err.get(KeyConstants._code, null), null));
				}

				// Add assistant's response to the conversation history
				Array choices = Caster.toArray(raw.get(KeyConstants._choices));
				if (choices != null && choices.size() > 0) {
					Struct choice = Caster.toStruct(choices.getE(1));
					Struct message = Caster.toStruct(choice.get(KeyConstants._message));
					conversationHistory.add(message);
				}

				return new ChatGPTResponse(raw, cs);
			}
			else {
				throw new ApplicationException("Chat GPT did answer with the mime type [" + ct.getMimeType() + "] that is not supported, only [application/json] is supported");
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static void log(MalformedURLException e) {
		LogUtil.log("ai", e);
	}

	public static void main(String[] args) throws PageException, IOException {
		Struct props = new StructImpl();

		props.set(KeyConstants._secretKey, "");
		// props.set(KeyConstants._model, "gpt-4");

		AIEngine ai = new ChatGPTEngine().init(props);

		String code = IOUtil.toString(ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test-cfconfig/webapps/ROOT/test3.cfm"), CharsetUtil.UTF8);

		Request req = new Request(new String[] { "Please analyze the following Lucee (CFML) code for best practices, performance, and security improvements",
				" give me suggestions for doc comments", "The code is intended for Lucee version 5.4.4.42.", "keep it as short as possible", "Here is the code:", code });
		Response rsp = ai.invoke(req);

		print.e(rsp.getAnswer());
		print.e("-----------------------------------");
		print.e(rsp);
	}
}
