package lucee.runtime.functions.thread;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.tag.ThreadTag;
import lucee.runtime.thread.ChildThread;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.scope.Threads;

/**
 * Checks whether a specified thread has been interrupted.
 * The interrupted status of the thread is unaffected by this method.
 */
public final class IsThreadInterrupted extends BIF {

	private static final long serialVersionUID = 1L;

	/**
	 * Checks if the specified thread has been interrupted.
	 *
	 * @param pc PageContext
	 * @param threadName The name of the thread to check
	 * @return true if the thread has been interrupted, false otherwise
	 * @throws PageException if the thread does not exist
	 */
	public static boolean call( PageContext pc, String threadName ) throws PageException {
		Threads ts = ThreadTag.getThreadScope( pc, KeyImpl.init( threadName ) );
		if ( ts == null ) {
			throw new FunctionException( pc, "IsThreadInterrupted", 1, "threadName",
					"There is no thread running with the name [" + threadName + "]" );
		}
		ChildThread ct = ts.getChildThread();
		return ct.isInterrupted();
	}

	@Override
	public Object invoke( PageContext pc, Object[] args ) throws PageException {
		if ( args.length == 1 ) {
			return call( pc, Caster.toString( args[0] ) );
		}
		throw new FunctionException( pc, "IsThreadInterrupted", 1, 1, args.length );
	}
}
