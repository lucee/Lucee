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
package lucee.runtime.net.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * implementation of <code>ServletInputStream</code>.
 */
public final class ServletInputStreamDummy extends ServletInputStream {
	private InputStream stream;

	/**
	 * @param data
	 */
	public ServletInputStreamDummy(byte[] data) {
		stream = new ByteArrayInputStream(data == null ? new byte[0] : data);
	}

	public ServletInputStreamDummy(File file) throws FileNotFoundException {
		if (file == null) stream = new ByteArrayInputStream(new byte[0]);
		else stream = new FileInputStream(file);
	}

	/**
	 * @param barr
	 */
	public ServletInputStreamDummy(InputStream is) {
		stream = is == null ? new ByteArrayInputStream(new byte[0]) : is;
	}

	@Override
	public int read() throws IOException {
		return stream.read();
	}

	@Override
	public int readLine(byte[] barr, int arg1, int arg2) throws IOException {
		return stream.read(barr, arg1, arg2);
	}

	@Override
	public int available() throws IOException {
		return stream.available();
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		stream.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return stream.markSupported();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return stream.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return stream.read(b);
	}

	@Override
	public synchronized void reset() throws IOException {
		stream.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return stream.skip(n);
	}

	@Override
	public boolean isFinished() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public boolean isReady() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public void setReadListener(ReadListener arg0) {
		throw new RuntimeException("not supported!");
	}
}