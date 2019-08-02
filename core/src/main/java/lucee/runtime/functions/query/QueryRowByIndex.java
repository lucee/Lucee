package lucee.runtime.functions.query;

import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
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

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]));
	}

	public static int getIndex(Query query, String index) throws ApplicationException {
		Map<Key, Integer> indexes = ((QueryImpl) query).getIndexes();
		if (indexes == null) throw new ApplicationException("the query does not have no index table defined [" + index + "]");
		Integer indx = indexes.get(KeyImpl.getInstance(index));
		if (indx == null) throw new ApplicationException("there is no index with the value [" + index + "]");
		return indx;

	}
}