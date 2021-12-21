package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.closure.Each;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.StringListData;

public class StringEach extends BIF implements Function {
	private static final long serialVersionUID = 2207105205243253849L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 2) {
			throw new FunctionException(pc, "StringEach", 2, 2, args.length);
		}
		return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]));
	}

	public static String call(PageContext pc, String str, UDF udf) throws PageException {
		return Each.call(pc, (Object) str.toCharArray(), udf);
	}
	// call(Llucee/runtime/PageContext;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;

}