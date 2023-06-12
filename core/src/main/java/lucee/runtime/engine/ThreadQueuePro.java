package lucee.runtime.engine;

//FUTURE add to loader
public interface ThreadQueuePro extends ThreadQueue {

	public short MODE_UNDEFINED = 0;

	/**
	 * Thread queue is disabled, so all thread pass through
	 */
	public short MODE_DISABLED = 1;

	/**
	 * thread queue is enabled
	 */
	public short MODE_ENABLED = 2;

	/**
	 * thread queue is in blocking mode, so no thread is passing until that mode is left
	 */
	public short MODE_BLOCKING = 4;

	/**
	 * set the mode for the queue
	 * 
	 * @return previous mode value
	 */
	public short setMode(short mode);

	/**
	 * 
	 * @return returns the current mode of the queue
	 */
	public short getMode();
}
