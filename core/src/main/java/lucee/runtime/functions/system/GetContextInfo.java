package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * returns the root of this current Page Context
 */
public final class GetContextInfo extends BIF {

	private static final long serialVersionUID = 6287311028101499094L;

	public static Struct call(PageContext pc) throws PageException {
		Struct data = new StructImpl();
		data.set("flushed", pc.getHttpServletResponse().isCommitted());
		return data;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);

		throw new FunctionException(pc, "GetContextInfo", 0, 0, args.length);
	}
}