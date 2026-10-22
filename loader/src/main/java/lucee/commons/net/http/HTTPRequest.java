package lucee.commons.net.http;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lucee.commons.io.res.Resource;
import lucee.runtime.net.proxy.ProxyData;

/**
 * A request for an {@link HTTPClientEngine}, independent of the HTTP library that sends it. The
 * request is built by the caller (for example the tag cfhttp) and sent with
 * {@link HTTPClientEngine#execute(HTTPRequest)}.
 */
public final class HTTPRequest {

	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String DELETE = "DELETE";
	public static final String HEAD = "HEAD";
	public static final String OPTIONS = "OPTIONS";
	public static final String TRACE = "TRACE";
	public static final String PATCH = "PATCH";

	public static final short AUTH_BASIC = 0;
	public static final short AUTH_NTLM = 1;

	/**
	 * a part of a multipart body, either a text value or a resource (file)
	 */
	public static final class Part {
		private final String name;
		private final String value;
		private final Resource resource;
		private final String mimeType;
		private final String charset;

		private Part(String name, String value, Resource resource, String mimeType, String charset) {
			this.name = name;
			this.value = value;
			this.resource = resource;
			this.mimeType = mimeType;
			this.charset = charset;
		}

		public static Part text(String name, String value, String charset) {
			return new Part(name, value, null, null, charset);
		}

		public static Part file(String name, Resource resource, String mimeType, String charset) {
			return new Part(name, null, resource, mimeType, charset);
		}

		public String getName() {
			return name;
		}

		/**
		 * @return the text value, null for a file part
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @return the resource, null for a text part
		 */
		public Resource getResource() {
			return resource;
		}

		public String getMimeType() {
			return mimeType;
		}

		public String getCharset() {
			return charset;
		}
	}

	/**
	 * a cookie that is sent with the request
	 */
	public static final class Cookie {
		private final String name;
		private final String value;
		private final String domain;
		private final String path;

		public Cookie(String name, String value, String domain, String path) {
			this.name = name;
			this.value = value;
			this.domain = domain;
			this.path = path;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public String getDomain() {
			return domain;
		}

		public String getPath() {
			return path;
		}
	}

	private String method = GET;
	private URL url;
	private final List<Header> headers = new ArrayList<>();
	private final List<Cookie> cookies = new ArrayList<>();

	// body, at most one of the following: raw body, form fields (url encoded) or multipart parts
	private Object body;
	private String bodyMimeType;
	private String bodyCharset;
	private List<String[]> formFields;
	private List<Part> parts;

	private String charset;

	// timeouts in milliseconds, -1 for the default of the engine, 0 for no timeout
	private long connectTimeout = -1;
	private long readTimeout = -1;
	private long connectionRequestTimeout = -1;

	// "true", "false" or "lax" (lax follows redirects of every method)
	private String redirect = "true";
	private boolean pooling = true;

	private String clientCert;
	private String clientCertPassword;
	private String trustStore;
	private String trustStorePassword;
	private boolean sslVerify = true;

	private String username;
	private String password;
	private short authType = AUTH_BASIC;
	private String workStation;
	private String domain;
	private boolean preAuth = true;

	private ProxyData proxy;

	private Runnable abortHandler;
	private volatile boolean aborted;

	public String getMethod() {
		return method;
	}

	public HTTPRequest setMethod(String method) {
		this.method = method.toUpperCase();
		return this;
	}

	public URL getURL() {
		return url;
	}

	public HTTPRequest setURL(URL url) {
		this.url = url;
		return this;
	}

	public List<Header> getHeaders() {
		return headers;
	}

	/**
	 * adds a header, existing headers with the same name are kept
	 */
	public HTTPRequest addHeader(String name, String value) {
		headers.add(new HeaderImpl(name, value));
		return this;
	}

	/**
	 * sets a header, existing headers with the same name are removed
	 */
	public HTTPRequest setHeader(String name, String value) {
		headers.removeIf(h -> h.getName().equalsIgnoreCase(name));
		return addHeader(name, value);
	}

	public boolean hasHeader(String name) {
		for (Header h: headers) {
			if (h.getName().equalsIgnoreCase(name)) return true;
		}
		return false;
	}

	public List<Cookie> getCookies() {
		return cookies;
	}

	public HTTPRequest addCookie(String name, String value, String domain, String path) {
		cookies.add(new Cookie(name, value, domain, path));
		return this;
	}

	public Object getBody() {
		return body;
	}

	public String getBodyMimeType() {
		return bodyMimeType;
	}

	public String getBodyCharset() {
		return bodyCharset;
	}

	/**
	 * sets a raw body
	 * 
	 * @param body a String, byte[], InputStream, Resource, or any value that can be cast to binary. The
	 *            String "&lt;empty&gt;" is an empty body.
	 * @param mimeType mime type of the body, can be null (not used for binary input)
	 * @param charset charset of the body, can be null (not used for binary input)
	 */
	public HTTPRequest setBody(Object body, String mimeType, String charset) {
		this.body = body;
		this.bodyMimeType = mimeType;
		this.bodyCharset = charset;
		return this;
	}

	/**
	 * @return the form fields as pairs of name and value, null if there are none
	 */
	public List<String[]> getFormFields() {
		return formFields;
	}

	/**
	 * adds a form field, the form is sent url encoded, or as multipart when there are parts
	 */
	public HTTPRequest addFormField(String name, String value) {
		if (formFields == null) formFields = new ArrayList<>();
		formFields.add(new String[] { name, value });
		return this;
	}

	/**
	 * @return the multipart parts, null if the body is not multipart
	 */
	public List<Part> getParts() {
		return parts;
	}

	public HTTPRequest addPart(Part part) {
		if (parts == null) parts = new ArrayList<>();
		parts.add(part);
		return this;
	}

	public String getCharset() {
		return charset;
	}

	/**
	 * charset used to encode the form fields
	 */
	public HTTPRequest setCharset(String charset) {
		this.charset = charset;
		return this;
	}

	public long getConnectTimeout() {
		return connectTimeout;
	}

	public HTTPRequest setConnectTimeout(long connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	public long getReadTimeout() {
		return readTimeout;
	}

	public HTTPRequest setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
		return this;
	}

	public long getConnectionRequestTimeout() {
		return connectionRequestTimeout;
	}

	public HTTPRequest setConnectionRequestTimeout(long connectionRequestTimeout) {
		this.connectionRequestTimeout = connectionRequestTimeout;
		return this;
	}

	public String getRedirect() {
		return redirect;
	}

	public HTTPRequest setRedirect(String redirect) {
		this.redirect = redirect;
		return this;
	}

	public boolean isPooling() {
		return pooling;
	}

	public HTTPRequest setPooling(boolean pooling) {
		this.pooling = pooling;
		return this;
	}

	public String getClientCert() {
		return clientCert;
	}

	public String getClientCertPassword() {
		return clientCertPassword;
	}

	public HTTPRequest setClientCert(String clientCert, String clientCertPassword) {
		this.clientCert = clientCert;
		this.clientCertPassword = clientCertPassword;
		return this;
	}

	public String getTrustStore() {
		return trustStore;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public HTTPRequest setTrustStore(String trustStore, String trustStorePassword) {
		this.trustStore = trustStore;
		this.trustStorePassword = trustStorePassword;
		return this;
	}

	public boolean isSslVerify() {
		return sslVerify;
	}

	public HTTPRequest setSslVerify(boolean sslVerify) {
		this.sslVerify = sslVerify;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public short getAuthType() {
		return authType;
	}

	public String getWorkStation() {
		return workStation;
	}

	public String getDomain() {
		return domain;
	}

	public boolean isPreAuth() {
		return preAuth;
	}

	/**
	 * basic authentication
	 */
	public HTTPRequest setCredentials(String username, String password, boolean preAuth) {
		this.username = username;
		this.password = password == null ? "" : password;
		this.authType = AUTH_BASIC;
		this.preAuth = preAuth;
		return this;
	}

	/**
	 * NTLM authentication
	 */
	public HTTPRequest setNTCredentials(String username, String password, String workStation, String domain) {
		this.username = username;
		this.password = password == null ? "" : password;
		this.authType = AUTH_NTLM;
		this.workStation = workStation;
		this.domain = domain;
		return this;
	}

	public ProxyData getProxy() {
		return proxy;
	}

	public HTTPRequest setProxy(ProxyData proxy) {
		this.proxy = proxy;
		return this;
	}

	/**
	 * called by the engine when it starts sending the request, so the request can be aborted from another
	 * thread
	 */
	public void setAbortHandler(Runnable abortHandler) {
		this.abortHandler = abortHandler;
		if (aborted && abortHandler != null) abortHandler.run();
	}

	/**
	 * aborts the request, can be called from any thread
	 */
	public void abort() {
		aborted = true;
		Runnable h = abortHandler;
		if (h != null) h.run();
	}

	public boolean isAborted() {
		return aborted;
	}

	private static final class HeaderImpl implements Header {
		private final String name;
		private final String value;

		HeaderImpl(String name, String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return name + ": " + value;
		}
	}

	/**
	 * @throws IOException if the request is not valid
	 */
	public void validate() throws IOException {
		if (url == null) throw new IOException("no URL defined for the HTTP request");
		if (formFields != null && body != null) throw new IOException("a request can not have a body and form fields at the same time");
	}
}
