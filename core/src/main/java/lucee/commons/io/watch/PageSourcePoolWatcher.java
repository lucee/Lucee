package lucee.commons.io.watch;

import java.lang.ref.SoftReference;
import java.util.Map;

import lucee.print;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.type.dt.DateTimeImpl;

public class PageSourcePoolWatcher {

	private final Map<String, SoftReference<PageSource>> pageSources;
	private PageSourcePoolWatcherThread thread;
	private final MappingImpl mapping;

	public PageSourcePoolWatcher(MappingImpl mapping, Map<String, SoftReference<PageSource>> pageSources) {
		this.mapping = mapping;
		this.pageSources = pageSources;
	}

	public void startIfNecessary() {
		if (thread == null || !thread.isAlive()) {
			thread = new PageSourcePoolWatcherThread();
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
	}

	public void stopIfNecessary() {
		if (thread != null) {
			thread.active(false);
			thread = null;
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
				for (SoftReference<PageSource> ref: pageSources.values()) {
					try {
						PageSourceImpl ps = (PageSourceImpl) ref.get();
						if (ps == null) continue;

						boolean show = ps.getDisplayPath().endsWith("test6.cfm");
						if (show) print.e("------ " + mapping.getVirtual() + " ------ " + new DateTimeImpl());
						if (show) print.e("- interval: " + mapping.getInspectTemplateAutoInterval(true) + ":" + mapping.getInspectTemplateAutoInterval(false) + ":" + interval);
						if (show) print.e("- load? " + ps.isLoad());
						if (ps.isLoad()) {
							boolean res = ps.releaseWhenOutdatted(show);
							if (show) print.e("- released? " + res);
							if (res) {
								interval = mapping.getInspectTemplateAutoInterval(false);
								print.e("- " + mapping.getVirtual() + " : " + ps.getDisplayPath());
							}
							// ps.releaseWhenOutdatted();
						}
					}
					catch (Exception e) {
						LogUtil.log(mapping.getConfig(), "pagesource-pool", e);
					}
				}

				SystemUtil.sleep(interval);
				if (interval < mapping.getInspectTemplateAutoInterval(true)) interval += INCREASE_FROM_FAST_TO_LOW;
				/*
				 * if (doFast > 0) { doFast--; print.e("- check fast " + doFast + " : " +
				 * mapping.getInspectTemplateAutoInterval(false));
				 * SystemUtil.sleep(mapping.getInspectTemplateAutoInterval(false)); } else {
				 * SystemUtil.sleep(mapping.getInspectTemplateAutoInterval(true)); }
				 */
			}
		}
	}
}
