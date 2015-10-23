package lucee.runtime.functions.thread;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;

public class IsInThread extends BIF{

	@Override
	public Object invoke( PageContext pc, Object[] args ) throws PageException{
		// No arguments allowed
		if( args.length > 0 ){
			throw new FunctionException( pc, "isInThread", 0, 0, args.length );
		}
		return call( pc );
	}
	
	/**
	 * Verify if in thread or not
	 * @param pc
	 * @return 
	 * @throws PageException
	 */
	public static boolean call( PageContext pc ) throws PageException{
		return pc.getParentPageContext() != null;
	}

}
