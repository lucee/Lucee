package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.closure.Reduce;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.StringListData;

public class StringReduce extends BIF {
	private static final long serialVersionUID = -2153555241217815037L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 2) {
			throw new FunctionException(pc, "StringSome", 2, 3, args.length);
		}
		return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), args[2]);
	}

	public static Object call(PageContext pc, String inputString, Object value, Object initialValue) throws PageException {
		StringListData stringList = new StringListData(inputString, "", false, false);
		if (value instanceof UDF) {
			return Reduce.call(pc, (Object) stringList, (UDF) value, initialValue);
		}

		throw new FunctionException(pc, "StringReduce", "2", "callback", "The callback argument is wrong", "");
	}
}
