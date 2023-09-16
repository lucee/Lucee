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
package lucee.commons.net.http.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.HTTPResponseSupport;
import lucee.commons.net.http.Header;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

public class HTTPResponse4Impl extends HTTPResponseSupport implements HTTPResponse {

	HttpResponse rsp;
	HttpUriRequest req;
	private URL url;
	private HttpContext context;

	public HTTPResponse4Impl(URL url, HttpContext context, HttpUriRequest req, HttpResponse rsp) {
		this.url = url;
		this.context = context;
		this.req = req;
		this.rsp = rsp;
	}

	@Override
	public String getContentAsString() throws IOException {
		return getContentAsString(null);
	}

	@Override
	public String getContentAsString(String charset) throws IOException {
		HttpEntity entity = rsp.getEntity();
		InputStream is = null;
		if (StringUtil.isEmpty(charset, true)) charset = getCharset();
		try {
			return IOUtil.toString(is = entity.getContent(), charset);
		}
		finally {
			IOUtil.close(is);
		}
	}

	@Override
	public InputStream getContentAsStream() throws IOException {
		HttpEntity e = rsp.getEntity();
		if (e == null) return null;
		return e.getContent();
	}

	@Override
	public byte[] getContentAsByteArray() throws IOException {
		HttpEntity entity = rsp.getEntity();
		InputStream is = null;
		if (entity == null) return new byte[0];
		try {
			return IOUtil.toBytes(is = entity.getContent());
		}
		finally {
			IOUtil.close(is);
		}
	}

	public Array getLocations() {
		try {
			List<URI> locations = ((HttpClientContext) context).getRedirectLocations();
			if (locations != null) {
				Array arr = new ArrayImpl();
				for (URI loc: locations) {
					arr.appendEL(loc.toString());
				}
				return arr;
			}
		}
		catch (Exception e) {
		}
		return null;
	}

	@Override
	public Header getLastHeader(String name) {
		org.apache.http.Header header = rsp.getLastHeader(name);
		if (header != null) return new HeaderWrap(header);
		return null;
	}

	@Override
	public Header getLastHeaderIgnoreCase(String name) {
		return getLastHeaderIgnoreCase(rsp, name);
	}

	public static Header getLastHeaderIgnoreCase(HttpResponse rsp, String name) {
		org.apache.http.Header header = rsp.getLastHeader(name);
		if (header != null) return new HeaderWrap(header);

		org.apache.http.Header[] headers = rsp.getAllHeaders();
		for (int i = headers.length - 1; i >= 0; i--) {
			if (name.equalsIgnoreCase(headers[i].getName())) {
				return new HeaderWrap(headers[i]);
			}
		}
		return null;
	}

	@Override
	public URL getURL() {
		try {
			return req.getURI().toURL();
		}
		catch (MalformedURLException e) {
			return url;
		}
	}

	public URL getTargetURL() {
		URL start = getURL();

		HttpUriRequest req = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
		URI uri = req.getURI();
		String path = uri.getPath();
		String query = uri.getQuery();
		if (!StringUtil.isEmpty(query)) path += "?" + query;

		URL _url = start;
		try {
			_url = new URL(start.getProtocol(), start.getHost(), start.getPort(), path);
		}
		catch (MalformedURLException e) {
		}

		return _url;
	}

	@Override
	public int getStatusCode() {
		return rsp.getStatusLine().getStatusCode();
	}

	@Override
	public String getStatusText() {
		return rsp.getStatusLine().getReasonPhrase();
	}

	@Override
	public String getProtocolVersion() {
		return rsp.getStatusLine().getProtocolVersion().toString();
	}

	@Override
	public String getStatusLine() {
		return rsp.getStatusLine().toString();
	}

	@Override
	public Header[] getAllHeaders() {
		org.apache.http.Header[] src = rsp.getAllHeaders();
		if (src == null) return new Header[0];
		Header[] trg = new Header[src.length];
		for (int i = 0; i < src.length; i++) {
			trg[i] = new HeaderWrap(src[i]);
		}
		return trg;
	}

	public void close() throws IOException {
		/*
		 * if(rsp instanceof CloseableHttpResponse) { ((CloseableHttpResponse)rsp).close(); }
		 */
	}
}