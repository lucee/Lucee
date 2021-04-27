package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.util.ListUtil;

public class QueryAppend extends BIF implements Function {

	private static final long serialVersionUID = 5814257234774888827L;

	public static Query call(PageContext pc, Query qry1, Query qry2) throws PageException {
		// compare column names
		Key[] cn1 = qry1.getColumnNames();
		Key[] cn2 = qry2.getColumnNames();
		validate(qry1, cn1, cn2);

		int rowCount1 = qry1.getRowCount();
		int rowCount2 = qry2.getRowCount();
		if (rowCount2 == 0) return qry1;

		qry1.addRow(rowCount2);
		for (int row = 1; row <= rowCount2; row++) {
			for (Key k: cn2) {
				qry1.setAt(k, rowCount1 + row, qry2.getAt(k, row));
			}
		}
		return qry1;
	}

	static void validate(Query qry1, Key[] cn1, Key[] cn2) throws ApplicationException {
		boolean validColumnNames = cn1.length == cn2.length;
		if (validColumnNames) {
			for (Key k: cn2) {
				if (qry1.getColumn(k, null) == null) {
					validColumnNames = false;
					break;
				}
			}
		}

		if (!validColumnNames) {
			throw new ApplicationException("column names [" + ListUtil.arrayToList(cn1, ", ") + "] of the first query does not match the column names ["
					+ ListUtil.arrayToList(cn2, ", ") + "] of of the second query");
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toQuery(args[1]));

		throw new FunctionException(pc, "QueryAppend", 2, 2, args.length);
	}

}
