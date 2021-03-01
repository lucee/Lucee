package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class QueryRowSwap extends BIF implements Function {

	private static final long serialVersionUID = -812740090032092109L;

	public static Query call(PageContext pc, Query qry, double source, double destination) throws PageException {
		// validate source
		if (source < 1) throw new FunctionException(pc, "QueryRowSwap", 2, "source", "source most be at least one, now it is [" + Caster.toString(source) + "].");
		if (source > qry.getRowCount()) throw new FunctionException(pc, "QueryRowSwap", 2, "source",
				"source [" + Caster.toString(source) + "] cannot be bigger than recordcount [" + qry.getRecordcount() + "] of the query.");
		int src = (int) source;

		// validate destination
		if (destination < 1)
			throw new FunctionException(pc, "QueryRowSwap", 3, "destination", "destination most be at least one, now it is [" + Caster.toString(destination) + "].");
		if (destination > qry.getRowCount()) throw new FunctionException(pc, "QueryRowSwap", 3, "destination",
				"destination [" + Caster.toString(destination) + "] cannot be bigger than recordcount [" + qry.getRecordcount() + "] of the query.");
		int dest = (int) destination;

		Collection.Key[] colNames = qry.getColumnNames();

		// temp copy of dest
		Struct tmp = new StructImpl();
		for (Key cn: colNames) {
			tmp.set(cn, qry.getAt(cn, dest));
		}

		// write source to dest
		for (Key cn: colNames) {
			qry.setAt(cn, dest, qry.getAt(cn, src));
		}

		// write tmp to src
		for (Key cn: colNames) {
			qry.setAt(cn, src, tmp.get(cn));
		}
		return qry;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toQuery(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]));

		throw new FunctionException(pc, "QueryRowSwap", 3, 3, args.length);
	}

}