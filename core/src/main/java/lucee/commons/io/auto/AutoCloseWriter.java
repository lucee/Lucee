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
import java.io.Writer;

/**
 * Close the Writer automatically when object will destroyed by the garbage
 */
public final class AutoCloseWriter extends Writer {

	private final Writer writer;

	/**
	 * constructor of the class
	 * 
	 * @param writer
	 */
	public AutoCloseWriter(Writer writer) {
		this.writer = writer;
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		writer.write(cbuf, off, len);
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		writer.write(cbuf);
	}

	@Override
	public void write(int c) throws IOException {
		writer.write(c);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		writer.write(str, off, len);
	}

	@Override
	public void write(String str) throws IOException {
		writer.write(str);
	}

	@Override
	public void finalize() throws Throwable {
		super.finalize();
		try {
			writer.close();
		}
		catch (Exception e) {
		}
	}

}