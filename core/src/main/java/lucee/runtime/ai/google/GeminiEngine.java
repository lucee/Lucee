package lucee.runtime.ai.google;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.io.res.ContentType;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.commons.net.http.httpclient.HeaderImpl;
import lucee.loader.util.Util;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.AIEngineFactory;
import lucee.runtime.ai.AIEngineSupport;
import lucee.runtime.ai.AIModel;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public class GeminiEngine extends AIEngineSupport {

	public static final String TYPE_REG = "generateContent?";
	public static final String TYPE_STREAM = "streamGenerateContent?alt=sse&";

	private static final String DEFAULT_URL = "https://generativelanguage.googleapis.com/v1/";

	public static final String CHAT = "models/{model}:{cttype}key={apikey}";
	public static final String MODELS = "models/?key={apikey}";

	private static final long DEFAULT_TIMEOUT = 3000L;
	private static final String DEFAULT_CHARSET = null;
	private static final String DEFAULT_MIMETYPE = null;
	private static final String DEFAULT_MODEL = "gemini-1.5-flash";
	private static final String DEFAULT_LOCATION = "us-central1";

	Struct properties;
	String apikey;
	private long timeout;
	String location;
	String charset;
	String mimetype;
	ProxyData proxy = null;
	Map<String, String> formfields = null;
	String model;
	String systemMessage;
	String baseURL = null;

	@Override
	public AIEngine init(AIEngineFactory factory, Struct properties) throws PageException {
		super.init(factory);
		this.properties = properties;

		// base URL
		String str = Caster.toString(properties.get(KeyConstants._URL, null), null);
		if (!Util.isEmpty(str, true)) {
			baseURL = str.trim();
			if (!baseURL.endsWith("/")) baseURL += '/';
		}
		else baseURL = DEFAULT_URL;

		// api key
		str = Caster.toString(properties.get("apikey", null), null);
		if (Util.isEmpty(str, true)) {
			throw new ApplicationException("the property [apikey] is required for the AI Engine Gemini!");
		}
		apikey = str.trim();

		// location
		location = Caster.toString(properties.get(KeyConstants._location, null), DEFAULT_LOCATION);
		if (Util.isEmpty(location, true)) location = DEFAULT_LOCATION;

		// timeout
		timeout = Caster.toLongValue(properties.get(KeyConstants._timeout, null), DEFAULT_TIMEOUT);

		// charset
		charset = Caster.toString(properties.get(KeyConstants._charset, null), DEFAULT_CHARSET);
		if (Util.isEmpty(charset, true)) charset = null;

		// mimetype
		mimetype = Caster.toString(properties.get(KeyConstants._mimetype, null), DEFAULT_MIMETYPE);
		if (Util.isEmpty(mimetype, true)) mimetype = null;

		// model
		model = Caster.toString(properties.get(KeyConstants._model, DEFAULT_MODEL), DEFAULT_MODEL);
		// message
		systemMessage = Caster.toString(properties.get(KeyConstants._message, null), null);

		return this;

	}

	public URL toURL(String base, String scriptName, String cttype) throws PageException {
		scriptName = StringUtil.replace(scriptName, "{location}", location, false);
		scriptName = StringUtil.replace(scriptName, "{apikey}", apikey, false);
		scriptName = StringUtil.replace(scriptName, "{model}", model, false);
		if (cttype != null) scriptName = StringUtil.replace(scriptName, "{cttype}", cttype, false);
		try {
			return new URL(base + scriptName);
		}
		catch (MalformedURLException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public AISession createSession(String inialMessage, long timeout) {
		return new GeminiSession(this, StringUtil.isEmpty(inialMessage, true) ? systemMessage : inialMessage.trim(), timeout);
	}

	@Override
	public String getLabel() {
		return "Gemini";
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public List<AIModel> getModels() throws PageException {
		try {

			HTTPResponse rsp = HTTPEngine4Impl.get(toURL(baseURL, MODELS, null), null, null, timeout, false, charset, AIEngineSupport.DEFAULT_USERAGENT, proxy,
					new Header[] { new HeaderImpl("Content-Type", "application/json") });

			ContentType ct = rsp.getContentType();
			if ("application/json".equals(ct.getMimeType())) {
				String cs = ct.getCharset();
				if (Util.isEmpty(cs, true)) cs = charset;

				Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, rsp.getContentAsString(cs)));
				Struct err = Caster.toStruct(raw.get(KeyConstants._error, null), null);
				if (err != null) {
					throw AIUtil.toException(this, Caster.toString(err.get(KeyConstants._message)), Caster.toString(err.get(KeyConstants._type, null), null),
							Caster.toString(err.get(KeyConstants._code, null), null));
				}
				Array data = Caster.toArray(raw.get("models"));
				Iterator<Object> it = data.valueIterator();
				List<AIModel> list = new ArrayList<>();
				while (it.hasNext()) {

					list.add(new GeminiModel(Caster.toStruct(it.next()), charset));
				}
				return list;
			}
			throw new ApplicationException("Chat GPT did answer with the mime type [" + ct.getMimeType() + "] that is not supported, only [application/json] is supported");

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
}
