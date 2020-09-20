package lucee.runtime.listener;

import lucee.runtime.type.dt.TimeSpan;

public interface CookieData {

	public static short SAMESITE_EMPTY = 0;
	public static short SAMESITE_NONE = 1;
	public static short SAMESITE_STRICT = 2;
	public static short SAMESITE_LAX = 3;

	public abstract TimeSpan getTimeout();

	public abstract boolean isDisableUpdate();
}
