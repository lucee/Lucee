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
package lucee.commons.io.auto;

import java.io.IOException;
import java.io.Reader;

/**
 * Close the Reader automatically when object will destroyed by the garbage
 */
public final class AutoCloseReader extends Reader {

	private final Reader reader;

	/**
	 * constructor of the class
	 * 
	 * @param reader
	 */
	public AutoCloseReader(Reader reader) {
		this.reader = reader;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
		reader.mark(readAheadLimit);
	}

	@Override
	public boolean markSupported() {
		return reader.markSupported();
	}

	@Override
	public int read() throws IOException {
		return reader.read();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return reader.read(cbuf, off, len);
	}

	@Override
	public int read(char[] cbuf) throws IOException {
		return reader.read(cbuf);
	}

	@Override
	public boolean ready() throws IOException {
		return reader.ready();
	}

	@Override
	public void reset() throws IOException {
		reader.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return reader.skip(n);
	}

	@Override
	public void finalize() throws Throwable {
		super.finalize();
		try {
			reader.close();
		}
		catch (Exception e) {
		}
	}

}