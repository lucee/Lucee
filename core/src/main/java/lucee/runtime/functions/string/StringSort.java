package lucee.runtime.functions.string;

import java.util.Arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public class StringSort extends BIF implements Function {
	private static final long serialVersionUID = 8201208274877675500L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 1) {
			throw new FunctionException(pc, "StringSort", 1, 1, args.length);
		}
		return call(pc, Caster.toString(args[0]));
	}

	public static String call(PageContext pc, String input) {
		char inputArray[] = input.toCharArray();
		Arrays.sort(inputArray);
		return new String(inputArray);
	}
}
