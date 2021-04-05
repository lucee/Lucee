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
package lucee.runtime.util.pool;

/**
 * Box for an Object for the Pool
 */
public abstract class PoolHandler {

	long time;

	/**
	 * constructor of the class
	 */
	public PoolHandler() {
		time = System.currentTimeMillis();
	}

	/**
	 * clear the Handler
	 */
	public abstract void clear();

	/**
	 * @return returns the Time
	 */
	public final long getTime() {
		return time;
	}

	/**
	 * Sets the Time
	 */
	public final void setTime() {
		time = System.currentTimeMillis();
	}

	/**
	 * sets the value
	 * 
	 * @param o
	 */
	public abstract void setData(Object o);

	/**
	 * returns the Value
	 * 
	 * @return
	 */
	public abstract Object getData();

}