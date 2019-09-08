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
package lucee.commons.io.cache;

/**
 * An optional interface a Cache can implement. You can register CacheEventListener to a Cache, this
 * CacheEventListener are invoked when a certain event occur.
 */
public interface CacheEvent {

	/**
	 * allows to register a CacheEventListener for one or more certain events
	 * 
	 * @param event
	 */
	public void register(CacheEventListener listener);
}