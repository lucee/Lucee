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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.httpclient.HTTPResponse4Impl;
import lucee.runtime.net.proxy.ProxyData;

public final class HTTPEngine extends HTTPEngineBasic {

	/**
	 * HEAD request to check if URL exists (with default timeouts)
	 *
	 * @param url URL to check
	 * @return true if URL exists (200-299 status code), false otherwise
	 */
	public static boolean exists(URL url) {
		return exists(url, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, true);
	}

	/**
	 * HEAD request to check if URL exists
	 *
	 * @param url URL to check
	 * @param connectTimeout Connection timeout in milliseconds
	 * @param readTimeout Read timeout in milliseconds
	 * @return true if URL exists (200-299 status code), false otherwise
	 */
	public static boolean exists(URL url, long connectTimeout, long readTimeout, boolean pooling) {
		HTTPDownloaderHeadResponse response;
		try {
			response = head(url, connectTimeout, readTimeout, pooling);
			int statusCode = response.getStatusCode();
			return statusCode >= 200 && statusCode < 300;
		}
		catch (IOException e) {
			return false;
		}

	}

	public static HTTPDownloaderHeadResponse head(URL url, long connectTimeout, long readTimeout, boolean pooling) throws IOException {
		return head(url, null, null, connectTimeout, readTimeout, DEFAULT_USER_AGENT, null, pooling);
	}

	public static HTTPDownloaderHeadResponse head(URL url, long connectTimeout, long readTimeout, boolean pooling, HTTPDownloaderHeadResponse defaultValue) {
		return head(url, null, null, connectTimeout, readTimeout, DEFAULT_USER_AGENT, null, pooling, defaultValue);
	}

	public static HTTPDownloaderHeadResponse head(URL url) throws IOException {
		return head(url, null, null, DEFAULT_CONNECT_TIMEOUT, -1, null, null, true);
	}

	public static HTTPDownloaderHeadResponse head(URL url, long connectTimeout, long readTimeout) throws IOException {
		return head(url, null, null, connectTimeout, readTimeout, null, null, true);
	}

	public static HTTPDownloaderHeadResponse head(URL url, long connectTimeout, long readTimeout, String userAgent) throws IOException {
		return head(url, null, null, connectTimeout, readTimeout, userAgent, null, true);
	}

	public static HTTPDownloaderHeadResponse head(URL url, String username, String password, long connectTimeout, long readTimeout, String userAgent) throws IOException {
		return head(url, username, password, connectTimeout, readTimeout, userAgent, null, true);
	}

	public static HTTPDownloaderHeadResponse head(URL url, String username, String password, long connectTimeout, long readTimeout, String userAgent, ProxyData proxy)
			throws IOException {
		return head(url, username, password, connectTimeout, readTimeout, userAgent, proxy, true);
	}

	public static HTTPDownloaderHeadResponse head(URL url, String username, String password, long connectTimeout, long readTimeout, String userAgent, ProxyData proxy,
			boolean pooling) throws IOException {
		HTTPResponse response = null;
		try {
			response = head(url, username, password, DEFAULT_CONNECT_REQUEST_TIMEOUT, connectTimeout, readTimeout, true, null, userAgent, proxy, null, pooling);
			return new HTTPDownloaderHeadResponse(response);
		}
		finally {
			IOUtil.closeEL(response);
		}
	}

	public static HTTPDownloaderHeadResponse head(URL url, String username, String password, long connectTimeout, long readTimeout, String userAgent, ProxyData proxy,
			boolean pooling, HTTPDownloaderHeadResponse defaultValue) {
		HTTPResponse response = null;
		try {
			response = head(url, username, password, DEFAULT_CONNECT_REQUEST_TIMEOUT, connectTimeout, readTimeout, true, null, userAgent, proxy, null, pooling);
			return new HTTPDownloaderHeadResponse(response);
		}
		catch (Exception ex) {
			return defaultValue;
		}
		finally {
			IOUtil.closeEL(response);
		}
	}

	public static HTTPResponse head(URL url, String username, String password, long connectionRequestTimeout, long connectioTimeout, long socketTimeout, boolean redirect,
			String charset, String useragent, ProxyData proxy, lucee.commons.net.http.Header[] headers, boolean pooling) throws IOException {
		HttpHead head = new HttpHead(url.toExternalForm());
		return invoke(url, head, username, password, connectionRequestTimeout, connectioTimeout, socketTimeout, redirect, charset, useragent, proxy, headers, null, pooling);
	}

	public static InputStream get(URL url) throws IOException {
		return get(url, null, null, DEFAULT_CONNECT_TIMEOUT, -1, null, null, true);
	}

	public static InputStream get(URL url, long connectTimeout, long readTimeout) throws IOException {
		return get(url, null, null, connectTimeout, readTimeout, null, null, true);
	}

	public static InputStream get(URL url, long connectTimeout, long readTimeout, String userAgent) throws IOException {
		return get(url, null, null, connectTimeout, readTimeout, userAgent, null, true);
	}

	public static InputStream get(URL url, String username, String password, long connectTimeout, long readTimeout, String userAgent) throws IOException {
		return get(url, username, password, connectTimeout, readTimeout, userAgent, null, true);
	}

	public static InputStream get(URL url, String username, String password, long connectTimeout, long readTimeout, String userAgent, ProxyData proxy) throws IOException {
		return get(url, username, password, connectTimeout, readTimeout, userAgent, proxy, true);
	}

	public static InputStream get(URL url, String username, String password, long connectTimeout, long readTimeout, String userAgent, ProxyData proxy, boolean pooling)
			throws IOException {
		HTTPResponse4Impl response = get(url, username, password, DEFAULT_CONNECT_REQUEST_TIMEOUT, connectTimeout, readTimeout, true, null, userAgent, proxy, null, pooling);

		int sc = response.getStatusCode();
		if (sc < 200 || sc >= 300) {
			response.close();
			throw new IOException("unable to invoke [" + url + "], status code [" + sc + "]");
		}
		return response.getContentAsStream();
	}

	public static String getAsString(URL url, String charset) throws IOException {
		HTTPResponse4Impl rsp = get(url, null, null, DEFAULT_CONNECT_REQUEST_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, -1, true, charset, null, null, null, true);
		int sc = rsp.getStatusCode();
		if (sc < 200 || sc >= 300) {
			rsp.close();
			throw new IOException("unable to invoke [" + url + "], status code [" + sc + "]");
		}
		if (StringUtil.isEmpty(charset, true)) charset = rsp.getCharset();
		InputStream is = null;
		try {
			return IOUtil.toString(is = rsp.getContentAsStream(), charset);
		}
		finally {
			IOUtil.close(is);
		}
	}

	public static HTTPResponse4Impl get(URL url, String username, String password, long connectionRequestTimeout, long connectioTimeout, long socketTimeout, boolean redirect,
			String charset, String useragent, ProxyData proxy, lucee.commons.net.http.Header[] headers, boolean pooling) throws IOException {
		HttpGet get = new HttpGet(url.toExternalForm());
		return invoke(url, get, username, password, connectionRequestTimeout, connectioTimeout, socketTimeout, redirect, charset, useragent, proxy, headers, null, pooling);
	}

	public static HTTPResponse post(URL url, String username, String password, long connectionRequestTimeout, long connectioTimeout, long socketTimeout, boolean redirect,
			String charset, String useragent, ProxyData proxy, lucee.commons.net.http.Header[] headers, Map<String, String> formfields, boolean pooling) throws IOException {
		HttpPost post = new HttpPost(url.toExternalForm());

		return invoke(url, post, username, password, connectionRequestTimeout, connectioTimeout, socketTimeout, redirect, charset, useragent, proxy, headers, formfields, pooling);
	}

	public static HTTPResponse put(URL url, String username, String password, long connectionRequestTimeout, long connectioTimeout, long socketTimeout, boolean redirect,
			String mimetype, String charset, String useragent, ProxyData proxy, lucee.commons.net.http.Header[] headers, Object body, boolean pooling) throws IOException {
		HttpPut put = new HttpPut(url.toExternalForm());
		setBody(put, body, mimetype, charset);
		return invoke(url, put, username, password, connectionRequestTimeout, connectioTimeout, socketTimeout, redirect, charset, useragent, proxy, headers, null, pooling);

	}

	public static HTTPResponse delete(URL url, String username, String password, long connectionRequestTimeout, long connectioTimeout, long socketTimeout, boolean redirect,
			String charset, String useragent, ProxyData proxy, lucee.commons.net.http.Header[] headers, boolean pooling) throws IOException {
		HttpDelete delete = new HttpDelete(url.toExternalForm());
		return invoke(url, delete, username, password, connectionRequestTimeout, connectioTimeout, socketTimeout, redirect, charset, useragent, proxy, headers, null, pooling);
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
			is = get(url, null, null, connectTimeout, readTimeout, userAgent, null, true);

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