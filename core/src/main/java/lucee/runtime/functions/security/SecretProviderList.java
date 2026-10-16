package lucee.runtime.functions.security;

import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecretProviderFactory;
import lucee.runtime.security.SecretProviderFactory.Ref;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.StructImpl;

public class SecretProviderList extends BIF {

	private static final long serialVersionUID = 1208699252116365430L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length > 2) throw new FunctionException(pc, "SecretProviderList", 0, 2, args.length);

		String name = args.length > 0 ? Caster.toString(args[0]) : null;
		boolean resolve = args.length > 1 ? Caster.toBooleanValue(args[1]) : false;

		StructImpl sct = new StructImpl();
		for (Entry<String, Ref> e: SecretProviderFactory.listSecrets(pc.getConfig(), name, resolve).entrySet()) {
			sct.set(KeyImpl.init(e.getKey()), e.getValue());
		}
		return sct;
	}

}