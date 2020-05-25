package lucee.runtime.listener;

import lucee.commons.lang.StringUtil;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.scope.CookieImpl;

public class SessionCookieDataImpl implements SessionCookieData {

	public static final SessionCookieData DEFAULT = new SessionCookieDataImpl(true, false, TimeSpanImpl.fromMillis(CookieImpl.NEVER * 1000), null, false, null);

	private final boolean httpOnly;
	private final boolean secure;
	private final TimeSpan timeout;
	private final String domain;
	private final boolean disableUpdate;
	private final String samesite;

	public SessionCookieDataImpl(boolean httpOnly, boolean secure, TimeSpan timeout, String domain, boolean disableUpdate, String samesite) {
		this.httpOnly = httpOnly;
		this.secure = secure;
		this.timeout = timeout;
		this.domain = StringUtil.isEmpty(domain, true) ? null : domain.trim();
		this.disableUpdate = disableUpdate;
		this.samesite = samesite;
	}

	@Override
	public boolean isHttpOnly() {
		return httpOnly;
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	@Override
	public TimeSpan getTimeout() {
		return timeout;
	}

	@Override
	public String getDomain() {
		return domain;
	}

	@Override
	public boolean isDisableUpdate() {
		return disableUpdate;
	}

	@Override
	public String getSamesite() {
		return samesite;
	}
}
