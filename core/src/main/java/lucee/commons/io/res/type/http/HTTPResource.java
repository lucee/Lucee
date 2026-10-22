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
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.util.ReadOnlyResourceSupport;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;

public final class HTTPResource extends ReadOnlyResourceSupport {

	private final HTTPResourceProvider provider;
	private final HTTPConnectionData data;
	private final String path;
	private final String name;
	private HTTPResponse http;
	// LDEV-5912: status code of the HEAD request, kept for this resource (as 8.0 does), so a sequence of
	// checks like exists(), isFile() and isReadable() sends only one HEAD request
	private volatile Integer headStatusCode;

	public HTTPResource(HTTPResourceProvider provider, HTTPConnectionData data) {
		this.provider = provider;
		this.data = data;

		String[] pathName = ResourceUtil.translatePathName(data.path);
		this.path = pathName[0];
		this.name = pathName[1];

	}

	private URL toURL() throws MalformedURLException {
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

	private HTTPResponse getHTTPResponse(boolean create) throws IOException, GeneralSecurityException {
		if (create || http == null) {
			URL url = toURL();
			// TODO Support for proxy
			ProxyData pd = ProxyDataImpl.isValid(data.proxyData, url.getHost()) ? data.proxyData : ProxyDataImpl.NO_PROXY;

			http = HTTPEngine4Impl.get(url, data.username, data.password, _getTimeout(), true, null, data.userAgent, pd, null);
		}
		return http;
	}

	private int getStatusCode() throws IOException, GeneralSecurityException {
		if (http == null) {
			Integer cached = headStatusCode;
			if (cached != null) return cached.intValue();

			URL url = toURL();
			ProxyData pd = ProxyDataImpl.isValid(data.proxyData, url.getHost()) ? data.proxyData : ProxyDataImpl.NO_PROXY;
			int code = HTTPEngine4Impl.head(url, data.username, data.password, _getTimeout(), true, null, data.userAgent, pd, null).getStatusCode();
			headStatusCode = code;
			return code;
		}
		return http.getStatusCode();
	}

	public ContentType getContentType() throws IOException, GeneralSecurityException {
		if (http == null) {
			URL url = toURL();
			ProxyData pd = ProxyDataImpl.isValid(data.proxyData, url.getHost()) ? data.proxyData : ProxyDataImpl.NO_PROXY;
			return HTTPEngine4Impl.head(url, data.username, data.password, _getTimeout(), true, null, data.userAgent, pd, null).getContentType();
		}
		return http.getContentType();
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
		HTTPResponse rsp = null;
		try {
			provider.read(this);
			return (rsp = getHTTPResponse(false)).getStatusCode();
		}
		catch (Exception e) {
			return 0;
		}
		finally {
			HTTPEngine.closeEL(rsp);
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		// ResourceUtil.checkGetInputStreamOK(this);
		// provider.lock(this);
		provider.read(this);
		HTTPResponse method;
		try {
			method = getHTTPResponse(true);
		}
		catch (GeneralSecurityException e) {
			throw ExceptionUtil.toIOException(e);
		}
		try {
			int code = method.getStatusCode();
			if (code >= 200 && code <= 299) return IOUtil.toBufferedInputStream(method.getContentAsStream());
			else {
				throw new IOException("HTTP request [" + getParentResource().toString() + " returned [" + code + "]");
			}
		}
		finally {
			HTTPEngine.closeEL(method);
		}
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
		return new HTTPResource(provider, new HTTPConnectionData(data.username, data.password, data.host, data.port, path, data.proxyData, data.userAgent));
	}

	@Override
	public String getPath() {
		return provider.getProtocol().concat("://").concat(data.key()).concat(path).concat(name);
	}

	@Override
	public Resource getRealResource(String realpath) {
		realpath = ResourceUtil.merge(path.concat(name), realpath);
		if (realpath.startsWith("../")) return null;
		return new HTTPResource(provider, new HTTPConnectionData(data.username, data.password, data.host, data.port, realpath, data.proxyData, data.userAgent));
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
		int last = 0;
		HTTPResponse rsp = null;
		try {
			Header cl = (rsp = getHTTPResponse(false)).getLastHeaderIgnoreCase("last-modified");
			if (cl != null && exists()) last = Caster.toIntValue(cl.getValue(), 0);
		}
		catch (Exception e) {
			LogUtil.warn("http", e);
		}
		finally {
			HTTPEngine.closeEL(rsp);
		}
		return last;
	}

	@Override
	public long length() {
		HTTPResponse rsp = null;
		try {
			if (!exists()) return 0;
			return (rsp = getHTTPResponse(false)).getContentLength();
		}
		catch (Exception e) {
			return 0;
		}
		finally {
			HTTPEngine.closeEL(rsp);
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