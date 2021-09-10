package lucee.runtime.functions.system;

import java.nio.charset.Charset;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.CFConfigImport;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public class ConfigFileImport extends BIF {

	private static final long serialVersionUID = 2877661269574331695L;

	public static Struct call(PageContext pc, String path, String type, String password, String charset) throws PageException {

		// path
		Resource file = ResourceUtil.toResourceExisting(pc, path);

		// type
		if (StringUtil.isEmpty(type)) type = "server";
		else if (!"server".equalsIgnoreCase(type) && !"web".equalsIgnoreCase(type))
			throw new FunctionException(pc, "ConfigFileImport", "second", "type", "Invalid value for argument type (" + type + "), valid values are [server,web]", null);

		// password
		if (StringUtil.isEmpty(password)) {
			password = SystemUtil.getSystemPropOrEnvVar("lucee." + type.toLowerCase() + ".admin.password", null);
			if (StringUtil.isEmpty(password))
				throw new FunctionException(pc, "ConfigFileImport", "third", "password", "There is no password defined as an argument for the function",
						"You can define a password to access the " + type.toLowerCase() + " config in 3 ways. As an argument with this function, as enviroment variable [LUCEE_"
								+ type.toUpperCase() + "_ADMIN_PASSWORD] or as system property [lucee." + type.toLowerCase() + ".admin.password]");
		}

		// charset
		Charset cs = StringUtil.isEmpty(charset, true) ? pc.getResourceCharset() : CharsetUtil.toCharset(charset);

		return new CFConfigImport(pc.getConfig(), file, cs, password, type).execute();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]), null, null, null);
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), null, null);
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), null);
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]));
		else throw new FunctionException(pc, "ConfigFileImport", 1, 4, args.length);
	}
}
