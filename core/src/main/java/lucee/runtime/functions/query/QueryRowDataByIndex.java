package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * implements BIF QueryRowData
 */
public class QueryRowDataByIndex extends BIF {

	private static final long serialVersionUID = -3492163362858443357L;

	public static Struct call(PageContext pc, Query query, String index) throws PageException {
		int row = QueryRowByIndex.getIndex(query, index);
		Collection.Key[] colNames = query.getColumnNames();

		Struct result = new StructImpl();

		for (int col = 0; col < colNames.length; col++)
			result.setEL(colNames[col], query.getAt(colNames[col], row, NullSupportHelper.empty(pc)));

		return result;
	}

	public static Struct call(PageContext pc, Query query, String index, Struct defaultValue) {
		int row = QueryRowByIndex.getIndex(query, index, -1);
		if (row == -1) return defaultValue;
		Collection.Key[] colNames = query.getColumnNames();

		Struct result = new StructImpl();

		for (int col = 0; col < colNames.length; col++)
			result.setEL(colNames[col], query.getAt(colNames[col], row, NullSupportHelper.empty(pc)));

		return result;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]));
		else if (args.length == 3) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toStruct(args[2]));
		throw new FunctionException(pc, "QueryRowDataByIndex", 2, 3, args.length);
	}
}