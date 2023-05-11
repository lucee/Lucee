package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ListUtil;

public class QueryInsertAt extends BIF implements Function {

	private static final long serialVersionUID = -2549767593942513005L;

	public static Query call(PageContext pc, Query qry, Object value, double index) throws PageException {
		if (index < 1) throw new FunctionException(pc, "QueryInsertAt", 3, "index", "index most be at least one, now it is [" + Caster.toString(index) + "].");
		if (index - 1 > qry.getRowCount()) throw new FunctionException(pc, "QueryInsertAt", 3, "index",
				"index [" + Caster.toString(index) + "] cannot be bigger than recordcount [" + qry.getRecordcount() + "] of the query plus 1.");
		int off = (int) (index - 1);

		// QUERY
		if (Decision.isQuery(value)) {
			Query qry2 = (Query) value;
			Key[] cn1 = qry.getColumnNames();
			Key[] cn2 = qry2.getColumnNames();
			QueryAppend.validate(qry, cn1, cn2);

			int rowCount2 = qry2.getRowCount();
			if (rowCount2 == 0) return qry;

			QueryPrepend.makeSpace(qry, rowCount2, off);
			for (int row = rowCount2; row > 0; row--) {
				for (Key k: cn2) {
					qry.setAt(k, row + off, qry2.getAt(k, row));
				}
			}
		}
		// STRUCT
		else if (Decision.isStruct(value)) {
			Struct sct = (Struct) value;
			Key[] cn1 = qry.getColumnNames();
			Key[] cn2 = sct.keys();
			
			if (cn1.length != cn2.length) {
				throw new ApplicationException("query column count [" + cn1.length + "] and struct size [" + cn2.length + "] are not same");
			}

			for (Key k: cn2) {
				if (qry.getColumn(k, null) == null) {
					throw new ApplicationException("column names [" + ListUtil.arrayToList(cn1, ", ") + "] of the query does not match the keys [" 
						+ ListUtil.arrayToList(cn2, ", ") + "] of the struct");
				}
			}

			QueryPrepend.makeSpace(qry, 1, off);
			for (int row = 1; row > 0; row--) {
				for (Key k: cn2) {
					qry.setAt(k, row + off, sct.get(k));
				}
			}
		}
		// ARRAY
		else if (Decision.isArray(value)) {
			Array arr = (Array) value;
			Key[] cn1 = qry.getColumnNames();

			if (cn1.length != arr.size()) {
				throw new ApplicationException("there is not the same amount of records in the array [" + arr.size() + "] as there are columns in the query [" + cn1.length + "].");
			}

			QueryPrepend.makeSpace(qry, 1, off);
			for (int row = 1; row > 0; row--) {
				for (int col = 0; col < cn1.length; col++) {
					qry.setAt(cn1[col], row + off, arr.getE(col + 1));
				}
			}
		}
		return qry;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toQuery(args[0]), args[1], Caster.toDoubleValue(args[2]));

		throw new FunctionException(pc, "QueryInsertAt", 3, 3, args.length);
	}

}