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
	public static final Object DF = new Object();

	public static Object call(PageContext pc, Query query, String colName, String index) throws PageException {
		return query.getAt(KeyImpl.init(colName), QueryRowByIndex.getIndex(query, index));
	}

	public static Object call(PageContext pc, Query query, String colName, String index, Object defaultValue) {
		int indx = QueryRowByIndex.getIndex(query, index, -1);
		if (indx == -1) return defaultValue;
		Object res = query.getAt(KeyImpl.init(colName), indx, DF);
		if (res == DF) return defaultValue;
		return res;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		else if (args.length == 4) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), args[3]);
		throw new FunctionException(pc, "QueryGetCellByIndex", 3, 4, args.length);
	}
}