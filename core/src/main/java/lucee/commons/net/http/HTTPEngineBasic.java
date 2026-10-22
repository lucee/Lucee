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

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.PageContextImpl;
import lucee.runtime.type.util.CollectionUtil;

public abstract class HTTPEngineBasic {

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

	public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
	public static final int DEFAULT_CONNECT_REQUEST_TIMEOUT = 5000;

	public static final long DEFAULT_READ_TIMEOUT = 60000; // 60 seconds
	protected static final String DEFAULT_USER_AGENT = "Lucee";

	/**
	 * Get ProxyData from ThreadLocalPageContext Config (following Http.java pattern line 1099)
	 */
	protected static ProxyData getProxyData(String host) {
		Config config = ThreadLocalPageContext.getConfigServer();
		if (config != null) {
			ProxyData proxy = config.getProxyData();
			return ProxyDataImpl.validate(proxy, host);
		}
		return null;
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

	public static void closeEL(HTTPResponse rsp) {
		if (rsp != null) {
			try {
				rsp.close();
			}
			catch (Exception e) {}
		}
	}

	public static lucee.commons.net.http.Header header(String name, String value) {
		return new HeaderImpl(name, value);
	}

	/**
	 * the engine that sends the requests, the one the server config defines, the built-in one when there
	 * is no config (yet)
	 */
	public static HTTPClientEngine getEngine() {
		ConfigServerImpl cs = null;
		try {
			cs = ConfigUtil.getConfigServerImpl(ThreadLocalPageContext.getConfigServer());
		}
		catch (Exception e) {
		}
		return cs != null ? cs.getHTTPEngine() : ConfigServerImpl.getDefaultHTTPEngine();
	}

	protected static HTTPResponse invoke(URL url, String method, Object body, String mimetype, String username, String password, long connectionRequestTimeout,
			long connectioTimeout, long socketTimeout, boolean redirect, String charset, String useragent, ProxyData proxy, lucee.commons.net.http.Header[] headers,
			Map<String, String> formfields, boolean pooling) throws IOException {

		if (useragent == null) useragent = DEFAULT_USER_AGENT;

		HTTPRequest request = new HTTPRequest().setURL(url).setMethod(method).setPooling(pooling).setRedirect(String.valueOf(redirect));
		request.setReadTimeout((socketTimeout > 0) ? socketTimeout : 0);
		request.setConnectTimeout((connectionRequestTimeout > 0) ? connectionRequestTimeout : DEFAULT_CONNECT_TIMEOUT);
		request.setConnectionRequestTimeout((connectionRequestTimeout > 0) ? connectionRequestTimeout : DEFAULT_CONNECT_REQUEST_TIMEOUT);

		if (headers != null) {
			for (lucee.commons.net.http.Header h: headers) {
				request.addHeader(h.getName(), h.getValue());
			}
		}
		if (CollectionUtil.isEmpty(formfields) && charset != null) request.setHeader("Content-type", "text/html; charset=" + charset);
		if (!CollectionUtil.isEmpty(formfields)) {
			if (!HTTPRequest.POST.equals(method)) throw new IOException("form fields are only suppported for post request");
			for (Entry<String, String> e: formfields.entrySet()) {
				request.addFormField(e.getKey(), e.getValue());
			}
			if (StringUtil.isEmpty(charset)) charset = ((PageContextImpl) ThreadLocalPageContext.get()).getWebCharset().name();
		}
		request.setCharset(charset);
		if (body != null) request.setBody(body, mimetype, charset);
		request.setHeader("User-Agent", useragent);

		if (!StringUtil.isEmpty(username, true)) request.setCredentials(username, password, false);
		request.setProxy(proxy);

		try {
			return getEngine().execute(request);
		}
		catch (IOException cause) {
			IOException ioe = new IOException("failed with [" + method + "] request to URL [" + url + "]");
			ExceptionUtil.initCauseEL(ioe, cause);
			throw ioe;
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
