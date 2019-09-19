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
package lucee.runtime.debug;

import java.io.Serializable;

/**
 * a single debug entry
 */
public interface DebugEntry extends Serializable {

	/**
	 * @return Returns the exeTime.
	 */
	public abstract long getExeTime();

	/**
	 * @param exeTime The exeTime to set.
	 */
	public abstract void updateExeTime(long exeTime);

	/**
	 * @return Returns the src.
	 */
	public abstract String getSrc();

	/**
	 * @return Returns the count.
	 */
	public abstract int getCount();

	/**
	 * @return Returns the max.
	 */
	public abstract long getMax();

	/**
	 * @return Returns the min.
	 */
	public abstract long getMin();

	/**
	 * @return the file path of this entry
	 */
	public abstract String getPath();

	public abstract String getId();

}