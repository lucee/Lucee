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
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

public interface HTTPResponse extends java.io.Closeable {

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

	/**
	 * @return the URL of the last request, that is the target of the redirects that were followed
	 */
	public URL getTargetURL();

	public int getStatusCode();

	public String getStatusText();

	public String getProtocolVersion();

	public String getStatusLine();

	public Header[] getAllHeaders();

	/**
	 * @return the URLs of the redirects that were followed, null if there were none
	 */
	public Array getLocations();

	/**
	 * @return information about the connection that was used for the request, for example if it came
	 *         from a connection pool, null if the engine has no such information. It is returned in the
	 *         key "connection" of the cfhttp result.
	 */
	public default Struct getConnectionInfo() {
		return null;
	}

	/**
	 * releases the connection, must be called when the response is no longer needed
	 */
	public void close() throws IOException;
}
