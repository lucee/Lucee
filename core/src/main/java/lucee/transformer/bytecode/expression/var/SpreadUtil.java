package lucee.transformer.bytecode.expression.var;

import java.util.List;

import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Argument;
import lucee.transformer.expression.var.Member;
import lucee.transformer.expression.var.NamedArgument;
import lucee.transformer.expression.var.Variable;

/**
 * helper for the spread operator (...), a spread argument is an argument holding a call to the
 * internal function _spread.
 */
public final class SpreadUtil {

	public static final String FUNCTION_NAME = "_spread";

	private SpreadUtil() {
	}

	public static boolean isSpread(Argument arg) {
		if (arg == null || arg instanceof NamedArgument) return false;
		Expression value = arg.getValue();
		if (!(value instanceof Variable)) return false;
		List<Member> members = ((Variable) value).getMembers();
		if (members.size() != 1 || !(members.get(0) instanceof BIF)) return false;
		return FUNCTION_NAME.equals(((BIF) members.get(0)).getFlf().getName());
	}

	public static boolean hasSpread(Argument[] args) {
		if (args == null) return false;
		for (Argument arg: args) {
			if (isSpread(arg)) return true;
		}
		return false;
	}
}
