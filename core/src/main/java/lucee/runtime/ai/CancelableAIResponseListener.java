package lucee.runtime.ai;

/**
 * An {@link AIResponseListener} that can stop the inquiry it is attached to. After every chunk the
 * streaming loop of an engine asks {@link #isCancelled()}; when it returns true the engine stops
 * reading, closes the connection and returns what was received so far.
 * <p>
 * This is an extension of the listener contract on the core side, a plain {@link AIResponseListener}
 * never cancels.
 */
public interface CancelableAIResponseListener extends AIResponseListener {

	/**
	 * @return true when the listener asked to cancel the inquiry. Once true it stays true.
	 */
	public boolean isCancelled();
}
