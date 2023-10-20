
package lucee.runtime.functions.system;

import lucee.print;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public final class LuceeVersionsDetail extends BIF {

	private static final long serialVersionUID = -4525694087027654154L;

	public static Struct call(PageContext pc, String version) throws PageException {

		try {
			return LuceeVersionsDetailMvn.call(pc, version);

		}
		catch (Exception e) {
			print.e(e);
			return LuceeVersionsDetailS3.call(pc, version);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));

		throw new FunctionException(pc, "LuceeVersionsDetail", 1, 1, args.length);
	}
}