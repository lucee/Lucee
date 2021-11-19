package lucee.runtime.functions.thread;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;

public class IsInThread extends BIF {

	private static final long serialVersionUID = 9100222392353284434L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		// No arguments allowed
		if (args.length > 0) {
			throw new FunctionException(pc, "isInThread", 0, 0, args.length);
		}
		return call(pc);
	}

	/**
	 * Verify if in thread or not
	 * 
	 * @param pc
	 * @return
	 * @throws PageException
	 */
	public static boolean call(PageContext pc) throws PageException {
		PageContext root = ((PageContextImpl) pc).getRootPageContext();
		return root != null && root != pc;
	}

}