package lucee.runtime.functions.struct;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.wrap.MapAsStruct;

public final class StructIsOrdered extends BIF {

	private static final long serialVersionUID = -3364208235432316405L;

	public static boolean call(PageContext pc, Struct struct) {
		return isOrdered(struct);
	}

	public static boolean isOrdered(Struct struct) {
		// for scopes getType() returns the scope type and for components the type of the internal data
		if (struct instanceof Scope || struct instanceof Component) return false;
		if (struct instanceof MapAsStruct) return ((MapAsStruct) struct).isOrdered();
		if (struct instanceof StructSupport) return ((StructSupport) struct).getType() == Struct.TYPE_LINKED;
		return false;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "StructIsOrdered", 1, 1, args.length);
	}
}
