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
import java.io.OutputStream;

public final class ForkOutputStream extends OutputStream {

	private final OutputStream os1;
	private final OutputStream os2;

	public ForkOutputStream(OutputStream os1, OutputStream os2) {
		this.os1 = os1;
		this.os2 = os2;
	}

	@Override
	public void close() throws IOException {
		try {
			os1.close();
		}
		finally {
			os2.close();
		}
	}

	@Override
	public void flush() throws IOException {
		try {
			os1.flush();
		}
		finally {
			os2.flush();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		os1.write(b, off, len);
		os2.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		os1.write(b);
		os2.write(b);
	}

	@Override
	public void write(int b) throws IOException {
		os1.write(b);
		os2.write(b);
	}

}