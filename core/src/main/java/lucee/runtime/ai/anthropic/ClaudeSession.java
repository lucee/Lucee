package lucee.runtime.ai.anthropic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

public final class ClaudeSession extends AISessionSupport {
	private ClaudeEngine engine;
	private String systemMessage;

	public ClaudeSession(ClaudeEngine engine, String systemMessage, Conversation[] history, int limit, double temp, int connectTimeout, int socketTimeout) {
		super(engine, history, limit, temp, connectTimeout, socketTimeout);
		this.engine = engine;
		this.systemMessage = systemMessage;
	}

	@Override
	public Response inquiry(String message, AIResponseListener listener) throws PageException {
		try {
			Struct requestBody = buildRequest(message, null, listener != null);
			return executeRequest(requestBody, message, null, listener);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Response inquiry(List<Part> parts, AIResponseListener listener) throws PageException {
		try {
			Struct requestBody = buildRequest(null, parts, listener != null);
			return executeRequest(requestBody, null, parts, listener);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private Struct buildRequest(String message, List<Part> parts, boolean stream) throws PageException {
		Struct requestBody = new StructImpl();
		requestBody.set(KeyConstants._model, engine.getModel());
		requestBody.set("max_tokens", 4096);
		requestBody.set("stream", stream);

		// Add temperature if set
		Double temperature = getTemperature();
		if (temperature != null) {
			requestBody.set("temperature", temperature);
		}

		// Set system message at top level if exists
		if (!StringUtil.isEmpty(systemMessage)) {
			requestBody.set(KeyConstants._system, systemMessage);
		}

		// Build messages array
		Array messages = new ArrayImpl();

		// Add conversation history
		for (Conversation c: getHistoryAsList()) {
			if (c.getRequest().isMultiPart()) messages.append(createMessage("user", null, c.getRequest().getQuestions()));
			else messages.append(createMessage("user", c.getRequest().getQuestion(), null));
			messages.append(createMessage("assistant", AIUtil.extractStringAnswer(c.getResponse()), null));
		}

		// Add current message/parts
		if (parts != null) {
			messages.append(createMessage("user", null, parts));
		}
		else {
			messages.append(createMessage("user", message, null));
		}

		requestBody.set("messages", messages);

		// custom
		if (engine.custom != null && !engine.custom.isEmpty()) {
			Iterator<Entry<Key, Object>> it = engine.custom.entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				if (!requestBody.containsKey(e.getKey())) {
					requestBody.set(e.getKey(), e.getValue());
					LogUtil.logx(null, Log.LEVEL_DEBUG, "ai", "add custom value [" + e.getKey() + "]", "ai", "application");
				}
			}
		}

		return requestBody;
	}

	private Struct createMessage(String role, String content, List<Part> parts) throws PageException {
		Struct message = new StructImpl();
		message.setEL(KeyConstants._role, role);

		if (parts != null) {
			// Multipart content - Claude expects array of content blocks
			message.setEL(KeyConstants._content, createContent(parts));
		}
		else {
			// Simple text content
			message.setEL(KeyConstants._content, content);
		}

		return message;
	}

	private Object createContent(List<Part> parts) throws PageException {
		Array contentArray = new ArrayImpl();
		for (Part part: parts) {
			contentArray.append(createContent(part));
		}
		return contentArray;
	}

	private Struct createContent(Part part) throws PageException {
		Struct contentBlock = new StructImpl(StructImpl.TYPE_LINKED);

		if (part.isText()) {
			contentBlock.set(KeyConstants._type, "text");
			contentBlock.set(KeyConstants._text, part.getAsString());
		}
		else {
			String contentType = part.getContentType();
			String base64 = Caster.toBase64(part.getAsBinary());

			Struct source = new StructImpl(StructImpl.TYPE_LINKED);
			source.set(KeyConstants._type, "base64");
			source.set("media_type", contentType);
			source.set(KeyConstants._data, base64);
			contentBlock.set("source", source);

			// Check if it's an image
			if (contentType != null && contentType.startsWith("image/")) {
				// Images: supported formats are jpeg, png, gif, webp
				contentBlock.set(KeyConstants._type, "image");
			}
			// Check if it's a PDF
			else {
				// PDFs need to use document type (Claude API 2024-06-01+)
				contentBlock.set(KeyConstants._type, "document");
			}
		}
		return contentBlock;
	}

	private Response executeRequest(Struct requestBody, String message, List<Part> parts, AIResponseListener listener) throws Exception {
		// Make API request
		HttpPost post = new HttpPost(engine.getBaseURL().toExternalForm() + "messages");
		post.setHeader("Content-Type", "application/json");
		post.setHeader("x-api-key", engine.getApiKey());
		post.setHeader("anthropic-version", engine.getVersion());

		// custom headers
		if (engine.headers != null) {
			for (Entry<String, String> e: engine.headers.entrySet()) {
				post.setHeader(e.getKey(), e.getValue());
			}
		}

		// Convert request body to JSON
		JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
		String str = json.serialize(null, requestBody, SerializationSettings.SERIALIZE_AS_COLUMN, null);

		LogUtil.logx(null, Log.LEVEL_DEBUG, "ai", "request message send by [" + engine.getLabel() + "]: " + str, "ai", "application");

		// Create entity and set it to the post request
		StringEntity entity = new StringEntity(str, engine.getCharset());
		post.setEntity(entity);

		// Set timeout
		RequestConfig config = AISessionSupport.setTimeout(RequestConfig.custom(), this).build();
		post.setConfig(config);

		// Execute request
		try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {

			HttpEntity responseEntity = response.getEntity();
			Header ct = responseEntity.getContentType();
			MimeType mt = MimeType.getInstance(ct.getValue());

			String t = mt.getType() + "/" + mt.getSubtype();
			String cs = mt.getCharset() != null ? mt.getCharset().toString() : engine.getCharset();

			// Handle JSON response
			if ("application/json".equals(t)) {
				if (Util.isEmpty(cs, true)) cs = engine.getCharset();
				String rawStr = EntityUtils.toString(responseEntity, engine.getCharset());

				LogUtil.logx(null, Log.LEVEL_DEBUG, "ai", "response recieved send by [" + engine.getLabel() + "]: " + rawStr, "ai", "application");
				Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, rawStr));

				// Check for errors
				Struct err = Caster.toStruct(raw.get(KeyConstants._error, null), null);
				if (err != null) {
					throw AIUtil.toException(this.getEngine(), Caster.toString(err.get(KeyConstants._message)), Caster.toString(err.get(KeyConstants._type, null), null),
							Caster.toString(err.get(KeyConstants._code, null), null), AIUtil.getStatusCode(response));
				}

				// Create response object
				ClaudeResponse r = new ClaudeResponse(raw, cs);
				AIUtil.addConversation(this, getHistoryAsList(), new ConversationImpl(RequestSupport.getInstance(message, parts), r));
				return r;
			}
			// Handle streaming response
			else if ("text/event-stream".equals(t)) {
				if (Util.isEmpty(cs, true)) cs = engine.getCharset();
				JSONExpressionInterpreter interpreter = new JSONExpressionInterpreter();
				ClaudeStreamResponse r = new ClaudeStreamResponse(cs, listener);

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
						if ("[DONE]".equals(line)) break;
						LogUtil.logx(null, Log.LEVEL_DEBUG, "ai", "response chunk recieved send by [" + engine.getLabel() + "]: " + line, "ai", "application");
						prev = Caster.toStruct(interpreter.interpret(null, line));
					}
					if (prev != null) {
						r.addPart(prev, index, true);
					}
				}

				AIUtil.addConversation(this, getHistoryAsList(), new ConversationImpl(RequestSupport.getInstance(message, parts), r));
				return r;
			}
			else {
				throw unsupportedMimeTypeException(response, responseEntity, cs, t);
			}
		}
	}

	@Override
	public void release() throws PageException {
		// Nothing to release
	}

	@Override
	public String getSystemMessage() {
		return systemMessage;
	}
}