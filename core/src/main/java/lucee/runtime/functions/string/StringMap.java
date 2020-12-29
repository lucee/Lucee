package lucee.runtime.functions.string;

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.closure.Map;
import lucee.runtime.op.Caster;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.StringListData;

public class StringMap extends BIF {
	private static final long serialVersionUID = 8643893144992203939L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1) {
			throw new FunctionException(pc, "StringMap", 1, 2, args.length);
		}
		return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]));
	}

	public static String call(PageContext pc, String str, UDF udf) throws PageException {
		StringListData stringList = new StringListData(str, "", false, false);
		ArrayImpl array = (ArrayImpl) Map.call(pc, (Object) stringList, udf);
		Iterator it = array.getIterator();
		Object e;
		StringBuilder result = new StringBuilder();
		while (it.hasNext()) {
			e = it.next();
			result.append(e);
		}
		return result.toString();
	}
}
