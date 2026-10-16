package lucee.runtime.ai.google;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.MimeType;
import lucee.loader.util.Util;
import lucee.runtime.ai.AIResponseListener;
import lucee.runtime.ai.AISessionSupport;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.ai.Conversation;
import lucee.runtime.ai.ConversationImpl;
import lucee.runtime.ai.Part;
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
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class GeminiSession extends AISessionSupport {
	private GeminiEngine geminiEngine;
	private String systemMessage;

	public GeminiSession(GeminiEngine engine, String systemMessage, Conversation[] history, int limit, double temp, int connectTimeout, int socketTimeout) {
		super(engine, history, limit, temp, connectTimeout, socketTimeout);
		this.geminiEngine = engine;
		this.systemMessage = systemMessage;
	}

	@Override
	public Response inquiry(String message, AIResponseListener listener) throws PageException {
		try {
			Struct root = buildRequestRoot(message, null);
			return executeRequest(root, message, null, listener);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Response inquiry(List<Part> parts, AIResponseListener listener) throws PageException {
		try {
			Struct root = buildRequestRoot(null, parts);
			return executeRequest(root, null, parts, listener);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private Struct buildRequestRoot(String message, List<Part> parts) throws PageException {
		Struct root = new StructImpl(StructImpl.TYPE_LINKED);
		Array contents = new ArrayImpl();
		root.set(KeyConstants._contents, contents);
		Struct generationConfig = null;

		// Add generationConfig
		if (geminiEngine.generationConfig != null) {
			generationConfig = (Struct) geminiEngine.generationConfig.duplicate(true);
		}

		// Add temperature
		Double temperature = getTemperature();
		if (temperature != null) {
			if (generationConfig == null) generationConfig = new StructImpl();
			generationConfig.set(KeyConstants._temperature, temperature);
		}

		// generationConfig
		if (generationConfig != null) root.set("generationConfig", generationConfig);

		// Add system message
		if (!StringUtil.isEmpty(systemMessage, true)) {
			contents.append(createParts("user", systemMessage, null));
		}

		// Add history
		for (Conversation c: getHistoryAsList()) {
			if (c.getRequest().isMultiPart()) contents.append(createParts("user", null, c.getRequest().getQuestions()));
			else contents.append(createParts("user", c.getRequest().getQuestion(), null));
			contents.append(createParts("model", AIUtil.extractStringAnswer(c.getResponse()), null));
		}

		// Add current message/parts
		if (parts != null) {
			contents.append(createParts("user", null, parts));
		}
		else if (message != null) {
			contents.append(createParts("user", message, null));
		}
		else {
			throw new ApplicationException("you need to define parts or a message");
		}

		// custom
		if (geminiEngine.custom != null && !geminiEngine.custom.isEmpty()) {
			Iterator<Entry<Key, Object>> it = geminiEngine.custom.entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				if (!root.containsKey(e.getKey())) {
					root.set(e.getKey(), e.getValue());
				}
			}
		}
		return root;
	}

	private Response executeRequest(Struct root, String messageText, List<Part> parts, AIResponseListener listener) throws Exception {
		URL url = geminiEngine.toURL(geminiEngine.baseURL, GeminiEngine.CHAT, listener != null ? GeminiEngine.TYPE_STREAM : GeminiEngine.TYPE_REG);
		HttpPost post = new HttpPost(url.toExternalForm());
		post.setHeader("Content-Type", AIUtil.createJsonContentType(geminiEngine.charset));

		// custom headers
		if (geminiEngine.headers != null) {
			for (Entry<String, String> e: geminiEngine.headers.entrySet()) {
				post.setHeader(e.getKey(), e.getValue());
			}
		}

		JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
		String str = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_COLUMN, null);
		LogUtil.logx(null, Log.LEVEL_DEBUG, "ai", "send request message to [" + url.toExternalForm() + "] by [" + geminiEngine.getLabel() + "]: " + str, "ai", "application");

		StringEntity entity = new StringEntity(str, geminiEngine.charset);
		post.setEntity(entity);

		RequestConfig config = AISessionSupport.setTimeout(RequestConfig.custom(), this).build();
		post.setConfig(config);

		try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {

			HttpEntity responseEntity = response.getEntity();
			Header ct = responseEntity.getContentType();
			MimeType mt = MimeType.getInstance(ct.getValue());

			String t = mt.getType() + "/" + mt.getSubtype();
			String cs = mt.getCharset() != null ? mt.getCharset().toString() : geminiEngine.charset;

			if ("text/event-stream".equals(t)) {
				if (Util.isEmpty(cs, true)) cs = geminiEngine.charset;
				JSONExpressionInterpreter interpreter = new JSONExpressionInterpreter();
				GeminiStreamResponse r = new GeminiStreamResponse(cs, listener);

				try (BufferedReader reader = new BufferedReader(
						cs == null ? new InputStreamReader(responseEntity.getContent()) : new InputStreamReader(responseEntity.getContent(), cs))) {
					String line;
					int index = 0;
					Struct prev = null;
					while ((line = reader.readLine()) != null) {
						if (prev != null) {
							r.addPart(prev, index++, false);
							prev = null;
						}

						if (!line.startsWith("data: ")) continue;
						line = line.substring(6);
						LogUtil.logx(null, Log.LEVEL_DEBUG, "ai", "response chunk recieved send by [" + geminiEngine.getLabel() + "]: " + line, "ai", "application");
						prev = Caster.toStruct(interpreter.interpret(null, line));
					}
					if (prev != null) {
						r.addPart(prev, index, true);
					}
				}

				AIUtil.addConversation(this, getHistoryAsList(), new ConversationImpl(RequestSupport.getInstance(messageText, parts), r));
				return r;
			}
			else if ("application/json".equals(t)) {
				if (Util.isEmpty(cs, true)) cs = geminiEngine.charset;
				String rawStr = EntityUtils.toString(responseEntity, geminiEngine.charset);
				LogUtil.logx(null, Log.LEVEL_DEBUG, "ai", "response recieved send by [" + geminiEngine.getLabel() + "]: " + rawStr, "ai", "application");
				Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, rawStr));

				Struct err = Caster.toStruct(raw.get(KeyConstants._error, null), null);
				if (err != null) {
					throw AIUtil.toException(this.getEngine(), Caster.toString(err.get(KeyConstants._message)), Caster.toString(err.get(KeyConstants._status, null), null),
							Caster.toString(err.get(KeyConstants._code, null), null), AIUtil.getStatusCode(response));
				}

				GeminiResponse r = new GeminiResponse(raw, cs);
				AIUtil.addConversation(this, getHistoryAsList(), new ConversationImpl(RequestSupport.getInstance(messageText, parts), r));
				return r;
			}
			else {
				throw unsupportedMimeTypeException(response, responseEntity, cs, t);
			}
		}
	}

	private Struct createParts(String role, String msg, List<Part> parts) throws PageException {
		Struct sctContents = new StructImpl(StructImpl.TYPE_LINKED);
		Array partsArray = new ArrayImpl();

		if (parts != null) {
			// Multipart content
			for (Part part: parts) {
				Struct sct = new StructImpl(StructImpl.TYPE_LINKED);

				if (part.isText()) {
					sct.set(KeyConstants._text, part.getAsString());
				}
				else {
					// Binary content (images, audio, files, etc.)
					// Gemini expects: {"inlineData": {"mimeType": "image/png", "data": "base64..."}}
					Struct inlineData = new StructImpl(StructImpl.TYPE_LINKED);
					inlineData.set("mimeType", part.getContentType());
					inlineData.set("data", Caster.toBase64(part.getAsBinary()));
					sct.set("inlineData", inlineData);
				}

				partsArray.append(sct);
			}
		}
		else if (msg != null) {
			// Simple text message
			Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
			sct.set(KeyConstants._text, msg);
			partsArray.append(sct);
		}

		if (role != null) sctContents.set(KeyConstants._role, role.trim());
		sctContents.set(KeyConstants._parts, partsArray);

		return sctContents;
	}

	@Override
	public void release() {
		// nothing to give up
	}

	@Override
	public String getSystemMessage() {
		return systemMessage;
	}
}