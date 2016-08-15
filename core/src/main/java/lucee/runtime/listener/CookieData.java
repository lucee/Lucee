package lucee.runtime.listener;

import lucee.runtime.type.dt.TimeSpan;

public interface CookieData {

	public abstract TimeSpan getTimeout();

	public abstract boolean isDisableUpdate();
}
