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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import lucee.commons.io.IOUtil;

public class CachingGZIPInputStream extends InputStream {

	private final byte[] barr;
	private GZIPInputStream is;

	public CachingGZIPInputStream(InputStream is) throws IOException {
		barr = IOUtil.toBytes(is, true);
		this.is = new GZIPInputStream(new ByteArrayInputStream(barr));
	}

	@Override
	public int available() throws IOException {
		return is.available();
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

	@Override
	public void mark(int readlimit) {
		is.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return is.markSupported();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return is.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return is.read(b);
	}

	@Override
	public void reset() throws IOException {
		is.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return is.skip(n);
	}

	@Override
	public int read() throws IOException {
		return is.read();
	}

	public InputStream getRawData() {
		return new ByteArrayInputStream(barr);
	}
}