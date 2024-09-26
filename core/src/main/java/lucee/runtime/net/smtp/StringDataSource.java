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
import java.util.Scanner;

import javax.activation.DataSource;

import org.apache.commons.lang.WordUtils;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.StringUtil;

public final class StringDataSource implements DataSource {

	private final String text;
	private final String ct;
	private final CharSet charset;

	public final static char CR = (char) 13;
	public final static char LF = (char) 10;

	/*
	 * Some types of transfer encoding such as "quoted-printable" and "base64"
	 * do not require wrapping of lines, because it's handled automatically
	 * in the encoding.
	 */
	public StringDataSource(String text, String ct, CharSet charset) {
		this.text = text;
		this.ct = ct;
		this.charset = charset;
	}

	public StringDataSource(String text, String ct, CharSet charset, int maxLineLength) {
		this.text = wrapText(text, maxLineLength);
		this.ct = ct;
		this.charset = charset;
	}

	@Override
	public String getContentType() {
		return ct;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(charset == null ? text.getBytes() : text.getBytes(CharsetUtil.toCharset(charset)));
	}

	@Override
	public String getName() {
		return "StringDataSource";
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new IOException("no access to write");
	}

	public static String wrapText(String text, int maxLineLength) {

		if (StringUtil.isEmpty(text)) return "";

		StringBuilder sb = new StringBuilder(text.length());
		Scanner scanner = new Scanner(text);
		String line;

		while (scanner.hasNextLine()) {

			line = scanner.nextLine();
			if (line.length() > maxLineLength) line = WordUtils.wrap(line, maxLineLength);
			sb.append(line).append(CR).append(LF);
		}

		return sb.toString();
	}

}