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
package lucee.commons.io.res.type.cfml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import lucee.commons.lang.ExceptionUtil;

public final class CFMLResourceOutputStream extends OutputStream {
	private ByteArrayOutputStream baos;
	private CFMLResource res;

	public CFMLResourceOutputStream(CFMLResource res) {
		this.res = res;
		baos = new ByteArrayOutputStream();
	}

	@Override
	public void close() throws IOException {
		baos.close();

		try {
			res.setBinary(baos.toByteArray());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw ExceptionUtil.toIOException(t);
		}
		finally {
			res.getResourceProvider().unlock(res);
		}
	}

	@Override
	public void flush() throws IOException {
		baos.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		baos.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		baos.write(b);
	}

	@Override
	public void write(int b) throws IOException {
		baos.write(b);
	}
}