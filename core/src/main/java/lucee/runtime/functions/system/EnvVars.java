package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.config.Prop;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Struct;

public final class EnvVars extends BIF {

	private static final long serialVersionUID = 2745462617199977952L;

	@Override
	public Struct invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length > 0) {
			throw new FunctionException(pc, "EnvVars", 0, 0, args.length);
		}
		return Prop.createEnvVars();
	}
}
