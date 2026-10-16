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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngineBasic.HTTPDownloaderHeadResponse;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.HTTPResponseSupport;
import lucee.commons.net.http.Header;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

public final class HTTPResponse4Impl extends HTTPResponseSupport implements HTTPResponse, Closeable {

	HttpResponse rsp;
	HttpUriRequest req;
	private URL url;
	private HttpContext context;
	private CloseableHttpClient client;
	private boolean pooled;

	public HTTPResponse4Impl(URL url, HttpContext context, CloseableHttpClient client, HttpUriRequest req, HttpResponse rsp, boolean pooled) {
		this.url = url;
		this.context = context;
		this.client = client;
		this.req = req;
		this.rsp = rsp;
		this.pooled = pooled;
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
		return new HTTPEngineInputStream(this, e.getContent());
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
		if (context instanceof HttpClientContext) {
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
				LogUtil.warn("http-response", e);
			}
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
		catch (MalformedURLException e) {}

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

	@Override
	public void close() throws IOException {
		try {
			if (rsp instanceof Closeable) {
				((Closeable) rsp).close();
			}
		}
		finally {
			if (!pooled) {
				client.close();
			}
		}
	}

	public static class HTTPEngineInputStream extends InputStream {

		private HTTPResponse rsp;
		private InputStream is;
		private HTTPDownloaderHeadResponse meta;

		public HTTPEngineInputStream(HTTPResponse rsp, InputStream is) {
			this.rsp = rsp;
			this.is = is;
		}

		public HTTPDownloaderHeadResponse getHTTPDownloaderHeadResponse() {
			if (meta == null) meta = new HTTPDownloaderHeadResponse(rsp);
			return meta;
		}

		@Override
		public void close() throws IOException {
			try {
				is.close();
			}
			finally {
				if (rsp instanceof HTTPResponse4Impl) {
					((HTTPResponse4Impl) rsp).close();
				}
			}
		}

		@Override
		public int read(byte[] b) throws IOException {
			return is.read(b);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return is.read(b, off, len);
		}

		@Override
		public byte[] readAllBytes() throws IOException {
			return is.readAllBytes();
		}

		@Override
		public byte[] readNBytes(int len) throws IOException {
			return is.readNBytes(len);
		}

		@Override
		public int readNBytes(byte[] b, int off, int len) throws IOException {
			return is.readNBytes(b, off, len);
		}

		@Override
		public long skip(long n) throws IOException {
			return is.skip(n);
		}

		@Override
		public int available() throws IOException {
			return is.available();
		}

		@Override
		public synchronized void mark(int readlimit) {
			is.mark(readlimit);
		}

		@Override
		public synchronized void reset() throws IOException {
			is.reset();
		}

		@Override
		public boolean markSupported() {
			return is.markSupported();
		}

		@Override
		public long transferTo(OutputStream out) throws IOException {
			return is.transferTo(out);
		}

		@Override
		public int read() throws IOException {
			return is.read();
		}
	}
}