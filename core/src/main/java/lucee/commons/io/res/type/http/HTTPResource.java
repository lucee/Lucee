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
package lucee.commons.io.res.type.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.util.ReadOnlyResourceSupport;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.HTTPEngineBasic.HTTPDownloaderHeadResponse;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.HTTPEngineInputStream;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;

public final class HTTPResource extends ReadOnlyResourceSupport {

	private static final int MAX_REDIRECTS = 10;

	private final HTTPResourceProvider provider;
	private final HTTPConnectionData data;
	private final String path;
	private final String name;
	private HTTPResponse http;
	private HTTPDownloaderHeadResponse meta;

	public HTTPResource(HTTPResourceProvider provider, HTTPConnectionData data) {
		this.provider = provider;
		this.data = data;

		String[] pathName = ResourceUtil.translatePathName(data.path);
		this.path = pathName[0];
		this.name = pathName[1];

	}

	private URL getURL() throws MalformedURLException {
		return new URL(provider.getProtocol(), data.host, data.port, encodePath(data.path));
	}

	/**
	 * LDEV-5726: percent-encodes the characters that are not allowed in a URL (space, non-ASCII, [ ] { }
	 * | ...), leaves everything else as is, including existing %XX escapes, so an already encoded path
	 * does not get encoded twice
	 */
	static String encodePath(String path) {
		if (path == null) return null;
		int len = path.length();
		StringBuilder sb = null;
		char c;
		for (int i = 0; i < len; i++) {
			c = path.charAt(i);
			if (isAllowed(c) || (c == '%' && i + 2 < len && isHex(path.charAt(i + 1)) && isHex(path.charAt(i + 2)))) {
				if (sb != null) sb.append(c);
				continue;
			}
			if (sb == null) sb = new StringBuilder(len + 16).append(path, 0, i);
			int cp = path.codePointAt(i);
			if (Character.charCount(cp) == 2) i++;
			for (byte b: new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8)) {
				sb.append('%').append(HEX[(b >> 4) & 0xF]).append(HEX[b & 0xF]);
			}
		}
		return sb == null ? path : sb.toString();
	}

	private static final char[] HEX = "0123456789ABCDEF".toCharArray();

	private static boolean isAllowed(char c) {
		if (c >= 'a' && c <= 'z') return true;
		if (c >= 'A' && c <= 'Z') return true;
		if (c >= '0' && c <= '9') return true;
		// unreserved, sub-delims and the path/query/fragment delimiters (RFC 3986)
		return "-._~!$&'()*+,;=:@/?#".indexOf(c) != -1;
	}

	private static boolean isHex(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	private int getStatusCode() throws IOException {
		return getMeta().getStatusCode();
	}

	public ContentType getContentType() throws IOException {
		HTTPResponse rsp = null;
		try {
			if (http == null) {
				URL url = getURL();
				if (data.hasHeaders()) {
					rsp = invoke(true);
					return rsp.getContentType();
				}
				rsp = HTTPEngine.head(url, data.username, data.password, HTTPEngine.DEFAULT_CONNECT_REQUEST_TIMEOUT, HTTPEngine.DEFAULT_CONNECT_TIMEOUT, _getTimeout(), true, null,
						data.userAgent, getProxy(url), null, true);
				return rsp.getContentType();
			}
			return http.getContentType();
		}
		finally {
			HTTPEngine.closeEL(rsp);
		}
	}

	@Override
	public boolean exists() {
		try {
			provider.read(this);
			int code = getStatusCode();// getHttpMethod().getStatusCode();
			return code >= 200 && code <= 299;
		}
		catch (Exception e) {
			return false;
		}
	}

	public int statusCode() {
		try {
			provider.read(this);
			return getMeta().getStatusCode();
		}
		catch (Exception e) {
			return 0;
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		provider.read(this);

		if (data.hasHeaders()) return getInputStreamWithHeaders();

		URL url = getURL();
		InputStream is = HTTPEngine.get(url, data.username, data.password, HTTPEngine.DEFAULT_CONNECT_TIMEOUT, _getTimeout(), data.userAgent, getProxy(url));
		if (is instanceof HTTPEngineInputStream) {
			meta = ((HTTPEngineInputStream) is).getHTTPDownloaderHeadResponse();
		}
		return is;
	}

	private InputStream getInputStreamWithHeaders() throws IOException {
		HTTPResponse rsp = invoke(false);
		boolean ok = false;
		try {
			int sc = rsp.getStatusCode();
			if (sc < 200 || sc >= 300) {
				throw new IOException("unable to invoke [" + getURL() + "], status code [" + sc + "]");
			}
			InputStream is = rsp.getContentAsStream();
			if (is == null) return null;
			HTTPEngineInputStream stream = is instanceof HTTPEngineInputStream ? (HTTPEngineInputStream) is : new HTTPEngineInputStream(rsp, is);
			meta = stream.getHTTPDownloaderHeadResponse();
			ok = true;
			return stream;
		}
		finally {
			if (!ok) HTTPEngine.closeEL(rsp);
		}
	}

	/**
	 * LDEV-3376: a request with the headers defined in the Application.cfc. The headers (and the
	 * credentials of the path) are only sent to the origin of the resource, a redirect to another
	 * scheme, host or port is followed without them.
	 */
	private HTTPResponse invoke(boolean head) throws IOException {
		URL origin = getURL();
		URL url = origin;
		int redirects = 0;
		while (true) {
			boolean same = isSameOrigin(origin, url);
			HTTPResponse rsp;
			if (head) {
				rsp = HTTPEngine.head(url, same ? data.username : null, same ? data.password : null, HTTPEngine.DEFAULT_CONNECT_REQUEST_TIMEOUT, HTTPEngine.DEFAULT_CONNECT_TIMEOUT,
						_getTimeout(), false, null, data.userAgent, getProxy(url), same ? data.headers : null, true);
			}
			else {
				rsp = HTTPEngine.get(url, same ? data.username : null, same ? data.password : null, HTTPEngine.DEFAULT_CONNECT_REQUEST_TIMEOUT, HTTPEngine.DEFAULT_CONNECT_TIMEOUT,
						_getTimeout(), false, null, data.userAgent, getProxy(url), same ? data.headers : null, true);
			}
			int sc = rsp.getStatusCode();
			Header location = sc == 301 || sc == 302 || sc == 303 || sc == 307 || sc == 308 ? rsp.getLastHeaderIgnoreCase("location") : null;
			if (location == null || StringUtil.isEmpty(location.getValue(), true)) return rsp;

			HTTPEngine.closeEL(rsp);
			if (++redirects > MAX_REDIRECTS) throw new IOException("Maximum redirects (" + MAX_REDIRECTS + ") exceeded for [" + origin + "]");
			url = new URL(url, location.getValue().trim());
			String protocol = url.getProtocol().toLowerCase(Locale.ROOT);
			if (!protocol.equals("http") && !protocol.equals("https")) throw new IOException("invalid redirect location [" + location.getValue() + "]");
		}
	}

	private static boolean isSameOrigin(URL a, URL b) {
		return a.getProtocol().equalsIgnoreCase(b.getProtocol()) && a.getHost().equalsIgnoreCase(b.getHost()) && port(a) == port(b);
	}

	private static int port(URL url) {
		return url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
	}

	private HTTPDownloaderHeadResponse getMeta() throws IOException {
		if (meta == null && data.hasHeaders()) {
			HTTPResponse rsp = invoke(true);
			try {
				meta = new HTTPDownloaderHeadResponse(rsp);
			}
			finally {
				HTTPEngine.closeEL(rsp);
			}
		}
		if (meta == null) {
			URL url = getURL();
			meta = HTTPEngine.head(url, data.username, data.password, HTTPEngine.DEFAULT_CONNECT_TIMEOUT, _getTimeout(), null, getProxy(url));
		}
		return meta;
	}

	// the resource's own proxy or none, never the server proxy (HTTPEngine falls back to it on null)
	private ProxyData getProxy(URL url) {
		return ProxyDataImpl.isValid(data.proxyData, url.getHost()) ? data.proxyData : ProxyDataImpl.NO_PROXY;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getParent() {
		if (isRoot()) return null;
		return provider.getProtocol().concat("://").concat(data.key()).concat(path.substring(0, path.length() - 1));
	}

	private boolean isRoot() {
		return StringUtil.isEmpty(name);
	}

	@Override
	public Resource getParentResource() {
		if (isRoot()) return null;
		return new HTTPResource(provider, new HTTPConnectionData(data.username, data.password, data.host, data.port, path, data.proxyData, data.userAgent, data.headers));
	}

	@Override
	public String getPath() {
		return provider.getProtocol().concat("://").concat(data.key()).concat(path).concat(name);
	}

	@Override
	public Resource getRealResource(String realpath) {
		realpath = ResourceUtil.merge(path.concat(name), realpath);
		if (realpath.startsWith("../")) return null;
		return new HTTPResource(provider, new HTTPConnectionData(data.username, data.password, data.host, data.port, realpath, data.proxyData, data.userAgent, data.headers));
	}

	@Override
	public ResourceProvider getResourceProvider() {
		return provider;
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean isFile() {
		return exists();
	}

	@Override
	public boolean isReadable() {
		return exists();
	}

	@Override
	public long lastModified() {
		try {
			if (!exists()) return 0;
			return getMeta().getLastModified();
		}
		catch (Exception e) {
			return 0L;
		}
	}

	@Override
	public long length() {
		try {
			if (!exists()) return 0;
			return getMeta().getContentLength();
		}
		catch (Exception e) {
			return 0L;
		}

	}

	@Override
	public Resource[] listResources() {
		return null;
	}

	public void setProxyData(ProxyData pd) {
		this.http = null;
		this.data.setProxyData(pd);
	}

	public void setUserAgent(String userAgent) {
		this.http = null;
		this.data.userAgent = userAgent;
	}

	public void setTimeout(int timeout) {
		this.http = null;
		data.timeout = timeout;
	}

	private int _getTimeout() {
		return data.timeout < provider.getSocketTimeout() ? data.timeout : provider.getSocketTimeout();
	}
}