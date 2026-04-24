package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.config.Prop;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Query;

public class GetSystemPropOrEnvVarInfo extends BIF {

	@Override
	public Query invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length > 0) {
			throw new FunctionException(pc, "GetSystemPropOrEnvVarInfo", 0, 1, args.length);
		}

		return Prop.createSystemPropEnvVar();
	}
}