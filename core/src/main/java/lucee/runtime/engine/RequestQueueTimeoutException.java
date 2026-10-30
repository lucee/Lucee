package lucee.runtime.engine;

import java.io.IOException;

/**
 * Thrown when a request waited in the request queue for a free slot until the queue timeout was
 * reached. Extends IOException so existing callers of ThreadQueue.enter keep working.
 */
public final class RequestQueueTimeoutException extends IOException {

	private static final long serialVersionUID = 4263377396445813592L;

	private final long timeout;
	private final long waited;

	public RequestQueueTimeoutException(long waited, long timeout) {
		super("Concurrent request timeout (" + waited + ") [" + timeout
				+ " ms] has occurred, server is too busy handling other requests. This timeout setting can be changed in the server administrator.");
		this.waited = waited;
		this.timeout = timeout;
	}

	/**
	 * @return the effective queue timeout in milliseconds
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * @return the time in milliseconds the request waited in the queue
	 */
	public long getWaited() {
		return waited;
	}
}
