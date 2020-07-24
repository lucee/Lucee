package lucee.runtime.functions.other;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public class GenerateArgon2Hash extends BIF {
	private static final long serialVersionUID = 61397352504711269L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 5) {
			throw new FunctionException(pc, "GenerateArgon2Hash", 5, 5, args.length);
		}
		return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toByteValue(args[2]), Caster.toIntValue(args[3]), Caster.toByteValue(args[4]));
	}

	public static String call(PageContext pc, String variant, String input, double parallelismFactor, double memoryCost, double iterations) throws PageException {
		Argon2Types type;
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
			throw new FunctionException(pc, "GenerateArgon2Hash", 1, "variant", "The Variant should be ARGON2i or ARGON2d or ARGON2id", null);
		}
		Argon2 argon2 = Argon2Factory.create(type);

		if (parallelismFactor < 1 || parallelismFactor > 10) {
			throw new FunctionException(pc, "GenerateArgon2Hash", 2, "parallelismFactor", "The parallelism factor value should be between 1 and 10", null);
		}

		if (memoryCost < 8 || memoryCost > 100000) {
			throw new FunctionException(pc, "GenerateArgon2Hash", 3, "memoryCost", "The memory cost value should be between 8 and 100000", null);
		}

		if (iterations < 1 || iterations > 20) {
			throw new FunctionException(pc, "GenerateArgon2Hash", 4, "iterations", "The iterations value should be between 1 and 20", null);
		}

		int memory = Caster.toIntValue(memoryCost);
		String hash = argon2.hash(Caster.toIntValue(iterations), memory * memory, Caster.toIntValue(parallelismFactor), input.toCharArray());
		boolean success = argon2.verify(hash, input.toCharArray());

		if (!success) {
			throw new ExpressionException("Hashing failed!");
		}
		return hash;
	}
}
