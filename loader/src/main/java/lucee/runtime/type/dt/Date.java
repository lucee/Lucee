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
package lucee.runtime.type.dt;

/**
 * Date only object
 */
public abstract class Date extends DateTime {

	private static final long serialVersionUID = -3562358925913585840L;

	/**
	 * constructor of the class
	 * 
	 * @param date date object
	 */
	public Date(final DateTime date) {
		super(date.getTime());
	}

	/**
	 * constructor of the class
	 * 
	 * @param time
	 */
	public Date(final long time) {
		super(time);
	}

	/**
	 * constructor of the class (Now)
	 */
	public Date() {
		super();
	}
}