package lucee.runtime.functions.security;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecretProviderFactory;

public final class SecretProviderRemove extends BIF {

	private static final long serialVersionUID = 5943231698954334487L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 2) throw new FunctionException(pc, "SecretProviderRemove", 1, 2, args.length);

		String key = Caster.toString(args[0]);
		String name = args.length > 1 ? Caster.toString(args[1]) : null;

		SecretProviderFactory.removeSecret(pc.getConfig(), key, name);

		return null;
	}

}