package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.List;


import lucee.commons.net.http.HTTPRequest;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public abstract class AISessionSupport implements AISessionMultipart {

	private String id;
	private AIEngine engine;
	final List<Conversation> history = new ArrayList<>();

	private int socketTimeout;
	private int connectTimeout;

	private int limit;
	private Double temp;

	// usage of the session, LDEV-6527
	private long requests;
	private long unreportedRequests;
	private long failedRequests;
	private long inputTokens;
	private long outputTokens;
	private long totalTokens;
	private long cachedTokens;
	private long reasoningTokens;
	private AIUsageRecord lastUsage;

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

	/**
	 * adds a finished call to the usage of this session (LDEV-6527)
	 */
	final synchronized void addUsage(AIUsageRecord record) {
		requests++;
		lastUsage = record;
		AIUsage u = record.getUsage();
		if (u == null) {
			unreportedRequests++;
			return;
		}
		// a provider that does not report a value is not counted as 0, the sum of the others is used instead
		if (u.input >= 0L) inputTokens += u.input;
		if (u.output >= 0L) outputTokens += u.output;
		if (u.total >= 0L) totalTokens += u.total;
		if (u.cached >= 0L) cachedTokens += u.cached;
		if (u.reasoning >= 0L) reasoningTokens += u.reasoning;
	}

	final synchronized void addFailedRequest() {
		failedRequests++;
	}

	/**
	 * the token usage of all calls made with this session, see AIGetUsage
	 */
	public final synchronized Struct getUsage() {
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		sct.setEL("requests", requests);
		sct.setEL("unreportedRequests", unreportedRequests);
		sct.setEL("failedRequests", failedRequests);
		sct.setEL("input", inputTokens);
		sct.setEL("output", outputTokens);
		sct.setEL("total", totalTokens);
		sct.setEL("cached", cachedTokens);
		sct.setEL("reasoning", reasoningTokens);
		if (lastUsage != null) sct.setEL("last", lastUsage.toStruct());
		return sct;
	}

	/**
	 * restores the usage written by {@link #getUsage()}, used when a session gets loaded
	 */
	public final synchronized void restoreUsage(Struct usage) {
		if (usage == null) return;
		requests = toLong(usage, "requests");
		unreportedRequests = toLong(usage, "unreportedRequests");
		failedRequests = toLong(usage, "failedRequests");
		inputTokens = toLong(usage, "input");
		outputTokens = toLong(usage, "output");
		totalTokens = toLong(usage, "total");
		cachedTokens = toLong(usage, "cached");
		reasoningTokens = toLong(usage, "reasoning");
	}

	private static long toLong(Struct sct, String key) {
		return Math.max(0L, Caster.toLongValue(sct.get(key, null), 0L));
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
		// LDEV-6580: AI requests go through the configured server proxy, like cfhttp
		return new HTTPRequest().setMethod(method).setURL(u).setProxy(AIUtil.getProxyData(u.getHost()));
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
