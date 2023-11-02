package lucee.runtime.listener;

import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.scope.CookieImpl;

public class AuthCookieDataImpl implements AuthCookieData {

	public static final AuthCookieData DEFAULT = new AuthCookieDataImpl(TimeSpanImpl.fromMillis(CookieImpl.NEVER * 1000), false);
	private final TimeSpan timeout;
	private final boolean disableUpdate;

	public AuthCookieDataImpl(TimeSpan timeout, boolean disableUpdate) {
		this.timeout = timeout;
		this.disableUpdate = disableUpdate;
	}

	@Override
	public TimeSpan getTimeout() {
		return timeout;
	}

	@Override
	public boolean isDisableUpdate() {
		return disableUpdate;
	}

}
