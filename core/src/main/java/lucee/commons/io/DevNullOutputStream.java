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

import java.io.OutputStream;
import java.io.Serializable;

/**
 * dev null output stream, write data to nirvana
 */
public final class DevNullOutputStream extends OutputStream implements Serializable {

	public static final DevNullOutputStream DEV_NULL_OUTPUT_STREAM = new DevNullOutputStream();

	/**
	 * Constructor of the class
	 */
	private DevNullOutputStream() {
	}

	@Override
	public void close() {
	}

	@Override
	public void flush() {
	}

	@Override
	public void write(byte[] b, int off, int len) {
	}

	@Override
	public void write(byte[] b) {
	}

	@Override
	public void write(int b) {
	}

}