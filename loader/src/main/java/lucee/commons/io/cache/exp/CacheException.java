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
package lucee.commons.io.cache.exp;

import java.io.IOException;

/**
 * Exceptin thrown by Cache or CacheEntry
 */
public class CacheException extends IOException {

	private static final long serialVersionUID = -7937763383640628704L;

	/**
	 * Constructor of the class
	 * 
	 * @param message message of the exception
	 */
	public CacheException(final String message) {
		super(message);
	}
}