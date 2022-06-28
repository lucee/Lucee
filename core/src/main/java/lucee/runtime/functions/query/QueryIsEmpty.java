package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;

/**
 * Implements the CFML Function querynew
 */
public final class QueryIsEmpty extends BIF {

	public static boolean call(PageContext pc, Query qry) {
		return qry.getRowCount() == 0;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toQuery(args[0]));
		else throw new FunctionException(pc, "QueryIsEmpty", 1, 1, args.length);
	}
}