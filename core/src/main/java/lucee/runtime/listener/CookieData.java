package lucee.runtime.listener;

import lucee.runtime.type.dt.TimeSpan;

public interface CookieData {

	public static short SAMESITE_NONE = 0;
	public static short SAMESITE_STRICT = 1;
	public static short SAMESITE_LAX = 2;

	public abstract TimeSpan getTimeout();

	public abstract boolean isDisableUpdate();
}
