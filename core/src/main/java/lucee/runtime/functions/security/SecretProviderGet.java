package lucee.runtime.functions.security;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecretProviderFactory;

public final class SecretProviderGet extends BIF {

	private static final long serialVersionUID = -2880816702728323010L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 3) throw new FunctionException(pc, "SecretProviderGet", 1, 3, args.length);

		String key = Caster.toString(args[0]);
		String name = args.length > 1 ? Caster.toString(args[1]) : null;
		boolean resolve = args.length > 2 ? Caster.toBooleanValue(args[2]) : false;
		return SecretProviderFactory.getSecret(pc.getConfig(), name, key, resolve);
	}

}