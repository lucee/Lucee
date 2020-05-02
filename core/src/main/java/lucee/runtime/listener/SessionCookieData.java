package lucee.runtime.listener;
// FUTURE move to loader

public interface SessionCookieData extends CookieData {

	public abstract boolean isHttpOnly();

	public abstract boolean isSecure();

	public abstract String getDomain();

	public abstract String getSamesite();
}