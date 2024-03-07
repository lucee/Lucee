package lucee.runtime.functions.international;

import java.util.Locale;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class LSUCase extends BIF {

	private static final long serialVersionUID = -8360184992907564592L;

	public static String call(PageContext pc, String string) {
		return string.toUpperCase(pc.getLocale());
	}

	public static String call(PageContext pc, String string, Locale locale) {
		return string.toUpperCase(locale == null ? pc.getLocale() : locale);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) {
			return call(pc, Caster.toString(args[0]), (StringUtil.isEmpty(args[1], true)) ? pc.getLocale() : Caster.toLocale(args[1]));
		}

		throw new FunctionException(pc, "LSUCase", 1, 2, args.length);
	}
}