package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

/**
 * Implements the CFML Function setlocale
 */
public final class SetJavaSettings extends BIF implements Function {

	private static final long serialVersionUID = -3909808263885912504L;

	public static String call(PageContext pc, Struct data) {
		((PageContextImpl) pc).setJavaSettings(JavaSettingsImpl.getInstance(pc.getConfig(), data, null));
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "SetJavaSettings", 1, 1, args.length);
	}
}