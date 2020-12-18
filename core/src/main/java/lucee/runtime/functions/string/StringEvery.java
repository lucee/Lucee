package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.closure.Every;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.StringListData;

public class StringEvery extends BIF {
	private static final long serialVersionUID = -2889095341490820411L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 2) {
			throw new FunctionException(pc, "StringEvery", 2, 2, args.length);
		}
		return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]));
	}

	public static boolean call(PageContext pc, String inputString, Object value) throws PageException {
		StringListData stringList = new StringListData(inputString, "", false, false);
		if (value instanceof UDF) {
			return Every.call(pc, (Object) stringList, (UDF) value);
		}

		throw new FunctionException(pc, "StringEvery", "2", "callback", "The callback argument is wrong", "");
	}

}
