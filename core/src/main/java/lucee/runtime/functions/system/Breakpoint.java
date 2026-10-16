package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.debug.DebuggerRegistry;

public final class Breakpoint {

	public static boolean call(PageContext pc) {
		return call(pc, null, true);
	}

	public static boolean call(PageContext pc, String label) {
		return call(pc, label, true);
	}

	public static boolean call(PageContext pc, String label, boolean condition) {
		// Only suspend if debugger breakpoint support is enabled AND a debugger client is connected
		if (condition && ConfigImpl.DEBUGGER && DebuggerRegistry.isClientConnected()) {
			((PageContextImpl) pc).debuggerSuspend(label);
			return true;
		}
		return false;
	}
}
