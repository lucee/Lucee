package lucee.commons.net.http.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngineInputStream;
import lucee.commons.net.http.HTTPResponseSupport;
import lucee.commons.net.http.Header;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

/**
 * response of the built-in HTTP engine (java.net.http)
 */
final class JDKResponse extends HTTPResponseSupport {

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

	private final HttpResponse<InputStream> rsp;
	private final URI target;
	private final URL url;
	private final List<String> locations;
	private final HttpClient client;
	private final Runnable onClose;
	private final java.util.concurrent.atomic.AtomicBoolean closed = new java.util.concurrent.atomic.AtomicBoolean();
	private final Header[] headers;
	private InputStream content;
	private boolean decoded;

	JDKResponse(HttpResponse<InputStream> rsp, URL url, URI target, List<String> locations, HttpClient client, Runnable onClose) {
		this.rsp = rsp;
		this.onClose = onClose;
		this.url = url;
		this.target = target;
		this.locations = locations;
		this.client = client;

		// the content is decompressed like the other engines do, the headers that described the compressed content are removed.
		// a response without a body (HEAD, 204, 304, 1xx) keeps them, they describe the content the server would send (LDEV-5932)
		String ce = firstValue(rsp, "content-encoding");
		decoded = ce != null && hasBody(rsp) && (ce.toLowerCase().contains("gzip") || ce.toLowerCase().contains("deflate"));

		List<Header> list = new ArrayList<>();
		for (Map.Entry<String, List<String>> e: rsp.headers().map().entrySet()) {
			String name = e.getKey();
			if (decoded && (name.equalsIgnoreCase("content-encoding") || name.equalsIgnoreCase("content-length") || name.equalsIgnoreCase("content-md5"))) continue;
			for (String value: e.getValue()) {
				list.add(new HeaderImpl(name, value));
			}
		}
		headers = list.toArray(new Header[list.size()]);
	}

	private static boolean hasBody(HttpResponse<?> rsp) {
		int status = rsp.statusCode();
		if (status < 200 || status == 204 || status == 304) return false;
		return !"HEAD".equalsIgnoreCase(rsp.request().method());
	}

	private static String firstValue(HttpResponse<?> rsp, String name) {
		return rsp.headers().firstValue(name).orElse(null);
	}

	private InputStream raw() throws IOException {
		if (content == null) {
			InputStream is = rsp.body();
			if (is != null && decoded) {
				// an empty compressed body is not a valid gzip stream, it is returned as it is
				java.io.PushbackInputStream pis = new java.io.PushbackInputStream(is, 1);
				int first = pis.read();
				if (first == -1) {
					is = pis;
				}
				else {
					pis.unread(first);
					String ce = firstValue(rsp, "content-encoding").toLowerCase();
					if (ce.contains("gzip")) is = new GZIPInputStream(pis);
					else is = new InflaterInputStream(pis);
				}
			}
			content = is;
		}
		return content;
	}

	@Override
	public String getContentAsString() throws IOException {
		return getContentAsString(null);
	}

	@Override
	public String getContentAsString(String charset) throws IOException {
		InputStream is = raw();
		if (is == null) return "";
		if (StringUtil.isEmpty(charset, true)) charset = getCharset();
		try {
			return IOUtil.toString(is, charset);
		}
		finally {
			IOUtil.close(is);
		}
	}

	@Override
	public InputStream getContentAsStream() throws IOException {
		InputStream is = raw();
		if (is == null) return null;
		return new HTTPEngineInputStream(this, is);
	}

	@Override
	public byte[] getContentAsByteArray() throws IOException {
		InputStream is = raw();
		if (is == null) return new byte[0];
		try {
			return IOUtil.toBytes(is);
		}
		finally {
			IOUtil.close(is);
		}
	}

	@Override
	public Array getLocations() {
		if (locations == null || locations.isEmpty()) return null;
		Array arr = new ArrayImpl();
		for (String l: locations) {
			arr.appendEL(l);
		}
		return arr;
	}

	@Override
	public Header getLastHeader(String name) {
		for (int i = headers.length - 1; i >= 0; i--) {
			if (name.equals(headers[i].getName())) return headers[i];
		}
		return null;
	}

	@Override
	public Header getLastHeaderIgnoreCase(String name) {
		for (int i = headers.length - 1; i >= 0; i--) {
			if (name.equalsIgnoreCase(headers[i].getName())) return headers[i];
		}
		return null;
	}

	@Override
	public URL getURL() {
		return url;
	}

	@Override
	public URL getTargetURL() {
		try {
			return target.toURL();
		}
		catch (Exception e) {
			return url;
		}
	}

	@Override
	public int getStatusCode() {
		return rsp.statusCode();
	}

	@Override
	public String getStatusText() {
		return Reasons.get(rsp.statusCode());
	}

	@Override
	public String getProtocolVersion() {
		return rsp.version() == HttpClient.Version.HTTP_2 ? "HTTP/2.0" : "HTTP/1.1";
	}

	@Override
	public String getStatusLine() {
		return getProtocolVersion() + " " + rsp.statusCode() + " " + getStatusText();
	}

	@Override
	public Header[] getAllHeaders() {
		return headers;
	}

	@Override
	public void close() throws IOException {
		if (!closed.compareAndSet(false, true)) return;
		try {
			InputStream is = rsp.body();
			if (is != null) is.close();
		}
		finally {
			try {
				if (client != null) closeClient(client);
			}
			finally {
				if (onClose != null) onClose.run();
			}
		}
	}

	static void closeClient(HttpClient client) {
		try {
			client.close();
		}
		catch (Throwable t) {
		}
	}
}
