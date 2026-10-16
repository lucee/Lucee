package lucee.runtime.ai.openai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.MimeType;
import lucee.loader.util.Util;
import lucee.runtime.ai.AIResponseListener;
import lucee.runtime.ai.AISessionSupport;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.ai.Conversation;
import lucee.runtime.ai.ConversationImpl;
import lucee.runtime.ai.Part;
import lucee.runtime.ai.PartImpl;
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

public final class OpenAISession extends AISessionSupport {

	private OpenAIEngine openaiEngine;
	private String systemMessage;

	public OpenAISession(OpenAIEngine engine, String systemMessage, Conversation[] history, int limit, double temp, int connectTimeout, int socketTimeout) {
		super(engine, history, limit, temp, connectTimeout, socketTimeout);
		this.openaiEngine = engine;
		this.systemMessage = systemMessage;
	}

	@Override
	public Response inquiry(String message, AIResponseListener listener) throws PageException {
		try {
			Struct request = buildRequest(message, null, listener != null);
			String messageText = message;
			return executeRequest(request, messageText, null, listener);
		}
		catch (SocketTimeoutException ste) {
			ApplicationException ae = new ApplicationException(
					"A socket timeout occurred while querying the AI Engine [" + openaiEngine.getLabel() + "]. The configured timeout was " + getSocketTimeout() + " ms.");
			ExceptionUtil.initCauseEL(ae, ste);
			throw ae;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Response inquiry(List<Part> parts, AIResponseListener listener) throws PageException {
		try {
			Struct request = buildRequest(null, parts, listener != null);
			return executeRequest(request, null, parts, listener);
		}
		catch (SocketTimeoutException ste) {
			ApplicationException ae = new ApplicationException(
					"A socket timeout occurred while querying the AI Engine [" + openaiEngine.getLabel() + "]. The configured timeout was " + getSocketTimeout() + " ms.");
			ExceptionUtil.initCauseEL(ae, ste);
			throw ae;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private Struct buildRequest(String message, List<Part> parts, boolean stream) throws PageException {
		Array messages = new ArrayImpl();

		// Add system message
		if (!StringUtil.isEmpty(systemMessage)) {
			Struct msg = new StructImpl(StructImpl.TYPE_LINKED);
			msg.set(KeyConstants._role, "system");
			msg.set(KeyConstants._content, systemMessage);
			messages.append(msg);
		}

		// Add conversation history
		for (Conversation c: getHistoryAsList()) {
			// question
			Struct msg = new StructImpl(StructImpl.TYPE_LINKED);
			msg.set(KeyConstants._role, "user");
			if (c.getRequest().isMultiPart()) msg.set(KeyConstants._content, createContent(c.getRequest().getQuestions()));
			else msg.set(KeyConstants._content, c.getRequest().getQuestion());
			messages.append(msg);

			// answer
			msg = new StructImpl(StructImpl.TYPE_LINKED);
			msg.set(KeyConstants._role, "assistant");
			msg.set(KeyConstants._content, AIUtil.extractStringAnswer(c.getResponse()));
			messages.append(msg);
		}

		// Add current message/parts
		Struct userMsg = new StructImpl(StructImpl.TYPE_LINKED);
		userMsg.set(KeyConstants._role, "user");

		if (parts != null) {
			// Multipart content
			userMsg.set(KeyConstants._content, createContent(parts));
		}
		else {
			// Simple text message
			userMsg.set(KeyConstants._content, message);
		}
		messages.append(userMsg);

		// Build request
		Struct request = new StructImpl(StructImpl.TYPE_LINKED);
		request.set(KeyConstants._model, openaiEngine.getModel());
		request.set(KeyConstants._messages, messages);
		request.set(KeyConstants._stream, stream);

		// Add temperature if set
		Double temperature = getTemperature();
		if (temperature != null) {
			request.set(KeyConstants._temperature, temperature);
		}

		// custom
		if (openaiEngine.custom != null && !openaiEngine.custom.isEmpty()) {
			Iterator<Entry<Key, Object>> it = openaiEngine.custom.entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				if (!request.containsKey(e.getKey())) {
					request.set(e.getKey(), e.getValue());
				}
			}
		}

		return request;
	}

	private Object createContent(List<Part> parts) throws PageException {
		// OpenAI expects array of content parts for multipart
		Array contentArray = new ArrayImpl();

		for (Part part: parts) {
			Struct contentPart = new StructImpl(StructImpl.TYPE_LINKED);

			if (part.isText()) {
				contentPart.set(KeyConstants._type, "text");
				contentPart.set(KeyConstants._text, part.getAsString());
			}
			else {
				String contentType = part.getContentType();
				String base64 = Caster.toBase64(part.getAsBinary());
				String dataUrl = "data:" + contentType + ";base64," + base64;

				// true or undefined
				if (!Boolean.FALSE.equals(openaiEngine.isMultiPartSupported())) {
					if (contentType.startsWith("image/")) {
						Struct image_url = new StructImpl(StructImpl.TYPE_LINKED);
						contentPart.set(KeyConstants._type, "image_url");
						contentPart.set("image_url", image_url);
						image_url.set(KeyConstants._url, dataUrl);
					}
					else {
						contentPart.set(KeyConstants._type, "file");
						Struct file = new StructImpl(StructImpl.TYPE_LINKED);
						contentPart.set(KeyConstants._file, file);

						String filename = PartImpl.getFileName(part);
						if (!StringUtil.isEmpty(filename)) file.set(KeyConstants._filename, filename);
						file.set("file_data", dataUrl);
					}
				}
				// false
				else {
					throw new ApplicationException(
							"The mime type [" + contentType + "] is not supported with the AI Engine [" + openaiEngine.getLabel() + "], only text is supported");
				}

				// possible types
				// 'text', 'image_url', 'input_audio', 'refusal', 'audio', and 'file'
			}

			contentArray.append(contentPart);
		}

		return contentArray;
	}

	private Response executeRequest(Struct request, String messageText, List<Part> parts, AIResponseListener listener) throws Exception {
		if (openaiEngine.chatCompletionsURI == null) {
			openaiEngine.chatCompletionsURI = new URI(openaiEngine.getBaseURL() + "chat/completions");
		}

		JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
		String str = json.serialize(null, request, SerializationSettings.SERIALIZE_AS_COLUMN, null);
		LogUtil.logx(null, Log.LEVEL_DEBUG, "ai", "request message send by [" + openaiEngine.getLabel() + "]: " + str, "ai", "application");

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			CloseableHttpResponse response = null;
			try {
				response = execute(httpClient, str);

				HttpEntity responseEntity = response.getEntity();
				Header ct = responseEntity.getContentType();
				MimeType mt = MimeType.getInstance(ct.getValue());
				String t = mt.getType() + "/" + mt.getSubtype();
				String cs = mt.getCharset() != null ? mt.getCharset().toString() : openaiEngine.charset;

				// stream false
				if ("application/json".equals(t)) {
					if (Util.isEmpty(cs, true)) cs = openaiEngine.charset;
					String rawStr = EntityUtils.toString(responseEntity, openaiEngine.charset);
					LogUtil.logx(null, Log.LEVEL_DEBUG, "ai", "response recieved send by [" + openaiEngine.getLabel() + "]: " + rawStr, "ai", "application");
					Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, rawStr));

					Struct err = Caster.toStruct(raw.get(KeyConstants._error, null), null);
					if (err != null) {
						throw AIUtil.toException(this.getEngine(), Caster.toString(err.get(KeyConstants._message)), Caster.toString(err.get(KeyConstants._type, null), null),
								Caster.toString(err.get(KeyConstants._code, null), null), response.getStatusLine().getStatusCode());
					}

					OpenAIResponse r = new OpenAIResponse(raw, cs);
					AIUtil.addConversation(this, getHistoryAsList(), new ConversationImpl(RequestSupport.getInstance(messageText, parts), r));
					return r;
				}
				// stream true
				else if ("text/event-stream".equals(t)) {
					if (Util.isEmpty(cs, true)) cs = openaiEngine.charset;
					JSONExpressionInterpreter interpreter = new JSONExpressionInterpreter();
					OpenAIStreamResponse r = new OpenAIStreamResponse(cs, listener);

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
							LogUtil.logx(null, Log.LEVEL_DEBUG, "ai", "response chunk recieved send by [" + openaiEngine.getLabel() + "]: " + line, "ai", "application");
							prev = Caster.toStruct(interpreter.interpret(null, line));
						}
						if (prev != null) {
							r.addPart(prev, index, true);
						}
					}

					AIUtil.addConversation(this, getHistoryAsList(), new ConversationImpl(RequestSupport.getInstance(messageText, parts), r));
					return r;
				}
				else {
					throw unsupportedMimeTypeException(response, responseEntity, cs, t);
				}
			}
			finally {
				IOUtil.closeEL(response);
			}
		}
	}

	private CloseableHttpResponse execute(CloseableHttpClient httpClient, String str) throws ClientProtocolException, IOException, URISyntaxException {
		int max = 3;
		CloseableHttpResponse response = httpClient.execute(createHttpPost(openaiEngine.chatCompletionsURI, str));
		while (response.getStatusLine().getStatusCode() >= 300 && response.getStatusLine().getStatusCode() < 400) {
			if (--max == 0) return response;
			Header locationHeader = response.getFirstHeader("Location");
			if (locationHeader != null) {
				String redirectUrl = locationHeader.getValue();
				IOUtil.closeEL(response);

				openaiEngine.chatCompletionsURI = new URI(openaiEngine.chatCompletionsURI.getScheme(), openaiEngine.chatCompletionsURI.getUserInfo(),
						openaiEngine.chatCompletionsURI.getHost(), openaiEngine.chatCompletionsURI.getPort(), redirectUrl, openaiEngine.chatCompletionsURI.getQuery(),
						openaiEngine.chatCompletionsURI.getFragment());

				response = httpClient.execute(createHttpPost(openaiEngine.chatCompletionsURI, str));
			}
		}
		return response;
	}

	private HttpPost createHttpPost(URI uri, String str) {
		HttpPost post = new HttpPost(uri);
		post.setHeader("Content-Type", AIUtil.createJsonContentType(openaiEngine.charset));
		post.setHeader("Authorization", "Bearer " + openaiEngine.secretKey);
		// custom headers
		if (this.openaiEngine.headers != null) {
			for (Entry<String, String> e: openaiEngine.headers.entrySet()) {
				post.setHeader(e.getKey(), e.getValue());
			}
		}

		StringEntity entity = new StringEntity(str, openaiEngine.charset);
		post.setEntity(entity);

		RequestConfig config = AISessionSupport.setRedirect(AISessionSupport.setTimeout(RequestConfig.custom(), this)).build();
		post.setConfig(config);

		return post;
	}

	private String extractTextFromParts(List<Part> parts) {
		if (parts == null) return "";

		StringBuilder sb = new StringBuilder();
		for (Part part: parts) {
			if (part.isText()) {
				String text = part.getAsString();
				if (text != null) {
					if (sb.length() > 0) sb.append(" ");
					sb.append(text);
				}
			}
		}
		return sb.toString();
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