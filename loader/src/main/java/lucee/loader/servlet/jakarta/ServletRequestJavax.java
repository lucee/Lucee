package lucee.loader.servlet.jakarta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

// Inner class for ServletRequestJavax
public class ServletRequestJavax implements ServletRequest, Jakarta {

	private jakarta.servlet.ServletRequest req;

	public ServletRequestJavax(jakarta.servlet.ServletRequest req) {
		this.req = req;
	}

	@Override
	public Object getAttribute(String name) {
		return req.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return req.getAttributeNames();
	}

	@Override
	public String getCharacterEncoding() {
		return req.getCharacterEncoding();
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		req.setCharacterEncoding(env);
	}

	@Override
	public int getContentLength() {
		return req.getContentLength();
	}

	@Override
	public long getContentLengthLong() {
		return req.getContentLengthLong();
	}

	@Override
	public String getContentType() {
		return req.getContentType();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new ServletInputStreamJavax(req.getInputStream());
	}

	@Override
	public String getParameter(String name) {
		return req.getParameter(name);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return req.getParameterNames();
	}

	@Override
	public String[] getParameterValues(String name) {
		return req.getParameterValues(name);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return req.getParameterMap();
	}

	@Override
	public String getProtocol() {
		return req.getProtocol();
	}

	@Override
	public String getScheme() {
		return req.getScheme();
	}

	@Override
	public String getServerName() {
		return req.getServerName();
	}

	@Override
	public int getServerPort() {
		return req.getServerPort();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return req.getReader();
	}

	@Override
	public String getRemoteAddr() {
		return req.getRemoteAddr();
	}

	@Override
	public String getRemoteHost() {
		return req.getRemoteHost();
	}

	@Override
	public void setAttribute(String name, Object o) {
		req.setAttribute(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		req.removeAttribute(name);
	}

	@Override
	public Locale getLocale() {
		return req.getLocale();
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return req.getLocales();
	}

	@Override
	public boolean isSecure() {
		return req.isSecure();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return new RequestDispatcherJavax(req.getRequestDispatcher(path));
	}

	@Override
	public String getRealPath(String path) {
		return req.getServletContext().getRealPath(path);
	}

	@Override
	public int getRemotePort() {
		return req.getRemotePort();
	}

	@Override
	public String getLocalName() {
		return req.getLocalName();
	}

	@Override
	public String getLocalAddr() {
		return req.getLocalAddr();
	}

	@Override
	public int getLocalPort() {
		return req.getLocalPort();
	}

	@Override
	public ServletContext getServletContext() {
		return new ServletContextJavax(req.getServletContext());
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return new AsyncContextJavax(req.startAsync());
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
		throw new RuntimeException("method [startAsync] is not supported");
	}

	@Override
	public boolean isAsyncStarted() {
		return req.isAsyncStarted();
	}

	@Override
	public boolean isAsyncSupported() {
		return req.isAsyncSupported();
	}

	@Override
	public AsyncContext getAsyncContext() {
		return new AsyncContextJavax(req.getAsyncContext());
	}

	@Override
	public DispatcherType getDispatcherType() {
		return DispatcherType.valueOf(req.getDispatcherType().name());
	}

	@Override
	public Object getJakartaInstance() {
		return req;
	}
}