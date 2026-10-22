package lucee.commons.net.http.jdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import lucee.commons.io.IOUtil;
import lucee.commons.io.TemporaryStream;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPClientEngine;
import lucee.commons.net.http.HTTPRequest;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.http.SSLUtil;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * The built-in HTTP engine, based on java.net.http. It covers the common features of the tag cfhttp
 * (headers, form fields, multipart, body, cookies, basic authentication, proxy, timeouts, redirects,
 * client certificate and trust store). What it can not do (NTLM, restricted headers, ...) is done by
 * the HTTP extension.
 */
public final class JDKEngine implements HTTPClientEngine {

	private static final int MAX_REDIRECTS = 50;

	private final Map<String, HttpClient> clients = new ConcurrentHashMap<>();
	private final SecureRandom random = new SecureRandom();
	private final PoolStats poolStats = new PoolStats();
	// state of the custom cacerts (SSLCertificateInstall/Remove) the pooled clients were created with
	private volatile String customCaCertsState = null;

	@Override
	public void init(Config config) throws PageException {
	}

	@Override
	public String getName() {
		return "jdk";
	}

	@Override
	public boolean supports(String feature) {
		return FEATURE_PREEMPTIVE_AUTH.equals(feature) || FEATURE_TLS_OPTIONS.equals(feature);
	}

	// ------------------------------------------------------------------------------------------
	// client
	// ------------------------------------------------------------------------------------------

	private HttpClient client(HTTPRequest r, ProxyData proxy, boolean[] dedicated) throws IOException {
		String key = r.getConnectTimeout() + "|" + (proxy == null ? "" : proxy.getServer() + ":" + proxy.getPort() + ":" + proxy.getUsername() + ":" + proxy.getPassword()) + "|"
				+ r.getClientCert() + ":" + r.getClientCertPassword() + "|" + r.getTrustStore() + ":" + r.getTrustStorePassword() + "|" + r.isSslVerify();

		// there is no connection pool to turn off with the JDK client, a request that must not use the pool gets a client of its own
		if (!r.isPooling()) {
			dedicated[0] = true;
			return build(r, proxy);
		}
		// the SSL context of a client is created once, a client created before a certificate was installed in or removed from the
		// custom cacerts would not see the change (LDEV-5571)
		String caState = customCaCertsState();
		if (!caState.equals(customCaCertsState)) {
			synchronized (clients) {
				if (!caState.equals(customCaCertsState)) {
					evictClients();
					customCaCertsState = caState;
				}
			}
		}
		HttpClient client = clients.get(key);
		if (client == null) {
			synchronized (clients) {
				client = clients.get(key);
				if (client == null) {
					client = build(r, proxy);
					clients.put(key, client);
				}
			}
		}
		return client;
	}

	private HttpClient build(HTTPRequest r, ProxyData proxy) throws IOException {
		HttpClient.Builder builder = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).followRedirects(HttpClient.Redirect.NEVER);
		if (r.getConnectTimeout() > 0) builder.connectTimeout(Duration.ofMillis(r.getConnectTimeout()));

		if (proxy != null) {
			builder.proxy(ProxySelector.of(new InetSocketAddress(proxy.getServer(), proxy.getPort() == -1 ? 80 : proxy.getPort())));
			if (!StringUtil.isEmpty(proxy.getUsername())) {
				final String user = proxy.getUsername();
				final char[] pass = (proxy.getPassword() == null ? "" : proxy.getPassword()).toCharArray();
				final String host = proxy.getServer();
				builder.authenticator(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						if (getRequestorType() == RequestorType.PROXY && host.equalsIgnoreCase(getRequestingHost())) return new PasswordAuthentication(user, pass);
						return null;
					}
				});
			}
		}

		// always an SSL context of our own, the default SSL context of the JVM does not know the certificates installed in the
		// custom cacerts of Lucee (SSLCertificateInstall, LDEV-5571)
		try {
			builder.sslContext(createSSLContext(r));
		}
		catch (GeneralSecurityException e) {
			throw lucee.commons.lang.ExceptionUtil.toIOException(e);
		}
		return builder.build();
	}

	private static String customCaCertsState() {
		java.nio.file.Path p = SSLUtil.getCustomCaCertsPath();
		if (p == null) return "";
		try {
			if (!java.nio.file.Files.isRegularFile(p)) return p + ":none";
			return p + ":" + java.nio.file.Files.getLastModifiedTime(p).toMillis() + ":" + java.nio.file.Files.size(p);
		}
		catch (IOException e) {
			return p + ":error";
		}
	}

	/**
	 * removes all pooled clients, requests that are still running on them can complete
	 */
	private void evictClients() {
		List<HttpClient> old = new ArrayList<>(clients.values());
		clients.clear();
		for (HttpClient c: old) {
			try {
				c.shutdown();
			}
			catch (Throwable t) {
			}
		}
	}

	private SSLContext createSSLContext(HTTPRequest r) throws GeneralSecurityException, IOException {
		java.nio.file.Path cert = StringUtil.isEmpty(r.getClientCert(), true) ? null : Paths.get(r.getClientCert());
		char[] certPassword = r.getClientCertPassword() != null ? r.getClientCertPassword().toCharArray() : null;

		if (!r.isSslVerify()) {
			// no certificate or hostname verification at all (like curl -k). The JDK client verifies the host name inside the trust manager
			// when it is a plain X509TrustManager, with an extended trust manager it is up to the trust manager.
			KeyManager[] keyManagers = null;
			if (cert != null) {
				KeyStore ks = SSLUtil.loadKeyStore(cert, certPassword);
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(ks, certPassword);
				keyManagers = kmf.getKeyManagers();
			}
			SSLContext c = SSLContext.getInstance("TLS");
			c.init(keyManagers, new TrustManager[] { new TrustAll() }, random);
			return c;
		}

		if (!StringUtil.isEmpty(r.getTrustStore(), true)) {
			char[] trustPassword = r.getTrustStorePassword() != null ? r.getTrustStorePassword().toCharArray() : "changeit".toCharArray();
			List<SSLUtil.TrustStoreConfig> additional = new ArrayList<>();
			additional.add(new SSLUtil.TrustStoreConfig(Paths.get(r.getTrustStore()), trustPassword));
			return SSLUtil.createSSLContext(cert, certPassword, additional);
		}
		return SSLUtil.createSSLContext(cert, certPassword);
	}

	/**
	 * trusts every certificate and does not verify the host name
	 */
	private static final class TrustAll extends X509ExtendedTrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType, java.net.Socket socket) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType, java.net.Socket socket) {
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}

	// ------------------------------------------------------------------------------------------
	// request
	// ------------------------------------------------------------------------------------------

	@Override
	public HTTPResponse execute(HTTPRequest r) throws IOException {
		r.validate();
		if (!StringUtil.isEmpty(r.getUsername(), true) && r.getAuthType() == HTTPRequest.AUTH_NTLM) {
			throw new IOException("NTLM authentication is not supported by the built-in HTTP engine, install the HTTP extension to use it");
		}

		URL url = r.getURL();
		ProxyData proxy = ProxyDataImpl.validate(r.getProxy(), url.getHost());
		if (!ProxyDataImpl.isValid(proxy, url.getHost())) proxy = null;

		boolean[] dedicated = new boolean[1];
		HttpClient client = client(r, proxy, dedicated);

		final java.util.concurrent.atomic.AtomicReference<CompletableFuture<?>> current = new java.util.concurrent.atomic.AtomicReference<>();
		r.setAbortHandler(() -> {
			CompletableFuture<?> f = current.get();
			if (f != null) f.cancel(true);
		});

		String redirect = r.getRedirect() == null ? "true" : r.getRedirect().trim();
		boolean follow = !"false".equalsIgnoreCase(redirect);
		boolean lax = "lax".equalsIgnoreCase(redirect);

		String method = r.getMethod();
		URI uri = toURI(url);
		BodyData body = bodyOf(r);
		Map<String, String> cookies = new LinkedHashMap<>();
		for (HTTPRequest.Cookie c: r.getCookies()) {
			cookies.put(encodeCookie(c.getName(), r), encodeCookie(c.getValue(), r));
		}

		List<String> locations = new ArrayList<>();
		boolean authSent = false;
		String authHeader = null;
		if (!StringUtil.isEmpty(r.getUsername(), true) && r.isPreAuth()) {
			authHeader = basic(r.getUsername(), r.getPassword());
			authSent = true;
		}

		try {
			int redirects = 0;
			while (true) {
				HttpRequest.Builder rb = HttpRequest.newBuilder(uri);
				if (r.getReadTimeout() > 0) rb.timeout(Duration.ofMillis(r.getReadTimeout()));
				applyHeaders(rb, r, body, cookies, authHeader, method);
				rb.method(method, body == null ? BodyPublishers.noBody() : body.publisher());

				String route = PoolStats.route(uri, proxy);
				poolStats.lease(route);
				HttpResponse<InputStream> rsp;
				try {
					CompletableFuture<HttpResponse<InputStream>> f = client.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofInputStream());
					current.set(f);
					if (r.isAborted()) f.cancel(true);
					try {
						rsp = f.get();
					}
					catch (InterruptedException e) {
						f.cancel(true);
						Thread.currentThread().interrupt();
						throw new java.io.InterruptedIOException("the request was interrupted");
					}
					catch (ExecutionException e) {
						throw toIOException(e.getCause(), r, proxy, uri);
					}
					catch (java.util.concurrent.CancellationException e) {
						throw new IOException("the request was aborted");
					}
				}
				catch (IOException | RuntimeException e) {
					poolStats.release(route, false);
					throw e;
				}
				final boolean keepAlive = !dedicated[0] && PoolStats.keepAlive(rsp);

				int status = rsp.statusCode();
				storeCookies(rsp, cookies);

				// basic authentication, after the server asked for it
				if (status == 401 && !authSent && !StringUtil.isEmpty(r.getUsername(), true)) {
					String challenge = rsp.headers().firstValue("www-authenticate").orElse("");
					if (challenge.toLowerCase(Locale.ROOT).startsWith("basic")) {
						IOUtil.closeEL(rsp.body());
						poolStats.release(route, keepAlive);
						authHeader = basic(r.getUsername(), r.getPassword());
						authSent = true;
						continue;
					}
				}

				String location = rsp.headers().firstValue("location").orElse(null);
				if (follow && location != null && isRedirect(status) && mayFollow(status, method, lax)) {
					IOUtil.closeEL(rsp.body());
					poolStats.release(route, keepAlive);
					if (++redirects > MAX_REDIRECTS) {
						throw new IOException("Maximum redirects (" + MAX_REDIRECTS + ") exceeded");
					}
					URI next;
					try {
						next = uri.resolve(location.trim());
					}
					catch (IllegalArgumentException e) {
						throw new IOException("invalid redirect location [" + location + "]: " + e.getMessage());
					}
					if (next.getHost() == null || !("http".equalsIgnoreCase(next.getScheme()) || "https".equalsIgnoreCase(next.getScheme()))) {
						throw new IOException("invalid redirect location [" + location + "]");
					}
					locations.add(next.toString());
					// a redirect to another host does not get the credentials
					if (!next.getHost().equalsIgnoreCase(uri.getHost())) {
						authHeader = null;
						authSent = true;
					}
					if (status == 301 || status == 302 || status == 303) {
						if (!"HEAD".equals(method) && !"GET".equals(method)) {
							method = "GET";
							body = null;
						}
					}
					uri = next;
					continue;
				}

				return new JDKResponse(rsp, url, uri, locations, dedicated[0] ? client : null, () -> poolStats.release(route, keepAlive));
			}
		}
		catch (IOException | RuntimeException e) {
			if (dedicated[0]) JDKResponse.closeClient(client);
			throw e;
		}
	}

	private static boolean isRedirect(int status) {
		return status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
	}

	private static boolean mayFollow(int status, String method, boolean lax) {
		if (lax) return true;
		if (status == 303) return true;
		return "GET".equals(method) || "HEAD".equals(method);
	}

	private static URI toURI(URL url) throws IOException {
		try {
			return URI.create(url.toExternalForm());
		}
		catch (IllegalArgumentException e) {
			throw new IOException("invalid URL [" + url + "]: " + e.getMessage());
		}
	}

	private static IOException toIOException(Throwable t, HTTPRequest r, ProxyData proxy, URI uri) {
		if (t instanceof HttpTimeoutException) {
			// a request timeout that expires while the connection is still being made is reported as a connect timeout by the JDK client
			boolean connect = t instanceof java.net.http.HttpConnectTimeoutException;
			boolean requestTimeoutFirst = r.getReadTimeout() > 0 && (r.getConnectTimeout() <= 0 || r.getReadTimeout() <= r.getConnectTimeout());
			if (!connect || requestTimeoutFirst) {
				SocketTimeoutException ste = new SocketTimeoutException(t.getMessage());
				ste.initCause(t);
				return ste;
			}
			return (IOException) t;
		}
		if (t instanceof java.net.ConnectException) {
			// the exception of the JDK client has no message, so the log does not tell what could not be reached
			String target = proxy != null ? proxy.getServer() + ":" + (proxy.getPort() == -1 ? 80 : proxy.getPort()) : uri.getHost() + ":" + (uri.getPort() == -1 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort());
			java.net.ConnectException ce = new java.net.ConnectException("Connect to " + target + " failed" + (StringUtil.isEmpty(t.getMessage(), true) ? "" : ": " + t.getMessage()));
			ce.initCause(t);
			return ce;
		}
		if (t instanceof IOException) return (IOException) t;
		return lucee.commons.lang.ExceptionUtil.toIOException(t);
	}

	private static String basic(String username, String password) {
		return "Basic " + Base64.getEncoder().encodeToString((username + ":" + (password == null ? "" : password)).getBytes(StandardCharsets.UTF_8));
	}

	private static String encodeCookie(String str, HTTPRequest r) {
		if (str == null) return "";
		String cs = StringUtil.isEmpty(r.getCharset(), true) ? "UTF-8" : r.getCharset();
		if (lucee.runtime.net.http.ReqRspUtil.needEncoding(str)) return lucee.runtime.net.http.ReqRspUtil.encode(str, cs);
		return str;
	}

	private static void storeCookies(HttpResponse<?> rsp, Map<String, String> cookies) {
		for (String sc: rsp.headers().allValues("set-cookie")) {
			int semi = sc.indexOf(';');
			String pair = semi == -1 ? sc : sc.substring(0, semi);
			int eq = pair.indexOf('=');
			if (eq > 0) cookies.put(pair.substring(0, eq).trim(), pair.substring(eq + 1).trim());
		}
	}

	private void applyHeaders(HttpRequest.Builder rb, HTTPRequest r, BodyData body, Map<String, String> cookies, String authHeader, String method) throws IOException {
		boolean hasContentType = false;
		Set<String> seen = new java.util.HashSet<>();
		StringBuilder cookieHeader = new StringBuilder();

		for (Header h: r.getHeaders()) {
			String name = h.getName();
			String lower = name.toLowerCase(Locale.ROOT);
			if (lower.equals("cookie")) {
				if (cookieHeader.length() > 0) cookieHeader.append("; ");
				cookieHeader.append(h.getValue());
				continue;
			}
			if (lower.equals("content-type")) {
				hasContentType = true;
				// the multipart header needs the boundary
				if (body != null && body.contentType != null && body.multipart) continue;
			}
			if (isRestricted(lower)) {
				throw new IOException("the header [" + name + "] can not be set with the built-in HTTP engine, install the HTTP extension to set it");
			}
			if (lower.equals("authorization") && authHeader != null) continue;
			rb.header(name, h.getValue());
			seen.add(lower);
		}

		if (body != null && body.contentType != null && (body.multipart || !hasContentType)) rb.header("Content-Type", body.contentType);

		if (authHeader != null && !seen.contains("authorization")) rb.header("Authorization", authHeader);

		if (!cookies.isEmpty()) {
			for (Map.Entry<String, String> e: cookies.entrySet()) {
				if (cookieHeader.length() > 0) cookieHeader.append("; ");
				cookieHeader.append(e.getKey()).append('=').append(e.getValue());
			}
		}
		if (cookieHeader.length() > 0) rb.header("Cookie", cookieHeader.toString());
	}

	private static boolean isRestricted(String lowerName) {
		return lowerName.equals("connection") || lowerName.equals("content-length") || lowerName.equals("expect") || lowerName.equals("host") || lowerName.equals("upgrade");
	}

	// ------------------------------------------------------------------------------------------
	// body
	// ------------------------------------------------------------------------------------------

	private interface PublisherSupplier {
		BodyPublisher get() throws IOException;
	}

	private static final class BodyData {
		final PublisherSupplier supplier;
		final String contentType;
		final boolean multipart;

		BodyData(PublisherSupplier supplier, String contentType, boolean multipart) {
			this.supplier = supplier;
			this.contentType = contentType;
			this.multipart = multipart;
		}

		BodyPublisher publisher() throws IOException {
			return supplier.get();
		}
	}

	private BodyData bodyOf(HTTPRequest r) throws IOException {
		String charset = StringUtil.isEmpty(r.getCharset(), true) ? "UTF-8" : r.getCharset();

		// multipart
		if (r.getParts() != null) {
			String boundary = randomBoundary();
			final List<HTTPRequest.Part> parts = r.getParts();
			final Charset cs = Charset.forName(charset);
			return new BodyData(() -> multipart(parts, boundary, cs), "multipart/form-data; boundary=" + boundary, true);
		}

		// form fields
		if (r.getFormFields() != null) {
			StringBuilder sb = new StringBuilder();
			for (String[] f: r.getFormFields()) {
				if (sb.length() > 0) sb.append('&');
				sb.append(URLEncoder.encode(f[0], charset)).append('=').append(URLEncoder.encode(f[1] == null ? "" : f[1], charset));
			}
			final byte[] data = sb.toString().getBytes(StandardCharsets.US_ASCII);
			return new BodyData(() -> BodyPublishers.ofByteArray(data), "application/x-www-form-urlencoded; charset=" + charset, false);
		}

		// raw body
		Object value = r.getBody();
		if (value == null) return null;
		String mimetype = r.getBodyMimeType();
		String bodyCharset = r.getBodyCharset();

		try {
			if (value instanceof TemporaryStream) {
				final byte[] data = IOUtil.toBytes(((TemporaryStream) value).getInputStream());
				return binary(data, mimetype, bodyCharset);
			}
			if (value instanceof InputStream) {
				final byte[] data = IOUtil.toBytes((InputStream) value);
				return binary(data, mimetype, bodyCharset);
			}
			if (value instanceof Resource) {
				final Resource res = (Resource) value;
				return new BodyData(() -> resourcePublisher(res), contentType(mimetype, bodyCharset), false);
			}
			if (Decision.isCastableToBinary(value, false)) return binary(Caster.toBinary(value), mimetype, bodyCharset);

			String str = Caster.toString(value);
			if ("<empty>".equals(str)) return new BodyData(BodyPublishers::noBody, contentType(mimetype, bodyCharset), false);

			boolean noMime = StringUtil.isEmpty(mimetype, true);
			Charset cs = !StringUtil.isEmpty(bodyCharset, true) ? Charset.forName(bodyCharset.trim()) : (noMime ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8);
			final byte[] data = str.getBytes(cs);
			String ct;
			if (noMime) ct = !StringUtil.isEmpty(bodyCharset, true) ? "text/plain; charset=" + cs.name() : "application/octet-stream";
			else ct = contentType(mimetype, bodyCharset);
			return new BodyData(() -> BodyPublishers.ofByteArray(data), ct, false);
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw lucee.commons.lang.ExceptionUtil.toIOException(e);
		}
	}

	private static BodyData binary(byte[] data, String mimetype, String charset) {
		return new BodyData(() -> BodyPublishers.ofByteArray(data), StringUtil.isEmpty(mimetype, true) ? null : contentType(mimetype, charset), false);
	}

	private static String contentType(String mimetype, String charset) {
		if (StringUtil.isEmpty(mimetype, true)) return null;
		if (!StringUtil.isEmpty(charset, true)) return mimetype.trim() + "; charset=" + charset.trim();
		return mimetype.trim();
	}

	private String randomBoundary() {
		String chars = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder sb = new StringBuilder();
		int count = 30 + random.nextInt(11);
		for (int i = 0; i < count; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	private static BodyPublisher multipart(List<HTTPRequest.Part> parts, String boundary, Charset defaultCharset) throws IOException {
		List<BodyPublisher> list = new ArrayList<>();
		for (HTTPRequest.Part part: parts) {
			ByteArrayOutputStream head = new ByteArrayOutputStream();
			head.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.US_ASCII));
			if (part.getResource() != null) {
				Resource res = part.getResource();
				String mime = StringUtil.isEmpty(part.getMimeType(), true) ? "application/octet-stream" : part.getMimeType();
				head.write(("Content-Disposition: form-data; name=\"" + escape(part.getName()) + "\"; filename=\"" + escape(res.getName()) + "\"\r\n").getBytes(StandardCharsets.UTF_8));
				head.write(("Content-Type: " + mime + "\r\n").getBytes(StandardCharsets.US_ASCII));
				head.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
				list.add(BodyPublishers.ofByteArray(head.toByteArray()));
				list.add(resourcePublisher(res));
				list.add(BodyPublishers.ofByteArray("\r\n".getBytes(StandardCharsets.US_ASCII)));
			}
			else {
				Charset cs = StringUtil.isEmpty(part.getCharset(), true) ? defaultCharset : Charset.forName(part.getCharset().trim());
				head.write(("Content-Disposition: form-data; name=\"" + escape(part.getName()) + "\"\r\n").getBytes(StandardCharsets.UTF_8));
				head.write(("Content-Type: text/plain; charset=" + cs.name() + "\r\n").getBytes(StandardCharsets.US_ASCII));
				head.write("Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
				head.write((part.getValue() == null ? "" : part.getValue()).getBytes(cs));
				head.write("\r\n".getBytes(StandardCharsets.US_ASCII));
				list.add(BodyPublishers.ofByteArray(head.toByteArray()));
			}
		}
		list.add(BodyPublishers.ofByteArray(("--" + boundary + "--\r\n").getBytes(StandardCharsets.US_ASCII)));
		return BodyPublishers.concat(list.toArray(new BodyPublisher[0]));
	}

	// the length is known, so the request has a Content-Length and is not sent chunked
	private static BodyPublisher resourcePublisher(Resource res) {
		return BodyPublishers.fromPublisher(BodyPublishers.ofInputStream(() -> {
			try {
				return res.getInputStream();
			}
			catch (IOException e) {
				throw new java.io.UncheckedIOException(e);
			}
		}), res.length());
	}

	private static String escape(String str) {
		return str == null ? "" : str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "%0D").replace("\n", "%0A");
	}

	// ------------------------------------------------------------------------------------------
	// maintenance
	// ------------------------------------------------------------------------------------------

	@Override
	public void releaseConnections() {
		Map<String, HttpClient> old = new ConcurrentHashMap<>(clients);
		clients.clear();
		for (HttpClient c: old.values()) {
			JDKResponse.closeClient(c);
		}
		poolStats.clearIdle();
	}

	@Override
	public void closeIdleConnections() {
		// the JDK client closes idle connections itself (jdk.httpclient.keepalive.timeout)
	}

	@Override
	public void close() {
		releaseConnections();
	}

	@Override
	public Struct getMetaData() throws IOException {
		Struct sct = new StructImpl();
		sct.setEL("engine", getName());
		sct.setEL("clients", clients.size());
		return sct;
	}

	@Override
	public Struct getPoolStats() {
		return poolStats.toStruct();
	}
}
