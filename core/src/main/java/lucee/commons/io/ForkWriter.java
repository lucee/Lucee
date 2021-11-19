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
import java.io.Writer;

public class ForkWriter extends Writer {

	private final Writer w1;
	private final Writer w2;

	public ForkWriter(Writer w1, Writer w2) {
		this.w1 = w1;
		this.w2 = w2;
	}

	@Override
	public Writer append(char c) throws IOException {
		try {
			w1.write(c);
		}
		finally {
			w2.write(c);
		}
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		try {
			w1.write(csq.toString(), start, end);
		}
		finally {
			w2.write(csq.toString(), start, end);
		}
		return this;
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		try {
			w1.write(csq.toString());
		}
		finally {
			w2.write(csq.toString());
		}
		return this;
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		try {
			w1.write(cbuf);
		}
		finally {
			w2.write(cbuf);
		}
	}

	@Override
	public void write(int c) throws IOException {
		try {
			w1.write(c);
		}
		finally {
			w2.write(c);
		}
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		try {
			w1.write(str, off, len);
		}
		finally {
			w2.write(str, off, len);
		}
	}

	@Override
	public void write(String str) throws IOException {
		try {
			w1.write(str);
		}
		finally {
			w2.write(str);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			w1.close();
		}
		finally {
			w2.close();
		}
	}

	@Override
	public void flush() throws IOException {

		try {
			w1.flush();
		}
		finally {
			w2.flush();
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {

		try {
			w1.write(cbuf, off, len);
		}
		finally {
			w2.write(cbuf, off, len);
		}
	}

}