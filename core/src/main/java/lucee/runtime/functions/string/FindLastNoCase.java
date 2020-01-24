package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class FindLastNoCase extends BIF {

	private static final long serialVersionUID = -5722812211523628009L;

	public static double call(PageContext pc, String sub, String str) {
		return FindLast.call(pc, sub.toLowerCase(), str.toLowerCase());
	}

	public static double call(PageContext pc, String sub, String str, double number) {
		return FindLast.call(pc, sub.toLowerCase(), str.toLowerCase(), number);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]));

		throw new FunctionException(pc, "FindLastNoCase", 2, 3, args.length);
	}
}