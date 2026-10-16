package lucee.runtime.functions.thread;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;

/**
 * Checks whether the current thread has been interrupted and clears the interrupted status.
 * The interrupted status of the thread is cleared by this method.
 */
public final class ThreadInterrupted extends BIF {

	private static final long serialVersionUID = 1L;

	/**
	 * Checks if the current thread has been interrupted and clears the interrupted status.
	 *
	 * @param pc PageContext
	 * @return true if the current thread has been interrupted, false otherwise
	 */
	public static boolean call( PageContext pc ) {
		return Thread.interrupted();
	}

	@Override
	public Object invoke( PageContext pc, Object[] args ) throws PageException {
		if ( args.length == 0 ) {
			return call( pc );
		}
		throw new FunctionException( pc, "ThreadInterrupted", 0, 0, args.length );
	}
}
