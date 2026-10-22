package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.List;


import lucee.commons.net.http.HTTPRequest;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
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

	public static HTTPRequest setTimeout(HTTPRequest req, AISession session) {
		return setTimeout(req, session.getConnectTimeout(), session.getSocketTimeout());
	}

	public static HTTPRequest setTimeout(HTTPRequest req, int connectTimeout, int socketTimeout) {
		if (connectTimeout > 0) req.setConnectTimeout(connectTimeout);
		if (socketTimeout > 0) req.setReadTimeout(socketTimeout);
		return req;
	}

	public static HTTPRequest setRedirect(HTTPRequest req) {
		return req.setRedirect("true");
	}

	/**
	 * creates a request for the given URL
	 * 
	 * @param url a String, URL or URI
	 */
	public static HTTPRequest newRequest(String method, Object url) throws java.net.MalformedURLException {
		java.net.URL u;
		if (url instanceof java.net.URL) u = (java.net.URL) url;
		else if (url instanceof java.net.URI) u = ((java.net.URI) url).toURL();
		else u = new java.net.URL(url.toString());
		return new HTTPRequest().setMethod(method).setURL(u);
	}

	/**
	 * @return the value of the header Content-Type of the response, null if there is none
	 */
	public static String contentType(HTTPResponse response) {
		Header h = response.getLastHeaderIgnoreCase("Content-Type");
		return h == null ? null : h.getValue();
	}

	@Override
	public abstract String getSystemMessage();

	protected Exception unsupportedMimeTypeException(HTTPResponse response, String cs, String mimetype) {
		int statusCode = response.getStatusCode();
		String statusReason = response.getStatusText();

		// Optionally read a snippet of the response body for context
		String bodySnippet = "";
		try {
			String body = response.getContentAsString(cs);
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
