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
import java.net.URL;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.util.ReadOnlyResourceSupport;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;

public class HTTPResource extends ReadOnlyResourceSupport {

	private final HTTPResourceProvider provider;
	private final HTTPConnectionData data;
	private final String path;
	private final String name;
	private HTTPResponse http;

	public HTTPResource(HTTPResourceProvider provider, HTTPConnectionData data) {
		this.provider = provider;
		this.data = data;

		String[] pathName = ResourceUtil.translatePathName(data.path);
		this.path = pathName[0];
		this.name = pathName[1];

	}

	private HTTPResponse getHTTPResponse(boolean create) throws IOException {
		if (create || http == null) {
			// URL url = HTTPUtil.toURL("http://"+data.host+":"+data.port+"/"+data.path);
			URL url = new URL(provider.getProtocol(), data.host, data.port, data.path);
			// TODO Support for proxy
			ProxyData pd = ProxyDataImpl.isValid(data.proxyData, url.getHost()) ? data.proxyData : ProxyDataImpl.NO_PROXY;

			http = HTTPEngine.get(url, data.username, data.password, _getTimeout(), true, null, data.userAgent, pd, null);
		}
		return http;
	}

	private int getStatusCode() throws IOException {
		if (http == null) {
			URL url = new URL(provider.getProtocol(), data.host, data.port, data.path);
			ProxyData pd = ProxyDataImpl.isValid(data.proxyData, url.getHost()) ? data.proxyData : ProxyDataImpl.NO_PROXY;
			return HTTPEngine.head(url, data.username, data.password, _getTimeout(), true, null, data.userAgent, pd, null).getStatusCode();
		}
		return http.getStatusCode();
	}

	public ContentType getContentType() throws IOException {
		if (http == null) {
			URL url = new URL(provider.getProtocol(), data.host, data.port, data.path);
			ProxyData pd = ProxyDataImpl.isValid(data.proxyData, url.getHost()) ? data.proxyData : ProxyDataImpl.NO_PROXY;
			return HTTPEngine.head(url, data.username, data.password, _getTimeout(), true, null, data.userAgent, pd, null).getContentType();
		}
		return http.getContentType();
	}

	@Override
	public boolean exists() {
		try {
			provider.read(this);
			int code = getStatusCode();// getHttpMethod().getStatusCode();
			return code != 404;
		}
		catch (IOException e) {
			return false;
		}
	}

	public int statusCode() {
		HTTPResponse rsp = null;
		try {
			provider.read(this);
			return (rsp = getHTTPResponse(false)).getStatusCode();
		}
		catch (IOException e) {
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
		HTTPResponse method = getHTTPResponse(true);
		try {
			return IOUtil.toBufferedInputStream(method.getContentAsStream());
		}
		catch (IOException e) {
			// provider.unlock(this);
			throw e;
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
		catch (IOException e) {
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
		catch (IOException e) {
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