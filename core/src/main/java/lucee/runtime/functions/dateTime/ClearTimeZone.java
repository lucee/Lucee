package lucee.runtime.functions.dateTime;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;

public class ClearTimeZone extends BIF implements Function {

	private static final long serialVersionUID = 2953112893625358220L;

	public static String call(PageContext pc) {
		((PageContextImpl) pc).clearTimeZone();
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 0) throw new FunctionException(pc, "ClearTimeZone", 0, 0, args.length);
		return call(pc);
	}

}