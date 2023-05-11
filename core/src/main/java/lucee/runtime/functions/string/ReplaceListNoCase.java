package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;


public class ReplaceListNoCase extends BIF {

	private static final long serialVersionUID = -8530160236310177587L;

	public static String call(PageContext pc, String str, String list1, String list2) {
		return ReplaceList._call(pc, str, list1, list2, ",", ",", true, false);
	}

	public static String call(PageContext pc, String str, String list1, String list2, String delimiter_list1) throws PageException {
		if (Decision.isBoolean(delimiter_list1)) return ReplaceList._call(pc, str, list1, list2, ",", ",", true, Caster.toBooleanValue(delimiter_list1));
		return ReplaceList._call(pc, str, list1, list2, delimiter_list1, delimiter_list1, true, false);
	}

	public static String call(PageContext pc, String str, String list1, String list2, String delimiter_list1, String delimiter_list2) throws PageException {
		if (Decision.isBoolean(delimiter_list2)) return ReplaceList._call(pc, str, list1, list2, delimiter_list1, delimiter_list1, true, Caster.toBooleanValue(delimiter_list2));
		return ReplaceList._call(pc, str, list1, list2, delimiter_list1, delimiter_list2, true, false);
	}

	public static String call(PageContext pc, String str, String list1, String list2, String delimiter_list1, String delimiter_list2, boolean includeEmptyFields) {
		return ReplaceList._call(pc, str, list1, list2, delimiter_list1, delimiter_list2, true, includeEmptyFields);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 6) return ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]),
				Caster.toString(args[4]), true, Caster.toBooleanValue(args[5]));
		if (args.length == 5) {
			if (Decision.isBoolean(args[4])) return ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toString(args[3]), false, Caster.toBooleanValue(args[4]));

			return ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]),
				Caster.toString(args[4]), true, false);
		}
		if (args.length == 4) {
			if (Decision.isBoolean(args[3])) return ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), ",", ",", false, Caster.toBooleanValue(args[3]));

			return ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), ",", true, false);
		}
		if (args.length == 3) return ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), ",", ",", true, false);
		throw new FunctionException(pc, "ReplaceListNoCase", 3, 6, args.length);
	}

}
