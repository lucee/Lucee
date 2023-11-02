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

import org.apache.http.entity.ContentType;

import lucee.commons.io.TemporaryStream;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.commons.net.http.httpclient.HeaderImpl;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.type.util.CollectionUtil;

public class HTTPEngine {

	// private static final boolean use4=true;

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
	// private static final String NO_MIMETYPE="Unable to determine MIME type of file.";

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

	public static HTTPResponse get(URL url) throws IOException {
		// if(use4)
		return HTTPEngine4Impl.get(url, null, null, -1, true, null, null, null, null);
		// return HTTPEngine3Impl.get(url, null, null, -1,MAX_REDIRECT, null, null, null, null);
	}

	public static HTTPResponse post(URL url) throws IOException {
		// if(use4)
		return HTTPEngine4Impl.post(url, null, null, -1, true, null, null, null, null);
		// return HTTPEngine3Impl.post(url, null, null, -1,MAX_REDIRECT, null, null, null, null,null);
	}

	public static HTTPResponse get(URL url, String username, String password, long timeout, boolean followRedirect, String charset, String useragent, ProxyData proxy,
			Header[] headers) throws IOException {
		// if(use4)
		return HTTPEngine4Impl.get(url, username, password, timeout, followRedirect, charset, useragent, proxy, headers);
		// return HTTPEngine3Impl.get(url, username, password, timeout, followRedirect?MAX_REDIRECT:0,
		// charset, useragent, proxy, headers);
	}

	public static HTTPResponse post(URL url, String username, String password, long timeout, boolean followRedirect, String charset, String useragent, ProxyData proxy,
			Map<String, String> headers, Map<String, String> params) throws IOException {
		// if(use4)
		return HTTPEngine4Impl.post(url, username, password, timeout, followRedirect, charset, useragent, proxy, toHeaders(headers), params);
		// return HTTPEngine3Impl.post(url, username, password, timeout, followRedirect?MAX_REDIRECT:0,
		// charset, useragent, proxy, toHeaders(headers),params);
	}

	public static HTTPResponse head(URL url, String username, String password, int timeout, boolean followRedirect, String charset, String useragent, ProxyData proxy,
			Header[] headers) throws IOException {
		// if(use4)
		return HTTPEngine4Impl.head(url, username, password, timeout, followRedirect, charset, useragent, proxy, headers);
		// return HTTPEngine3Impl.head(url, username, password, timeout, followRedirect?MAX_REDIRECT:0,
		// charset, useragent, proxy, headers);
	}

	public static HTTPResponse put(URL url, String username, String password, int timeout, boolean followRedirect, String mimetype, String charset, String useragent,
			ProxyData proxy, Header[] headers, Object body) throws IOException {
		// if(use4)
		return HTTPEngine4Impl.put(url, username, password, timeout, followRedirect, mimetype, charset, useragent, proxy, headers, body);
		// return HTTPEngine3Impl.put(url, username, password, timeout, followRedirect?MAX_REDIRECT:0,
		// mimetype,charset, useragent, proxy, headers,body);
	}

	public static HTTPResponse delete(URL url, String username, String password, int timeout, boolean followRedirect, String charset, String useragent, ProxyData proxy,
			Header[] headers) throws IOException {
		// if(use4)
		return HTTPEngine4Impl.delete(url, username, password, timeout, followRedirect, charset, useragent, proxy, headers);
		// return HTTPEngine3Impl.delete(url, username, password, timeout, followRedirect?MAX_REDIRECT:0,
		// charset, useragent, proxy, headers);
	}

	public static Header header(String name, String value) {
		// if(use4)
		return HTTPEngine4Impl.header(name, value);
		// return HTTPEngine3Impl.header(name, value);
	}

	public static Entity getEmptyEntity(String mimetype, String charset) {
		ContentType ct = toContentType(mimetype, charset);
		// if(use4)
		return HTTPEngine4Impl.getEmptyEntity(ct);
		// return HTTPEngine3Impl.getEmptyEntity(ct==null?null:ct.toString());
	}

	public static Entity getByteArrayEntity(byte[] barr, String mimetype, String charset) {
		ContentType ct = toContentType(mimetype, charset);
		// if(use4)
		return HTTPEngine4Impl.getByteArrayEntity(barr, ct);
		// return HTTPEngine3Impl.getByteArrayEntity(barr,ct==null?null:ct.toString());
	}

	public static Entity getTemporaryStreamEntity(TemporaryStream ts, String mimetype, String charset) {
		ContentType ct = toContentType(mimetype, charset);
		// if(use4)
		return HTTPEngine4Impl.getTemporaryStreamEntity(ts, ct);
		// return HTTPEngine3Impl.getTemporaryStreamEntity(ts,ct==null?null:ct.toString());
	}

	public static Entity getResourceEntity(Resource res, String mimetype, String charset) {
		ContentType ct = toContentType(mimetype, charset);
		// if(use4)
		return HTTPEngine4Impl.getResourceEntity(res, ct);
		// return HTTPEngine3Impl.getResourceEntity(res,ct==null?null:ct.toString());
	}

	private static Header[] toHeaders(Map<String, String> headers) {
		if (CollectionUtil.isEmpty(headers)) return null;
		Header[] rtn = new Header[headers.size()];
		Iterator<Entry<String, String>> it = headers.entrySet().iterator();
		Entry<String, String> e;
		int index = 0;
		while (it.hasNext()) {
			e = it.next();
			rtn[index++] = new HeaderImpl(e.getKey(), e.getValue());
		}
		return rtn;
	}

	public static ContentType toContentType(String mimetype, String charset) {
		ContentType ct = null;
		if (!StringUtil.isEmpty(mimetype, true)) {
			if (!StringUtil.isEmpty(charset, true)) ct = ContentType.create(mimetype.trim(), charset.trim());
			else ct = ContentType.create(mimetype.trim());
		}
		return ct;
	}

	public static void closeEL(HTTPResponse rsp) {
		/*
		 * DISBALED BECAUSE THIS SEEM TO CAUSE PROBLEM WITH MULTITHREADING, THIS NEEDS MORE INVESTIGATION
		 * if(rsp instanceof HTTPResponse4Impl) { try { ((HTTPResponse4Impl)rsp).close(); } catch (Exception
		 * e) {} }
		 */
	}
}