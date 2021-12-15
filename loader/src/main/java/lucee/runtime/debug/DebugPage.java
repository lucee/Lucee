/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.debug;

import lucee.commons.io.res.Resource;

/**
 * debug page
 */
public interface DebugPage {

	/**
	 * sets the execution time of the page
	 * 
	 * @param t execution time of the page
	 */
	public abstract void set(long t);

	/**
	 * return the minimum execution time of the page
	 * 
	 * @return minimum execution time
	 */
	public abstract int getMinimalExecutionTime();

	/**
	 * return the maximum execution time of the page
	 * 
	 * @return maximum execution time
	 */
	public abstract int getMaximalExecutionTime();

	/**
	 * return the average execution time of the page
	 * 
	 * @return average execution time
	 */
	public abstract int getAverageExecutionTime();

	/**
	 * return count of call the page
	 * 
	 * @return average execution time
	 */
	public abstract int getCount();

	/**
	 * return file represetati9on of the debug page
	 * 
	 * @return file object
	 */
	public abstract Resource getFile();

}