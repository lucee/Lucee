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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * this class is the short form for <code>new BufferedOutputStream(new FileOutputStream())</code>
 */
public final class BufferedFileOutputStream extends BufferedOutputStream {

	/**
	 * @param file
	 * @param append
	 * @throws FileNotFoundException
	 */
	public BufferedFileOutputStream(File file, boolean append) throws FileNotFoundException {
		super(new FileOutputStream(file, append));
	}

	/**
	 * @param file
	 * @throws FileNotFoundException
	 */
	public BufferedFileOutputStream(File file) throws FileNotFoundException {
		super(new FileOutputStream(file));
	}

}