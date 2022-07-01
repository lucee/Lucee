/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.net.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import lucee.commons.collection.MapFactory;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.commons.net.URLItem;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.scope.Form;
import lucee.runtime.type.scope.FormImpl;
import lucee.runtime.type.scope.URL;
import lucee.runtime.type.scope.URLImpl;
import lucee.runtime.type.scope.UrlFormImpl;
import lucee.runtime.type.scope.util.ScopeUtil;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.EnumerationWrapper;

/**
 * extends an existing {@link HttpServletRequest} with the possibility to reread the input as many
 * you want.
 */
public final class HTTPServletRequestWrap implements HttpServletRequest, Serializable {

	private static final long serialVersionUID = 7286638632320246809L;

	private boolean firstRead = true;
	private byte[] bytes;
	private File file;

	private static final int MAX_MEMORY_SIZE = 1024 * 1024;

	private String servlet_path;
	private String request_uri;
	private String context_path;
	private String path_info;
	private String query_string;
	private boolean disconnected;
	private final HttpServletRequest req;

	private static class DisconnectData {
		private Map<String, Object> attributes;
		private String authType;
		private Cookie[] cookies;
		private Map<Collection.Key, LinkedList<String>> headers;// this is a Pait List because there could by multiple entries with the same name
		private String method;
		private String pathTranslated;
		private String remoteUser;
		private String requestedSessionId;
		private boolean requestedSessionIdFromCookie;
		// private Request _request;
		private boolean requestedSessionIdFromURL;
		private boolean secure;
		private boolean requestedSessionIdValid;
		private String characterEncoding;
		private int contentLength;
		private String contentType;
		private int serverPort;
		private String serverName;
		private String scheme;
		private String remoteHost;
		private String remoteAddr;
		private String protocol;
		private Locale locale;
		private HttpSession session;
		private Principal userPrincipal;
	}

	DisconnectData disconnectData;

	/**
	 * Constructor of the class
	 * 
	 * @param req
	 * @param max how many is possible to re read
	 */
	public HTTPServletRequestWrap(HttpServletRequest req) {
		this.req = pure(req);
		if ((servlet_path = attrAsString("javax.servlet.include.servlet_path")) != null) {
			request_uri = attrAsString("javax.servlet.include.request_uri");
			context_path = attrAsString("javax.servlet.include.context_path");
			path_info = attrAsString("javax.servlet.include.path_info");
			query_string = attrAsString("javax.servlet.include.query_string");
		}
		else {
			servlet_path = req.getServletPath();
			request_uri = req.getRequestURI();
			context_path = req.getContextPath();
			path_info = req.getPathInfo();
			query_string = req.getQueryString();
		}
	}

	private String attrAsString(String key) {
		Object res = getAttribute(key);
		if (res == null) return null;
		return res.toString();
	}

	public static HttpServletRequest pure(HttpServletRequest req) {
		HttpServletRequest req2;
		while (req instanceof HTTPServletRequestWrap) {
			req2 = ((HTTPServletRequestWrap) req).getOriginalRequest();
			if (req2 == req) break;
			req = req2;
		}
		return req;
	}

	@Override
	public String getContextPath() {
		return context_path;
	}

	@Override
	public String getPathInfo() {
		return path_info;
	}

	@Override
	public StringBuffer getRequestURL() {
		if (String.valueOf(getServerPort()).equals("80") || String.valueOf(getServerPort()).equals("443")) {
			return new StringBuffer(isSecure() ? "https" : "http").append("://").append(getServerName()).append(request_uri.startsWith("/") ? request_uri : "/" + request_uri);
		}
		else {
			return new StringBuffer(isSecure() ? "https" : "http").append("://").append(getServerName()).append(':').append(getServerPort())
					.append(request_uri.startsWith("/") ? request_uri : "/" + request_uri);
		}
	}

	@Override
	public String getQueryString() {
		return query_string;
	}

	@Override
	public String getRequestURI() {
		return request_uri;
	}

	@Override
	public String getServletPath() {
		return servlet_path;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String realpath) {
		return new RequestDispatcherWrap(this, realpath);
	}

	public RequestDispatcher getOriginalRequestDispatcher(String realpath) {
		if (disconnected) return null;
		return req.getRequestDispatcher(realpath);
	}

	@Override
	public synchronized void removeAttribute(String name) {
		if (disconnected) disconnectData.attributes.remove(name);
		else req.removeAttribute(name);
	}

	@Override
	public synchronized void setAttribute(String name, Object value) {
		if (disconnected) disconnectData.attributes.put(name, value);
		else req.setAttribute(name, value);
	}

	@Override
	public synchronized Object getAttribute(String name) {
		if (disconnected) return disconnectData.attributes.get(name);
		return req.getAttribute(name);
	}

	@Override
	public synchronized Enumeration getAttributeNames() {
		if (disconnected) {
			return new EnumerationWrapper(disconnectData.attributes.keySet().toArray());
		}
		return req.getAttributeNames();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (bytes == null && file == null) {
			if (!firstRead) {
				if (bytes != null) return new ServletInputStreamDummy(bytes);
				if (file != null) return new ServletInputStreamDummy(file);

				PageContext pc = ThreadLocalPageContext.get();
				if (pc != null) return pc.formScope().getInputStream();

				return new ServletInputStreamDummy(new byte[] {}); // throw new IllegalStateException();
			}

			firstRead = false;
			// keep the content in memory
			storeEL();
		}
		if (file != null) return new ServletInputStreamDummy(file);
		if (bytes != null) return new ServletInputStreamDummy(bytes);
		return new ServletInputStreamDummy(new byte[] {});
	}

	private void storeEL() {
		ServletInputStream is = null;
		RefBoolean maxReached = new RefBooleanImpl();
		try {
			{
				try {
					is = req.getInputStream();
					bytes = IOUtil.toBytesMax(is, MAX_MEMORY_SIZE, maxReached);

					if (!maxReached.toBooleanValue()) {
						return;
					}
				}
				catch (Exception e) {
				}
			}
			FileOutputStream fos = null;
			try {
				file = File.createTempFile("upload", ".tmp");
				fos = new FileOutputStream(file);
				// first we store what we did already load
				if (maxReached.toBooleanValue()) {
					IOUtil.copy(new ByteArrayInputStream(bytes), fos, true, false);
					bytes = null;
				}

				if (is == null) is = req.getInputStream();
				// now we store the rest
				IOUtil.copy(is, fos, 0xfffff, true, true);
				file.deleteOnExit();
			}
			catch (Exception e) {
			}
			finally {
				IOUtil.closeEL(fos);
			}
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		PageContext pc = ThreadLocalPageContext.get();
		FormImpl form = _form(pc);
		URLImpl url = _url(pc);
		return ScopeUtil.getParameterMap(new URLItem[][] { form.getRaw(), url.getRaw() }, new String[] { form.getEncoding(), url.getEncoding() });
	}

	@Override
	public String getParameter(String name) {
		if (!disconnected) {
			String val = req.getParameter(name);
			if (val != null) return val;
		}
		String[] values = getParameterValues(name);
		if (ArrayUtil.isEmpty(values)) return null;
		return values[0];
	}

	private static URLImpl _url(PageContext pc) {
		URL u = pc.urlScope();
		if (u instanceof UrlFormImpl) {
			return ((UrlFormImpl) u).getURL();
		}
		return (URLImpl) u;
	}

	private static FormImpl _form(PageContext pc) {
		Form f = pc.formScope();
		if (f instanceof UrlFormImpl) {
			return ((UrlFormImpl) f).getForm();
		}
		return (FormImpl) f;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return new ItasEnum<String>(getParameterMap().keySet().iterator());
	}

	@Override
	public String[] getParameterValues(String name) {
		return getParameterValues(ThreadLocalPageContext.get(), name);
	}

	public static String[] getParameterValues(PageContext pc, String name) {
		pc = ThreadLocalPageContext.get(pc);
		FormImpl form = _form(pc);
		URLImpl url = _url(pc);
		return ScopeUtil.getParameterValues(new URLItem[][] { form.getRaw(), url.getRaw() }, new String[] { form.getEncoding(), url.getEncoding() }, name);
	}

	@Override
	public BufferedReader getReader() throws IOException {
		String strEnc = getCharacterEncoding();
		Charset enc = null;
		if (StringUtil.isEmpty(strEnc)) enc = CharsetUtil.ISO88591;
		else CharsetUtil.toCharset(strEnc);
		return IOUtil.toBufferedReader(IOUtil.getReader(getInputStream(), enc));
	}

	public HttpServletRequest getOriginalRequest() {
		if (disconnected) return null;
		return req;
	}

	public synchronized void disconnect(PageContextImpl pc) {
		if (disconnected) return;
		disconnectData = new DisconnectData();

		// attributes
		{
			Iterator<String> it = ListUtil.toIterator(req.getAttributeNames());
			disconnectData.attributes = MapFactory.getConcurrentMap();
			String k;
			while (it.hasNext()) {
				k = it.next();
				if (!StringUtil.isEmpty(k)) disconnectData.attributes.put(k, req.getAttribute(k));
			}
		}

		// headers
		{
			Enumeration<String> headerNames = req.getHeaderNames();
			disconnectData.headers = MapFactory.getConcurrentMap();// new ConcurrentHashMap<Collection.Key, LinkedList<String>>();

			String k;
			Enumeration<String> e;
			while (headerNames.hasMoreElements()) {
				k = headerNames.nextElement().toString();
				e = req.getHeaders(k);
				LinkedList<String> list = new LinkedList<String>();
				while (e.hasMoreElements()) {
					list.add(e.nextElement().toString());
				}
				if (!StringUtil.isEmpty(k)) disconnectData.headers.put(KeyImpl.init(k), list);
			}
		}

		// cookies
		{
			Cookie[] _cookies = req.getCookies();
			if (!ArrayUtil.isEmpty(_cookies)) {
				disconnectData.cookies = new Cookie[_cookies.length];
				for (int i = 0; i < _cookies.length; i++)
					disconnectData.cookies[i] = _cookies[i];
			}
			else disconnectData.cookies = new Cookie[0];
		}

		disconnectData.authType = req.getAuthType();
		disconnectData.method = req.getMethod();
		disconnectData.pathTranslated = req.getPathTranslated();
		disconnectData.remoteUser = req.getRemoteUser();
		disconnectData.requestedSessionId = req.getRequestedSessionId();
		disconnectData.requestedSessionIdFromCookie = req.isRequestedSessionIdFromCookie();
		disconnectData.requestedSessionIdFromURL = req.isRequestedSessionIdFromURL();
		disconnectData.secure = req.isSecure();
		disconnectData.requestedSessionIdValid = req.isRequestedSessionIdValid();
		disconnectData.characterEncoding = req.getCharacterEncoding();
		disconnectData.contentLength = req.getContentLength();
		disconnectData.contentType = req.getContentType();
		disconnectData.serverPort = req.getServerPort();
		disconnectData.serverName = req.getServerName();
		disconnectData.scheme = req.getScheme();
		disconnectData.remoteHost = req.getRemoteHost();
		disconnectData.remoteAddr = req.getRemoteAddr();
		disconnectData.protocol = req.getProtocol();
		disconnectData.locale = req.getLocale();
		// only store it when j2ee sessions are enabled
		if (pc.getSessionType() == Config.SESSION_TYPE_JEE) disconnectData.session = req.getSession(true); // create if necessary

		disconnectData.userPrincipal = req.getUserPrincipal();

		if (bytes == null || file == null) {
			storeEL();
		}
		disconnected = true;
		// req=null;
	}

	static class ArrayEnum<E> implements Enumeration<E> {

		@Override
		public boolean hasMoreElements() {
			return false;
		}

		@Override
		public E nextElement() {
			return null;
		}

	}

	static class ItasEnum<E> implements Enumeration<E> {

		private Iterator<E> it;

		public ItasEnum(Iterator<E> it) {
			this.it = it;
		}

		@Override
		public boolean hasMoreElements() {
			return it.hasNext();
		}

		@Override
		public E nextElement() {
			return it.next();
		}
	}

	static class EmptyEnum<E> implements Enumeration<E> {

		@Override
		public boolean hasMoreElements() {
			return false;
		}

		@Override
		public E nextElement() {
			return null;
		}
	}

	static class StringItasEnum implements Enumeration<String> {

		private Iterator<?> it;

		public StringItasEnum(Iterator<?> it) {
			this.it = it;
		}

		@Override
		public boolean hasMoreElements() {
			return it.hasNext();
		}

		@Override
		public String nextElement() {
			return StringUtil.toStringNative(it.next(), "");
		}

	}

	@Override
	public String getAuthType() {
		if (disconnected) return disconnectData.authType;
		return req.getAuthType();
	}

	@Override
	public Cookie[] getCookies() {
		if (disconnected) return disconnectData.cookies;
		return req.getCookies();

	}

	@Override
	public long getDateHeader(String name) {
		if (!disconnected) return req.getDateHeader(name);

		String h = getHeader(name);
		if (h == null) return -1;
		DateTime dt = DateCaster.toDateAdvanced(h, null, null);
		if (dt == null) throw new IllegalArgumentException("cannot convert [" + getHeader(name) + "] to date time value");
		return dt.getTime();
	}

	@Override
	public int getIntHeader(String name) {
		if (!disconnected) return req.getIntHeader(name);

		String h = getHeader(name);
		if (h == null) return -1;
		Integer i = Caster.toInteger(h, null);
		if (i == null) throw new NumberFormatException("cannot convert [" + getHeader(name) + "] to int value");
		return i.intValue();
	}

	@Override
	public String getHeader(String name) {
		if (!disconnected) return req.getHeader(name);

		LinkedList<String> value = disconnectData.headers.get(KeyImpl.init(name));
		if (value == null) return null;
		return value.getFirst();
	}

	@Override
	public Enumeration getHeaderNames() {
		if (!disconnected) return req.getHeaderNames();
		Set<Key> set = disconnectData.headers.keySet();
		return new StringIterator(set.toArray(new Key[set.size()]));
	}

	@Override
	public Enumeration getHeaders(String name) {
		if (!disconnected) return req.getHeaders(name);

		LinkedList<String> value = disconnectData.headers.get(KeyImpl.init(name));
		if (value != null) return new ItasEnum<String>(value.iterator());
		return new EmptyEnum<String>();
	}

	@Override
	public String getMethod() {
		if (!disconnected) return req.getMethod();
		return disconnectData.method;
	}

	@Override
	public String getPathTranslated() {
		if (!disconnected) return req.getPathTranslated();
		return disconnectData.pathTranslated;
	}

	@Override
	public String getRemoteUser() {
		if (!disconnected) return req.getRemoteUser();
		return disconnectData.remoteUser;
	}

	@Override
	public String getRequestedSessionId() {
		if (!disconnected) return req.getRequestedSessionId();
		return disconnectData.requestedSessionId;
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	@Override
	public HttpSession getSession(boolean create) {
		if (!disconnected) return req.getSession(create);
		return this.disconnectData.session;
	}

	@Override
	public Principal getUserPrincipal() {
		if (!disconnected) return req.getUserPrincipal();
		return this.disconnectData.userPrincipal;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		if (!disconnected) return req.isRequestedSessionIdFromCookie();
		return disconnectData.requestedSessionIdFromCookie;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		if (!disconnected) return req.isRequestedSessionIdFromURL();
		return disconnectData.requestedSessionIdFromURL;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		if (!disconnected) return req.isRequestedSessionIdValid();
		return disconnectData.requestedSessionIdValid;
	}

	@Override
	public String getCharacterEncoding() {
		if (!disconnected) return req.getCharacterEncoding();
		return disconnectData.characterEncoding;
	}

	@Override
	public int getContentLength() {
		if (!disconnected) return req.getContentLength();
		return disconnectData.contentLength;
	}

	@Override
	public String getContentType() {
		if (!disconnected) return req.getContentType();
		return disconnectData.contentType;
	}

	@Override
	public Locale getLocale() {
		if (!disconnected) return req.getLocale();
		return disconnectData.locale;
	}

	@Override
	public boolean isUserInRole(String role) {
		if (!disconnected) return req.isUserInRole(role);
		// try it anyway, in some servlet engine it is still working
		try {
			return req.isUserInRole(role);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		// TODO add support for this
		throw new RuntimeException("this method is not supported when root request is gone");
	}

	@Override
	public Enumeration getLocales() {
		if (!disconnected) return req.getLocales();
		// try it anyway, in some servlet engine it is still working
		try {
			return req.getLocales();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		// TODO add support for this
		throw new RuntimeException("this method is not supported when root request is gone");
	}

	@Override
	public String getRealPath(String path) {
		if (!disconnected) return req.getRealPath(path);
		// try it anyway, in some servlet engine it is still working
		try {
			return req.getRealPath(path);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		// TODO add support for this
		throw new RuntimeException("this method is not supported when root request is gone");
	}

	@Override
	public String getProtocol() {
		if (!disconnected) return req.getProtocol();
		return disconnectData.protocol;
	}

	@Override
	public String getRemoteAddr() {
		if (!disconnected) return req.getRemoteAddr();
		return disconnectData.remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		if (!disconnected) return req.getRemoteHost();
		return disconnectData.remoteHost;
	}

	@Override
	public String getScheme() {
		if (!disconnected) return req.getScheme();
		return disconnectData.scheme;
	}

	@Override
	public String getServerName() {
		if (!disconnected) return req.getServerName();
		return disconnectData.serverName;
	}

	@Override
	public int getServerPort() {
		if (!disconnected) return req.getServerPort();
		return disconnectData.serverPort;
	}

	@Override
	public boolean isSecure() {
		if (!disconnected) return req.isSecure();
		return disconnectData.secure;
	}

	@Override
	public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
		if (!disconnected) req.setCharacterEncoding(enc);
		else disconnectData.characterEncoding = enc;
	}

	@Override
	public AsyncContext getAsyncContext() {
		if (!disconnected) return req.getAsyncContext();
		throw new RuntimeException("not supported!");
	}

	@Override
	public long getContentLengthLong() {
		if (!disconnected) return req.getContentLengthLong();
		return getContentLength();
	}

	@Override
	public DispatcherType getDispatcherType() {
		if (!disconnected) return req.getDispatcherType();
		throw new RuntimeException("not supported!");
	}

	@Override
	public String getLocalAddr() {
		if (!disconnected) return req.getLocalAddr();
		throw new RuntimeException("not supported!");
	}

	@Override
	public String getLocalName() {
		if (!disconnected) return req.getLocalName();
		throw new RuntimeException("not supported!");
	}

	@Override
	public int getLocalPort() {
		if (!disconnected) return req.getLocalPort();
		throw new RuntimeException("not supported!");
	}

	@Override
	public int getRemotePort() {
		if (!disconnected) return req.getRemotePort();
		throw new RuntimeException("not supported!");
	}

	@Override
	public ServletContext getServletContext() {
		if (!disconnected) return req.getServletContext();
		throw new RuntimeException("not supported!");
	}

	@Override
	public boolean isAsyncStarted() {
		if (!disconnected) return req.isAsyncStarted();
		throw new RuntimeException("not supported!");
	}

	@Override
	public boolean isAsyncSupported() {
		if (!disconnected) return req.isAsyncSupported();
		throw new RuntimeException("not supported!");
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		if (!disconnected) return req.startAsync();
		throw new RuntimeException("not supported!");
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {
		if (!disconnected) return req.startAsync(arg0, arg1);
		throw new RuntimeException("not supported!");
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {
		if (!disconnected) return req.authenticate(arg0);
		throw new RuntimeException("not supported!");
	}

	@Override
	public String changeSessionId() {
		if (!disconnected) return req.changeSessionId();
		throw new RuntimeException("not supported!");
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {
		if (!disconnected) return req.getPart(arg0);
		throw new RuntimeException("not supported!");
	}

	@Override
	public java.util.Collection<Part> getParts() throws IOException, ServletException {
		if (!disconnected) return req.getParts();
		throw new RuntimeException("not supported!");
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		if (!disconnected) req.login(arg0, arg1);
		throw new RuntimeException("not supported!");
	}

	@Override
	public void logout() throws ServletException {
		if (!disconnected) req.logout();
		throw new RuntimeException("not supported!");
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0) throws IOException, ServletException {
		if (!disconnected) return req.upgrade(arg0);
		throw new RuntimeException("not supported!");
	}

	public void close() {
		if (file != null) {
			if (!file.delete()) file.deleteOnExit();
			file = null;
		}
		bytes = null;
	}
}