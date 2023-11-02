package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;

public class ArrayDeleteNoCase extends BIF {

	private static final long serialVersionUID = 1120923916196967210L;

	public static boolean call(PageContext pc, Array array, Object value) throws PageException {
		return ArrayDelete._call(pc, array, value, null, false);
	}

	public static boolean call(PageContext pc, Array array, Object value, String scope) throws PageException {
		return ArrayDelete._call(pc, array, value, scope, false);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return ArrayDelete._call(pc, Caster.toArray(args[0]), args[1], null, false);
		else if (args.length == 3) return ArrayDelete._call(pc, Caster.toArray(args[0]), args[1], Caster.toString(args[2]), false);
		else throw new FunctionException(pc, "ArrayDeleteNoCase", 2, 3, args.length);
	}

}
