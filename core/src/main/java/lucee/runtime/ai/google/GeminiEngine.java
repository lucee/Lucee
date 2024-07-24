package lucee.runtime.ai.google;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import lucee.print;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.log.LogUtil;
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
import lucee.runtime.ai.Response;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class GeminiEngine extends AIEngineSupport {
	private static final String DEFAULT_URL = "https://{location}-aiplatform.googleapis.com/v1/projects/{projectid}/locations/{location}/publishers/google/models/{model}:generateContent";

	private static final long DEFAULT_TIMEOUT = 3000L;
	private static final String DEFAULT_CHARSET = null;
	private static final String DEFAULT_MIMETYPE = null;
	private static final String DEFAULT_USERAGENT = "Lucee (AI Request)";
	// private static final String DEFAULT_MODEL = "generativeModel-v1"; // Change to your preferred
	// model
	// private static final String DEFAULT_MODEL = "gemini-1.5-flash"; // Change to your preferred model
	private static final String DEFAULT_MODEL = "gemini-1.5-flash"; // https://cloud.google.com/vertex-ai/generative-ai/docs/learn/models
	private static final String DEFAULT_LOCATION = "us-central1";

	private Struct properties;
	private URL url;
	private String projectid;
	private String apikey;
	private long timeout;
	private String location;
	private String charset;
	private String mimetype;
	private ProxyData proxy = null;
	private Map<String, String> formfields = null;
	private String model;
	private String systemMessage;

	@Override
	public AIEngine init(AIEngineFactory factory, Struct properties, String initalMessage) throws PageException {
		super.init(factory);
		this.properties = properties;

		// base URL
		String baseURL = null;
		String str = Caster.toString(properties.get(KeyConstants._URL, null), null);
		if (!Util.isEmpty(str, true)) {
			baseURL = str.trim();
			if (!baseURL.endsWith("/")) baseURL += '/';
		}
		else baseURL = DEFAULT_URL;

		// project id
		str = Caster.toString(properties.get("projectid", null), null);
		if (Util.isEmpty(str, true)) {
			throw new ApplicationException("the property [projectid] is required for the AI Engine Gemini!");
		}
		projectid = str.trim();

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
		systemMessage = !StringUtil.isEmpty(initalMessage, true) ? initalMessage.trim() : Caster.toString(properties.get(KeyConstants._message, null), null);

		// url
		baseURL = StringUtil.replace(baseURL, "{projectid}", projectid, false);
		baseURL = StringUtil.replace(baseURL, "{location}", location, false);
		baseURL = StringUtil.replace(baseURL, "{model}", model, false);
		print.e(baseURL);
		try {
			url = new URL(baseURL);
		}
		catch (MalformedURLException e) {
			throw Caster.toPageException(e);
		}

		return this;

	}

	@Override
	public Response invoke(String message) throws PageException {
		try {
			Struct msg;
			Array arr = new ArrayImpl();

			// add system
			/*
			 * if (!StringUtil.isEmpty(systemMessage)) { msg = new StructImpl(); msg.set(KeyConstants._role,
			 * "system"); msg.set(KeyConstants._content, systemMessage); arr.append(msg); }
			 */

			// Add conversation history
			/*
			 * for (Conversation c: getHistoryAsList()) { // question msg = new StructImpl();
			 * msg.set(KeyConstants._role, "user"); msg.set(KeyConstants._content,
			 * c.getRequest().getQuestion()); arr.append(msg); // answer msg = new StructImpl();
			 * msg.set(KeyConstants._role, "assistant"); msg.set(KeyConstants._content,
			 * c.getResponse().getAnswer()); arr.append(msg); }
			 */

			Struct sct = new StructImpl();
			sct.set(KeyConstants._model, model);
			sct.set("prompt", message);

			JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
			String str = json.serialize(null, sct, SerializationSettings.SERIALIZE_AS_COLUMN, null);
			print.e("--------------");
			print.e(url);
			print.e(str);

			HTTPResponse rsp = HTTPEngine4Impl.post(url, null, null, timeout, false, mimetype, charset, DEFAULT_USERAGENT, proxy, new Header[] {

					new HeaderImpl("Authorization", "Bearer " + apikey),

					new HeaderImpl("Content-Type", "application/json")

			}, formfields, str);

			ContentType ct = rsp.getContentType();
			print.e(rsp.getStatusLine());
			if ("application/json".equals(ct.getMimeType())) {
				String cs = ct.getCharset();
				if (Util.isEmpty(cs, true)) cs = charset;

				print.e(rsp.getContentAsString(cs));
				/*
				 * Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null,
				 * rsp.getContentAsString(cs))); Struct err = Caster.toStruct(raw.get(KeyConstants._error, null),
				 * null); if (err != null) { print.e(err); throw
				 * ChatGPTUtil.toException(Caster.toString(err.get(KeyConstants._message)),
				 * Caster.toString(err.get(KeyConstants._type, null), null),
				 * Caster.toString(err.get(KeyConstants._code, null), null)); }
				 * 
				 * ChatGPTResponse response = new ChatGPTResponse(raw, cs); getHistoryAsList().add(new
				 * ConversationImpl(new RequestSupport(message), response)); return response;
				 */
				return null;
			}
			else {
				String cs = ct.getCharset();
				if (Util.isEmpty(cs, true)) cs = charset;

				print.e(rsp.getContentAsString(cs));
				throw new ApplicationException("Gemini did answer with the mime type [" + ct.getMimeType() + "] that is not supported, only [application/json] is supported");
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static void log(MalformedURLException e) {
		LogUtil.log("ai", e);
	}

	public static void main(String[] args) throws Exception {
		Struct props = new StructImpl();

		props.set("projectid", "");
		props.set("apikey", "");
		// props.set("location", "lucee.org");
		props.set(KeyConstants._timeout, "10000");
		props.set(KeyConstants._message, "keep the answers as short as possible");
		props.set(KeyConstants._model, "gemini-1.5-flash");

		props.set(KeyConstants._url, "https://generativelanguage.googleapis.com/v1/models/{model}");
		// props.set(KeyConstants._model, "gpt-4");

		props.set(KeyConstants._url, "https://generativelanguage.googleapis.com/v1/projects/{projectid}/locations/{location}/models/{model}");
		props.set(KeyConstants._url, "https://{location}-aiplatform.googleapis.com/v1/projects/{projectid}/locations/{location}/publishers/google/models/{model}:generateContent");

		// https://generative-language.googleapis.com/v1/projects/fine-way-396107/locations/us-central1/models/gemini-pro
		// https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$GOOGLE_API_KEY
		// \

		AIEngine ai = new GeminiEngine().init(null, props, null);

		// String code =
		// IOUtil.toString(ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test-cfconfig/webapps/ROOT/test3.cfm"),
		// CharsetUtil.UTF8);

		// Request req = new RequestSupport(new String[] { "Please analyze the following Lucee (CFML) code
		// for best practices, performance, and security improvements",
		// "give me suggestions for doc comments", "The code is intended for Lucee version 5.4.4.42.", "keep
		// it as short as possible", "Here is the code:", code });

		Response rsp = ai.invoke("Where was Albert Einstein born");
		// print.e(rsp.getAnswer());
		print.e("-----------------------------------");
		print.e(rsp);

		/*
		 * rsp = ai.invoke("When did he move to Switzerland and why?"); print.e(rsp.getAnswer());
		 * print.e("-----------------------------------"); print.e(rsp);
		 * 
		 * rsp = ai.invoke("what was his eye color?"); print.e(rsp.getAnswer());
		 * print.e("-----------------------------------"); print.e(rsp);
		 */

	}
}
