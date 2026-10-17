package lucee.runtime.functions.system;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.compiler.BytecodeInPlaceUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Struct;

public final class CompileToBytecode implements Function {

	public static Struct call(PageContext pc, String path) throws PageException {
		return call(pc, path, false, null, true);
	}

	public static Struct call(PageContext pc, String path, boolean keepSource) throws PageException {
		return call(pc, path, keepSource, null, true);
	}

	public static Struct call(PageContext pc, String path, boolean keepSource, String privateKey) throws PageException {
		return call(pc, path, keepSource, privateKey, true);
	}

	public static Struct call(PageContext pc, String path, boolean keepSource, String privateKey, boolean recursive) throws PageException {
		Resource res = ResourceUtil.toResourceExisting(pc, path);
		pc.getConfig().getSecurityManager().checkFileLocation(res);
		return BytecodeInPlaceUtil.compile(pc, res, keepSource, privateKey, recursive);
	}

}
