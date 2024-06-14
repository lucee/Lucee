package lucee.loader.servlet.jakarta;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class HttpServletResponseJavax extends ServletResponseJavax implements HttpServletResponse {

	private jakarta.servlet.http.HttpServletResponse rsp;

	public HttpServletResponseJavax(jakarta.servlet.http.HttpServletResponse rsp) {
		super(rsp);
		this.rsp = rsp;
	}

	@Override
	public void addCookie(Cookie cookie) {
		rsp.addCookie((jakarta.servlet.http.Cookie) ((CookieJavax) cookie).getJakartaInstance());
	}

	@Override
	public boolean containsHeader(String name) {
		return rsp.containsHeader(name);
	}

	@Override
	public String encodeURL(String url) {
		return rsp.encodeURL(url);
	}

	@Override
	public String encodeRedirectURL(String url) {
		return rsp.encodeRedirectURL(url);
	}

	@Override
	public String encodeUrl(String url) {
		return rsp.encodeURL(url);
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return rsp.encodeRedirectURL(url);
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		rsp.sendError(sc, msg);
	}

	@Override
	public void sendError(int sc) throws IOException {
		rsp.sendError(sc);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		rsp.sendRedirect(location);
	}

	@Override
	public void setDateHeader(String name, long date) {
		rsp.setDateHeader(name, date);
	}

	@Override
	public void addDateHeader(String name, long date) {
		rsp.addDateHeader(name, date);
	}

	@Override
	public void setHeader(String name, String value) {
		rsp.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		rsp.addHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		rsp.setIntHeader(name, value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		rsp.addIntHeader(name, value);
	}

	@Override
	public void setStatus(int sc) {
		rsp.setStatus(sc);
	}

	@Override
	public void setStatus(int sc, String sm) {
		rsp.setStatus(sc);
	}

	@Override
	public int getStatus() {
		return rsp.getStatus();
	}

	@Override
	public String getHeader(String name) {
		return rsp.getHeader(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return rsp.getHeaders(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return rsp.getHeaderNames();
	}
}
