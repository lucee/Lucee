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

import lucee.runtime.PageContext;

/**
 * scope that is used for multiple requests, attention scope can be used from multiple threads ad
 * same state, make no internal state!
 */
public interface SharedScope extends Scope {

	/**
	 * Initialize Scope only for this request, scope was already used
	 * 
	 * @param pc Page Context
	 */
	public void touchBeforeRequest(PageContext pc);

	/**
	 * release scope only for current request, scope will be used again
	 */
	public void touchAfterRequest(PageContext pc);
}