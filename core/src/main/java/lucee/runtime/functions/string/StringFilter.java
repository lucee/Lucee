package lucee.runtime.functions.string;

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.closure.Filter;
import lucee.runtime.op.Caster;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.StringListData;

public class StringFilter extends BIF implements Function {
	private static final long serialVersionUID = -3273443514000974993L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 2) {
			throw new FunctionException(pc, "StringFilter", 2, 2, args.length);
		}
		return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]));
	}

	public static String call(PageContext pc, String str, UDF udf) throws PageException {
		ArrayImpl array = (ArrayImpl) Filter.call(pc, str.toCharArray(), udf);
		Iterator it = array.getIterator();
		StringBuilder result = new StringBuilder();
		while (it.hasNext()) {
			result.append(Caster.toString(it.next()));
		}
		return result.toString();
	}

}