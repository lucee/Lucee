package lucee.runtime.util.threading;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import lucee.commons.io.log.Log;

public class Closer {
	private CloserThread thread;
	private Log log;
	private final BlockingQueue<CloserJob> jobs = new LinkedBlockingQueue<>();

	public Closer(Log log) {
		this.log = log;
	}

	public void touch() {
		if (thread == null || !thread.isAlive()) {
			synchronized (this) {
				if (thread == null || !thread.isAlive()) {
					thread = new CloserThread(jobs, log);
					thread.start();
				}
			}
		}
	}

	public void add(CloserJob job) {
		jobs.add(job);
		touch();
	}

	private static class CloserThread extends Thread {
		private static final long IDLE_TIMEOUT = 10000;
		private static final long INTERVALL = 1000;
		private long lastMod;
		private Log log;
		private BlockingQueue<CloserJob> jobs;

		public CloserThread(BlockingQueue<CloserJob> jobs, Log log) {
			this.jobs = jobs;
			this.log = log;
		}

		@Override
		public void run() {
			while (true) {
				CloserJob job = jobs.poll();

				if (job != null) {
					if (log != null) log.debug("Closer", "executing job: " + job.getLablel());

					long now = System.currentTimeMillis();
					try {
						job.execute();
					}
					catch (Exception e) {
						if (log != null) log.error("Closer", e);
					}
					lastMod = now;
				}
				// nothing to do ATM
				else {
					long now = System.currentTimeMillis();
					if (lastMod + IDLE_TIMEOUT < now) {
						if (log != null) log.debug("Closer", "nothing to do, idle timeout reached, stoping observer ");
						break;
					}
					else if (log != null) log.debug("Closer", "nothing to do, remaining idle for another " + ((lastMod + IDLE_TIMEOUT) - now) + "ms");
				}
				if (log != null) log.debug("Closer", "sleep for " + INTERVALL + "ms");
				try {
					sleep(INTERVALL);
				}
				catch (InterruptedException e) {
					if (log != null) log.error("Closer", e);
				}
			}
		}
	}

}
