package lucee.runtime.functions.system;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.transformer.bytecode.util.SystemExitScanner;

public final class SystemExitHas extends BIF implements Function {

	private static final long serialVersionUID = -525836425397031512L;

	public static boolean call(PageContext pc, String src) throws PageException {
		try {
			return SystemExitScanner.has(CFMLEngineFactory.getInstance().getCastUtil().toFile(src));
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 1) throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(pc, "SystemExitHas", 1, 1, args.length);
		return call(pc, CFMLEngineFactory.getInstance().getCastUtil().toString(args[0]));
	}
}
