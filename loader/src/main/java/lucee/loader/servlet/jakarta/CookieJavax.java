package lucee.loader.servlet.jakarta;

import javax.servlet.http.Cookie;

public class CookieJavax extends Cookie implements Jakarta {

	private static final long serialVersionUID = 8505225995912632019L;
	private jakarta.servlet.http.Cookie cookie;

	public CookieJavax(jakarta.servlet.http.Cookie cookie) {
		super(cookie.getName(), cookie.getValue());
		this.cookie = cookie;
	}

	@Override
	public String getName() {
		return cookie.getName();
	}

	@Override
	public String getValue() {
		return cookie.getValue();
	}

	@Override
	public void setValue(String value) {
		cookie.setValue(value);
	}

	@Override
	public String getComment() {
		return cookie.getComment();
	}

	@Override
	public void setComment(String purpose) {
		cookie.setComment(purpose);
	}

	@Override
	public String getDomain() {
		return cookie.getDomain();
	}

	@Override
	public void setDomain(String domain) {
		cookie.setDomain(domain);
	}

	@Override
	public int getMaxAge() {
		return cookie.getMaxAge();
	}

	@Override
	public void setMaxAge(int expiry) {
		cookie.setMaxAge(expiry);
	}

	@Override
	public String getPath() {
		return cookie.getPath();
	}

	@Override
	public void setPath(String uri) {
		cookie.setPath(uri);
	}

	@Override
	public boolean getSecure() {
		return cookie.getSecure();
	}

	@Override
	public void setSecure(boolean flag) {
		cookie.setSecure(flag);
	}

	@Override
	public int getVersion() {
		return cookie.getVersion();
	}

	@Override
	public void setVersion(int v) {
		cookie.setVersion(v);
	}

	@Override
	public boolean isHttpOnly() {
		return cookie.isHttpOnly();
	}

	@Override
	public void setHttpOnly(boolean isHttpOnly) {
		cookie.setHttpOnly(isHttpOnly);
	}

	@Override
	public Object getJakartaInstance() {
		return cookie;
	}
}
