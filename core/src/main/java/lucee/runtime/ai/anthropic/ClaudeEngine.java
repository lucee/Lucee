package lucee.runtime.ai.anthropic;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.loader.util.Util;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.AIEngineSupport;
import lucee.runtime.ai.AIModel;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.Conversation;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public final class ClaudeEngine extends AIEngineSupport {
	private static final String DEFAULT_URL = "https://api.anthropic.com/v1/";
	private static final int DEFAULT_CONVERSATION_SIZE_LIMIT = 100;
	private static final String DEFAULT_VERSION = "2023-06-01";
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final String DEFAULT_MODEL = "claude-3-sonnet-20240229";

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
		String urlStr = Caster.toStringTrim(props.remove(KeyConstants._URL, DEFAULT_URL), DEFAULT_URL);
		if (Util.isEmpty(urlStr, true)) urlStr = DEFAULT_URL;
		try {
			baseURL = HTTPUtil.toURL(urlStr, HTTPUtil.ENCODED_AUTO);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		// Timeout
		connectTimeout = Caster.toIntValue(props.remove("connectTimeout", null), DEFAULT_CONNECT_TIMEOUT);
		if (connectTimeout <= 0) connectTimeout = DEFAULT_CONNECT_TIMEOUT;

		socketTimeout = Caster.toIntValue(props.remove("socketTimeout", null), DEFAULT_SOCKET_TIMEOUT);
		if (socketTimeout <= 0) socketTimeout = DEFAULT_SOCKET_TIMEOUT;

		// temperature
		temperature = Caster.toDouble(props.remove(KeyConstants._temperature, null), null);
		if (temperature != null && (temperature < 0D || temperature > 1D)) {
			throw new ApplicationException("temperature has to be a number between 0 and 1, now it is [" + temperature + "]");
		}
		// conversation Size Limit
		conversationSizeLimit = Caster.toIntValue(props.remove("conversationSizeLimit", null), DEFAULT_CONVERSATION_SIZE_LIMIT);

		// Model
		// TODO read available models and throw exception
		model = Caster.toStringTrim(props.remove(KeyConstants._model, DEFAULT_MODEL), DEFAULT_MODEL);
		if (StringUtil.isEmpty(model, true)) model = DEFAULT_MODEL;

		// System Message
		systemMessage = Caster.toStringTrim(props.remove(KeyConstants._message, null), null);

		// version
		version = Caster.toStringTrim(props.remove(KeyConstants._version, DEFAULT_VERSION), DEFAULT_VERSION);
		if (StringUtil.isEmpty(version, true)) version = DEFAULT_VERSION;

		// charset
		charset = Caster.toStringTrim(props.remove(KeyConstants._charset, null), DEFAULT_CHARSET);
		if (Util.isEmpty(charset, true)) charset = DEFAULT_CHARSET;

		// headers
		headers = toHeaders(Caster.toStruct(props.remove("headers", null), null));

		this.custom = props;

		return this;
	}

	@Override
	public AISession createSession(String initialMessage, Conversation[] history, int limit, double temp, int connectTimeout, int socketTimeout) {
		return new ClaudeSession(this, StringUtil.isEmpty(initialMessage, true) ? systemMessage : initialMessage.trim(), history, limit, temp, connectTimeout, socketTimeout);
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getModel() {
		return model;
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
	public List<AIModel> getModels() {
		// not supported by Claude YET
		return new ArrayList<>();

	}

	@Override
	public List<AIModel> getModels(List<AIModel> defaultValue) {
		return defaultValue;
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