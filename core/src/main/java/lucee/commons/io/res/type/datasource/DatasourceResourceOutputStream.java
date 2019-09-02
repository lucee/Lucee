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
package lucee.commons.io.res.type.datasource;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import lucee.runtime.exp.AlwaysThrow;

public class DatasourceResourceOutputStream extends OutputStream {

	private final DataWriter dw;
	private final OutputStream os;

	/**
	 * Constructor of the class
	 * 
	 * @param res
	 * @param os
	 */
	public DatasourceResourceOutputStream(DataWriter dw, OutputStream os) {
		this.dw = dw;
		this.os = os;
	}

	@Override
	public void write(int b) throws IOException {
		os.write(b);
	}

	@Override
	public void close() throws IOException {
		os.close();
		try {
			dw.join();
		}
		catch (InterruptedException e) {
			throw new AlwaysThrow(e.getMessage());
		}

		SQLException ioe = dw.getException();
		if (ioe != null) {
			throw new AlwaysThrow(ioe.getMessage());
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

}