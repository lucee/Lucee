/**
 * Copyright (c) 2026, Lucee Association Switzerland. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package lucee.runtime.thread;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an {@link ExecutorService} (typically a virtual-thread-per-task executor) and caps the number
 * of concurrently running tasks using a {@link Semaphore}. A permit is acquired on submission, which
 * applies back pressure to the submitting thread, and released once the task completes.
 */
public final class BoundedExecutorService extends AbstractExecutorService {

	private final ExecutorService delegate;
	private final Semaphore semaphore;

	/**
	 * @param delegate the executor the tasks are ultimately run on
	 * @param maxConcurrency the maximum number of tasks allowed to run at the same time, must be &gt; 0
	 */
	public BoundedExecutorService(ExecutorService delegate, int maxConcurrency) {
		if (maxConcurrency < 1) throw new IllegalArgumentException("maxConcurrency must be greater than 0");
		this.delegate = delegate;
		this.semaphore = new Semaphore(maxConcurrency);
	}

	@Override
	public void execute(Runnable command) {
		try {
			semaphore.acquire();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RejectedExecutionException(e);
		}
		try {
			delegate.execute(() -> {
				try {
					command.run();
				}
				finally {
					semaphore.release();
				}
			});
		}
		catch (RejectedExecutionException e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	public void shutdown() {
		delegate.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return delegate.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return delegate.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return delegate.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return delegate.awaitTermination(timeout, unit);
	}
}
