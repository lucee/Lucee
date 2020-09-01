package lucee.runtime.functions.other;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public class Argon2CheckHash extends BIF {
	private static final long serialVersionUID = 4730626229333277363L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 3) {
			throw new FunctionException(pc, "Argon2CheckHash", 3, 3, args.length);
		}
		return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
	}

	public static boolean call(PageContext pc, String input, String hash, String variant) throws PageException {
		Argon2Types type;
		if (StringUtil.isEmpty(variant, true)) throw new FunctionException(pc, "GenerateArgon2Hash", 1, "variant", "The Variant should be ARGON2i or ARGON2d or ARGON2id");
		variant = variant.trim();
		switch (variant.toLowerCase()) {
		case "argon2i":
			type = Argon2Types.ARGON2i;
			break;
		case "argon2d":
			type = Argon2Types.ARGON2d;
			break;
		case "argon2id":
			type = Argon2Types.ARGON2id;
			break;
		default:
			throw new FunctionException(pc, "Argon2CheckHash", 1, "variant", "The Variant should be ARGON2i or ARGON2d or ARGON2id");
		}
		Argon2 argon2 = Argon2Factory.create(type);
		char[] carrInput = input == null ? new char[0] : input.toCharArray();
		return argon2.verify(hash, carrInput);
	}
}