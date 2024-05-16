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
package lucee.runtime.cfx;

/**
 * Custom Tag Exception
 */
public final class CFXTagException extends Exception {

	private static final long serialVersionUID = 680174337882347105L;

	/**
	 * Constructor of the Exception
	 * 
	 * @param message exception message
	 */
	public CFXTagException(final String message) {
		super(message);
	}

	/**
	 * Constructor of the Exception
	 * 
	 * @param e exception
	 */
	public CFXTagException(final Throwable e) {
		super(e.getClass().getName() + ":" + e.getMessage());
	}
}