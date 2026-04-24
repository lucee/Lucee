/**
 * Implements the CFML Function reEscape
 */
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.regex.Regex;

public final class REEscape extends BIF {

	private static final long serialVersionUID = -1240669656936340678L;

	public static String call(PageContext pc, String string) throws PageException {
		Regex regex = ((PageContextImpl) ThreadLocalPageContext.get(pc)).getRegex();
		return regex.escape(string);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		throw new FunctionException(pc, "REEscape", 1, 1, args.length);
	}

}