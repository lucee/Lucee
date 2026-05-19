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
package lucee.runtime.type.scope.storage;

import java.util.Map;
import lucee.runtime.type.Collection;

// FUTURE merge into loader

/**
 * Extended storage scope interface with token management and dirty/clean state tracking.
 */
public interface StorageScopePro extends StorageScope {

	public boolean verifyToken(String token, String key, boolean remove);

	public Map<Collection.Key, String> getTokens();

	public void setTokens(Map<Collection.Key, String> tokens);

	/**
	 * Mark the scope as dirty so it will be persisted to storage at end of request.
	 * Use this when you've modified nested data that Lucee's change detection cannot track.
	 */
	public void setDirty();

	/**
	 * Mark the scope as clean, indicating no pending changes need to be persisted.
	 * Called after successfully writing to storage.
	 */
	public void setClean();

}