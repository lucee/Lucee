package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.config.Prop;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class ConfigSchema extends BIF {

	private static final long serialVersionUID = 2745462617199977951L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) {
			return Prop.createConfigSchema(true);
		}
		else if (args.length == 1) {
			return Prop.createConfigSchema(Caster.toBooleanValue(args[0]));
		}
		else {
			throw new FunctionException(pc, "ConfigSchema", 0, 1, args.length);
		}

	}
}
