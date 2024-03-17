package lucee.runtime.functions.system;

import java.io.File;

import lucee.commons.io.FileUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.transformer.bytecode.util.SystemExitScanner;

public final class SystemExitClean extends BIF implements Function {

	private static final long serialVersionUID = 765287782000310234L;

	public static String call(PageContext pc, String src, String trg) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		try {
			// source
			File fsrc = eng.getCastUtil().toFile(src);
			// target
			File ftrg = null;
			boolean srcEQtrg = true;
			if (!Util.isEmpty(trg, true)) {
				ftrg = eng.getCastUtil().toFile(trg);
				srcEQtrg = fsrc.equals(ftrg);
			}

			if (srcEQtrg) ftrg = File.createTempFile("SystemExitClean", ".jar");

			SystemExitScanner.clean(fsrc, ftrg);

			if (srcEQtrg) {
				if (fsrc.exists()) fsrc.delete();
				FileUtil.move(ftrg, fsrc);
			}
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 2) throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(pc, "SystemExitClean", 1, 2, args.length);

		CFMLEngine eng = CFMLEngineFactory.getInstance();
		// source
		String src = eng.getCastUtil().toString(args[0]);
		// target
		String trg = null;
		if (args.length == 2) {
			trg = eng.getCastUtil().toString(args[1]);
		}
		return call(pc, src, trg);
	}
}
