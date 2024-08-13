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

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.commons.net.http.httpclient.HeaderImpl;
import lucee.loader.util.Util;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.AIEngineFactory;
import lucee.runtime.ai.AIEngineFile;
import lucee.runtime.ai.AIEngineSupport;
import lucee.runtime.ai.AIFile;
import lucee.runtime.ai.AIFileSupport;
import lucee.runtime.ai.AIModel;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class OpenAIEngine extends AIEngineSupport implements AIEngineFile {
	// https://platform.openai.com/docs/api-reference/introduction

	private static final long DEFAULT_TIMEOUT = 3000L;
	private static final String DEFAULT_CHARSET = null;
	private static final String DEFAULT_MIMETYPE = null;
	private static final URL DEFAULT_URL_OPENAI;
	private static final URL DEFAULT_URL_OLLAMA;

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
	public Double temperature = null;

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
		if (!Util.isEmpty(str, true)) secretKey = str.trim();

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
		// temperature
		temperature = Caster.toDouble(properties.get(KeyConstants._temperature, null), null);
		if (temperature != null && (temperature < 0D || temperature > 1D)) {
			throw new ApplicationException("temperature has to be a number between 0 and 1, now it is [" + temperature + "]");
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
				throwIfError(raw);

				Array data = Caster.toArray(raw.get(KeyConstants._data));
				Iterator<Object> it = data.valueIterator();
				List<AIModel> list = new ArrayList<>();
				while (it.hasNext()) {
					list.add(new OpenAIModel(Caster.toStruct(it.next()), charset));
				}
				return list;
			}
			throw new ApplicationException("OpenAI did answer with the mime type [" + ct.getMimeType() + "] that is not supported, only [application/json] is supported");

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void throwIfError(Struct raw) throws PageException {
		Struct err = Caster.toStruct(raw.get(KeyConstants._error, null), null);
		if (err != null) {
			throw AIUtil.toException(this, Caster.toString(err.get(KeyConstants._message)), Caster.toString(err.get(KeyConstants._type, null), null),
					Caster.toString(err.get(KeyConstants._code, null), null));
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
				post.setHeader("Content-Type", "application/json");
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
					throwIfError(raw);
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
	public String uploadFile(Resource jsonl) throws PageException {
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
				builder.addTextBody("purpose", "fine-tune", org.apache.http.entity.ContentType.TEXT_PLAIN);
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
					throwIfError(raw);
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
						throwIfError(raw);
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
}
