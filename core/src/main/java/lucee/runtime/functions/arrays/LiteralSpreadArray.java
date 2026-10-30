package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.SpreadValue;

public final class LiteralSpreadArray extends BIF {

	private static final long serialVersionUID = 4467790327188611392L;

	public static Array call(PageContext pc, Object[] objArr) throws PageException {
		return new ArrayImpl(SpreadValue.expand(objArr, "an array literal"));
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		return call(pc, (Object[]) args[0]);
	}
}
