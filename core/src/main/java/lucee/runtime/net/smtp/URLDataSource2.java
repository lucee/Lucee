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
package lucee.runtime.net.smtp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.activation.DataSource;

import lucee.commons.io.IOUtil;

public final class URLDataSource2 implements DataSource {

	private URL url;
	private final static String DEFAULT_CONTENT_TYPE = "application/octet-stream";
	private byte[] barr;

	/**
	 * Creates a URLDataSource from a URL object
	 */
	public URLDataSource2(URL url) {
		this.url = url;
	}

	/**
	 * Returns the value of the URL content-type header field
	 * 
	 */
	@Override
	public String getContentType() {
		URLConnection connection = null;
		try {
			connection = url.openConnection();
		}
		catch (IOException e) {
		}
		if (connection == null) return DEFAULT_CONTENT_TYPE;

		return connection.getContentType();

	}

	/**
	 * Returns the file name of the URL object
	 */
	@Override
	public String getName() {
		return url.getFile();
	}

	/**
	 * Returns an InputStream obtained from the data source
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (barr == null) {
			barr = IOUtil.toBytes(url.openStream());
		}
		return new ByteArrayInputStream(barr);
	}

	/**
	 * Returns an OutputStream obtained from the data source
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {

		URLConnection connection = url.openConnection();
		if (connection == null) return null;

		connection.setDoOutput(true); // is it necessary?
		return connection.getOutputStream();
	}

	/**
	 * Returns the URL of the data source
	 */
	public URL getURL() {
		return url;
	}
}