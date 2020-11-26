package lucee.runtime.functions.string;

import java.util.ArrayList;
import java.util.List;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.arrays.ArrayFind;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;
import lucee.runtime.type.wrap.ListAsArray;

public class StringSome extends BIF {
	private static final long serialVersionUID = 4167438066376325970L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 1) {
			throw new FunctionException(pc, "StringSome", 1, 1, args.length);
		}
		if (args.length == 2) {
			return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]));
		}
		return call(pc, Caster.toString(args[0]), null);
	}

	public static boolean call(PageContext pc, String inputString, Object value) throws PageException {
		List<String> inputArray = new ArrayList<String>();
		inputArray.add(inputString);
		if (value instanceof UDF) {
			return ArrayFind.find(pc, ListAsArray.toArray(inputArray), (UDF) value) > 0 ? true : false;
		}
		return ArrayFind.find(ListAsArray.toArray(inputArray), value, true) > 0 ? true : false;
	}
}
