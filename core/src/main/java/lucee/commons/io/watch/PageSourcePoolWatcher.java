package lucee.commons.io.watch;

import java.lang.ref.SoftReference;
import java.util.Map;

import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.SerializableObject;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.PageSourcePool;
import lucee.runtime.config.ConfigPro;

public final class PageSourcePoolWatcher {

	private final Map<String, SoftReference<PageSource>> pageSources;
	private PageSourcePoolWatcherThread thread;
	private final MappingImpl mapping;
	private PageSourcePool pageSourcePool;
	private SerializableObject token = new SerializableObject();

	public PageSourcePoolWatcher(MappingImpl mapping, PageSourcePool pageSourcePool, Map<String, SoftReference<PageSource>> pageSources) {
		this.mapping = mapping;
		this.pageSourcePool = pageSourcePool;
		this.pageSources = pageSources;
	}

	public void startIfNecessary() {
		if (thread == null || !thread.isAlive()) {
			synchronized (token) {
				if (thread == null || !thread.isAlive()) {
					if (thread != null) {
						thread.active(false);
					}
					thread = new PageSourcePoolWatcherThread();
					thread.setDaemon(true);
					thread.setPriority(Thread.MIN_PRIORITY);
					thread.setName("PageSourcePoolWatcher");
					thread.start();
				}
			}

		}
	}

	public void stopIfNecessary() {
		if (thread != null) {
			synchronized (token) {
				if (thread != null) {
					thread.active(false);

					// Wait for thread to actually exit
					try {
						thread.interrupt(); // Wake it from sleep
						thread.join(500); // Wait max 5 seconds
					}
					catch (InterruptedException e) {
						// Handle
					}
					thread = null;
				}
			}

		}
	}

	private class PageSourcePoolWatcherThread extends Thread {

		private static final int INCREASE_FROM_FAST_TO_LOW = 5;
		private boolean active = true;

		public PageSourcePoolWatcherThread() {}

		public void active(boolean active) {
			this.active = active;
		}

		@Override
		public void run() {
			int interval = mapping.getInspectTemplateAutoInterval(true);
			while (active) {

				if (mapping.getInspectTemplate() != ConfigPro.INSPECT_AUTO) {
					active = false;
					pageSourcePool.stopWatcher();
					break;
				}

				for (SoftReference<PageSource> ref: pageSources.values()) {
					if (!active) break;
					try {
						PageSourceImpl ps = (PageSourceImpl) ref.get();
						if (ps == null) continue;

						if (ps.isLoad()) {
							boolean res = ps.releaseWhenOutdatted();
							if (res) {
								interval = mapping.getInspectTemplateAutoInterval(false);
							}
						}
					}
					catch (Exception e) {
						LogUtil.log(mapping.getConfig(), "pagesource-pool", e);
					}
				}
				if (!active) break;
				try {
					Thread.sleep(interval);
				}
				catch (InterruptedException e) {
					break; // Exit immediately on interrupt
				}
				if (interval < mapping.getInspectTemplateAutoInterval(true)) interval += INCREASE_FROM_FAST_TO_LOW;
			}
		}
	}

}
