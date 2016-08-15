package lucee.runtime.listener;
// FUTURE move to loader
import lucee.runtime.type.dt.TimeSpan;

public interface SessionCookieData extends CookieData {

	public abstract boolean isHttpOnly();

	public abstract boolean isSecure();

	public abstract String getDomain();

}