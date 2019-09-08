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
package lucee.commons.lock;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.lock.LockManager;

/**
 * Lock Timeout
 */
public final class LockException extends ApplicationException {

	private static final long serialVersionUID = 9132132031478280069L;

	/**
	 * @param type type of the log
	 * @param name name of the Lock
	 * @param timeout
	 */
	public LockException(int type, String name, long timeout) {
		// A timeout occurred while attempting to lock lockname
		super("a timeout occurred on a " + toString(type) + " lock with name [" + name + "] after " + (timeout / 1000) + " seconds");
	}

	public LockException(Long timeout) {

		super("a timeout occurred after " + toTime(timeout));
	}

	public LockException(String text) {
		super(text);
	}

	private static String toTime(long timeout) {

		if (timeout >= 1000 && (((timeout / 1000)) * 1000) == timeout) return (timeout / 1000) + " seconds";
		return timeout + " milliseconds";
	}

	private static String toString(int type) {
		if (LockManager.TYPE_EXCLUSIVE == type) return "exclusive";
		return "read-only";
	}

}