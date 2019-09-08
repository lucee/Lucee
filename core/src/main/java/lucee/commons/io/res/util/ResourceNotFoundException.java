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

public final class ResourceNotFoundException extends IOException {

	/**
	 * Constructs a <code>FileNotFoundException</code> with <code>null</code> as its error detail
	 * message.
	 */
	public ResourceNotFoundException() {
		super();
	}

	/**
	 * Constructs a <code>FileNotFoundException</code> with the specified detail message. The string
	 * <code>s</code> can be retrieved later by the <code>{@link java.lang.Throwable#getMessage}</code>
	 * method of class <code>java.lang.Throwable</code>.
	 *
	 * @param s the detail message.
	 */
	public ResourceNotFoundException(String s) {
		super(s);
	}

	/**
	 * Constructs a <code>FileNotFoundException</code> with a detail message consisting of the given
	 * pathname string followed by the given reason string. If the <code>reason</code> argument is
	 * <code>null</code> then it will be omitted. This private constructor is invoked only by native I/O
	 * methods.
	 *
	 *
	 */
	public ResourceNotFoundException(String path, String reason) {
		super(path + ((reason == null) ? "" : " (" + reason + ")"));
	}

}