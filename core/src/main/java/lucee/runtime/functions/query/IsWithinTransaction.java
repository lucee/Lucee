package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;

public class IsWithinTransaction extends BIF {

	private static final long serialVersionUID = 7490842489165167839L;

	public static boolean call(PageContext pc) {
		return !pc.getDataSourceManager().isAutoCommit();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		else throw new FunctionException(pc, "IsWithinTransaction", 0, 0, args.length);
	}

}