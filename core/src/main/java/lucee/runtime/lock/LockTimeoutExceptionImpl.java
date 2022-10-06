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

import lucee.commons.lang.StringUtil;

/**
 * Lock Timeout // FUTURE replace LockTimeoutException with this implementation
 */
public final class LockTimeoutExceptionImpl extends Exception {

	private int type;
	private String name;
	private int timeout;
	private Boolean readLocked;
	private Boolean writeLocked;

	/**
	 * @param type type of the log
	 * @param name name of the Lock
	 * @param timeout
	 * @param readLocked
	 * @param writeLocked
	 */
	public LockTimeoutExceptionImpl(int type, String name, int timeout, Boolean readLocked, Boolean writeLocked) {
		this.type = type;
		this.name = name;
		this.timeout = timeout;
		this.readLocked = readLocked;
		this.writeLocked = writeLocked;
	}

	public static String createMessage(int type, String name, String scopeName, int timeout, Boolean readLocked, Boolean writeLocked) {
		// if(LockManager.TYPE_EXCLUSIVE==type && readLocked==Boolean.TRUE && writeLocked==Boolean.FALSE)

		StringBuilder sb = new StringBuilder().append("A timeout occurred after [").append(getTime(timeout)).append("], trying to acquire a [").append(toString(type) + "] lock.");

		if (StringUtil.isEmpty(scopeName)) {
			sb.append(" lock with name [").append(name).append("]");
		}
		else {
			sb.append(" [").append(scopeName).append("] scope lock");
		}

		if (readLocked == Boolean.TRUE && writeLocked == Boolean.FALSE) {
			sb.append(" on an existing read lock.");
			if (LockManager.TYPE_EXCLUSIVE == type) sb.append(" You cannot upgrade an existing lock from \"read\" to \"exclusive\".");
		}
		else sb.append(".");

		return sb.toString();
	}

	private static String getTime(int timeout) {
		if (timeout / 1000 * 1000 == timeout) {
			int s = timeout / 1000;
			return s + (s > 1 ? " seconds" : " second");
		}
		return timeout + (timeout > 1 ? " milliseconds" : " millisecond");
	}

	private static String toString(int type) {
		if (LockManager.TYPE_EXCLUSIVE == type) return "exclusive";
		return "read-only";
	}

	public String getMessage(String scopeName) {
		return createMessage(type, name, scopeName, timeout, readLocked, writeLocked);
	}

	@Override
	public String getMessage() {
		return createMessage(type, name, null, timeout, readLocked, writeLocked);
	}

}