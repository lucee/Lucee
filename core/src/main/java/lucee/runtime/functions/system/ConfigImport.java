package lucee.runtime.functions.system;

import java.nio.charset.Charset;
import java.util.Map;

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

public class ConfigImport extends BIF {

	private static final long serialVersionUID = 2877661269574331695L;

	public static Struct call(PageContext pc, Object pathOrData, String type, String password, Struct placeHolderData, String charset) throws PageException {

		// path
		Resource res = null;
		Struct data = null;
		if (pathOrData instanceof CharSequence) res = ResourceUtil.toResourceExisting(pc, pathOrData.toString());
		else if (pathOrData instanceof Map) data = Caster.toStruct(pathOrData);
		else throw new FunctionException(pc, "ConfigFileImport", "first", "pathOrData",
				"Invalid value for argument pathOrData, the argument must contain a string that points to a .CFConfig.json file or a struct containing the data itself.", null);

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

		return (res != null ? new CFConfigImport(pc.getConfig(), res, cs, password, type, placeHolderData)
				: new CFConfigImport(pc.getConfig(), data, cs, password, type, placeHolderData)).execute();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]), null, null, null, null);
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), null, null, null);
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), null, null);
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toStruct(args[3]), null);
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toStruct(args[3]), Caster.toString(args[4]));
		else throw new FunctionException(pc, "ConfigFileImport", 1, 5, args.length);
	}
}
