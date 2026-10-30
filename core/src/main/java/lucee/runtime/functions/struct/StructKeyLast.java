/**
 * Implements the CFML Function structKeyLast
 */
package lucee.runtime.functions.struct;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public final class StructKeyLast extends BIF {

	private static final long serialVersionUID = -3951148866803716942L;

	public static String call(PageContext pc, Struct struct) throws PageException {
		return StructKeyFirst.key(struct, false);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "StructKeyLast", 1, 1, args.length);
	}
}
