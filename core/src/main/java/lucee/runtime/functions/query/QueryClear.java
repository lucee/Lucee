package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;

public class QueryClear extends BIF implements Function {

	private static final long serialVersionUID = 3755794610970965992L;

	public static Query call(PageContext pc, Query qry) throws PageException {
		qry.clear();
		return qry;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]));

		throw new FunctionException(pc, "QueryClear", 1, 1, args.length);
	}

}
