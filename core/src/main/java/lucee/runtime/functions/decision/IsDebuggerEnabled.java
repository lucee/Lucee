package lucee.runtime.functions.decision;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.ext.function.Function;

public final class IsDebuggerEnabled implements Function {

	public static boolean call(PageContext pc) {
		return ConfigImpl.DEBUGGER;
	}
}
