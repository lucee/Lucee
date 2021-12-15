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
package lucee.commons.io.cache;

import java.util.Date;

import lucee.runtime.type.Struct;

/**
 * interface for an entry inside the cache, this interface is read-only
 */
public interface CacheEntry {

	/**
	 * when was the entry accessed last time. this information is optional and depends on the
	 * implementation, when information is not available -1 is returned
	 * 
	 * @return time in milliseconds since 1/1/1970 GMT
	 */
	public Date lastHit();

	/**
	 * when was the entry last time modified. this information is optional and depends on the
	 * implementation, when information is not available -1 is returned
	 * 
	 * @return time offset in milliseconds since 1/1/1970 GMT
	 */
	public Date lastModified();

	/**
	 * when was the entry created. this information is optional and depends on the implementation, when
	 * information is not available -1 is returned
	 * 
	 * @return time offset in milliseconds since 1/1/1970 GMT
	 */
	public Date created();

	/**
	 * how many time was the entry accessed? this information is optional and depends on the
	 * implementation, when information is not available -1 is returned
	 * 
	 * @return access count
	 */
	public int hitCount();

	/**
	 * @return the key associated with this entry
	 */
	public String getKey();

	/**
	 * @return the value associated with this entry
	 */
	public Object getValue();

	/**
	 * the size of the object
	 * 
	 * @return size of the object
	 */
	public long size();

	/**
	 * define time until the entry is valid
	 * 
	 * @return time offset in milliseconds since 1/1/1970 GMT or Long.MIN_VALUE if value is not defined
	 */
	public long liveTimeSpan();

	/**
	 * time in milliseconds after which the object is flushed from the cache if it is not accessed
	 * during that time.
	 * 
	 * @return time milliseconds since 1/1/1970 GMT or Long.MIN_VALUE if value is not defined
	 */
	public long idleTimeSpan();

	/**
	 * get all information data available for this entry
	 * 
	 * @return custom information to the entry
	 */
	public Struct getCustomInfo();
}