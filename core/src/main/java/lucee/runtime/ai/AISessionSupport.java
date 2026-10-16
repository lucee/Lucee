package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.other.CreateUniqueId;

public abstract class AISessionSupport implements AISessionMultipart {

	private String id;
	private AIEngine engine;
	final List<Conversation> history = new ArrayList<>();

	private int socketTimeout;
	private int connectTimeout;

	private int limit;
	private Double temp;

	public AISessionSupport(AIEngine engine, Conversation[] history, int limit, double temp, int connectTimeout, int socketTimeout) {
		this.engine = engine;

		if (socketTimeout < 0) this.socketTimeout = engine.getSocketTimeout();
		else this.socketTimeout = socketTimeout;

		if (connectTimeout < 0) this.connectTimeout = engine.getConnectTimeout();
		else this.connectTimeout = connectTimeout;

		if (limit <= 0) this.limit = engine.getConversationSizeLimit();
		else this.limit = limit;

		if (temp <= 0D) this.temp = engine.getTemperature();
		else this.temp = temp;

		if (history != null && history.length > 0) {
			for (Conversation c: history) {
				this.history.add(c);
			}
		}

	}

	@Override
	public final int getConversationSizeLimit() {
		return limit;
	}

	@Override
	public final Double getTemperature() {
		return temp;
	}

	@Override
	public final int getConnectTimeout() {
		return connectTimeout;
	}

	@Override
	public final int getSocketTimeout() {
		return socketTimeout;
	}

	@Override
	public final AIEngine getEngine() {
		return engine;
	}

	@Override
	public final Conversation[] getHistory() {
		return history.toArray(new Conversation[history.size()]);
	}

	protected final List<Conversation> getHistoryAsList() {
		return history;
	}

	@Override
	public final String getId() {
		if (id == null) {
			id = CreateUniqueId.invoke();
		}
		return id;
	}

	@Override
	public Response inquiry(String message) throws PageException {
		return inquiry(message, null);
	}

	@Override
	public Response inquiry(List<Part> parts) throws PageException {
		return inquiry(parts, null);
	}

	@Override
	public Response inquiry(List<Part> parts, AIResponseListener listener) throws PageException {
		// Default implementation: extract first text part and delegate to string inquiry
		if (parts == null || parts.isEmpty()) {
			throw new ApplicationException("Parts list cannot be null or empty");
		}

		for (Part part: parts) {
			if (part.isText()) {
				String text = part.getAsString();
				if (text != null) {
					return inquiry(text, listener);
				}
			}
		}
		throw new ApplicationException("this session type only supports single part text inquiries.");
	}

	public static Builder setTimeout(Builder builder, AISession session) {
		if (session.getConnectTimeout() > 0) builder.setConnectTimeout(session.getConnectTimeout());
		if (session.getSocketTimeout() > 0) builder.setSocketTimeout(session.getSocketTimeout());
		return builder;
	}

	public static Builder setTimeout(Builder builder, int connectTimeout, int socketTimeout) {
		if (connectTimeout > 0) builder.setConnectTimeout(connectTimeout);
		if (socketTimeout > 0) builder.setSocketTimeout(socketTimeout);
		return builder;
	}

	public static Builder setRedirect(Builder builder) {
		builder

				.setRedirectsEnabled(true)

				.setMaxRedirects(2)

				.setCircularRedirectsAllowed(false)

				.setRelativeRedirectsAllowed(true);
		;
		return builder;
	}

	// TODO add to interface
	@Override
	public abstract String getSystemMessage();

	protected Exception unsupportedMimeTypeException(CloseableHttpResponse response, HttpEntity responseEntity, String cs, String mimetype) {
		int statusCode = response.getStatusLine().getStatusCode();
		String statusReason = response.getStatusLine().getReasonPhrase();

		// Optionally read a snippet of the response body for context
		String bodySnippet = "";
		try {
			String body = EntityUtils.toString(responseEntity, cs);
			bodySnippet = body.length() > 200 ? body.substring(0, 200) + "..." : body;
		}
		catch (Exception ex) {
			// If we can't read the body, just note that
			bodySnippet = "[Unable to read response body]";
		}

		return new ApplicationException("Unsupported mime type [" + mimetype + "], only [application/json, text/event-stream] are supported. " + "HTTP Status: " + statusCode + " "
				+ statusReason + ", " + "Charset: " + cs + ", " + "Response body: " + bodySnippet);
	}
}
