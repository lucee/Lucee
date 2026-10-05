package lucee.runtime.functions.jfr;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.jfr.JfrUtil;

public final class JfrEnabled implements Function {

	private static final long serialVersionUID = 7856423454322L;

	public static boolean call(PageContext pc) {
		return JfrUtil.isEnabled();
	}
}
