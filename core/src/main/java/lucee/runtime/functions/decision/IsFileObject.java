package lucee.runtime.functions.decision;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Decision;

public final class IsFileObject implements Function {
	public static boolean call(PageContext pc, Object source) {
		return Decision.isFileObject(source);
	}
}