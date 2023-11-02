package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public class FindLast extends BIF {

	private static final long serialVersionUID = -176191593295823013L;

	public static double call(PageContext pc, String sub, String str) {
		return str.lastIndexOf(sub) + 1;
	}

	public static double call(PageContext pc, String sub, String str, double number) {
		if (sub.length() == 0) return (int) number;
		return str.lastIndexOf(sub, (int) number - 1) + 1;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]));

		throw new FunctionException(pc, "FindLast", 2, 3, args.length);
	}
}
