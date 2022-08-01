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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleLock<L> implements Lock {

	private ReentrantLock lock;
	private L label;

	public SimpleLock(L label) {
		this.lock = new ReentrantLock(true);
		this.label = label;
	}

	@Override
	public void lock(long timeout) throws LockException {
		if (timeout <= 0) throw new LockException("Timeout must be a positive number");
		long initialTimeout = timeout;
		long start = System.currentTimeMillis();
		do {
			try {
				if (!lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
					throw new LockException(initialTimeout);
				}
				break; // exit loop
			}
			catch (InterruptedException e) {
				timeout -= System.currentTimeMillis() - start;
			}
			if (timeout <= 0) {
				// Lucee was not able to aquire lock in time
				throw new LockException(initialTimeout);
			}
		}
		while (true);

	}

	@Override
	public void unlock() {
		lock.unlock();
	}

	@Override
	public int getQueueLength() {
		return lock.getQueueLength();
	}

	public L getLabel() {
		return label;
	}
}