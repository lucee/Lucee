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
package lucee.commons.net.http.httpclient3;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponseSupport;
import lucee.commons.net.http.Header;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;

public class HTTPResponse3Impl extends HTTPResponseSupport  {

	private HttpMethod rsp;
	private URL url;

	public HTTPResponse3Impl(HttpMethod rsp, URL url) {
		this.rsp=rsp;
		this.url=url;
	}

	@Override
	public String getContentAsString() throws IOException {
		return getContentAsString(getCharset());
	}

	@Override
	public String getContentAsString(String charset) throws IOException {
		InputStream is=null;
		try{
			is=getContentAsStream();
			return IOUtil.toString(is,charset);
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	@Override
	public InputStream getContentAsStream() throws IOException {
		return rsp.getResponseBodyAsStream();
	}

	@Override
	public byte[] getContentAsByteArray() throws IOException {
		InputStream is=null;
		try{
			return IOUtil.toBytes(is=getContentAsStream());
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	@Override
	public Header getLastHeader(String name) {
		return new HeaderWrap(rsp.getResponseHeader(name));
	}

	@Override
	public Header getLastHeaderIgnoreCase(String name) {
		org.apache.commons.httpclient.Header[] headers = rsp.getResponseHeaders();
		for(int i=headers.length-1;i>=0;i--){
			if(headers[i].getName().equalsIgnoreCase(name)) return new HeaderWrap(headers[i]);
		}
		return null;
	}

	@Override
	public URL getURL() {
		HostConfiguration config = rsp.getHostConfiguration();
		
		try {
			String qs = rsp.getQueryString();
			if(StringUtil.isEmpty(qs))
				return new URL(config.getProtocol().getScheme(),config.getHost(),config.getPort(),rsp.getPath());
			return new URL(config.getProtocol().getScheme(),config.getHost(),config.getPort(),rsp.getPath()+"?"+qs);
		} catch (MalformedURLException e) {
		}
		
		return url;
	}

	@Override
	public int getStatusCode() {
		return rsp.getStatusCode();
	}

	@Override
	public String getStatusText() {
		return rsp.getStatusText();
	}

	@Override
	public String getProtocolVersion() {
		return rsp.getStatusLine().getHttpVersion();
	}

	@Override
	public String getStatusLine() {
		return rsp.getStatusLine().toString();
	}

	@Override
	public Header[] getAllHeaders() {
		org.apache.commons.httpclient.Header[] src = rsp.getResponseHeaders();
		if(src==null) return new Header[0];
		Header[] trg=new Header[src.length];
		for(int i=0;i<src.length;i++){
			trg[i]=new HeaderWrap(src[i]);
		}
		return trg;
	}


}