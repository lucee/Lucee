package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.util.KeyConstants;

public final class LuceeVersionsList extends BIF {

	private static final long serialVersionUID = -2983771649902173955L;

	public static Array call(PageContext pc, String type) throws PageException {
		try {
			return LuceeVersionsListMvn.call(pc, type);
		}
		catch (Exception e) {
			Query qry = LuceeVersionsListS3.call(pc, type);
			int rows = qry.getRecordcount();
			Array arr = new ArrayImpl();
			for (int row = 1; row <= rows; row++) {
				arr.append(qry.getAt(KeyConstants._version, row));
			}
			return arr;
		}

	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 0) return call(pc, null);

		throw new FunctionException(pc, "LuceeVersionsListS3", 0, 1, args.length);
	}
}