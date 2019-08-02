package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;

public final class QueryGetCellByIndex extends BIF {

	private static final long serialVersionUID = 2515614953776095300L;

	public static Object call(PageContext pc, Query query, String colName, String index) throws PageException {
		return query.getAt(KeyImpl.init(colName), QueryRowByIndex.getIndex(query, index));
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		throw new FunctionException(pc, "QueryGetCellByIndex", 3, 3, args.length);
	}
}