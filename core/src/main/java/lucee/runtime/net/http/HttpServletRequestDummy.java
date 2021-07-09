/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.net.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

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
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.Array;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.it.ItAsEnum;
import lucee.runtime.util.EnumerationWrapper;

public final class HttpServletRequestDummy implements HttpServletRequest, Serializable {

	private Cookie[] cookies;

	private String authType;
	private Pair<String, Object>[] headers = new Pair[0];
	private Pair<String, Object>[] parameters = new Pair[0];
	private Struct attributes = new StructImpl();
	private String method = "GET";
	private String pathInfo;
	private String pathTranslated;
	private String contextPath = "";
	private String queryString;
	private String remoteUser;
	private String requestedSessionId;
	private String requestURI;

	private String protocol = "HTTP/1.1";
	private String serverName = "localhost";
	private int port = 80;

	private String characterEncoding = "ISO-8859-1";

	private String contentType;
	private byte[] inputData = new byte[0];

	private static InetAddress DEFAULT_REMOTE;
	private static String DEFAULT_REMOTE_ADDR;
	private static String DEFAULT_REMOTE_HOST;
	static {
		try {
			DEFAULT_REMOTE = InetAddress.getLocalHost();
			DEFAULT_REMOTE_ADDR = DEFAULT_REMOTE.getHostAddress();
			DEFAULT_REMOTE_HOST = DEFAULT_REMOTE.getHostName();
		}
		catch (UnknownHostException e) {
		}
	}
	// private InetAddress remoteq=DEFAULT_REMOTE;
	private String remoteAddr = DEFAULT_REMOTE_ADDR;
	private String remoteHost = DEFAULT_REMOTE_HOST;

	private Locale locale = Locale.getDefault();

	private boolean secure;

	private Resource contextRoot;

	private String scheme = "http";

	private HttpSession session;

	/**
	 * constructor of the class
	 * 
	 * @param headers
	 * @param parameters
	 * @param httpSession
	 * @param pairs
	 * @param cookiess
	 */
	public HttpServletRequestDummy(Resource contextRoot, String serverName, String scriptName, String queryString, Cookie[] cookies, Pair[] headers, Pair[] parameters,
			Struct attributes, HttpSession session, byte[] inputData) {
		this.serverName = serverName;
		requestURI = scriptName;
		this.queryString = queryString;
		this.parameters = translateQS(queryString);
		this.contextRoot = contextRoot;
		if (cookies != null) setCookies(cookies);
		if (headers != null) this.headers = headers;
		if (parameters != null) this.parameters = parameters;
		if (attributes != null) this.attributes = attributes;
		this.session = session;
		this.inputData = inputData;
	}

	/**
	 * constructor of the class
	 * 
	 * @throws PageException / public HttpServletRequestDummy(String serverName, String
	 *             scriptName,Struct queryString) throws PageException { this.serverName=serverName;
	 *             requestURI=scriptName;
	 * 
	 *             StringBuffer qs=new StringBuffer(); String[] keys=queryString.keys(); parameters=new
	 *             Item[keys.length]; String key; Object value; for(int i=0;i<keys.length;i++) { if(i>0)
	 *             qs.append('&'); key=keys[i]; value=queryString.get(key); parameters[i]=new
	 *             Item(key,value);
	 * 
	 *             qs.append(key); qs.append('='); qs.append(Caster.toString(value)); }
	 * 
	 *             this.queryString=qs.toString(); }
	 */

	private Pair[] translateQS(String qs) {
		if (qs == null) return new Pair[0];
		Array arr = lucee.runtime.type.util.ListUtil.listToArrayRemoveEmpty(qs, "&");
		Pair[] parameters = new Pair[arr.size()];
		// Array item;
		int index;
		String name;

		for (int i = 1; i <= parameters.length; i++) {
			name = Caster.toString(arr.get(i, ""), "");
			index = name.indexOf('=');
			if (index != -1) parameters[i - 1] = new Pair(name.substring(0, index), name.substring(index + 1));
			else parameters[i - 1] = new Pair(name, "");

		}
		return parameters;
	}

	@Override
	public String getAuthType() {
		return authType;
	}

	/**
	 * sets the name of the authentication scheme used to protect the servlet. All servlet containers
	 * support basic, form and client certificate authentication, and may additionally support digest
	 * authentication.
	 * 
	 * @param authType authentication type
	 */
	public void setAuthType(String authType) {
		this.authType = authType;
	}

	@Override
	public Cookie[] getCookies() {
		return cookies;
	}

	/**
	 * sets an array containing all of the Cookie objects the client sent with this request. This method
	 * returns null if no cookies were sent.
	 * 
	 * @param cookies
	 */
	public void setCookies(Cookie[] cookies) {
		this.cookies = cookies;
	}

	@Override
	public long getDateHeader(String name) {
		Object value = getHeader(name);
		if (value != null) {
			Date date = DateCaster.toDateAdvanced(value, null, null);
			if (date != null) return date.getTime();
			throw new IllegalArgumentException("can't convert value " + value + " to a Date");
		}
		return -1;
	}

	public void setDateHeader(String name, long value) {
		// TODO wrong format
		setHeader(name, new DateTimeImpl(value, false).castToString());
	}

	@Override
	public String getHeader(String name) {
		return ReqRspUtil.get(headers, name);
	}

	/**
	 * sets a new header value
	 * 
	 * @param name name of the new value
	 * @param value header value
	 */
	public void setHeader(String name, String value) {
		headers = ReqRspUtil.set(headers, name, value);
	}

	/**
	 * add a new header value
	 * 
	 * @param name name of the new value
	 * @param value header value
	 */
	public void addHeader(String name, String value) {
		headers = ReqRspUtil.add(headers, name, value);
	}

	@Override
	public Enumeration getHeaders(String name) {
		HashSet set = new HashSet();
		for (int i = 0; i < headers.length; i++) {
			if (headers[i].getName().equalsIgnoreCase(name)) set.add(Caster.toString(headers[i].getValue(), null));
		}
		return new EnumerationWrapper(set);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < headers.length; i++) {
			set.add(headers[i].getName());
		}
		return new EnumerationWrapper<String>(set);
	}

	@Override
	public int getIntHeader(String name) {
		Object value = getHeader(name);
		if (value != null) {
			try {
				return Caster.toIntValue(value);
			}
			catch (PageException e) {
				throw new NumberFormatException(e.getMessage());
			}
		}
		return -1;
	}

	@Override
	public String getMethod() {
		return method;
	}

	/**
	 * sets the request method
	 * 
	 * @param method
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	/**
	 * Sets any extra path information associated with the URL the client sent when it made this
	 * request. The extra path information follows the servlet path but precedes the query string.
	 * 
	 * @param pathInfo
	 */
	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	@Override
	public String getPathTranslated() {
		return pathTranslated;
	}

	/**
	 * sets any extra path information after the servlet name but before the query string, translates to
	 * a real path. Same as the value of the CGI variable PATH_TRANSLATED.
	 * 
	 * @param pathTranslated
	 */
	public void setPathTranslated(String pathTranslated) {
		// TODO muss auf pathinfo basieren
		this.pathTranslated = pathTranslated;
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}

	/**
	 * sets the portion of the request URI that indicates the context of the request. The context path
	 * always comes first in a request URI. The path starts with a "/" character but does not end with a
	 * "/" character.
	 * 
	 * @param contextPath
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	/**
	 * sets the query string that is contained in the request URL after the path. Same as the value of
	 * the CGI variable QUERY_STRING.
	 * 
	 * @param queryString
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
		parameters = translateQS(queryString);
	}

	@Override
	public String getRemoteUser() {
		return remoteUser;
	}

	/**
	 * sets the login of the user making this request, if the user has been authenticated, or null if
	 * the user has not been authenticated. Whether the user name is sent with each subsequent request
	 * depends on the browser and type of authentication. Same as the value of the CGI variable
	 * REMOTE_USER.
	 * 
	 * @param remoteUser
	 */
	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	@Override
	public boolean isUserInRole(String role) {
		// TODO impl
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO impl
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return requestedSessionId;
	}

	/**
	 * sets the session ID specified by the client. This may not be the same as the ID of the actual
	 * session in use. For example, if the request specified an old (expired) session ID and the server
	 * has started a new session, this method gets a new session with a new ID.
	 * 
	 * @param requestedSessionId
	 */
	public void setRequestedSessionId(String requestedSessionId) {
		this.requestedSessionId = requestedSessionId;
	}

	@Override
	public String getRequestURI() {
		return requestURI;
	}

	/**
	 * sets the part of this request's URL from the protocol name up to the query string in the first
	 * line of the HTTP request. The web container does not decode this String.
	 * 
	 * @param requestURI
	 */
	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	@Override
	public StringBuffer getRequestURL() {
		return new StringBuffer(isSecure() ? "https" : "http").append("://").append(serverName).append(':').append(port).append('/').append(requestURI);
	}

	@Override
	public String getServletPath() {
		// TODO when different ?
		return requestURI;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		return session;
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// not supported
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// not supported
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// not supported
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key, null);
	}

	@Override
	public void setAttribute(String key, Object value) {
		attributes.setEL(key, value);
	}

	@Override
	public void removeAttribute(String key) {
		attributes.removeEL(KeyImpl.init(key));
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return ItAsEnum.toStringEnumeration(attributes.keyIterator());
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
		this.characterEncoding = characterEncoding;
	}

	@Override
	public int getContentLength() {
		return inputData == null ? -1 : inputData.length;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	/**
	 * sets the content Type of the Request
	 * 
	 * @param contentType
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new ServletInputStreamDummy(inputData);
	}

	public void setParameter(String key, String value) {
		parameters = ReqRspUtil.set(parameters, key, value);
		rewriteQS();
	}

	public void addParameter(String key, String value) {
		parameters = ReqRspUtil.add(parameters, key, value);
		rewriteQS();
	}

	@Override
	public String getParameter(String key) {
		return ReqRspUtil.get(parameters, key);
	}

	@Override
	public String[] getParameterValues(String key) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].getName().equalsIgnoreCase(key)) list.add(Caster.toString(parameters[i].getValue(), null));
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < parameters.length; i++) {
			set.add(parameters[i].getName());
		}
		return new EnumerationWrapper<String>(set);
	}

	@Override
	public Map getParameterMap() {
		Map<String, Object> p = MapFactory.<String, Object>getConcurrentMap();
		for (int i = 0; i < parameters.length; i++) {
			p.put(parameters[i].getName(), parameters[i].getValue());
		}
		return p;
	}

	/**
	 * set the Protocol (Default "http")
	 * 
	 * @param protocol
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public int getServerPort() {
		return port;
	}

	/**
	 * @param port The port to set.
	 */
	public void setServerPort(int port) {
		this.port = port;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return IOUtil.toBufferedReader(IOUtil.getReader(getInputStream(), CharsetUtil.ISO88591));
	}

	@Override
	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	@Override
	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteInetAddress(InetAddress ia) {
		setRemoteAddr(ia.getHostAddress());
		setRemoteHost(ia.getHostName());
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public Enumeration<java.util.Locale> getLocales() {
		return new EnumerationWrapper<Locale>(Locale.getAvailableLocales());
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return new RequestDispatcherDummy(this);
	}

	@Override
	public String getRealPath(String path) {
		return contextRoot.getReal(path);
	}

	/**
	 * @return the inputData
	 */
	public byte[] getInputData() {
		return inputData;
	}

	/**
	 * @param inputData the inputData to set
	 */
	public void setInputData(byte[] inputData) {
		this.inputData = inputData;
	}

	private void rewriteQS() {
		StringBuffer qs = new StringBuffer();
		Pair<String, Object> p;
		for (int i = 0; i < parameters.length; i++) {
			if (i > 0) qs.append('&');
			p = parameters[i];
			qs.append(p.getName());
			qs.append('=');
			qs.append(Caster.toString(p.getValue(), ""));
		}
		queryString = qs.toString();
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

	public static HttpServletRequestDummy clone(Config config, Resource rootDirectory, HttpServletRequest req) {
		byte[] inputData = null;
		try {
			inputData = IOUtil.toBytes(req.getInputStream(), true, null);
		}
		catch (IOException e) {
		}

		HttpServletRequestDummy dest = new HttpServletRequestDummy(rootDirectory, req.getServerName(), req.getRequestURI(), req.getQueryString(),
				HttpUtil.cloneCookies(config, req), HttpUtil.cloneHeaders(req), HttpUtil.cloneParameters(req), HttpUtil.getAttributesAsStruct(req), getSessionEL(req), inputData);

		try {
			dest.setCharacterEncoding(req.getCharacterEncoding());
		}
		catch (Exception e) {

		}

		dest.setRemoteAddr(req.getRemoteAddr());
		dest.setRemoteHost(req.getRemoteHost());
		dest.setAuthType(req.getAuthType());
		dest.setContentType(req.getContentType());
		dest.setContextPath(req.getContextPath());
		dest.setLocale(req.getLocale());
		dest.setMethod(req.getMethod());
		dest.setPathInfo(req.getPathInfo());
		dest.setProtocol(req.getProtocol());
		dest.setRequestedSessionId(req.getRequestedSessionId());
		dest.setScheme(req.getScheme());
		dest.setServerPort(req.getServerPort());
		dest.setSession(getSessionEL(req));
		return dest;
	}

	private static HttpSession getSessionEL(HttpServletRequest req) {
		try {
			return req.getSession();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return null;
	}

	public void setAttributes(Struct attributes) {
		this.attributes = attributes;
	}

	@Override
	public AsyncContext getAsyncContext() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public long getContentLengthLong() {
		return getContentLength();
	}

	@Override
	public DispatcherType getDispatcherType() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public String getLocalAddr() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public String getLocalName() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public int getLocalPort() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public int getRemotePort() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public ServletContext getServletContext() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public boolean isAsyncStarted() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public boolean isAsyncSupported() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		throw new RuntimeException("not supported!");
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {
		throw new RuntimeException("not supported!");
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {
		throw new RuntimeException("not supported!");
	}

	@Override
	public String changeSessionId() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {
		throw new RuntimeException("not supported!");
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		throw new RuntimeException("not supported!");
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		throw new RuntimeException("not supported!");
	}

	@Override
	public void logout() throws ServletException {
		throw new RuntimeException("not supported!");
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0) throws IOException, ServletException {
		throw new RuntimeException("not supported!");
	}

}