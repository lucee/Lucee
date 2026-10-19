package lucee.commons.net.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.commons.net.http.httpclient.HTTPResponse4Impl;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;

/**
 * Unified HTTP downloader utility that wraps HTTPEngine4Impl. Provides consistent error handling,
 * logging, and connection pooling for all internal downloads.
 * 
 * @see HTTPEngine4Impl
 * @see <a href="https://luceeserver.atlassian.net/browse/LDEV-5122">LDEV-5122</a>
 */
public final class HTTPDownloader {

	public static final long DEFAULT_CONNECT_TIMEOUT = 10000; // 10 seconds
	public static final long DEFAULT_READ_TIMEOUT = 60000; // 60 seconds
	private static final String DEFAULT_USER_AGENT = "Lucee";

	private HTTPDownloader() {
		// Utility class, prevent instantiation
	}

	private static volatile CloseableHttpClient SHARED_CLIENT;
	private static final Object CLIENT_LOCK = new Object();

	public static void releaseSharedClient() {
		synchronized (CLIENT_LOCK) {
			if (SHARED_CLIENT != null) {
				IOUtil.closeEL(SHARED_CLIENT);
				SHARED_CLIENT = null;
			}
		}
	}

	private static CloseableHttpClient getSharedClient() throws GeneralSecurityException, IOException {
		if (SHARED_CLIENT == null) {
			synchronized (CLIENT_LOCK) {
				if (SHARED_CLIENT == null) {
					HttpClientBuilder builder = HTTPEngine4Impl.getHttpClientBuilder(true, null, null, "true");
					SHARED_CLIENT = builder.build();
				}
			}
		}
		return SHARED_CLIENT;
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
		Config config = ThreadLocalPageContext.getConfig();
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
		HttpContext context = HTTPEngine4Impl.setCredentials(builder, httpHost, username, password, false);

		// Set proxy on builder and request
		HTTPEngine4Impl.setProxy(url.getHost(), builder, request, proxy);

		// Build client
		CloseableHttpClient client = builder.build();

		// Return both client and context
		if (context == null) context = new HttpClientContext();
		return new ClientContext(client, context);
	}

	/**
	 * Simple GET request with default timeouts (10s connect, 60s read)
	 *
	 * @param url URL to download from
	 * @return InputStream of the response content
	 * @throws IOException if download fails
	 * @throws GeneralSecurityException if SSL/TLS fails
	 */
	public static InputStream get(URL url) throws IOException, GeneralSecurityException {
		return get(url, null, null, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, null);
	}

	/**
	 * GET request with custom timeouts
	 *
	 * @param url URL to download from
	 * @param connectTimeout Connection timeout in milliseconds
	 * @param readTimeout Read timeout in milliseconds
	 * @return InputStream of the response content
	 * @throws IOException if download fails
	 * @throws GeneralSecurityException if SSL/TLS fails
	 */
	public static InputStream get(URL url, long connectTimeout, long readTimeout) throws IOException, GeneralSecurityException {
		return get(url, null, null, connectTimeout, readTimeout, null);
	}

	/**
	 * GET request with custom User-Agent
	 *
	 * @param url URL to download from
	 * @param connectTimeout Connection timeout in milliseconds
	 * @param readTimeout Read timeout in milliseconds
	 * @param userAgent User-Agent header (can be null, defaults to "Lucee")
	 * @return InputStream of the response content
	 * @throws IOException if download fails
	 * @throws GeneralSecurityException if SSL/TLS fails
	 */
	public static InputStream get(URL url, long connectTimeout, long readTimeout, String userAgent) throws IOException, GeneralSecurityException {
		return get(url, null, null, connectTimeout, readTimeout, userAgent);
	}

	/**
	 * GET request with full options
	 *
	 * @param url URL to download from
	 * @param username HTTP Basic Auth username (can be null)
	 * @param password HTTP Basic Auth password (can be null)
	 * @param connectTimeout Connection timeout in milliseconds
	 * @param readTimeout Read timeout in milliseconds
	 * @param userAgent User-Agent header (can be null, defaults to "Lucee")
	 * @return InputStream of the response content
	 * @throws IOException if download fails
	 * @throws GeneralSecurityException if SSL/TLS fails
	 */
	public static InputStream get(URL url, String username, String password, long connectTimeout, long readTimeout, String userAgent) throws IOException, GeneralSecurityException {
		return get(url, username, password, connectTimeout, readTimeout, userAgent, Log.LEVEL_DEBUG);
	}

	/**
	 * GET request with full options including log level control
	 *
	 * @param url URL to download from
	 * @param username HTTP Basic Auth username (can be null)
	 * @param password HTTP Basic Auth password (can be null)
	 * @param connectTimeout Connection timeout in milliseconds
	 * @param readTimeout Read timeout in milliseconds
	 * @param userAgent User-Agent header (can be null, defaults to "Lucee")
	 * @param logLevel Log level for success messages (Log.LEVEL_TRACE for minimal logging,
	 *            Log.LEVEL_DEBUG for visibility)
	 * @return InputStream of the response content
	 * @throws IOException if download fails
	 * @throws GeneralSecurityException if SSL/TLS fails
	 */
	public static InputStream get(URL url, String username, String password, long connectTimeout, long readTimeout, String userAgent, int logLevel)
			throws IOException, GeneralSecurityException {

		long start = System.currentTimeMillis();

		try {
			// Create HTTP GET request
			HttpGet request = new HttpGet(url.toString());
			request.setHeader("User-Agent", userAgent != null ? userAgent : DEFAULT_USER_AGENT);

			// Apply timeouts per-request (not per-client)
			request.setConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).setConnectionRequestTimeout(2000) // fast-fail if pool exhausted
					.setConnectTimeout((int) connectTimeout).setSocketTimeout((int) readTimeout).build());

			// Handle proxy and credentials
			ProxyData proxy = getProxyData(url.getHost());
			HttpClientBuilder builder = HTTPEngine4Impl.getHttpClientBuilder(true, null, null, "true");
			HttpHost httpHost = new HttpHost(url.getHost(), url.getPort());
			HttpContext context = HTTPEngine4Impl.setCredentials(builder, httpHost, username, password, false);
			HTTPEngine4Impl.setProxy(url.getHost(), builder, request, proxy);
			if (context == null) context = new HttpClientContext();

			// Use shared client — not cc.client.execute() anymore
			HTTPResponse rsp = new HTTPResponse4Impl(url, context, request, getSharedClient().execute(request, context));
			int statusCode = rsp.getStatusCode();
			if (statusCode < 200 || statusCode >= 300) {
				throw new IOException("Failed to download from [" + url + "], status code: " + statusCode);
			}

			long duration = System.currentTimeMillis() - start;
			LogUtil.log(logLevel, "download", "Downloaded from [" + url + "] in " + duration + "ms");

			return rsp.getContentAsStream();
		}
		catch (IOException | GeneralSecurityException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IOException("Failed to download from [" + url + "]: " + e.getMessage(), e);
		}
	}

	/**
	 * GET request returning the full HTTPResponse whatever the status code, so callers can handle 404 vs
	 * 429/5xx and read headers like Retry-After (LDEV-6579, used by MavenUtil).
	 * 
	 * Uses the shared pooled client and the engine cookie spec (no "Invalid cookie header" warnings
	 * for RFC 1123 expires, see LDEV-2321). The configured server proxy (incl. proxy credentials) and
	 * the given credentials are applied per request.
	 * 
	 * The caller must read or close the content stream, see {@link #releaseEL(HTTPResponse)}, to give
	 * the connection back to the pool.
	 *
	 * @param url URL to download from
	 * @param username HTTP Basic Auth username (can be null)
	 * @param password HTTP Basic Auth password (can be null)
	 * @param connectTimeout Connection timeout in milliseconds, also used as timeout to get a pooled
	 *            connection
	 * @param readTimeout Read (socket) timeout in milliseconds
	 * @param userAgent User-Agent header (can be null, defaults to "Lucee")
	 * @return the response, never null
	 * @throws IOException if the request fails (connection, proxy, timeout)
	 * @throws GeneralSecurityException if SSL/TLS fails
	 */
	public static HTTPResponse getResponse(URL url, String username, String password, long connectTimeout, long readTimeout, String userAgent)
			throws IOException, GeneralSecurityException {
		HttpGet request = new HttpGet(url.toString());
		request.setHeader("User-Agent", userAgent != null ? userAgent : DEFAULT_USER_AGENT);

		RequestConfig.Builder config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).setConnectionRequestTimeout((int) connectTimeout)
				.setConnectTimeout((int) connectTimeout).setSocketTimeout((int) readTimeout);

		HttpClientContext context = HttpClientContext.create();
		// cookies only live for this request, nothing is shared through the shared client
		context.setCookieStore(new BasicCookieStore());
		CredentialsProvider cp = null;

		// server proxy, the shared client has none, so it is set on the request
		ProxyData proxy = getProxyData(url.getHost());
		if (ProxyDataImpl.isValid(proxy, url.getHost())) {
			int port = proxy.getPort() == -1 ? 80 : proxy.getPort();
			config.setProxy(new HttpHost(proxy.getServer(), port));
			if (!StringUtil.isEmpty(proxy.getUsername())) {
				cp = new BasicCredentialsProvider();
				cp.setCredentials(new AuthScope(proxy.getServer(), port), new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
			}
		}
		// credentials for the target host
		if (!StringUtil.isEmpty(username, true)) {
			if (cp == null) cp = new BasicCredentialsProvider();
			cp.setCredentials(new AuthScope(url.getHost(), url.getPort()), new UsernamePasswordCredentials(username, password == null ? "" : password));
		}
		if (cp != null) context.setCredentialsProvider(cp);

		request.setConfig(config.build());
		return new HTTPResponse4Impl(url, context, request, getSharedClient().execute(request, context));
	}

	/**
	 * Gives the connection of a response from {@link #getResponse} back to the pool (drains or closes
	 * the content stream). Never throws.
	 */
	public static void releaseEL(HTTPResponse rsp) {
		if (rsp == null) return;
		try {
			IOUtil.closeEL(rsp.getContentAsStream());
		}
		catch (Exception e) {
			LogUtil.log("download", e);
		}
	}

	/**
	 * HEAD request returning full HTTPResponse
	 *
	 * @param url URL to check
	 * @param connectTimeout Connection timeout in milliseconds
	 * @param readTimeout Read timeout in milliseconds
	 * @param logLevel Log level for messages (Log.LEVEL_TRACE for minimal logging, Log.LEVEL_DEBUG for
	 *            visibility)
	 * @return HTTPResponse object, or null if request fails
	 */
	public static HTTPResponse head(URL url, long connectTimeout, long readTimeout, int logLevel) {
		long start = System.currentTimeMillis();

		try {
			// Get configured HttpClientBuilder (with connection pooling, true = use pooling)
			HttpClientBuilder builder = HTTPEngine4Impl.getHttpClientBuilder(true, null, null, "true");

			// Create HTTP HEAD request
			HttpHead request = new HttpHead(url.toString());
			request.setHeader("User-Agent", DEFAULT_USER_AGENT);

			// Setup client with proxy, credentials, and timeouts
			ClientContext cc = buildHttpClient(url, null, null, connectTimeout, readTimeout, builder, request);

			HTTPResponse rsp = new HTTPResponse4Impl(url, cc.context, request, cc.client.execute(request, cc.context));

			long duration = System.currentTimeMillis() - start;
			LogUtil.log(logLevel, "download", "HEAD request to [" + url + "] returned status code: " + rsp.getStatusCode() + " in " + duration + "ms");

			return rsp;
		}
		catch (Exception e) {
			LogUtil.log(Log.LEVEL_ERROR, "download", e);
			return null;
		}
	}

	/**
	 * HEAD request to check if URL exists (with default timeouts)
	 *
	 * @param url URL to check
	 * @return true if URL exists (200-299 status code), false otherwise
	 */
	public static boolean exists(URL url) {
		return exists(url, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
	}

	/**
	 * HEAD request to check if URL exists
	 *
	 * @param url URL to check
	 * @param connectTimeout Connection timeout in milliseconds
	 * @param readTimeout Read timeout in milliseconds
	 * @return true if URL exists (200-299 status code), false otherwise
	 */
	public static boolean exists(URL url, long connectTimeout, long readTimeout) {
		try {
			// Get configured HttpClientBuilder (with connection pooling, true = use pooling)
			HttpClientBuilder builder = HTTPEngine4Impl.getHttpClientBuilder(true, null, null, "true");

			// Create HTTP HEAD request
			HttpHead request = new HttpHead(url.toString());
			request.setHeader("User-Agent", DEFAULT_USER_AGENT);

			// Setup client with proxy, credentials, and timeouts
			ClientContext cc = buildHttpClient(url, null, null, connectTimeout, readTimeout, builder, request);

			HTTPResponse rsp = new HTTPResponse4Impl(url, cc.context, request, cc.client.execute(request, cc.context));

			int statusCode = rsp.getStatusCode();
			boolean exists = statusCode >= 200 && statusCode < 300;

			if (!exists) {
				LogUtil.log(Log.LEVEL_DEBUG, "download", "HEAD request to [" + url + "] returned status code: " + statusCode);
			}

			return exists;
		}
		catch (Exception e) {
			LogUtil.log(Log.LEVEL_ERROR, "download", e);
			return false;
		}
	}

	/**
	 * Download to file with atomic write (via temp file) using default timeouts
	 *
	 * @param url URL to download from
	 * @param target Target file
	 * @throws IOException if download or file operations fail
	 * @throws GeneralSecurityException if SSL/TLS fails
	 */
	public static void downloadToFile(URL url, File target) throws IOException, GeneralSecurityException {
		downloadToFile(url, target, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, null);
	}

	/**
	 * Download to file with atomic write (via temp file) with custom timeouts
	 *
	 * @param url URL to download from
	 * @param target Target file
	 * @param connectTimeout Connection timeout in milliseconds
	 * @param readTimeout Read timeout in milliseconds
	 * @throws IOException if download or file operations fail
	 * @throws GeneralSecurityException if SSL/TLS fails
	 */
	public static void downloadToFile(URL url, File target, long connectTimeout, long readTimeout) throws IOException, GeneralSecurityException {
		downloadToFile(url, target, connectTimeout, readTimeout, null);
	}

	/**
	 * Download to file with atomic write (via temp file)
	 *
	 * @param url URL to download from
	 * @param target Target file
	 * @param connectTimeout Connection timeout in milliseconds
	 * @param readTimeout Read timeout in milliseconds
	 * @param userAgent User-Agent header (can be null)
	 * @throws IOException if download or file operations fail
	 * @throws GeneralSecurityException if SSL/TLS fails
	 */
	public static void downloadToFile(URL url, File target, long connectTimeout, long readTimeout, String userAgent) throws IOException, GeneralSecurityException {

		InputStream is = null;
		Resource temp = null;

		try {
			is = get(url, null, null, connectTimeout, readTimeout, userAgent);

			// Download to temp file first (atomic write)
			temp = SystemUtil.getTempFile("download", false);
			IOUtil.copy(is, temp.getOutputStream(), true, true);

			// Atomic move to target
			File tempFile = ResourceUtil.toFile(temp);
			if (!tempFile.renameTo(target)) {
				// renameTo failed, try copying instead
				Resource targetResource = ResourceUtil.toResource(target);
				IOUtil.copy(temp, targetResource.getOutputStream(), true);
				tempFile.delete();
			}

		}
		finally {
			IOUtil.closeEL(is);
			if (temp != null && temp.exists()) {
				temp.delete();
			}
		}
	}
}
