package lucee.runtime.ai.openai;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.io.res.ContentType;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
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

public class OpenAIEngine extends AIEngineSupport {
	private static final long DEFAULT_TIMEOUT = 3000L;
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final String DEFAULT_MIMETYPE = null;
	private static final URL DEFAULT_URL_OPENAI;
	private static final URL DEFAULT_URL_OLLAMA;

	static {

		// ChatGPT
		URL tmp = null;
		try {
			tmp = new URL("https://api.openai.com/v1/");
			// tmp = new URL("https://api.customopenai.com/v1/chat/completions");
			// https://chatgpt.com/g/g-EFSGvsHVN-lucee
		}
		catch (MalformedURLException e) {
			log(e);
		}
		DEFAULT_URL_OPENAI = tmp;

		// Ollama (lokal)
		tmp = null;
		try {
			tmp = new URL("http://localhost:11434/v1/");
			// tmp = new URL("https://api.customopenai.com/v1/chat/completions");
			// https://chatgpt.com/g/g-EFSGvsHVN-lucee
		}
		catch (MalformedURLException e) {
			log(e);
		}
		DEFAULT_URL_OLLAMA = tmp;

	}

	Struct properties;
	String secretKey;
	long timeout = DEFAULT_TIMEOUT;
	String charset;
	String mimetype;
	ProxyData proxy = null;
	Map<String, String> formfields = null;
	String model;
	private String systemMessage;

	private URL baseURL;

	@Override
	public AIEngine init(AIEngineFactory factory, Struct properties) throws PageException {
		super.init(factory);
		this.properties = properties;

		// URL
		/// we support some hard coded types to keep it simple
		String str = Caster.toString(properties.get(KeyConstants._type, null), null);
		if (!Util.isEmpty(str, true)) {
			if ("chatgpt".equals(str.trim()) || "openai".equals(str.trim())) baseURL = DEFAULT_URL_OPENAI;
			else if ("ollama".equals(str.trim())) baseURL = DEFAULT_URL_OLLAMA;
			else throw new ApplicationException(
					"ATM only 2 types are supported [openai, ollama], for any other endpoint simply define the attribute `url` that looks like this [https://api.lucee.com/v1/].");
		}
		else {
			str = Caster.toString(properties.get(KeyConstants._URL, null), null);
			if (!Util.isEmpty(str, true)) {
				if (!str.endsWith("/")) str += "/";
				try {
					baseURL = HTTPUtil.toURL(str.trim(), HTTPUtil.ENCODED_AUTO);
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}
			else baseURL = DEFAULT_URL_OPENAI;
		}

		// secret key
		str = Caster.toString(properties.get(KeyConstants._secretKey, null), null);
		if (Util.isEmpty(str, true)) {
			throw new ApplicationException("the property [secretKey] is required for the AI Engine ChatGPT!");
		}
		secretKey = str.trim();

		// timeout
		timeout = Caster.toLongValue(properties.get(KeyConstants._timeout, null), DEFAULT_TIMEOUT);
		// charset
		charset = Caster.toString(properties.get(KeyConstants._charset, null), DEFAULT_CHARSET);
		if (Util.isEmpty(charset, true)) charset = DEFAULT_CHARSET;
		// mimetype
		mimetype = Caster.toString(properties.get(KeyConstants._mimetype, null), DEFAULT_MIMETYPE);
		if (Util.isEmpty(mimetype, true)) mimetype = null;
		// model
		model = Caster.toString(properties.get(KeyConstants._model, null), null);
		if (Util.isEmpty(model, true)) {
			// nice to have
			String appendix = "";
			try {
				appendix = " Available models for this engine are [" + AIUtil.getModelNamesAsStringList(this) + "]";
			}
			catch (PageException pe) {
			}

			throw new ApplicationException("the property [model] is required for a OpenAI Engine!." + appendix);
		}

		// message
		systemMessage = Caster.toString(properties.get(KeyConstants._message, null), null);
		return this;
	}

	@Override
	public AISession createSession(String inialMessage, long timeout) {
		return new OpenAISession(this, StringUtil.isEmpty(inialMessage, true) ? systemMessage : inialMessage.trim(), timeout);
	}

	@Override
	public String getLabel() {
		return "ChatGPT";
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	public URL getBaseURL() {
		return baseURL;
	}

	@Override
	public List<AIModel> getModels() throws PageException {
		try {

			URL url = new URL(baseURL, "models");
			HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, timeout, false, charset, AIEngineSupport.DEFAULT_USERAGENT, proxy,
					new Header[] { new HeaderImpl("Authorization", "Bearer " + secretKey), new HeaderImpl("Content-Type", "application/json") });

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

				Array data = Caster.toArray(raw.get(KeyConstants._data));
				Iterator<Object> it = data.valueIterator();
				List<AIModel> list = new ArrayList<>();
				while (it.hasNext()) {
					list.add(new OpenAIModel(Caster.toStruct(it.next()), charset));
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
