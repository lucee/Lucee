package lucee.runtime.functions.other;

import com.github.f4b6a3.ulid.UlidCreator;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

/**
 * Implements the CFML Function createulid
 */
public final class CreateULID extends BIF {

	private static final long serialVersionUID = 6025094449148339680L;

	public static String call(PageContext pc) throws PageException {
		return invoke(pc, null, -1, null);
	}

	public static String call(PageContext pc, String type) throws PageException {
		return invoke(pc, type, -1, null);
	}

	public static String call(PageContext pc, String type, double input1, String input2) throws PageException {
		return invoke(pc, type, input1, input2);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return invoke(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]));
		if (args.length == 2) return invoke(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), null);
		if (args.length == 1) return invoke(pc, Caster.toString(args[0]), -1, null);
		if (args.length == 0) return invoke(pc, null, -1, null);

		throw new FunctionException(pc, CreateUniqueId.class.getSimpleName(), 0, 3, args.length);
	}

	public static String invoke(PageContext pc, String type, double input1, String input2) throws PageException {
		if (StringUtil.isEmpty(type)) return UlidCreator.getUlid().toString();

		// empty,monotonic,hash
		type = type.trim();
		if ("monotonic".equalsIgnoreCase(type)) return UlidCreator.getMonotonicUlid().toString();
		else if ("hash".equalsIgnoreCase(type)) return UlidCreator.getHashUlid(Caster.toLong(input1), input2).toString();

		throw new FunctionException(pc, "CreateULID", 1, "type", "Type [" + type + "] is not supported. supported types are [monotonic,hash]");
	}
}