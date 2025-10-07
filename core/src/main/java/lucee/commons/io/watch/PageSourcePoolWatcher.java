package lucee.commons.io.watch;

import java.lang.ref.SoftReference;
import java.util.Map;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.SerializableObject;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.PageSourcePool;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.op.Caster;

public final class PageSourcePoolWatcher {

	private final Map<String, SoftReference<PageSource>> pageSources;
	private FileWatcher fileWatcher;
	private PageSourcePoolWatcherThread thread;
	private final MappingImpl mapping;
	private PageSourcePool pageSourcePool;
	private SerializableObject token = new SerializableObject();
	private boolean useEventBasedWatching;

	public PageSourcePoolWatcher(MappingImpl mapping, PageSourcePool pageSourcePool, Map<String, SoftReference<PageSource>> pageSources) {
		this.mapping = mapping;
		this.pageSourcePool = pageSourcePool;
		this.pageSources = pageSources;
		this.useEventBasedWatching = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.inspect.auto.useEvents", null), true);
	}

	public void startIfNecessary() {
		synchronized (token) {
			// Try event-based watching first
			if (useEventBasedWatching && fileWatcher == null) {
				try {
					// Check if inspect mode is still AUTO
					java.util.function.BooleanSupplier shouldContinue = () -> mapping.getInspectTemplate() == ConfigPro.INSPECT_AUTO;
					// Callback when watcher stops itself
					Runnable onStop = () -> pageSourcePool.stopWatcher();

					WatchServiceFileWatcher wsfw = new WatchServiceFileWatcher(mapping.getConfig(), shouldContinue, onStop);

					// Register all currently loaded pages
					for (SoftReference<PageSource> ref: pageSources.values()) {
						PageSourceImpl ps = (PageSourceImpl) ref.get();
						if (ps != null && ps.isLoad()) {
							wsfw.register(ps);
						}
					}

					wsfw.start();
					fileWatcher = wsfw;
					LogUtil.log(mapping.getConfig(), Log.LEVEL_DEBUG, "page-source-pool", "Using event-based file watching for auto inspect mode");
					return;
				}
				catch (Exception e) {
					LogUtil.log(mapping.getConfig(), Log.LEVEL_WARN, "page-source-pool", "Failed to start event-based watching, falling back to polling: " + e.getMessage());
					fileWatcher = null;
				}
			}

			// Fall back to polling
			if (thread == null || !thread.isAlive()) {
				if (thread == null || !thread.isAlive()) {
					thread = new PageSourcePoolWatcherThread();
					thread.setPriority(Thread.MIN_PRIORITY);
					thread.setName("PageSourcePoolWatcher");
					thread.start();
					LogUtil.log(mapping.getConfig(), Log.LEVEL_DEBUG, "page-source-pool", "Using polling-based file watching for auto inspect mode");
				}
			}
		}
	}

	public void stopIfNecessary() {
		synchronized (token) {
			// Stop event-based watcher
			if (fileWatcher != null) {
				fileWatcher.stop();
				fileWatcher = null;
			}

			// Stop polling thread
			if (thread != null) {
				thread.active(false);
				thread = null;
			}
		}
	}

	/**
	 * Register a newly loaded page source with the watcher (for event-based watching)
	 */
	public void registerPageSource(PageSourceImpl ps) {
		if (fileWatcher != null && fileWatcher instanceof WatchServiceFileWatcher) {
			((WatchServiceFileWatcher) fileWatcher).register(ps);
		}
	}

	private class PageSourcePoolWatcherThread extends Thread {

		private static final int INCREASE_FROM_FAST_TO_LOW = 5;
		private boolean active = true;

		public PageSourcePoolWatcherThread() {
		}

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

				SystemUtil.sleep(interval);
				if (interval < mapping.getInspectTemplateAutoInterval(true)) interval += INCREASE_FROM_FAST_TO_LOW;
			}
		}
	}
}
