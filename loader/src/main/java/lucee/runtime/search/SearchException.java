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
package lucee.runtime.search;

/**
 * Search Exception
 */
public final class SearchException extends Exception {

	private static final long serialVersionUID = 5156297144445929730L;

	/**
	 * constructor o the class
	 * 
	 * @param message message
	 */
	public SearchException(final String message) {
		super(message);
	}

	/**
	 * @param e exception
	 */
	public SearchException(final Exception e) {
		super(e.getMessage());
		setStackTrace(e.getStackTrace());
	}
}