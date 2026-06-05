package lucee.runtime.functions.system;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.compiler.BytecodeInPlaceUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Struct;

public final class RestoreToSource implements Function {

	public static Struct call(PageContext pc, String path) throws PageException {
		return call(pc, path, true);
	}

	public static Struct call(PageContext pc, String path, boolean recursive) throws PageException {
		Resource res = ResourceUtil.toResourceExisting(pc, path);
		pc.getConfig().getSecurityManager().checkFileLocation(res);
		return BytecodeInPlaceUtil.restore(pc, res, recursive);
	}

}
