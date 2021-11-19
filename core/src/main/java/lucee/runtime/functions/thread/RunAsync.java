package lucee.runtime.functions.thread;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.future.Future;
import lucee.runtime.op.Caster;

public class RunAsync extends BIF {

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, args[0], Caster.toDoubleValue(args[1]));
		else if (args.length == 1) return call(pc, args[0], 0);
		else throw new FunctionException(pc, "RunAsync", 1, 2, args.length);
	}

	/**
	 * Verify if in thread or not
	 * 
	 * @param pc
	 * @return
	 * @throws PageException
	 */
	public static Object call(PageContext pc, Object udf, double timeout) throws PageException {
		return Future._then(pc, Caster.toFunction(udf), (long) timeout);
	}

}
