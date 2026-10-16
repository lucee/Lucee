package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public final class ConfigMerge extends BIF {

	private static final long serialVersionUID = 7928188743318414402L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 2) {
			throw new FunctionException(pc, "ConfigMerge", 2, 2, args.length);
		}
		Struct left = (Struct) Caster.toStruct(args[0]).duplicate(true);
		Struct right = Caster.toStruct(args[1]);
		lucee.runtime.config.ConfigMerge.merge(left, right);
		return left;
	}
}
