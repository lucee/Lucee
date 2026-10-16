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

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

/**
 * implementation of <code>ServletInputStream</code>.
 */
public final class ServletInputStreamDummy extends ServletInputStream {
	private InputStream stream;
	private boolean finished;

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
	 * @param is
	 */
	public ServletInputStreamDummy(InputStream is) {
		stream = is == null ? new ByteArrayInputStream(new byte[0]) : is;
	}

	@Override
	public int read() throws IOException {
		int b = stream.read();
		if (b == -1) finished = true;
		return b;
	}

	@Override
	public int readLine(byte[] barr, int arg1, int arg2) throws IOException {
		int n = stream.read(barr, arg1, arg2);
		if (n == -1) finished = true;
		return n;
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
		int n = stream.read(b, off, len);
		if (n == -1) finished = true;
		return n;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int n = stream.read(b);
		if (n == -1) finished = true;
		return n;
	}

	@Override
	public synchronized void reset() throws IOException {
		stream.reset();
		finished = false;
	}

	@Override
	public long skip(long n) throws IOException {
		return stream.skip(n);
	}

	// Servlet 3.1 contract: false until EOF is reached, true after a read returns -1.
	// Matches Undertow/Tomcat/Jetty so _InternalRequest behaves like a real container.
	@Override
	public boolean isFinished() {
		return finished;
	}

	@Override
	public boolean isReady() {
		return !finished;
	}

	@Override
	public void setReadListener(ReadListener arg0) {
		throw new IllegalStateException("async I/O not supported on internal request stream");
	}
}