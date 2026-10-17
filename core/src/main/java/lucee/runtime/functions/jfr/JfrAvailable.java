package lucee.runtime.functions.jfr;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.jfr.JfrUtil;

public final class JfrAvailable implements Function {

	private static final long serialVersionUID = 7856423454321L;

	public static boolean call(PageContext pc) {
		return JfrUtil.isAvailable();
	}
}
