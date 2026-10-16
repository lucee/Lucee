/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
 */
package lucee.runtime.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import lucee.commons.net.URLDecoder;
import lucee.commons.net.URLEncoder;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.net.proxy.ProxyDataImpl;

public final class HTTPUtilImpl implements lucee.runtime.util.HTTPUtil {

	private static lucee.runtime.util.HTTPUtil instance = new HTTPUtilImpl();

	private HTTPUtilImpl() {}

	public static lucee.runtime.util.HTTPUtil getInstance() {
		return instance;
	}

	/**
	 * @see lucee.runtime.util.HTTPUtil#decode(java.lang.String, java.lang.String)
	 */
	@Override
	public String decode(String str, String charset) throws UnsupportedEncodingException {
		try {
			return URLDecoder.decode(str, charset, false);
		}
		catch (ApplicationException e) {
			throw new PageRuntimeException(e);
		}
	}

	/**
	 * @see lucee.runtime.util.HTTPUtil#delete(java.net.URL, java.lang.String, java.lang.String, int,
	 *      java.lang.String, java.lang.String, java.lang.String, int, java.lang.String,
	 *      java.lang.String, lucee.commons.net.http.Header[])
	 */
	@Override
	public HTTPResponse delete(URL url, String username, String password, int timeout, String charset, String useragent, String proxyserver, int proxyport, String proxyuser,
			String proxypassword, Header[] headers) throws IOException {
		return HTTPEngine.delete(url, username, password, HTTPEngine.DEFAULT_CONNECT_REQUEST_TIMEOUT, HTTPEngine.DEFAULT_CONNECT_TIMEOUT, timeout, true, charset, useragent,
				ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword), headers, false);
	}

	/**
	 * @param str
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@Override
	public String encode(String str, String charset) throws UnsupportedEncodingException {
		return URLEncoder.encode(str, charset);
	}

	/**
	 * @see lucee.runtime.util.HTTPUtil#head(java.net.URL, java.lang.String, java.lang.String, int,
	 *      java.lang.String, java.lang.String, java.lang.String, int, java.lang.String,
	 *      java.lang.String, lucee.commons.net.http.Header[])
	 */
	@Override
	public HTTPResponse head(URL url, String username, String password, int timeout, String charset, String useragent, String proxyserver, int proxyport, String proxyuser,
			String proxypassword, Header[] headers) throws IOException {
		return HTTPEngine.head(url, username, password, HTTPEngine.DEFAULT_CONNECT_REQUEST_TIMEOUT, HTTPEngine.DEFAULT_CONNECT_TIMEOUT, timeout, true, charset, useragent,
				ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword), headers, false);
	}

	/**
	 * @see lucee.runtime.util.HTTPUtil#get(java.net.URL, java.lang.String, java.lang.String, int,
	 *      java.lang.String, java.lang.String, java.lang.String, int, java.lang.String,
	 *      java.lang.String, lucee.commons.net.http.Header[])
	 */
	@Override
	public HTTPResponse get(URL url, String username, String password, int timeout, String charset, String useragent, String proxyserver, int proxyport, String proxyuser,
			String proxypassword, Header[] headers) throws IOException {
		return HTTPEngine.get(url, username, password, HTTPEngine.DEFAULT_CONNECT_REQUEST_TIMEOUT, HTTPEngine.DEFAULT_CONNECT_TIMEOUT, timeout, true, charset, useragent,
				ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword), headers, false);
	}

	@Override
	public HTTPResponse put(URL url, String username, String password, int timeout, String charset, String useragent, String proxyserver, int proxyport, String proxyuser,
			String proxypassword, Header[] headers, Object body) throws IOException {
		return put(url, username, proxypassword, timeout, null, charset, useragent, proxyserver, proxyport, proxyuser, proxypassword, headers, body);
	}

	@Override
	public HTTPResponse put(URL url, String username, String password, int timeout, String mimetype, String charset, String useragent, String proxyserver, int proxyport,
			String proxyuser, String proxypassword, Header[] headers, Object body) throws IOException {
		return HTTPEngine.put(url, username, password, HTTPEngine.DEFAULT_CONNECT_REQUEST_TIMEOUT, HTTPEngine.DEFAULT_CONNECT_TIMEOUT, timeout, true, mimetype, charset, useragent,
				ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword), headers, body, false);
	}

	@Override
	public URL toURL(String strUrl, int port) throws MalformedURLException {
		return toURL(strUrl, port, true);
	}

	@Override
	public URL toURL(String strUrl, int port, boolean encodeIfNecessary) throws MalformedURLException {
		return lucee.commons.net.HTTPUtil.toURL(strUrl, port, encodeIfNecessary ? lucee.commons.net.HTTPUtil.ENCODED_AUTO : lucee.commons.net.HTTPUtil.ENCODED_NO);
	}

	/**
	 * @see lucee.commons.net.HTTPUtil#toURL(java.lang.String, short)
	 */
	@Override
	public URL toURL(String strUrl) throws MalformedURLException {
		return lucee.commons.net.HTTPUtil.toURL(strUrl, lucee.commons.net.HTTPUtil.ENCODED_AUTO);
	}

	@Override
	public URI toURI(String strUrl) throws URISyntaxException {
		return lucee.commons.net.HTTPUtil.toURI(strUrl);
	}

	@Override
	public URI toURI(String strUrl, int port) throws URISyntaxException {
		return lucee.commons.net.HTTPUtil.toURI(strUrl, port);
	}

	@Override
	public URL removeUnecessaryPort(URL url) {
		return new lucee.commons.net.HTTPUtil().removeUnecessaryPort(url);
	}

	@Override
	public Header createHeader(String name, String value) {
		return HTTPEngine.header(name, value);
	}

}