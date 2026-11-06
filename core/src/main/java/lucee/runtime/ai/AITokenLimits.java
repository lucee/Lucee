package lucee.runtime.ai;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * The optional token limits of an AI connection (LDEV-6528), they are read from the connection
 * settings and are not sent to the provider.
 */
public final class AITokenLimits {

	public static final String ACTION_THROW = "throw";
	public static final String ACTION_LOG = "log";

	public static final String ERROR_CODE = "ai.token.limit.exceeded";

	public static final Key MAX_TOKENS_PER_SESSION = KeyImpl.init("maxTokensPerSession");
	public static final Key MAX_TOKENS_PER_REQUEST = KeyImpl.init("maxTokensPerRequest");
	public static final Key TOKEN_LIMIT_ACTION = KeyImpl.init("tokenLimitAction");
	public static final Key TOKEN_WARNING_THRESHOLD = KeyImpl.init("tokenWarningThreshold");

	public static final AITokenLimits NONE = new AITokenLimits(0L, 0L, ACTION_THROW, 0D);

	private final long maxPerSession;
	private final long maxPerRequest;
	private final String action;
	private final double warningThreshold;

	private AITokenLimits(long maxPerSession, long maxPerRequest, String action, double warningThreshold) {
		this.maxPerSession = maxPerSession;
		this.maxPerRequest = maxPerRequest;
		this.action = action;
		this.warningThreshold = warningThreshold;
	}

	/**
	 * reads the limits from the connection settings and removes them from it, so they are not forwarded
	 * to the provider
	 * 
	 * @throws PageException when a value is invalid
	 */
	public static AITokenLimits read(Struct props) throws PageException {
		long perSession = readLimit(props, MAX_TOKENS_PER_SESSION);
		long perRequest = readLimit(props, MAX_TOKENS_PER_REQUEST);

		String action = ACTION_THROW;
		Object o = props.remove(TOKEN_LIMIT_ACTION, null);
		if (o != null && !StringUtil.isEmpty(Caster.toString(o, null), true)) {
			action = Caster.toString(o).trim().toLowerCase();
			if (!ACTION_THROW.equals(action) && !ACTION_LOG.equals(action)) {
				throw new ApplicationException("tokenLimitAction has to be [" + ACTION_THROW + "] or [" + ACTION_LOG + "], now it is [" + o + "]");
			}
		}

		double threshold = 0D;
		o = props.remove(TOKEN_WARNING_THRESHOLD, null);
		if (o != null && !StringUtil.isEmpty(Caster.toString(o, null), true)) {
			threshold = Caster.toDoubleValue(o, false, Double.NaN);
			if (Double.isNaN(threshold) || !Decision.isNumber(o) || threshold < 0D || threshold > 1D) {
				throw new ApplicationException("tokenWarningThreshold has to be a number between 0 and 1 (a fraction of maxTokensPerSession), now it is [" + o + "]");
			}
		}

		if (perSession == 0L && perRequest == 0L && threshold == 0D && ACTION_THROW.equals(action)) return NONE;
		return new AITokenLimits(perSession, perRequest, action, threshold);
	}

	private static long readLimit(Struct props, Key key) throws PageException {
		Object o = props.remove(key, null);
		if (o == null || StringUtil.isEmpty(Caster.toString(o, null), true)) return 0L;
		double d = Caster.toDoubleValue(o, false, Double.NaN);
		if (Double.isNaN(d) || d < 0D || d != Math.floor(d) || !Decision.isNumber(o)) {
			throw new ApplicationException(key.getString() + " has to be a positive whole number (0 means no limit), now it is [" + o + "]");
		}
		return (long) d;
	}

	public static AITokenLimits get(AIEngine engine) {
		if (engine instanceof AIEngineSupport) return ((AIEngineSupport) engine).getTokenLimits();
		return NONE;
	}

	/** maximum tokens for all inquiries of a session, 0 for no limit */
	public long getMaxTokensPerSession() {
		return maxPerSession;
	}

	/** maximum tokens for a single inquiry, 0 for no limit */
	public long getMaxTokensPerRequest() {
		return maxPerRequest;
	}

	public String getAction() {
		return action;
	}

	public boolean isThrow() {
		return ACTION_THROW.equals(action);
	}

	/** fraction of maxTokensPerSession that causes a warning, 0 for none */
	public double getWarningThreshold() {
		return warningThreshold;
	}

	/** the number of tokens of a session where the warning is written, 0 if there is none */
	public long getWarningTokens() {
		if (maxPerSession <= 0L || warningThreshold <= 0D) return 0L;
		return Math.max(1L, (long) Math.ceil(maxPerSession * warningThreshold));
	}

	public boolean hasSessionLimit() {
		return maxPerSession > 0L;
	}

	public boolean hasRequestLimit() {
		return maxPerRequest > 0L;
	}

	/**
	 * true when nothing is configured
	 */
	public boolean isEmpty() {
		return this == NONE;
	}

	public Struct toStruct() {
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		if (maxPerSession > 0L) sct.setEL(MAX_TOKENS_PER_SESSION, maxPerSession);
		if (maxPerRequest > 0L) sct.setEL(MAX_TOKENS_PER_REQUEST, maxPerRequest);
		sct.setEL(TOKEN_LIMIT_ACTION, action);
		if (warningThreshold > 0D) sct.setEL(TOKEN_WARNING_THRESHOLD, warningThreshold);
		return sct;
	}
}
