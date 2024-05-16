package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ArrayUtil;

public final class ArrayPop extends BIF {

	private static final long serialVersionUID = -5628212614796853287L;

	public static Object call(PageContext pc, Array array) throws PageException {
		return ArrayUtil.toArrayPro(array).pop();
	}

	public static Object call(PageContext pc, Array array, Object defaultValue) throws PageException {
		return ArrayUtil.toArrayPro(array).pop(defaultValue);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toArray(args[0]));
		if (args.length == 2) return call(pc, Caster.toArray(args[0]), args[1]);
		else throw new FunctionException(pc, "ArrayPop", 1, 2, args.length);
	}
}