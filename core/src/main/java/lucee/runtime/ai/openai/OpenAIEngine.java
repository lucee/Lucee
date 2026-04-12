package lucee.runtime.ai.openai;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.MimeType;
import lucee.commons.net.HTTPUtil;
import lucee.loader.util.Util;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.AIEngineFile;
import lucee.runtime.ai.AIEngineSupport;
import lucee.runtime.ai.AIFile;
import lucee.runtime.ai.AIFileSupport;
import lucee.runtime.ai.AIModel;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AISessionSupport;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.ai.Conversation;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;

public final class OpenAIEngine extends AIEngineSupport implements AIEngineFile {
	// https://platform.openai.com/docs/api-reference/introduction

	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final String DEFAULT_MIMETYPE = null;
	private static final URL DEFAULT_URL_OPENAI;
	private static final URL DEFAULT_URL_OLLAMA;
	private static final URL DEFAULT_URL_PERPLEXITY;
	private static final URL DEFAULT_URL_DEEPSEEK;
	private static final URL DEFAULT_URL_GROK;
	private static final int DEFAULT_CONVERSATION_SIZE_LIMIT = 100;

	// TODO
	// post https://api.openai.com/v1/audio/speech
	// post https://api.openai.com/v1/audio/transcriptions
	// post https://api.openai.com/v1/audio/translations

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

		// perplexity
		tmp = null;
		try {
			tmp = new URL("https://api.perplexity.ai/");
		}
		catch (MalformedURLException e) {
			log(e);
		}
		DEFAULT_URL_PERPLEXITY = tmp;

		// deep seek
		tmp = null;
		try {
			tmp = new URL("https://api.deepseek.com/");
		}
		catch (MalformedURLException e) {
			log(e);
		}
		DEFAULT_URL_DEEPSEEK = tmp;

		// grok
		tmp = null;
		try {
			tmp = new URL("https://api.x.ai/v1/");
		}
		catch (MalformedURLException e) {
			log(e);
		}
		DEFAULT_URL_GROK = tmp;
	}

	String secretKey;
	int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
	int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	String charset;
	ProxyData proxy = null;
	Map<String, String> formfields = null;
	String model;
	String label = "OpenAI";
	private String systemMessage;

	private URL baseURL;
	public Double temperature = null;
	private int conversationSizeLimit = DEFAULT_CONVERSATION_SIZE_LIMIT;
	private Boolean multiPartSupported;
	URI chatCompletionsURI;
	Struct custom;
	Map<String, String> headers;

	@Override
	public AIEngine init(ClassDefinition<? extends AIEngine> cd, Struct properties, String name, String _default, String id) throws PageException {
		super.init(cd, properties, name, _default, id);
		Struct props = (Struct) properties.duplicate(true);
		// URL
		/// we support some hard coded types to keep it simple
		String str = Caster.toStringTrim(props.remove(KeyConstants._type, null), null);
		if (!Util.isEmpty(str, true) && !str.equalsIgnoreCase("other")) {
			if ("chatgpt".equals(str) || "openai".equals(str)) {
				label = "ChatGPT";
				baseURL = DEFAULT_URL_OPENAI;
				multiPartSupported = true;
			}
			else if ("ollama".equals(str)) {
				label = "Ollama";
				baseURL = DEFAULT_URL_OLLAMA;
			}
			else if ("perplexity".equals(str)) {
				label = "Perplexity";
				baseURL = DEFAULT_URL_PERPLEXITY;
				multiPartSupported = false;
			}
			else if ("deepseek".equals(str)) {
				label = "DeepSeek";
				baseURL = DEFAULT_URL_DEEPSEEK;
				multiPartSupported = false;
			}
			else if ("grok".equals(str)) {
				label = "Grok";
				baseURL = DEFAULT_URL_GROK;
				multiPartSupported = false;
			}

			else throw new ApplicationException(
					"ATM only 5 types are supported [deepseek, grok, openai, ollama, perplexity], for any other endpoint simply define the attribute `url` that looks like this [https://api.lucee.com/v1/].");
		}
		else {
			str = Caster.toStringTrim(props.remove(KeyConstants._URL, null), null);
			if (!Util.isEmpty(str, true)) {
				if (!str.endsWith("/")) str += "/";
				try {
					baseURL = HTTPUtil.toURL(str.trim(), HTTPUtil.ENCODED_AUTO);

					if (baseURL.equals(DEFAULT_URL_OPENAI)) label = "ChatGPT";
					if (baseURL.equals(DEFAULT_URL_PERPLEXITY)) label = "Perplexity";
					if (baseURL.equals(DEFAULT_URL_DEEPSEEK)) label = "DeepSeek";
					if (baseURL.equals(DEFAULT_URL_GROK)) label = "Grok";

				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}
			else baseURL = DEFAULT_URL_OPENAI;
		}

		// secret key
		str = Caster.toStringTrim(props.remove(KeyConstants._secretKey, null), null);
		if (!Util.isEmpty(str, true)) secretKey = str;

		// conversation Size Limit
		conversationSizeLimit = Caster.toIntValue(props.remove("conversationSizeLimit", null), DEFAULT_CONVERSATION_SIZE_LIMIT);

		// timeout
		connectTimeout = Caster.toIntValue(props.remove("connectTimeout", null), DEFAULT_CONNECT_TIMEOUT);
		if (connectTimeout <= 0) connectTimeout = DEFAULT_CONNECT_TIMEOUT;

		socketTimeout = Caster.toIntValue(props.remove("socketTimeout", null), DEFAULT_SOCKET_TIMEOUT);
		if (socketTimeout <= 0) socketTimeout = DEFAULT_SOCKET_TIMEOUT;

		// charset
		charset = Caster.toStringTrim(props.remove(KeyConstants._charset, null), DEFAULT_CHARSET);
		if (Util.isEmpty(charset, true)) charset = DEFAULT_CHARSET;

		// model
		model = Caster.toStringTrim(props.remove(KeyConstants._model, null), null);
		if (Util.isEmpty(model, true)) {
			// nice to have
			String appendix = "";
			List<String> models = AIUtil.getModelNames(this);
			if (models.size() > 0) {
				model = models.get(0);
			}
			else {
				appendix = " There are no models available.";
			}

			if (Util.isEmpty(model, true)) throw new ApplicationException("the property [model] is required for a OpenAI Engine!." + appendix);
		}

		// temperature
		temperature = Caster.toDouble(props.remove(KeyConstants._temperature, null), null);
		if (temperature != null && (temperature < 0D || temperature > 1D)) {
			throw new ApplicationException("temperature has to be a number between 0 and 1, now it is [" + temperature + "]");
		}

		// message
		systemMessage = Caster.toStringTrim(props.remove(KeyConstants._message, null), null);
		if (!Util.isEmpty(systemMessage, true)) systemMessage = systemMessage.trim();

		// validate
		boolean validate = Caster.toBooleanValue(props.remove(KeyConstants._validate, null), false);
		if (validate) AIUtil.valdate(this, getConnectTimeout(), getSocketTimeout());

		// headers
		headers = toHeaders(Caster.toStruct(props.remove("headers", null), null));

		this.custom = props;

		return this;
	}

	@Override
	public AISession createSession(String inialMessage, Conversation[] history, int limit, double temp, int connectTimeout, int socketTimeout) {
		return new OpenAISession(this, StringUtil.isEmpty(inialMessage, true) ? systemMessage : inialMessage.trim(), history, limit, temp, connectTimeout, socketTimeout);
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

	public Boolean isMultiPartSupported() {
		return multiPartSupported;
	}

	@Override
	public List<AIModel> getModels() throws PageException {
		try {
			HttpGet get = new HttpGet(new URL(baseURL, "models").toExternalForm());
			get.setHeader("Authorization", "Bearer " + secretKey);
			get.setHeader("Content-Type", AIUtil.createJsonContentType(charset));

			RequestConfig config = AISessionSupport.setRedirect(AISessionSupport.setTimeout(RequestConfig.custom(), getConnectTimeout(), getSocketTimeout())).build();
			get.setConfig(config);

			try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(get)) {

				HttpEntity responseEntity = response.getEntity();
				Header ct = responseEntity.getContentType();
				MimeType mt = MimeType.getInstance(ct.getValue());

				String t = mt.getType() + "/" + mt.getSubtype();
				String cs = mt.getCharset() != null ? mt.getCharset().toString() : charset;

				if ("application/json".equals(t)) {
					String rawStr = EntityUtils.toString(responseEntity, charset);
					Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, rawStr));
					throwIfError(raw, AIUtil.getStatusCode(response));

					List<AIModel> list = new ArrayList<>();
					Object o = raw.get(KeyConstants._data, null);
					if (o != null) {
						Array data = Caster.toArray(o);
						Iterator<Object> it = data.valueIterator();
						while (it.hasNext()) {
							list.add(new OpenAIModel(Caster.toStruct(it.next()), charset));
						}
					}
					else if (!CollectionUtil.hasKey(raw, KeyConstants._data)) {
						throw new ApplicationException("unable to read models from response [" + rawStr + "]");
					}
					return list;
				}
				throw new ApplicationException("OpenAI API returned unsupported mime type [" + t + "], only [application/json] is supported");
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void throwIfError(Struct raw, int statusCode) throws PageException {
		Struct err = Caster.toStruct(raw.get(KeyConstants._error, null), null);
		if (err != null) {
			throw AIUtil.toException(this, Caster.toString(err.get(KeyConstants._message)), Caster.toString(err.get(KeyConstants._type, null), null),
					Caster.toString(err.get(KeyConstants._code, null), null), statusCode);
		}
	}

	public Struct createFineTuningJob(String trainingFileId) throws PageException {
		try {
			URI url = new URI(getBaseURL() + "fine_tuning/jobs");
			InputStream is = null;
			// Create HttpClient
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				// Create HttpPost request
				HttpPost post = new HttpPost(url);
				post.setHeader("Content-Type", AIUtil.createJsonContentType(charset));
				post.setHeader("Authorization", "Bearer " + secretKey);

				Struct sct = new StructImpl();
				sct.set("training_file", trainingFileId);
				sct.set(KeyConstants._model, model);
				JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
				String str = json.serialize(null, sct, SerializationSettings.SERIALIZE_AS_COLUMN, null);
				StringEntity entity = new StringEntity(str);
				post.setEntity(entity);

				// Execute the request
				try (CloseableHttpResponse response = httpClient.execute(post)) {
					HttpEntity responseEntity = response.getEntity();
					String responseString = EntityUtils.toString(responseEntity, charset);

					Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, responseString));
					throwIfError(raw, AIUtil.getStatusCode(response));
					return raw;
				}
			}
			finally {
				IOUtil.close(is);
			}

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public String uploadFile(Resource jsonl, String purpose) throws PageException {
		if (StringUtil.isEmpty(purpose, true)) purpose = "assistants";
		else purpose = purpose.trim();

		try {
			URI url = new URI(getBaseURL() + "files");
			InputStream is = null;
			// Create HttpClient
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				// Create HttpPost request
				HttpPost uploadFile = new HttpPost(url);
				uploadFile.setHeader("Authorization", "Bearer " + secretKey);

				// Build the multipart entity
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.addTextBody("purpose", purpose, org.apache.http.entity.ContentType.TEXT_PLAIN);
				builder.addBinaryBody("file", is = jsonl.getInputStream(), org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM, jsonl.getName());

				HttpEntity multipart = builder.build();
				uploadFile.setEntity(multipart);

				// Execute the request
				CloseableHttpResponse response = httpClient.execute(uploadFile);
				try {

					// Get response
					HttpEntity responseEntity = response.getEntity();
					String responseString = EntityUtils.toString(responseEntity, charset);

					/*
					 * { "object": "file", "id": "file-NvDokaQZjf06auxzzU5ONayK", "purpose": "fine-tune", "filename":
					 * "markdown_data.jsonl", "bytes": 179207, "created_at": 1723452279, "status": "processed",
					 * "status_details": null }
					 */

					Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, responseString));
					throwIfError(raw, AIUtil.getStatusCode(response));
					return Caster.toString(raw.get(KeyConstants._id));

				}
				finally {
					response.close();
				}
			}
			finally {
				IOUtil.close(is);
			}

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public List<AIFile> listFiles() throws PageException {
		try {
			URI url = new URI(getBaseURL() + "files");
			InputStream is = null;
			// Create HttpClient
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				// Create HttpPost request
				HttpGet get = new HttpGet(url);
				get.setHeader("Authorization", "Bearer " + secretKey);

				// Execute the request
				try (CloseableHttpResponse response = httpClient.execute(get)) {
					// Get response
					HttpEntity responseEntity = response.getEntity();
					List<AIFile> list = new ArrayList<>();
					if ("application/json".equals(responseEntity.getContentType().getValue())) {
						String responseString = EntityUtils.toString(responseEntity, charset);
						Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, responseString));
						throwIfError(raw, AIUtil.getStatusCode(response));
						Array data = Caster.toArray(raw.get(KeyConstants._data));
						Iterator<?> it = data.getIterator();
						Struct sct;
						while (it.hasNext()) {
							sct = Caster.toStruct(it.next());
							list.add(new AIFileSupport(

									Caster.toString(sct.get(KeyConstants._object)),

									Caster.toString(sct.get(KeyConstants._id)),

									Caster.toString(sct.get("purpose")),

									Caster.toString(sct.get(KeyConstants._filename)),

									Caster.toLongValue(sct.get(KeyConstants._bytes)),

									Caster.toDatetime(new Date(Caster.toLongValue(sct.get("created_at")) * 1000L), null),

									Caster.toString(sct.get(KeyConstants._status)),

									Caster.toString(sct.get("status_details", null))));

						}
					}
					return list;
				}
			}
			finally {
				IOUtil.close(is);
			}

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public AIFile getFile(String id) throws PageException {
		try {
			URI url = new URI(getBaseURL() + "files/" + id.trim());
			InputStream is = null;
			// Create HttpClient
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				// Create HttpPost request
				HttpGet get = new HttpGet(url);
				get.setHeader("Authorization", "Bearer " + secretKey);

				// Execute the request
				try (CloseableHttpResponse response = httpClient.execute(get)) {
					// Get response
					HttpEntity responseEntity = response.getEntity();
					if ("application/json".equals(responseEntity.getContentType().getValue())) {
						String responseString = EntityUtils.toString(responseEntity, charset);
						Struct sct = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, responseString));
						throwIfError(sct, AIUtil.getStatusCode(response));
						return new AIFileSupport(

								Caster.toString(sct.get(KeyConstants._object)),

								Caster.toString(sct.get(KeyConstants._id)),

								Caster.toString(sct.get("purpose")),

								Caster.toString(sct.get(KeyConstants._filename)),

								Caster.toLongValue(sct.get(KeyConstants._bytes)),

								Caster.toDatetime(new Date(Caster.toLongValue(sct.get("created_at")) * 1000L), null),

								Caster.toString(sct.get(KeyConstants._status)),

								Caster.toString(sct.get("status_details", null)));

					}
					throw new ApplicationException("The AI did answer with the mime type [" + responseEntity.getContentType().getValue()
							+ "] that is not supported, only [application/json] is supported");
				}

			}
			finally {
				IOUtil.close(is);
			}

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public InputStream getFileContent(String id) throws PageException {
		try {
			URI url = new URI(getBaseURL() + "files/" + id.trim() + "/content");

			// Create HttpClient
			CloseableHttpClient httpClient = HttpClients.createDefault();

			// Create HttpGet request
			HttpGet get = new HttpGet(url);
			get.setHeader("Authorization", "Bearer " + secretKey);

			// Execute the request
			CloseableHttpResponse response = httpClient.execute(get);
			HttpEntity responseEntity = response.getEntity();

			if ("application/json".equals(responseEntity.getContentType().getValue())) {
				String responseString = EntityUtils.toString(responseEntity, charset);
				Struct sct = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, responseString));
				throwIfError(sct, AIUtil.getStatusCode(response));
			}

			// Return the InputStream, caller is responsible for closing it
			return responseEntity.getContent();

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public boolean deleteFile(String id) throws PageException {
		try {
			URI url = new URI(getBaseURL() + "files/" + id.trim());
			InputStream is = null;
			// Create HttpClient
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				// Create HttpPost request
				HttpDelete get = new HttpDelete(url);
				get.setHeader("Authorization", "Bearer " + secretKey);

				// Execute the request
				try (CloseableHttpResponse response = httpClient.execute(get)) {
					// Get response
					HttpEntity responseEntity = response.getEntity();
					if ("application/json".equals(responseEntity.getContentType().getValue())) {
						String responseString = EntityUtils.toString(responseEntity, charset);
						Struct sct = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, responseString));
						throwIfError(sct, AIUtil.getStatusCode(response));
						return Caster.toBooleanValue(sct.get(KeyConstants._deleted));

					}
					throw new ApplicationException("The AI did answer with the mime type [" + responseEntity.getContentType().getValue()
							+ "] that is not supported, only [application/json] is supported");
				}

			}
			finally {
				IOUtil.close(is);
			}

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
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
