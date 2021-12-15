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
package coldfusion.runtime;

import lucee.runtime.engine.ThreadLocalPageContext;

/**
 * this is just a wrapper class to simulate the ACF implementation
 */
public class RequestMonitor {
	// public void beginRequestMonitor(String str){/* ignored */ }
	// public void endRequestMonitor(){/* ignored */ }
	// public void checkSlowRequest(Object obj){/* ignored */ }
	// public boolean isRequestTimedOut()
	public long getRequestTimeout() {
		return ThreadLocalPageContext.get().getRequestTimeout() / 1000;
	}

	public void overrideRequestTimeout(long timeout) {
		ThreadLocalPageContext.get().setRequestTimeout(timeout * 1000);
	}

}