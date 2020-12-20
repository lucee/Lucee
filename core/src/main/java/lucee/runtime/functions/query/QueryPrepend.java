package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;

public class QueryPrepend extends BIF implements Function {

	private static final long serialVersionUID = -5241509284480974613L;

	public static Query call(PageContext pc, Query qry1, Query qry2) throws PageException {
		// compare column names
		Key[] cn1 = qry1.getColumnNames();
		Key[] cn2 = qry2.getColumnNames();
		QueryAppend.validate(qry1, qry2, cn1, cn2);

		int rowCount1 = qry1.getRowCount();
		int rowCount2 = qry2.getRowCount();
		if (rowCount2 == 0) return qry1;

		qry1.addRow(rowCount2);
		for (int row = rowCount1; row > 0; row--) {
			for (Key k: cn2) {
				qry1.setAt(k, rowCount2 + row, qry1.getAt(k, row));
			}
		}

		for (int row = rowCount2; row > 0; row--) {
			for (Key k: cn2) {
				qry1.setAt(k, row, qry2.getAt(k, row));
			}
		}
		return qry1;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toQuery(args[1]));

		throw new FunctionException(pc, "QueryPrepend", 2, 2, args.length);
	}

}