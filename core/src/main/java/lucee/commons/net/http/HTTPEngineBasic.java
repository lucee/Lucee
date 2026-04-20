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
package lucee.commons.net.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpMessage;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultClientConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.TemporaryStream;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.httpclient.HTTPResponse4Impl;
import lucee.commons.net.http.httpclient.HeaderImpl;
import lucee.commons.net.http.httpclient.HeaderWrap;
import lucee.commons.net.http.httpclient.entity.ByteArrayHttpEntity;
import lucee.commons.net.http.httpclient.entity.EmptyHttpEntity;
import lucee.commons.net.http.httpclient.entity.ResourceHttpEntity;
import lucee.commons.net.http.httpclient.entity.TemporaryStreamHttpEntity;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.http.sni.DefaultHostnameVerifierImpl;
import lucee.runtime.net.http.sni.DefaultHttpClientConnectionOperatorImpl;
import lucee.runtime.net.http.sni.SSLConnectionSocketFactoryImpl;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;

public abstract class HTTPEngineBasic {

	// private static final boolean use4=true;

	/**
	 * Field <code>ACTION_POST</code>
	 */
	public static final short ACTION_POST = 0;

	/**
	 * Field <code>ACTION_GET</code>
	 */
	public static final short ACTION_GET = 1;

	/**
	 * Field <code>STATUS_OK</code>
	 */
	public static final int STATUS_OK = 200;
	// private static final String NO_MIMETYPE="Unable to determine MIME type of file.";

	public static final int MAX_REDIRECT = 15;

	/**
	 * Constant value for HTTP Status Code "moved Permanently 301"
	 */
	public static final int STATUS_REDIRECT_MOVED_PERMANENTLY = 301;
	/**
	 * Constant value for HTTP Status Code "Found 302"
	 */
	public static final int STATUS_REDIRECT_FOUND = 302;
	/**
	 * Constant value for HTTP Status Code "see other 303"
	 */
	public static final int STATUS_REDIRECT_SEE_OTHER = 303;

	private static Field isShutDownField;
	private static Map<String, PoolingHttpClientConnectionManager> connectionManagers = new ConcurrentHashMap<>();
	private static boolean cannotAccess = false;

	public static final int POOL_MAX_CONN = 1000;
	public static final int POOL_MAX_CONN_PER_ROUTE = 100;
	public static final int POOL_CONN_TTL_MS = 15000;
	public static final int POOL_CONN_INACTIVITY_DURATION = 2000;

	public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
	public static final int DEFAULT_CONNECT_REQUEST_TIMEOUT = 5000;

	public static final long DEFAULT_READ_TIMEOUT = 60000; // 60 seconds
	protected static final String DEFAULT_USER_AGENT = "Lucee";

	private static volatile CloseableHttpClient SHARED_CLIENT_POOLING;
	private static HttpClientConnectionManager SHARED_CLIENT_POOLING_CM;
	private static final Object CLIENT_LOCK = new Object();

	public static void releaseSharedClient() {
		synchronized (CLIENT_LOCK) {
			if (SHARED_CLIENT_POOLING != null) {
				IOUtil.closeEL(SHARED_CLIENT_POOLING);
				SHARED_CLIENT_POOLING = null;
			}
		}
	}

	private static CloseableHttpClient getHttpClient(boolean pooling) throws IOException {
		if (pooling) {
			if (SHARED_CLIENT_POOLING == null) {
				synchronized (CLIENT_LOCK) {
					if (SHARED_CLIENT_POOLING == null) {
						Pair<HttpClientBuilder, HttpClientConnectionManager> pair;
						try {
							pair = getHttpClientBuilder(true, null, null, "true");
						}
						catch (GeneralSecurityException e) {
							throw ExceptionUtil.toIOException(e);
						}
						SHARED_CLIENT_POOLING_CM = pair.getValue();
						SHARED_CLIENT_POOLING = pair.getName().build();
					}
				}
			}

			// Stats are useful for debugging pool exhaustion, but noisy for Info level
			if (SHARED_CLIENT_POOLING_CM instanceof PoolingHttpClientConnectionManager) {
				if (LogUtil.does(Log.LEVEL_DEBUG)) {
					PoolingHttpClientConnectionManager cm = (PoolingHttpClientConnectionManager) SHARED_CLIENT_POOLING_CM;
					org.apache.http.pool.PoolStats stats = cm.getTotalStats();
					LogUtil.log(Log.LEVEL_DEBUG, "http", "Pool Stats -> " + "Max: " + stats.getMax() + ", Leased: " + stats.getLeased() + ", Pending: " + stats.getPending()
							+ ", Available: " + stats.getAvailable());
				}

			}
			return SHARED_CLIENT_POOLING;
		}

		// Always return a fresh, unshared client for non-pooling requests to ensure thread safety
		HttpClientBuilder builder;
		try {
			builder = getHttpClientBuilder(false, null, null, "true").getName();
		}
		catch (GeneralSecurityException e) {
			throw ExceptionUtil.toIOException(e);
		}
		return builder.build();
	}

	public static Struct getMetaData() throws IOException {
		getHttpClient(true); // neded to create objects
		PoolingHttpClientConnectionManager cm = (PoolingHttpClientConnectionManager) SHARED_CLIENT_POOLING_CM;
		org.apache.http.pool.PoolStats stats = cm.getTotalStats();
		Struct sct = new StructImpl();

		// status
		Struct status = new StructImpl();
		sct.setEL(KeyConstants._status, status);
		status.setEL(KeyConstants._max, stats.getMax());
		status.setEL("leased", stats.getLeased());
		status.setEL("pending", stats.getPending());
		status.setEL("available", stats.getAvailable());

		// config
		Struct config = new StructImpl();
		sct.setEL(KeyConstants._config, config);
		config.setEL("defaultMaxPerRoute", cm.getDefaultMaxPerRoute());
		config.setEL("maxTotal", cm.getMaxTotal());
		config.setEL("validateAfterInactivity", cm.getValidateAfterInactivity());

		return sct;
	}

	/**
	 * Build RequestConfig with separate connection and socket timeouts (following Http.java pattern)
	 */
	private static RequestConfig buildRequestConfig(long connectTimeout, long socketTimeout) {
		return RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).setConnectionRequestTimeout(2000).setConnectTimeout((int) connectTimeout)
				.setSocketTimeout((int) socketTimeout).build();
	}

	/**
	 * Get ProxyData from ThreadLocalPageContext Config (following Http.java pattern line 1099)
	 */
	private static ProxyData getProxyData(String host) {
		Config config = ThreadLocalPageContext.getConfigServer();
		if (config != null) {
			ProxyData proxy = config.getProxyData();
			return ProxyDataImpl.validate(proxy, host);
		}
		return null;
	}

	/**
	 * Simple container for client and context
	 */
	private static class ClientContext {
		final CloseableHttpClient client;
		final HttpContext context;

		ClientContext(CloseableHttpClient client, HttpContext context) {
			this.client = client;
			this.context = context;
		}
	}

	/**
	 * Setup HttpClientBuilder with proxy, credentials, and request config. Shared by get(), head(), and
	 * exists() methods to avoid code duplication.
	 */
	private static ClientContext buildHttpClient(URL url, String username, String password, long connectTimeout, long readTimeout, HttpClientBuilder builder,
			org.apache.http.client.methods.HttpUriRequest request) {

		// Get proxy from Config (already validated in getProxyData)
		ProxyData proxy = getProxyData(url.getHost());

		// Build RequestConfig with separate connection and socket timeouts
		RequestConfig requestConfig = buildRequestConfig(connectTimeout, readTimeout);
		builder.setDefaultRequestConfig(requestConfig);

		// Set credentials if provided
		HttpHost httpHost = new HttpHost(url.getHost(), url.getPort());
		HttpContext context = setCredentials(builder, httpHost, username, password, false);

		// Set proxy on builder and request
		setProxy(url.getHost(), builder, request, proxy);

		// Build client
		CloseableHttpClient client = builder.build();

		// Return both client and context
		if (context == null) context = new HttpClientContext();
		return new ClientContext(client, context);
	}

	public static HeaderImpl[] toHeaders(Map<String, String> headers) {
		if (CollectionUtil.isEmpty(headers)) return null;
		HeaderImpl[] rtn = new HeaderImpl[headers.size()];
		Iterator<Entry<String, String>> it = headers.entrySet().iterator();
		Entry<String, String> e;
		int index = 0;
		while (it.hasNext()) {
			e = it.next();
			rtn[index++] = new HeaderImpl(e.getKey(), e.getValue());
		}
		return rtn;
	}

	public static ContentType toContentType(String mimetype, String charset) {
		ContentType ct = null;
		if (!StringUtil.isEmpty(mimetype, true)) {
			if (!StringUtil.isEmpty(charset, true)) ct = ContentType.create(mimetype.trim(), charset.trim());
			else ct = ContentType.create(mimetype.trim());
		}
		return ct;
	}

	public static void closeEL(HTTPResponse rsp) {
		if (rsp instanceof HTTPResponse4Impl) {
			try {
				((HTTPResponse4Impl) rsp).close();
			}
			catch (Exception e) {}
		}

	}

	public static lucee.commons.net.http.Header header(String name, String value) {
		return new HeaderImpl(name, value);
	}

	private static Header toHeader(lucee.commons.net.http.Header header) {
		if (header instanceof Header) return (Header) header;
		if (header instanceof HeaderWrap) return ((HeaderWrap) header).header;
		return new HeaderImpl(header.getName(), header.getValue());
	}

	public static Pair<HttpClientBuilder, HttpClientConnectionManager> getHttpClientBuilder(boolean pooling, String clientCert, String clientCertPassword, String redirect)
			throws GeneralSecurityException, IOException {

		String key = clientCert + ":" + clientCertPassword;
		Registry<ConnectionSocketFactory> reg = StringUtil.isEmpty(clientCert, true) ? createRegistry() : createRegistry(clientCert, clientCertPassword);

		// NON-POOLING PATH (Simple, short-lived client)
		if (!pooling) {
			HttpClientBuilder builder = HttpClients.custom();
			// Basic manager does not pool; it opens/closes a new socket per request
			HttpClientConnectionManager cm = new BasicHttpClientConnectionManager(new DefaultHttpClientConnectionOperatorImpl(reg), null);
			builder.setConnectionManager(cm).setConnectionManagerShared(false).setRedirectStrategy(new LaxRedirectStrategy());

			if (!Caster.toBooleanValue(redirect, true)) builder.disableRedirectHandling();
			return new Pair(builder, cm);
		}

		// POOLING PATH (High-concurrency shared manager)
		PoolingHttpClientConnectionManager cm = connectionManagers.get(key);

		// Double-checked locking using your thread-safe Token utility
		if (cm == null || isShutDown(cm, true)) {
			synchronized (SystemUtil.createToken("PoolingHttpClientConnectionManager", key)) {
				cm = connectionManagers.get(key);
				if (cm == null || isShutDown(cm, true)) {
					cm = new PoolingHttpClientConnectionManager(new DefaultHttpClientConnectionOperatorImpl(reg), null, POOL_CONN_TTL_MS, TimeUnit.MILLISECONDS);

					// Configure Pool Limits
					cm.setDefaultMaxPerRoute(POOL_MAX_CONN_PER_ROUTE); // Limits threads per host
					cm.setMaxTotal(POOL_MAX_CONN); // Global limit for this manager

					// CRITICAL FOR MULTI-THREADING:
					// Check if a pooled connection is still alive if it's been idle for > 2s.
					// This prevents "Connection Reset" errors when a thread grabs an old socket.
					cm.setValidateAfterInactivity(POOL_CONN_INACTIVITY_DURATION);

					cm.setDefaultSocketConfig(SocketConfig.copy(SocketConfig.DEFAULT).setTcpNoDelay(true).setSoReuseAddress(true).setSoLinger(0).build());

					connectionManagers.put(key, cm);
				}
			}
		}

		// BUILDER CONFIGURATION
		HttpClientBuilder builder = HttpClients.custom();
		builder.setConnectionManager(cm)
				// Setting this to 'true' ensures that client.close() doesn't kill the whole pool
				.setConnectionManagerShared(true).setConnectionTimeToLive(POOL_CONN_TTL_MS, TimeUnit.MILLISECONDS)
				.setConnectionReuseStrategy(new DefaultClientConnectionReuseStrategy())
				.setRedirectStrategy("lax".equalsIgnoreCase(redirect) ? new LaxRedirectStrategy() : new DefaultRedirectStrategy())
				.setRetryHandler(new NoHttpResponseExceptionHttpRequestRetryHandler())
				// Safety net: background threads that reap dead/stale connections
				.evictIdleConnections(30, TimeUnit.SECONDS).evictExpiredConnections();

		if (!Caster.toBooleanValue(redirect, true)) builder.disableRedirectHandling();
		return new Pair(builder, cm);
	}

	public static void setTimeout(HttpClientBuilder builder, TimeSpan timeout) {
		int ms = -1;
		if (timeout != null && timeout.getMillis() > 0) {
			ms = (int) timeout.getMillis();
			// if overflow occurred (value was > Integer.MAX_VALUE), use 0 which means infinite timeout in
			// HttpClient
			if (ms < 0) ms = 0;

			SocketConfig sc = SocketConfig.custom().setSoTimeout(ms).build();
			builder.setDefaultSocketConfig(sc);
		}

		// Set RequestConfig with timeout values (if provided) and cookie spec
		// This ensures the timeout is enforced during the actual data transfer, not just connection
		RequestConfig.Builder rcBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD); // LDEV-2321
		if (ms > 0) {
			rcBuilder.setSocketTimeout(ms).setConnectTimeout(ms).setConnectionRequestTimeout(2000);
		}
		builder.setDefaultRequestConfig(rcBuilder.build());
	}

	private static Registry<ConnectionSocketFactory> createRegistry() throws GeneralSecurityException {
		SSLContext sslcontext = SSLContext.getInstance("TLS");
		sslcontext.init(null, null, new java.security.SecureRandom());
		SSLConnectionSocketFactory defaultsslsf = new SSLConnectionSocketFactoryImpl(sslcontext, new DefaultHostnameVerifierImpl());
		/* Register connection handlers */
		return RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", defaultsslsf).build();

	}

	private static Registry<ConnectionSocketFactory> createRegistry(String clientCert, String clientCertPassword)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
		// Currently, clientCert force usePool to being ignored
		if (clientCertPassword == null) clientCertPassword = "";
		// Load the client cert
		File ksFile = new File(clientCert);
		KeyStore clientStore = KeyStore.getInstance("PKCS12");
		clientStore.load(new FileInputStream(ksFile), clientCertPassword.toCharArray());
		// Prepare the keys
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(clientStore, clientCertPassword.toCharArray());
		SSLContext sslcontext = SSLContext.getInstance("TLS");
		// Configure the socket factory
		sslcontext.init(kmf.getKeyManagers(), null, new java.security.SecureRandom());
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactoryImpl(sslcontext, new DefaultHostnameVerifierImpl());
		return RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();
	}

	public static void releaseConnectionManager() {
		Collection<PoolingHttpClientConnectionManager> values = connectionManagers.values();
		connectionManagers = new ConcurrentHashMap<String, PoolingHttpClientConnectionManager>();
		for (PoolingHttpClientConnectionManager cm: values) {
			IOUtil.closeEL(cm);
		}
	}

	public static boolean isShutDown(PoolingHttpClientConnectionManager cm, boolean defaultValue) {
		if (cm != null && !cannotAccess) {
			try {
				if (isShutDownField == null || isShutDownField.getDeclaringClass() != cm.getClass()) {
					isShutDownField = cm.getClass().getDeclaredField("isShutDown");
					isShutDownField.setAccessible(true);
				}
				return ((AtomicBoolean) isShutDownField.get(cm)).get();
			}
			catch (Exception e) {
				cannotAccess = true;// depending on JRE used
				LogUtil.log("http", e);
			}
		}
		return defaultValue;
	}

	public static Map<String, PoolingHttpClientConnectionManager> getConnectionManagers() {
		return connectionManagers;
	}

	public static void closeIdleConnections() {
		for (PoolingHttpClientConnectionManager cm: connectionManagers.values()) {
			cm.closeIdleConnections(POOL_CONN_TTL_MS, TimeUnit.MILLISECONDS);
			cm.closeExpiredConnections();
		}
	}

	protected static HTTPResponse4Impl invoke(URL url, HttpUriRequest request, String username, String password, long connectionRequestTimeout, long connectioTimeout,
			long socketTimeout, boolean redirect, String charset, String useragent, ProxyData proxy, lucee.commons.net.http.Header[] headers, Map<String, String> formfields,
			boolean pooling) throws IOException {

		if (useragent == null) useragent = DEFAULT_USER_AGENT;

		// USE A SINGLETON CLIENT - DO NOT CALL builder.build() here!
		CloseableHttpClient client = getHttpClient(pooling);

		RequestConfig requestConfig = RequestConfig.custom()

				.setSocketTimeout((socketTimeout > 0) ? (int) socketTimeout : 0).

				setConnectTimeout((connectionRequestTimeout > 0) ? (int) connectionRequestTimeout : DEFAULT_CONNECT_TIMEOUT).

				setConnectionRequestTimeout((connectionRequestTimeout > 0) ? (int) connectionRequestTimeout : DEFAULT_CONNECT_REQUEST_TIMEOUT)

				.setCookieSpec(CookieSpecs.STANDARD)

				.setRedirectsEnabled(redirect).build();

		if (request instanceof HttpRequestBase) {
			((HttpRequestBase) request).setConfig(requestConfig);
		}

		// 3. Use a fresh Context for Credentials/Proxy (Thread-safe)
		HttpClientContext context = HttpClientContext.create();

		// Set Headers/Body/UserAgent
		setHeader(request, headers);
		if (CollectionUtil.isEmpty(formfields)) setContentType(request, charset);
		setFormFields(request, formfields, charset);
		setUserAgent(request, useragent);

		// 4. Handle Auth/Proxy via Context instead of Builder
		// This avoids mutating shared builder state across threads
		if (!StringUtil.isEmpty(username, true)) {
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			context.setCredentialsProvider(credsProvider);
		}

		proxy = ProxyDataImpl.validate(proxy, url.getHost());
		if (ProxyDataImpl.isValid(proxy, url.getHost())) {
			HttpHost proxyHost = new HttpHost(proxy.getServer(), proxy.getPort());

			// Check if it's a base request that supports configuration
			if (request instanceof org.apache.http.client.methods.HttpRequestBase) {
				org.apache.http.client.methods.HttpRequestBase baseRequest = (org.apache.http.client.methods.HttpRequestBase) request;

				// Build the new config with the proxy included
				RequestConfig newConfig = RequestConfig.copy(requestConfig).setProxy(proxyHost).build();

				baseRequest.setConfig(newConfig);
			}

		}

		// This will be fast (3s) because the client and pool are already "warm"
		try {
			return new HTTPResponse4Impl(url, context, client, request, client.execute(request, context), pooling);
		}
		catch (IOException cause) {
			IOException ioe = new IOException("failed with [" + toMethodName(request) + "] request to URL [" + url + "]");
			ExceptionUtil.initCauseEL(ioe, cause);
			throw ioe;
		}
	}

	private static String toMethodName(HttpUriRequest request) {
		if (request instanceof HttpGet) return "GET";
		if (request instanceof HttpPost) return "POST";
		if (request instanceof HttpHead) return "HEAD";
		if (request instanceof HttpPut) return "PUT";
		if (request instanceof HttpDelete) return "DELETE";
		if (request instanceof HttpPatch) return "PATCH";
		if (request instanceof HttpOptions) return "OPTIONS";
		if (request instanceof HttpTrace) return "TRACE";
		return request.getClass().getName();
	}

	private static void setFormFields(HttpUriRequest request, Map<String, String> formfields, String charset) throws IOException {
		if (!CollectionUtil.isEmpty(formfields)) {
			if (!(request instanceof HttpPost)) throw new IOException("form fields are only suppported for post request");
			HttpPost post = (HttpPost) request;
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			Iterator<Entry<String, String>> it = formfields.entrySet().iterator();
			Entry<String, String> e;
			while (it.hasNext()) {
				e = it.next();
				list.add(new BasicNameValuePair(e.getKey(), e.getValue()));
			}
			if (StringUtil.isEmpty(charset)) charset = ((PageContextImpl) ThreadLocalPageContext.get()).getWebCharset().name();

			post.setEntity(new org.apache.http.client.entity.UrlEncodedFormEntity(list, charset));
		}
	}

	private static void setUserAgent(HttpMessage hm, String useragent) {
		if (useragent != null) hm.setHeader("User-Agent", useragent);
	}

	private static void setContentType(HttpMessage hm, String charset) {
		if (charset != null) hm.setHeader("Content-type", "text/html; charset=" + charset);
	}

	private static void setHeader(HttpMessage hm, lucee.commons.net.http.Header[] headers) {
		addHeader(hm, headers);
	}

	private static void addHeader(HttpMessage hm, lucee.commons.net.http.Header[] headers) {
		if (headers != null) {
			for (int i = 0; i < headers.length; i++)
				hm.addHeader(toHeader(headers[i]));
		}
	}

	public static HttpClientContext setCredentials(HttpClientBuilder builder, HttpHost httpHost, String username, String password, boolean preAuth) {
		// set Username and Password
		if (!StringUtil.isEmpty(username, true)) {

			if (password == null) password = "";

			CredentialsProvider cp = new BasicCredentialsProvider();
			builder.setDefaultCredentialsProvider(cp);

			cp.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials(username, password));

			HttpClientContext httpContext = new HttpClientContext();
			if (preAuth) {
				AuthCache authCache = new BasicAuthCache();
				authCache.put(httpHost, new BasicScheme());
				httpContext.setAttribute(org.apache.http.client.protocol.ClientContext.AUTH_CACHE, authCache);
			}

			return httpContext;
		}

		return null;
	}

	public static void setNTCredentials(HttpClientBuilder builder, String username, String password, String workStation, String domain) {
		// set Username and Password
		if (!StringUtil.isEmpty(username, true)) {
			if (password == null) password = "";
			CredentialsProvider cp = new BasicCredentialsProvider();
			builder.setDefaultCredentialsProvider(cp);

			cp.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new NTCredentials(username, password, workStation, domain));
		}
	}

	public static void setBody(HttpEntityEnclosingRequest req, Object body, String mimetype, String charset) throws IOException {
		if (body != null) req.setEntity(toHttpEntity(body, mimetype, charset));
	}

	public static void setProxy(String host, HttpClientBuilder builder, HttpUriRequest request, ProxyData proxy) {
		// set Proxy
		if (ProxyDataImpl.isValid(proxy, host)) {
			HttpHost hh = new HttpHost(proxy.getServer(), proxy.getPort() == -1 ? 80 : proxy.getPort());
			builder.setProxy(hh);

			// username/password
			if (!StringUtil.isEmpty(proxy.getUsername())) {
				CredentialsProvider cp = new BasicCredentialsProvider();
				builder.setDefaultCredentialsProvider(cp);
				cp.setCredentials(new AuthScope(proxy.getServer(), proxy.getPort()), new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
			}
		}
	}

	public static void addCookie(CookieStore cookieStore, String domain, String name, String value, String path, String charset) {
		if (ReqRspUtil.needEncoding(name)) name = ReqRspUtil.encode(name, charset);
		if (ReqRspUtil.needEncoding(value)) value = ReqRspUtil.encode(value, charset);
		BasicClientCookie cookie = new BasicClientCookie(name, value);
		if (!StringUtil.isEmpty(domain, true)) cookie.setDomain(domain);
		if (!StringUtil.isEmpty(path, true)) cookie.setPath(path);
		cookieStore.addCookie(cookie);
	}

	/**
	 * convert input to HTTP Entity
	 * 
	 * @param value
	 * @param mimetype not used for binary input
	 * @param charset not used for binary input
	 * @return
	 * @throws IOException
	 */
	private static HttpEntity toHttpEntity(Object value, String mimetype, String charset) throws IOException {
		if (value instanceof HttpEntity) return (HttpEntity) value;

		// content type
		ContentType ct = toContentType(mimetype, charset);
		try {
			if (value instanceof TemporaryStream) {
				if (ct != null) return new TemporaryStreamHttpEntity((TemporaryStream) value, ct);
				return new TemporaryStreamHttpEntity((TemporaryStream) value, null);
			}
			else if (value instanceof InputStream) {
				if (ct != null) return new ByteArrayEntity(IOUtil.toBytes((InputStream) value), ct);
				return new ByteArrayEntity(IOUtil.toBytes((InputStream) value));
			}
			else if (Decision.isCastableToBinary(value, false)) {
				if (ct != null) return new ByteArrayEntity(Caster.toBinary(value), ct);
				return new ByteArrayEntity(Caster.toBinary(value));
			}
			else {
				boolean wasNull = false;
				if (ct == null) {
					wasNull = true;
					ct = ContentType.APPLICATION_OCTET_STREAM;
				}
				String str = Caster.toString(value);
				if (str.equals("<empty>")) {
					return new EmptyHttpEntity(ct);
				}
				if (wasNull && !StringUtil.isEmpty(charset, true)) return new StringEntity(str, charset.trim());
				else return new StringEntity(str, ct);
			}
		}
		catch (Exception e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	public static Entity getEmptyEntity(String mimetype, String charset) {
		return new EmptyHttpEntity(toContentType(mimetype, charset));
	}

	public static Entity getEmptyEntity(ContentType contentType) {
		return new EmptyHttpEntity(contentType);
	}

	public static Entity getByteArrayEntity(byte[] barr, String mimetype, String charset) {
		ContentType ct = toContentType(mimetype, charset);
		return new ByteArrayHttpEntity(barr, toContentType(mimetype, charset));
	}

	public static Entity getByteArrayEntity(byte[] barr, ContentType contentType) {
		return new ByteArrayHttpEntity(barr, contentType);
	}

	public static Entity getTemporaryStreamEntity(TemporaryStream ts, String mimetype, String charset) {
		return new TemporaryStreamHttpEntity(ts, toContentType(mimetype, charset));
	}

	public static Entity getTemporaryStreamEntity(TemporaryStream ts, ContentType contentType) {
		return new TemporaryStreamHttpEntity(ts, contentType);
	}

	public static Entity getResourceEntity(Resource res, String mimetype, String charset) {
		return new ResourceHttpEntity(res, toContentType(mimetype, charset));
	}

	public static Entity getResourceEntity(Resource res, ContentType contentType) {
		return new ResourceHttpEntity(res, contentType);
	}

	private static class NoHttpResponseExceptionHttpRequestRetryHandler implements HttpRequestRetryHandler {
		@Override
		public boolean retryRequest(java.io.IOException exception, int executionCount, HttpContext context) {
			if (executionCount <= 2 && exception instanceof org.apache.http.NoHttpResponseException) {
				LogUtil.log(Log.LEVEL_INFO, "http-conn", ExceptionUtil.getStacktrace(exception, true));
				return true;
			}
			return false;
		}
	}

	public static class HTTPDownloaderHeadResponse {

		private lucee.commons.net.http.Header[] headers;
		private int statusCode;
		private String statusText;
		private long contentLength;
		private long lastModified;

		public HTTPDownloaderHeadResponse(HTTPResponse response) {

			this.headers = response.getAllHeaders();
			this.statusCode = response.getStatusCode();
			this.statusText = response.getStatusText();
			try {
				this.contentLength = response.getContentLength();
			}
			catch (IOException e) {
				this.contentLength = 0;
			}

			lucee.commons.net.http.Header lmHeader = response.getLastHeaderIgnoreCase("last-modified");
			if (lmHeader != null && statusCode >= 200 && statusCode <= 299) lastModified = Caster.toLongValue(lmHeader.getValue(), 0L);
		}

		public long getContentLength() {
			return contentLength;
		}

		public long getLastModified() {
			return lastModified;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public String getStatusText() {
			return statusText;
		}

		public lucee.commons.net.http.Header[] getAllHeaders() {
			return headers;
		}

	}
}