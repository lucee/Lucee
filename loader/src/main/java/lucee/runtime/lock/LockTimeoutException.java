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
package lucee.runtime.lock;

/**
 * Lock Timeout
 */
public final class LockTimeoutException extends Exception {

	private static final long serialVersionUID = -2772267544602614500L;

	/**
	 * @param type type of the log
	 * @param name name of the Lock
	 * @param timeout
	 */
	public LockTimeoutException(final int type, final String name, final int timeout) {
		// A timeout occurred while attempting to lock lockname
		super("A timeout occurred on a [" + toString(type) + "] lock with name [" + name + "] after [" + getTime(timeout) + "]");
	}

	private static String getTime(final int timeout) {
		if (timeout / 1000 * 1000 == timeout) {
			final int s = timeout / 1000;
			return s + (s > 1 ? " seconds" : " second");
		}
		return timeout + (timeout > 1 ? " milliseconds" : " millisecond");
	}

	private static String toString(final int type) {
		if (LockManager.TYPE_EXCLUSIVE == type) return "exclusive";
		return "read-only";
	}

}