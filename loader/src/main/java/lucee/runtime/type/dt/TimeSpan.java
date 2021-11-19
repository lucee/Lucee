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

import java.io.Serializable;

import lucee.runtime.dump.Dumpable;
import lucee.runtime.op.Castable;

/**
 * defines a time span
 */
public interface TimeSpan extends Castable, Dumpable, Serializable {

	/**
	 * @return returns the timespan in milliseconds
	 */
	public abstract long getMillis();

	/**
	 * @return returns the timespan in seconds
	 */
	public abstract long getSeconds();

	/**
	 * @return Returns the day value.
	 */
	public abstract int getDay();

	/**
	 * @return Returns the hour value.
	 */
	public abstract int getHour();

	/**
	 * @return Returns the minute value.
	 */
	public abstract int getMinute();

	/**
	 * @return Returns the second value.
	 */
	public abstract int getSecond();

}