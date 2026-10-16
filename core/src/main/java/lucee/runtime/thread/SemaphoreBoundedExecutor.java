package lucee.runtime.thread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Wraps an ExecutorService with a Semaphore that caps the number of concurrently-running tasks
 * to {@code permits}. submit/execute block until a permit is available. Designed for
 * {@code Executors.newVirtualThreadPerTaskExecutor()}, which has no built-in parallelism cap.
 */
public final class SemaphoreBoundedExecutor implements ExecutorService {

	private final ExecutorService inner;
	private final Semaphore semaphore;

	public SemaphoreBoundedExecutor(ExecutorService inner, int permits) {
		if (permits < 1) throw new IllegalArgumentException("permits must be >= 1");
		this.inner = inner;
		this.semaphore = new Semaphore(permits);
	}

	private void acquireOrReject() {
		try {
			semaphore.acquire();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RejectedExecutionException("interrupted while acquiring semaphore permit", e);
		}
	}

	private Runnable wrap(Runnable r) {
		return () -> {
			try {
				r.run();
			}
			finally {
				semaphore.release();
			}
		};
	}

	private <T> Callable<T> wrap(Callable<T> c) {
		return () -> {
			try {
				return c.call();
			}
			finally {
				semaphore.release();
			}
		};
	}

	@Override
	public void execute(Runnable command) {
		acquireOrReject();
		try {
			inner.execute(wrap(command));
		}
		catch (RejectedExecutionException e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	public Future<?> submit(Runnable task) {
		acquireOrReject();
		try {
			return inner.submit(wrap(task));
		}
		catch (RejectedExecutionException e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		acquireOrReject();
		try {
			return inner.submit(wrap(task), result);
		}
		catch (RejectedExecutionException e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		acquireOrReject();
		try {
			return inner.submit(wrap(task));
		}
		catch (RejectedExecutionException e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	public void shutdown() {
		inner.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return inner.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return inner.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return inner.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return inner.awaitTermination(timeout, unit);
	}

	// invokeAll routed via submit() so each task acquires a permit — would otherwise bypass the cap.
	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		if (tasks == null) throw new NullPointerException();
		List<Future<T>> futures = new ArrayList<>(tasks.size());
		boolean done = false;
		try {
			for (Callable<T> task: tasks) {
				futures.add(submit(task));
			}
			for (Future<T> f: futures) {
				if (!f.isDone()) {
					try {
						f.get();
					}
					catch (ExecutionException ignore) {}
					catch (CancellationException ignore) {}
				}
			}
			done = true;
			return futures;
		}
		finally {
			if (!done) {
				for (Future<T> f: futures)
					f.cancel(true);
			}
		}
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		// timeout variant — same shape, simplified: no caller uses it currently
		return invokeAll(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException("invokeAny is not supported by SemaphoreBoundedExecutor");
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException("invokeAny is not supported by SemaphoreBoundedExecutor");
	}
}
