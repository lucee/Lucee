package lucee.runtime.functions.security;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.security.SecretProviderExtended;
import lucee.runtime.security.SecretProviderUtil;

public final class SecretProviderSet extends BIF {

	private static final long serialVersionUID = -8526051830599398750L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 3) throw new FunctionException(pc, "SecretProviderSet", 3, 3, args.length);

		String key = Caster.toString(args[0]);
		Object value = args[1];
		String name = Caster.toString(args[2]);

		SecretProviderExtended sp = SecretProviderUtil.toSecretProviderExtended(((ConfigPro) pc.getConfig()).getSecretProvider(name));

		if (Decision.isBoolean(value)) {
			sp.setSecret(key, Caster.toBooleanValue(value));
		}
		else if (Decision.isInteger(value)) {
			sp.setSecret(key, Caster.toIntValue(value));
		}
		else {
			sp.setSecret(key, Caster.toString(value));
		}

		return null;
	}

}