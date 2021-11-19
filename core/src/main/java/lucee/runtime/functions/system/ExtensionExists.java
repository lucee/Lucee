package lucee.runtime.functions.system;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.op.Caster;

public class ExtensionExists extends BIF implements Function {

	private static final long serialVersionUID = 2627423175121799118L;

	public static boolean call(PageContext pc, String id) throws PageException {
		return call(pc, id, null);
	}

	public static boolean call(PageContext pc, String id, String version) throws PageException {
		if (find(id, version, ((ConfigWebPro) pc.getConfig()).getServerRHExtensions())) return true;
		if (find(id, version, ((ConfigWebPro) pc.getConfig()).getRHExtensions())) return true;
		return false;
	}

	private static boolean find(String id, String version, RHExtension[] extensions) {
		for (RHExtension ext: extensions) {
			if (ext.getId().equalsIgnoreCase(id) || ext.getSymbolicName().equalsIgnoreCase(id)) {
				if (StringUtil.isEmpty(version) || ext.getVersion().equalsIgnoreCase(version)) return true;
			}
		}
		return false;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		else if (args.length == 1) return call(pc, Caster.toString(args[0]));
		else throw new FunctionException(pc, "ExtensionExists", 1, 2, args.length);
	}
}
