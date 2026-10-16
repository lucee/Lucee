package lucee.runtime.functions.security;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecretProviderFactory;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

public class SecretProviderListNames extends BIF {

	private static final long serialVersionUID = 6162601562044919183L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length > 1) throw new FunctionException(pc, "SecretProviderListNames", 0, 1, args.length);

		String name = args.length > 0 ? Caster.toString(args[0]) : null;

		Array arr = new ArrayImpl();

		for (String key: SecretProviderFactory.listSecretNames(pc.getConfig(), name)) {
			arr.append(key);
		}
		return arr;
	}

}