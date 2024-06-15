package lucee.loader.servlet.jakarta;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

public class HttpServletRequestJavax extends ServletRequestJavax implements HttpServletRequest {

	private jakarta.servlet.http.HttpServletRequest req;

	public HttpServletRequestJavax(jakarta.servlet.http.HttpServletRequest req) {
		super(req);
		this.req = req;
	}

	@Override
	public String getAuthType() {
		return req.getAuthType();
	}

	@Override
	public Cookie[] getCookies() {
		jakarta.servlet.http.Cookie[] cookies = req.getCookies();
		if (cookies == null) return null;
		Cookie[] javaxCookies = new Cookie[cookies.length];
		for (int i = 0; i < cookies.length; i++) {
			jakarta.servlet.http.Cookie c = cookies[i];
			javaxCookies[i] = new Cookie(c.getName(), c.getValue());
			if (c.getComment() != null) javaxCookies[i].setComment(c.getComment());
			if (c.getDomain() != null) javaxCookies[i].setDomain(c.getDomain());
			javaxCookies[i].setHttpOnly(c.isHttpOnly());
			javaxCookies[i].setMaxAge(c.getMaxAge());
			if (c.getPath() != null) javaxCookies[i].setPath(c.getPath());
			javaxCookies[i].setSecure(c.getSecure());
			javaxCookies[i].setVersion(c.getVersion());
		}
		return javaxCookies;
	}

	@Override
	public long getDateHeader(String name) {
		return req.getDateHeader(name);
	}

	@Override
	public String getHeader(String name) {
		return req.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		return req.getHeaders(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return req.getHeaderNames();
	}

	@Override
	public int getIntHeader(String name) {
		return req.getIntHeader(name);
	}

	@Override
	public String getMethod() {
		return req.getMethod();
	}

	@Override
	public String getPathInfo() {
		return req.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		return req.getPathTranslated();
	}

	@Override
	public String getContextPath() {
		return req.getContextPath();
	}

	@Override
	public String getQueryString() {
		return req.getQueryString();
	}

	@Override
	public String getRemoteUser() {
		return req.getRemoteUser();
	}

	@Override
	public boolean isUserInRole(String role) {
		return req.isUserInRole(role);
	}

	@Override
	public Principal getUserPrincipal() {
		return req.getUserPrincipal();
	}

	@Override
	public String getRequestedSessionId() {
		return req.getRequestedSessionId();
	}

	@Override
	public String getRequestURI() {
		return req.getRequestURI();
	}

	@Override
	public StringBuffer getRequestURL() {
		return req.getRequestURL();
	}

	@Override
	public String getServletPath() {
		return req.getServletPath();
	}

	@Override
	public HttpSession getSession(boolean create) {
		return new HttpSessionJavax(req.getSession(create));
	}

	@Override
	public HttpSession getSession() {
		return new HttpSessionJavax(req.getSession());
	}

	@Override
	public String changeSessionId() {
		return req.changeSessionId();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return req.isRequestedSessionIdValid();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return req.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return req.isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return req.isRequestedSessionIdFromURL();
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		throw new RuntimeException("the method [authenticate] is not supported");
	}

	@Override
	public void login(String username, String password) throws ServletException {
		try {
			req.login(username, password);
		}
		catch (jakarta.servlet.ServletException e) {
			throw new ServletExceptionJavax(e);
		}
	}

	@Override
	public void logout() throws ServletException {
		try {
			req.logout();
		}
		catch (jakarta.servlet.ServletException e) {
			throw new ServletExceptionJavax(e);
		}
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		try {
			Collection<jakarta.servlet.http.Part> jakartaParts = req.getParts();
			Collection<Part> javaxParts = new ArrayList<>();
			for (jakarta.servlet.http.Part jakartaPart: jakartaParts) {
				javaxParts.add(new PartJavax(jakartaPart));
			}
			return javaxParts;
		}
		catch (jakarta.servlet.ServletException e) {
			throw new ServletExceptionJavax(e);
		}
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		try {
			return new PartJavax(req.getPart(name));
		}
		catch (jakarta.servlet.ServletException e) {
			throw new ServletExceptionJavax(e);
		}
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		throw new RuntimeException("method [upgrade] is not supported");

	}

}
