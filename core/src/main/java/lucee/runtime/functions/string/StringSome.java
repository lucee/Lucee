package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.closure.Some;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.StringListData;

public class StringSome extends BIF {
	private static final long serialVersionUID = 4167438066376325970L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 2) {
			throw new FunctionException(pc, "StringSome", 1, 2, args.length);
		}
		if (args.length == 2) {
			return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]));
		}
		return call(pc, Caster.toString(args[0]), null);
	}

	public static boolean call(PageContext pc, String str, UDF udf) throws PageException {
		StringListData stringList = new StringListData(str, "", false, false);
		return Some.call(pc, (Object) stringList, udf);
	}
}
