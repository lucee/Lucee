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
package lucee.commons.lock.rw;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lucee.commons.lock.LockException;
import lucee.commons.lock.LockInterruptedException;

public class RWLock<L> {

	private final ReentrantReadWriteLock rwl;
	private final Lock rl;
	private final Lock wl;

	private L label;
	private int count;

	public RWLock(L label) {
		rwl = new ReentrantReadWriteLock(true);
		rl = rwl.readLock();
		wl = rwl.writeLock();
		this.label = label;
	}

	public void lock(long timeout, boolean readOnly) throws LockException, LockInterruptedException {
		if (timeout <= 0) throw new LockException("timeout must be a positive number");
		try {
			if (!getLock(readOnly).tryLock(timeout, TimeUnit.MILLISECONDS)) {
				throw new LockException(timeout);
			}
		}
		catch (InterruptedException e) {
			throw new LockInterruptedException(e);
		}
	}

	void inc() {
		synchronized (rwl) {
			count++;
		}
	}

	void dec() {
		synchronized (rwl) {
			count--;
		}
	}

	public void unlock(boolean readOnly) {
		// print.e("unlock:"+readOnly);
		getLock(readOnly).unlock();
	}

	private java.util.concurrent.locks.Lock getLock(boolean readOnly) {
		return readOnly ? rl : wl;
	}

	/**
	 * Returns an estimate of the number of threads waiting to acquire this lock. The value is only an
	 * estimate because the number of threads may change dynamically while this method traverses
	 * internal data structures. This method is designed for use in monitoring of the system state, not
	 * for synchronization control.
	 *
	 * @return the estimated number of threads waiting for this lock
	 */
	public int getQueueLength() {
		synchronized (rwl) {
			return count;
		}
	}

	/**
	 * Queries if the write lock is held by any thread.
	 */
	public boolean isWriteLocked() {
		return rwl.isWriteLocked();
	}

	/**
	 * Queries if one or more write lock is held by any thread.
	 */
	public boolean isReadLocked() {
		return rwl.getReadLockCount() > 0;
	}

	public L getLabel() {
		return label;
	}
}