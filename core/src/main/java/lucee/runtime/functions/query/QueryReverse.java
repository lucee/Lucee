package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;

public class QueryReverse extends BIF implements Function {

	private static final long serialVersionUID = -91336674628990980L;

	public static Query call(PageContext pc, Query qry) throws PageException {

		Key[] names = qry.getColumnNames();
		QueryImpl rq = new QueryImpl(names, qry.getRecordcount(), qry.getName());

		int newRow = 0;
		for (int row = qry.getRecordcount(); row > 0; row--) {
			newRow++;
			for (Key name: names) {
				rq.setAt(name, newRow, qry.getAt(name, row));
			}
		}
		return rq;

	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toQuery(args[0]));
		throw new FunctionException(pc, "QueryReverse", 1, 1, args.length);
	}
}
