package lucee.commons.io.watch;

/**
 * Interface for file watching implementations. Provides abstraction over different file watching
 * mechanisms (event-based via WatchService or polling-based fallback).
 */
public interface FileWatcher {

	/**
	 * Starts the file watcher if not already running.
	 */
	void start();

	/**
	 * Stops the file watcher if running.
	 */
	void stop();

	/**
	 * Checks if the watcher is currently active.
	 *
	 * @return true if the watcher is running
	 */
	boolean isActive();

	/**
	 * Returns the type of watcher implementation.
	 *
	 * @return watcher type (EVENT_BASED or POLLING)
	 */
	WatcherType getType();

	public enum WatcherType {
		EVENT_BASED, POLLING
	}
}
