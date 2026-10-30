package lucee.runtime.functions.other;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.SpreadValue;

/**
 * internal function behind the spread operator (...)
 */
public final class Spread extends BIF {

	private static final long serialVersionUID = 5148872342718033619L;

	public static Object call(PageContext pc, Object value) {
		return new SpreadValue(value);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, args[0]);
		throw new FunctionException(pc, "Spread", 1, 1, args.length);
	}
}
