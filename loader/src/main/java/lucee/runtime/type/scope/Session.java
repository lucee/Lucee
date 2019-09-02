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

import lucee.runtime.type.Collection;

/**
 * 
 */
public interface Session extends Scope, UserScope {// FUTURE add StorageScope
	/**
	 * @return returns the last access to this session scope
	 * @deprecated
	 */
	@Deprecated
	public abstract long getLastAccess();

	/**
	 * @return returns the current timespan of the session
	 * @deprecated
	 */
	@Deprecated
	public abstract long getTimeSpan();

	public long getCreated();

	/**
	 * @return is the scope expired or not
	 */
	public abstract boolean isExpired();

	/**
	 * sets the last access timestamp to now
	 */
	public abstract void touch();

	public int _getId();

	/**
	 * @return all keys except the readonly ones (cfid,cftoken,hitcount,lastvisit ...)
	 */
	public abstract Collection.Key[] pureKeys();

}