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
package lucee.runtime.type.scope;

/**
 * implementation of the application scope
 */
public interface Application extends Scope {

	/**
	 * @return returns the last access timestamp of this Application scope
	 */
	public abstract long getLastAccess();

	/**
	 * @return returns the actual timespan of the application
	 */
	public abstract long getTimeSpan();

	/**
	 * @return is expired
	 */
	public abstract boolean isExpired();

	/**
	 * sets the last access timestamp to now
	 */
	public abstract void touch();

	/**
	 * @return Timestamp of when the application scope was created
	 */
	public long getCreated();

}