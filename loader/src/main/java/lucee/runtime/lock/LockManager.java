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
 * Manager to open and close locks
 */
public interface LockManager {

	/**
	 * Field <code>TYPE_READONLY</code>
	 */
	public static final int TYPE_READONLY = 0;

	/**
	 * Field <code>TYPE_EXCLUSIVE</code>
	 */
	public static final int TYPE_EXCLUSIVE = 1;

	/**
	 * locks a thread if already another thread is come until other thread notify him by unlock method
	 * 
	 * @param type
	 * @param name Lock Name (not case sensitive)
	 * @param timeout timeout to for waiting in this method, if timeout occurs "lockTimeoutException"
	 *            will be thrown
	 * @param pageContextId
	 * @return lock data object key for unlocking this lock
	 * @throws LockTimeoutException
	 * @throws InterruptedException
	 */
	public abstract LockData lock(int type, String name, int timeout, int pageContextId) throws LockTimeoutException, InterruptedException;

	/**
	 * unlocks a locked thread in lock method
	 * 
	 * @param data
	 */
	public abstract void unlock(LockData data);

	public String[] getOpenLockNames();

	public abstract void clean();

}