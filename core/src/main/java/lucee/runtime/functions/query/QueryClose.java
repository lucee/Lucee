package lucee.runtime.functions.query;

import java.sql.SQLException;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;
import lucee.runtime.type.query.SimpleQuery;

public final class QueryClose extends BIF {

	private static final long serialVersionUID = 6778838386679577852L;

	public static boolean call(PageContext pc, Query qry) throws PageException {
		if (!(qry instanceof SimpleQuery)) {
			throw new FunctionException(pc, "queryClose", 1, "query", "you can only close lazy queries.");
		}
		try {
			if (!qry.isClosed()) {
				qry.close();
			}
		}
		catch (SQLException e) {
			// safe to ignore
		}
		return true;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toQuery(args[0]));
		throw new FunctionException(pc, "QueryClose", 1, 1, args.length);
	}
}