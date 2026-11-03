package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

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
	private boolean warningLogged;

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
		checkAfterCall(u);
	}

	/**
	 * the tokens used by this session so far
	 */
	private long getTokensUsed() {
		return Math.max(totalTokens, inputTokens + outputTokens);
	}

	/**
	 * checks the token limits of the connection (LDEV-6528) before a call is made, the check is soft:
	 * calls that overlap are not counted until they are finished.
	 * 
	 * @throws PageException when a limit is reached and the connection has tokenLimitAction=throw
	 */
	public final void checkTokenLimits() throws PageException {
		AITokenLimits limits = AITokenLimits.get(getEngine());
		if (limits.isEmpty()) return;

		String limit = null;
		long max = 0L;
		long used = 0L;
		String detail = null;
		synchronized (this) {
			long total = getTokensUsed();
			if (limits.hasSessionLimit() && total >= limits.getMaxTokensPerSession()) {
				limit = AITokenLimits.MAX_TOKENS_PER_SESSION.getString();
				max = limits.getMaxTokensPerSession();
				used = total;
				detail = "the session used " + total + " tokens (input " + inputTokens + ", output " + outputTokens + ") in " + requests + " requests";
			}
			// the whole history is sent with every call, so when the last request was already as big as the limit, the next one will be too
			else if (limits.hasRequestLimit() && lastUsage != null && lastUsage.getUsage() != null && lastUsage.getUsage().input >= limits.getMaxTokensPerRequest()) {
				limit = AITokenLimits.MAX_TOKENS_PER_REQUEST.getString();
				max = limits.getMaxTokensPerRequest();
				used = lastUsage.getUsage().input;
				detail = "the last request of this session already sent " + used + " input tokens and the whole conversation is sent with every request";
			}
		}
		if (limit == null) return;

		AIUtil.logTokenLimit(this, limit, max, used, limits.getAction(), "before");
		if (limits.isThrow()) {
			ApplicationException ae = new ApplicationException("The token limit [" + limit + "=" + max + "] of the AI connection [" + getEngine().getName() + "] is reached, "
					+ detail + ". The request was not sent.");
			ae.setErrorCode(AITokenLimits.ERROR_CODE);
			throw ae;
		}
	}

	/**
	 * checks the limits that can only be checked after a call, called with the lock held
	 */
	private void checkAfterCall(AIUsage u) {
		AITokenLimits limits = AITokenLimits.get(getEngine());
		if (limits.isEmpty()) return;

		if (limits.hasRequestLimit()) {
			long tokens = u.total >= 0L ? u.total : (u.input >= 0L ? u.input : 0L) + (u.output >= 0L ? u.output : 0L);
			if (tokens > limits.getMaxTokensPerRequest()) {
				AIUtil.logTokenLimit(this, AITokenLimits.MAX_TOKENS_PER_REQUEST.getString(), limits.getMaxTokensPerRequest(), tokens, "log", "after");
			}
		}

		long warn = limits.getWarningTokens();
		if (warn > 0L && !warningLogged) {
			long used = getTokensUsed();
			if (used >= warn) {
				warningLogged = true;
				AIUtil.logTokenLimit(this, AITokenLimits.TOKEN_WARNING_THRESHOLD.getString(), limits.getMaxTokensPerSession(), used, "log", "after");
			}
		}
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
		long warn = AITokenLimits.get(getEngine()).getWarningTokens();
		warningLogged = warn > 0L && getTokensUsed() >= warn;
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

	public static Builder setTimeout(Builder builder, AISession session) {
		builder.setCookieSpec(CookieSpecs.STANDARD); // LDEV-2321
		if (session.getConnectTimeout() > 0) builder.setConnectTimeout(session.getConnectTimeout());
		if (session.getSocketTimeout() > 0) builder.setSocketTimeout(session.getSocketTimeout());
		return builder;
	}

	public static Builder setTimeout(Builder builder, int connectTimeout, int socketTimeout) {
		builder.setCookieSpec(CookieSpecs.STANDARD); // LDEV-2321
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
