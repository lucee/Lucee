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
import java.nio.charset.Charset;

import javax.activation.DataSource;

import lucee.commons.lang.CharSet;

import org.apache.commons.lang.WordUtils;

public final class StringDataSource implements DataSource {

	private String text;
	private String ct;
	private CharSet charset;

	public StringDataSource(String text, String ct, CharSet charset, int maxLineLength) {
		this.text=WordUtils.wrap(text, maxLineLength);
		this.ct=ct;
		this.charset=charset;
	}

	@Override
	public String getContentType() {
		return ct;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(text.getBytes(charset.toCharset()));
	}

	@Override
	public String getName() {
		return "StringDataSource";
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new IOException("no access to write");
	}

}