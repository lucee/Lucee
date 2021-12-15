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
package lucee.commons.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

public final class CountingReader extends Reader {

	private final Reader reader;
	private int count = 0;

	public CountingReader(Reader reader) {
		this.reader = reader;
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
		count++;
		return reader.read();
	}

	@Override
	public int read(char[] cbuf) throws IOException {

		return reader.read(cbuf);
	}

	@Override
	public int read(CharBuffer arg0) throws IOException {
		return super.read(arg0.array());
	}

	@Override
	public boolean ready() throws IOException {
		// TODO Auto-generated method stub
		return super.ready();
	}

	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub
		super.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		// TODO Auto-generated method stub
		return super.skip(n);
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}