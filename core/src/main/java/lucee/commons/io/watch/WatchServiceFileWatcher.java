package lucee.commons.io.watch;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.Config;

/**
 * Event-based file watcher using Java NIO WatchService. Provides near-instant file change
 * detection on supported file systems.
 */
public class WatchServiceFileWatcher implements FileWatcher, Runnable {

	private final Map<Path, WatchKey> watchedDirectories = new ConcurrentHashMap<>();
	private final Map<Path, Set<PageSourceImpl>> directoryToPageSources = new ConcurrentHashMap<>();
	private final Config config;
	private final BooleanSupplier shouldContinue;
	private final Runnable onStop;
	private WatchService watchService;
	private Thread watchThread;
	private volatile boolean active = false;
	private final Object lock = new Object();
	private long lastCleanup = System.currentTimeMillis();
	private static final long CLEANUP_INTERVAL = 60000; // 1 minute

	public WatchServiceFileWatcher(Config config, BooleanSupplier shouldContinue, Runnable onStop) {
		this.config = config;
		this.shouldContinue = shouldContinue;
		this.onStop = onStop;
	}

	/**
	 * Registers a PageSource for watching. Registers the parent directory if not already watched.
	 * Only works for file-based resources that can be converted to java.nio.file.Path.
	 *
	 * @param ps the PageSource to watch
	 * @return true if successfully registered, false if not a file-based resource or failed
	 */
	public boolean register(PageSourceImpl ps) {
		Resource physicalFile = ps.getPhyscalFile();
		if (physicalFile == null || !physicalFile.exists()) {
			return false;
		}

		try {
			// Check if this is a file-based resource
			String scheme = physicalFile.getResourceProvider().getScheme();
			if (!"file".equalsIgnoreCase(scheme)) {
				// Not a file-based resource (could be zip, ram, http, etc.)
				return false;
			}

			// Try to get the file path
			String absolutePath = physicalFile.getAbsolutePath();
			if (absolutePath == null) {
				return false;
			}

			Path filePath = java.nio.file.Paths.get(absolutePath);
			Path directory = filePath.getParent();

			if (directory == null) {
				return false;
			}

			// Add to directory mapping
			directoryToPageSources.computeIfAbsent(directory, k -> ConcurrentHashMap.newKeySet()).add(ps);

			// Register directory if not already watched
			if (!watchedDirectories.containsKey(directory)) {
				synchronized (lock) {
					if (watchService != null && !watchedDirectories.containsKey(directory)) {
						WatchKey key = directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
						watchedDirectories.put(directory, key);
						if (LogUtil.doesDebug(config.getLog("application"))) {
							LogUtil.log(config, Log.LEVEL_DEBUG, "file-watcher", "Registered directory for watching: " + directory);
						}
					}
				}
			}

			return true;
		}
		catch (IOException e) {
			LogUtil.log(config, "file-watcher", e);
			return false;
		}
		catch (Exception e) {
			// Might not support Path conversion, or file system doesn't support watching
			return false;
		}
	}

	@Override
	public void start() {
		if (active) {
			return;
		}

		synchronized (lock) {
			if (active) {
				return;
			}

			try {
				watchService = FileSystems.getDefault().newWatchService();
				active = true;

				watchThread = new Thread(this, "Lucee-FileWatcher-WatchService");
				watchThread.setDaemon(true);
				watchThread.setPriority(Thread.MIN_PRIORITY);
				watchThread.start();

				LogUtil.log(config, Log.LEVEL_DEBUG, "file-watcher", "Started WatchService-based file watcher");
			}
			catch (IOException e) {
				LogUtil.log(config, Log.LEVEL_ERROR, "file-watcher", "Failed to start WatchService: " + e.getMessage());
				active = false;
				throw new RuntimeException("Failed to start WatchService", e);
			}
		}
	}

	@Override
	public void stop() {
		if (!active) {
			return;
		}

		synchronized (lock) {
			if (!active) {
				return;
			}

			active = false;

			// Cancel all watch keys
			for (WatchKey key: watchedDirectories.values()) {
				key.cancel();
			}
			watchedDirectories.clear();
			directoryToPageSources.clear();

			// Close watch service
			if (watchService != null) {
				try {
					watchService.close();
				}
				catch (IOException e) {
					LogUtil.log(config, "file-watcher", e);
				}
				watchService = null;
			}

			// Interrupt thread
			if (watchThread != null) {
				watchThread.interrupt();
				watchThread = null;
			}

			LogUtil.log(config, Log.LEVEL_DEBUG, "file-watcher", "Stopped WatchService-based file watcher");
		}
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public WatcherType getType() {
		return WatcherType.EVENT_BASED;
	}

	/**
	 * Removes PageSources that are no longer loaded from the watch lists.
	 * Also removes directories that have no more PageSources to watch.
	 */
	private void cleanupStaleReferences() {
		Iterator<Map.Entry<Path, Set<PageSourceImpl>>> dirIterator = directoryToPageSources.entrySet().iterator();
		while (dirIterator.hasNext()) {
			Map.Entry<Path, Set<PageSourceImpl>> entry = dirIterator.next();
			Set<PageSourceImpl> pageSources = entry.getValue();

			// Remove PageSources that are no longer loaded
			Iterator<PageSourceImpl> psIterator = pageSources.iterator();
			while (psIterator.hasNext()) {
				PageSourceImpl ps = psIterator.next();
				if (!ps.isLoad()) {
					psIterator.remove();
				}
			}

			// If no more PageSources in this directory, stop watching it
			if (pageSources.isEmpty()) {
				Path directory = entry.getKey();
				WatchKey key = watchedDirectories.remove(directory);
				if (key != null) {
					key.cancel();
				}
				dirIterator.remove();
			}
		}
	}

	@Override
	public void run() {
		while (active) {
			try {
				// Check if we should still be running (e.g., inspect mode changed)
				if (!shouldContinue.getAsBoolean()) {
					active = false;
					onStop.run();
					break;
				}

				// Periodic cleanup of stale references
				long now = System.currentTimeMillis();
				if (now - lastCleanup > CLEANUP_INTERVAL) {
					cleanupStaleReferences();
					lastCleanup = now;
				}

				// Wait for events (with timeout to check active flag)
				WatchKey key = watchService.poll(1, TimeUnit.SECONDS);

				if (key == null) {
					continue;
				}

				Path directory = (Path) key.watchable();

				for (WatchEvent<?> event: key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					// Skip overflow events
					if (kind == StandardWatchEventKinds.OVERFLOW) {
						continue;
					}

					// Get the filename from the event
					@SuppressWarnings("unchecked")
					WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
					Path filename = pathEvent.context();
					Path changedFile = directory.resolve(filename);

					// Find PageSources in this directory
					Set<PageSourceImpl> pageSources = directoryToPageSources.get(directory);
					if (pageSources != null) {
						for (PageSourceImpl ps: pageSources) {
							Resource physicalFile = ps.getPhyscalFile();
							if (physicalFile != null) {
								try {
									String absolutePath = physicalFile.getAbsolutePath();
									if (absolutePath != null) {
										Path psPath = java.nio.file.Paths.get(absolutePath);
										if (psPath.equals(changedFile)) {
											if (LogUtil.doesDebug(config.getLog("application"))) {
												LogUtil.log(config, Log.LEVEL_DEBUG, "file-watcher", "File changed: " + changedFile + " (event: " + kind.name() + ")");
											}
											// Flush the page from cache
											ps.flush();
										}
									}
								}
								catch (Exception e) {
									// Ignore
								}
							}
						}
					}
				}

				// Reset the key
				boolean valid = key.reset();
				if (!valid) {
					// Directory no longer accessible, remove it
					watchedDirectories.remove(directory);
					directoryToPageSources.remove(directory);
				}
			}
			catch (InterruptedException e) {
				// Expected when stopping
				break;
			}
			catch (ClosedWatchServiceException e) {
				// Expected when stopping
				break;
			}
			catch (Exception e) {
				LogUtil.log(config, "file-watcher", e);
			}
		}
	}
}
