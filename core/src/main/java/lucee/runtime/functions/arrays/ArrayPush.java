package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;

public final class ArrayPush extends BIF {

	private static final long serialVersionUID = -5673140457325547233L;

	public static double call(PageContext pc, Array array, Object object) throws PageException {
		// TODO need to be atomic
		array.append(object);
		return array.size();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toArray(args[0]), args[1]);
		else throw new FunctionException(pc, "ArrayPush", 2, 2, args.length);
	}
}