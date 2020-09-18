package lucee.runtime.listener;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.scope.CookieImpl;

public class SessionCookieDataImpl implements SessionCookieData {

	public static final SessionCookieData DEFAULT = new SessionCookieDataImpl(true, false, TimeSpanImpl.fromMillis(CookieImpl.NEVER * 1000), null, false, CookieData.SAMESITE_NONE, "/");

	private final boolean httpOnly;
	private final boolean secure;
	private final TimeSpan timeout;
	private final String domain;
	private final String path;
	private final boolean disableUpdate;
	private final short samesite;

	public SessionCookieDataImpl(boolean httpOnly, boolean secure, TimeSpan timeout, String domain, boolean disableUpdate, short samesite, String path) {
		this.httpOnly = httpOnly;
		this.secure = secure;
		this.timeout = timeout;
		this.domain = StringUtil.isEmpty(domain, true) ? null : domain.trim();
		this.path = StringUtil.isEmpty(path, true) ? null : path.trim();
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
	public String getPath() {
		return path;
	}

	@Override
	public boolean isDisableUpdate() {
		return disableUpdate;
	}

	@Override
	public short getSamesite() {
		return samesite;
	}

	public static short toSamesite(String str) throws ApplicationException {
		if (StringUtil.isEmpty(str)) return SAMESITE_NONE;
		str = str.trim();
		if ("NONE".equalsIgnoreCase(str)) return SAMESITE_NONE;
		if ("LAX".equalsIgnoreCase(str)) return SAMESITE_LAX;
		if ("STRICT".equalsIgnoreCase(str)) return SAMESITE_STRICT;
		throw new ApplicationException("invalid value [" + str + "] for samesite, valid values are [none,lax,strict]");
	}

	public static short toSamesite(String str, short defaultValue) {
		if (StringUtil.isEmpty(str)) return SAMESITE_NONE;
		str = str.trim();
		if ("NONE".equalsIgnoreCase(str)) return SAMESITE_NONE;
		if ("LAX".equalsIgnoreCase(str)) return SAMESITE_LAX;
		if ("STRICT".equalsIgnoreCase(str)) return SAMESITE_STRICT;
		return defaultValue;
	}

	public static String toSamesite(short s) {
		if (s == SAMESITE_STRICT) return "Strict";
		if (s == SAMESITE_LAX) return "Lax";
		return "None";
	}
}
