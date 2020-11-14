package lucee.runtime.functions.query;

import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;

/**
 * implements BIF QueryRowData
 */
public class QueryRowByIndex extends BIF {

	private static final long serialVersionUID = -1462555083727605910L;

	public static double call(PageContext pc, Query query, String index) throws PageException {
		return Caster.toDoubleValue(getIndex(query, index));
	}

	public static double call(PageContext pc, Query query, String index, double defaultValue) {
		return Caster.toDoubleValue(getIndex(query, index, (int) defaultValue));
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]));
		else if (args.length == 3) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]));
		throw new FunctionException(pc, "QueryRowByIndex", 2, 3, args.length);
	}

	public static int getIndex(Query query, String index) throws ApplicationException {
		Map<Key, Integer> indexes = ((QueryImpl) query).getIndexes();
		if (indexes == null) throw new ApplicationException("Query is not indexed, index [" + index + "] not found");
		Integer indx = indexes.get(KeyImpl.getInstance(index));
		if (indx == null) throw new ApplicationException("Query does not have an index for the column [" + index + "]");
		return indx;
	}

	public static int getIndex(Query query, String index, int defaultValue) {
		Map<Key, Integer> indexes = ((QueryImpl) query).getIndexes();
		if (indexes == null) return defaultValue;
		Integer indx = indexes.get(KeyImpl.getInstance(index));
		if (indx == null) return defaultValue;
		return indx;
	}
}
