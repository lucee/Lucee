package lucee.runtime.functions.query;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class QueryToStruct extends BIF {

	public static Struct call(PageContext pc, Query qry, String columnKey) throws PageException {
		return call(pc, qry, columnKey, "ordered", false);
	}

	public static Struct call(PageContext pc, Query qry, String columnKey, String structType) throws PageException {
		return call(pc, qry, columnKey, structType, false);
	}

	public static Struct call(PageContext pc, Query qry, String columnKey, String structType, boolean valueRowNumber) throws PageException {

		if (StringUtil.isEmpty(columnKey, true)) throw new FunctionException(pc, "queryToStruct", 2, "columnKey", "columnKey cannot be a empty value");

		Struct sct = new StructImpl(toType(pc, structType));
		int rows = qry.getRecordcount();
		if (rows == 0) return sct;
		Key[] columns = qry.getColumnNames();

		for (int r = 1; r <= rows; r++) {
			if (valueRowNumber) sct.set(Caster.toKey(qry.getAt(columnKey, r)), r);
			else {
				Struct tmp = new StructImpl();
				sct.set(Caster.toKey(qry.getAt(columnKey, r)), tmp);
				for (Key c: columns) {
					tmp.setEL(c, qry.getAt(c, r, null));
				}
			}
		}
		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]));
		else if (args.length == 3) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		else if (args.length == 4) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]));
		else throw new FunctionException(pc, "queryToStruct", 2, 4, args.length);
	}

	public static int toType(PageContext pc, String type) throws PageException {
		type = type.toLowerCase();
		if (type.equals("ordered")) return Struct.TYPE_LINKED;
		else if (type.equals("linked")) return Struct.TYPE_LINKED;
		else if (type.equals("weaked")) return Struct.TYPE_WEAKED;
		else if (type.equals("weak")) return Struct.TYPE_WEAKED;
		else if (type.equals("syncronized")) return Struct.TYPE_SYNC;
		else if (type.equals("synchronized")) return Struct.TYPE_SYNC;
		else if (type.equals("sync")) return Struct.TYPE_SYNC;
		else if (type.equals("soft")) return Struct.TYPE_SOFT;
		else if (type.equals("normal")) return Struct.TYPE_REGULAR;
		else if (type.equals("regular")) return Struct.TYPE_REGULAR;
		else throw new FunctionException(pc, "queryToStruct", 3, "structType", "valid struct types are [normal, weak, linked, soft, synchronized]");
	}
}
