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

import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.util.ReadOnlyResourceSupport;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPEngineBasic.HTTPDownloaderHeadResponse;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.httpclient.HTTPResponse4Impl.HTTPEngineInputStream;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;

public final class HTTPResource extends ReadOnlyResourceSupport {

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
		return new URL(provider.getProtocol(), data.host, data.port, data.path);
	}

	private int getStatusCode() throws IOException {
		return getMeta().getStatusCode();
	}

	public ContentType getContentType() throws IOException {
		HTTPResponse rsp = null;
		try {
			if (http == null) {
				URL url = getURL();
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

		URL url = getURL();
		InputStream is = HTTPEngine.get(url, data.username, data.password, HTTPEngine.DEFAULT_CONNECT_TIMEOUT, _getTimeout(), data.userAgent, getProxy(url));
		if (is instanceof HTTPEngineInputStream) {
			meta = ((HTTPEngineInputStream) is).getHTTPDownloaderHeadResponse();
		}
		return is;
	}

	private HTTPDownloaderHeadResponse getMeta() throws IOException {
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