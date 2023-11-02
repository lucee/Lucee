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
import java.io.InputStream;
import java.net.URL;

import lucee.commons.io.res.ContentType;

public interface HTTPResponse {

	public String getContentAsString() throws IOException;

	public String getContentAsString(String charset) throws IOException;

	public InputStream getContentAsStream() throws IOException;

	public byte[] getContentAsByteArray() throws IOException;

	public ContentType getContentType();

	public Header getLastHeader(String name);

	public Header getLastHeaderIgnoreCase(String name);

	public String getCharset();

	public long getContentLength() throws IOException;

	public URL getURL();

	public int getStatusCode();

	public String getStatusText();

	public String getProtocolVersion();

	public String getStatusLine();

	public Header[] getAllHeaders();
}