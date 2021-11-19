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
package lucee.commons.io.res.util;

import java.io.IOException;
import java.io.OutputStream;

import lucee.commons.io.res.Resource;

public class ResourceOutputStream extends OutputStream {

	private final Resource res;
	private final OutputStream os;

	/**
	 * Constructor of the class
	 * 
	 * @param res
	 * @param os
	 */
	public ResourceOutputStream(Resource res, OutputStream os) {
		this.res = res;
		this.os = os;
	}

	@Override
	public void write(int b) throws IOException {
		os.write(b);
	}

	@Override
	public void close() throws IOException {
		try {
			os.close();
		}
		finally {
			res.getResourceProvider().unlock(res);
		}
	}

	@Override
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		os.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		os.write(b);
	}

	/**
	 * @return the os
	 */
	public OutputStream getOutputStream() {
		return os;
	}

	/**
	 * @return the res
	 */
	public Resource getResource() {
		return res;
	}

}