package lucee.runtime.functions.system;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.transformer.bytecode.util.SystemExitScanner;

public final class SystemExitScan extends BIF implements Function {

	private static final long serialVersionUID = -6360841251247733951L;

	public static Struct call(PageContext pc, String src) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		try {
			Map<String, List<Integer>> matches = SystemExitScanner.scan(eng.getCastUtil().toFile(src), true);
			Struct res = new StructImpl();
			Array arr;
			for (Entry<String, List<Integer>> e: matches.entrySet()) {
				arr = new ArrayImpl();
				for (Integer i: e.getValue()) {
					arr.append(i);
				}
				res.set(e.getKey(), arr);
			}
			return res;
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
