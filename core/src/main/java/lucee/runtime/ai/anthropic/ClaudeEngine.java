package lucee.runtime.ai.anthropic;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.MimeType;
import lucee.commons.net.HTTPUtil;
import lucee.loader.util.Util;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.AIEngineSupport;
import lucee.runtime.ai.AIModel;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AISessionSupport;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.ai.Conversation;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public final class ClaudeEngine extends AIEngineSupport {
	private static final String DEFAULT_URL = "https://api.anthropic.com/v1/";
	private static final int DEFAULT_CONVERSATION_SIZE_LIMIT = 100;
	private static final String DEFAULT_VERSION = "2023-06-01";
	private static final String DEFAULT_CHARSET = "UTF-8";

	private String label = "Claude";
	private String apiKey;
	private URL baseURL;
	private String model;
	private String systemMessage;
	private String version;
	private Double temperature;
	private int socketTimeout;
	private int connectTimeout;
	private String charset;
	ProxyData proxy = null;
	private int conversationSizeLimit = DEFAULT_CONVERSATION_SIZE_LIMIT;
	Struct custom;
	Map<String, String> headers;

	@Override
	public AIEngine init(ClassDefinition<? extends AIEngine> cd, Struct properties, String name, String _default, String id) throws PageException {
		super.init(cd, properties, name, _default, id);

		Struct props = (Struct) properties.duplicate(true);

		// API Key
		apiKey = Caster.toStringTrim(props.remove(KeyConstants._apiKey, null), null);
		if (Util.isEmpty(apiKey, true)) throw new ApplicationException("the property [apiKey] is required for Claude");

		// Base URL
		String type = Caster.toStringTrim(props.remove(KeyConstants._type, null), null);
		String urlStr = Caster.toStringTrim(props.remove(KeyConstants._URL, null), null);
		if (Util.isEmpty(type, true)) type = "claude";
		else if (!type.equalsIgnoreCase("claude") && !type.equalsIgnoreCase("other")) {
			throw new ApplicationException("only [claude] and [other] are supported for the property [type] for Claude");
		}

		if (type.equalsIgnoreCase("other")) {
			if (Util.isEmpty(urlStr, true)) throw new ApplicationException("the property [url] is required when [type] is [other] for Claude");
		}
		else {
			urlStr = DEFAULT_URL;
		}
		try {
			baseURL = HTTPUtil.toURL(urlStr, HTTPUtil.ENCODED_AUTO);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		// Timeout
		connectTimeout = Caster.toIntValue(props.remove(KeyConstants._connectTimeout, null), DEFAULT_CONNECT_TIMEOUT);
		if (connectTimeout <= 0) connectTimeout = DEFAULT_CONNECT_TIMEOUT;

		socketTimeout = Caster.toIntValue(props.remove(KeyConstants._socketTimeout, null), DEFAULT_SOCKET_TIMEOUT);
		if (socketTimeout <= 0) socketTimeout = DEFAULT_SOCKET_TIMEOUT;

		// temperature
		temperature = Caster.toDouble(props.remove(KeyConstants._temperature, null), null);
		if (temperature != null && (temperature < 0D || temperature > 1D)) {
			throw new ApplicationException("temperature has to be a number between 0 and 1, now it is [" + temperature + "]");
		}
		// conversation Size Limit
		conversationSizeLimit = Caster.toIntValue(props.remove(KeyConstants._conversationSizeLimit, null), DEFAULT_CONVERSATION_SIZE_LIMIT);

		// Model, "auto" or empty resolves the model from the models the provider has (LDEV-6618)
		model = readModel(props, true);

		// System Message
		systemMessage = Caster.toStringTrim(props.remove(KeyConstants._message, null), null);

		// version
		version = Caster.toStringTrim(props.remove(KeyConstants._version, DEFAULT_VERSION), DEFAULT_VERSION);
		if (StringUtil.isEmpty(version, true)) version = DEFAULT_VERSION;

		// charset
		charset = Caster.toStringTrim(props.remove(KeyConstants._charset, null), DEFAULT_CHARSET);
		if (Util.isEmpty(charset, true)) charset = DEFAULT_CHARSET;

		// headers
		headers = toHeaders(Caster.toStruct(props.remove(KeyConstants._headers, null), null));

		props.remove(KeyConstants._timeout, null);
		props.remove(KeyConstants._apikey, null);

		this.custom = props;

		return this;
	}

	@Override
	public AISession createSession(String initialMessage, Conversation[] history, int limit, double temp, int connectTimeout, int socketTimeout) throws PageException {
		resolveModel();
		return new ClaudeSession(this, StringUtil.isEmpty(initialMessage, true) ? systemMessage : initialMessage.trim(), history, limit, temp, connectTimeout, socketTimeout);
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getModel() {
		return getModel(model);
	}

	@Override
	public List<String> getFallbackModels() {
		List<String> list = new ArrayList<>();
		list.add("claude-sonnet-*");
		list.add("claude-opus-*");
		list.add("claude-*");
		return list;
	}

	@Override
	public int getSocketTimeout() {
		return socketTimeout;
	}

	@Override
	public int getConnectTimeout() {
		return connectTimeout;
	}

	public URL getBaseURL() {
		return baseURL;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getVersion() {
		return version;
	}

	public String getCharset() {
		return charset;
	}

	@Override
	public List<AIModel> getModels() throws PageException {
		try {
			HttpGet get = new HttpGet(baseURL.toExternalForm() + "models?limit=1000");
			get.setHeader("x-api-key", apiKey);
			get.setHeader("anthropic-version", version);
			if (headers != null) {
				for (Entry<String, String> e: headers.entrySet()) {
					get.setHeader(e.getKey(), e.getValue());
				}
			}
			RequestConfig config = AISessionSupport.setTimeout(RequestConfig.custom(), getConnectTimeout(), getSocketTimeout()).build();
			get.setConfig(config);

			try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(get)) {
				HttpEntity responseEntity = response.getEntity();
				Header ct = responseEntity.getContentType();
				MimeType mt = ct == null ? null : MimeType.getInstance(ct.getValue());
				String t = mt == null ? null : mt.getType() + "/" + mt.getSubtype();
				if ("application/json".equals(t)) {
					String rawStr = EntityUtils.toString(responseEntity, charset);
					Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, rawStr));
					Struct err = Caster.toStruct(raw.get(KeyConstants._error, null), null);
					if (err != null) {
						throw new ApplicationException("unable to list the models: " + Caster.toString(err.get(KeyConstants._message, null), "error") + "; type:"
								+ Caster.toString(err.get(KeyConstants._type, null), null) + "; status-code:" + AIUtil.getStatusCode(response));
					}
					List<AIModel> list = new ArrayList<>();
					Object o = raw.get(KeyConstants._data, null);
					if (o == null) throw new ApplicationException("unable to read models from response [" + rawStr + "]");
					Iterator<Object> it = Caster.toArray(o).valueIterator();
					while (it.hasNext()) {
						list.add(new ClaudeModel(Caster.toStruct(it.next()), charset));
					}
					return list;
				}
				throw new ApplicationException("Claude API returned unsupported mime type [" + t + "], only [application/json] is supported");
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public List<AIModel> getModels(List<AIModel> defaultValue) {
		try {
			return getModels();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public int getConversationSizeLimit() {
		return conversationSizeLimit;
	}

	@Override
	public Double getTemperature() {
		return temperature;
	}
}