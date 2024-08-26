package lucee.runtime.functions.system;

import java.io.IOException;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Struct;

/**
 * Implements the CFML Function setlocale
 */
public final class ImportJavaSettings extends BIF implements Function {

	private static final long serialVersionUID = -3909808263885912504L;

	public static String call(PageContext pc, Object data) throws PageException {
		try {
			Struct sct;
			if (Decision.isSimpleValue(data)) {
				String str = Caster.toString(data);
				if (StringUtil.endsWithIgnoreCase(str, ".json")) {
					str = IOUtil.toString(ResourceUtil.toResourceExisting(pc, str), CharsetUtil.UTF8);
				}
				sct = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, str));

			}
			else {
				sct = Caster.toStruct(data);
			}
			JavaSettingsImpl js = (JavaSettingsImpl) JavaSettingsImpl.getInstance(pc.getConfig(), sct, null);
			// forces it do download the resources
			js.getAllResources();
		}
		catch (IOException ioe) {
			throw Caster.toPageException(ioe);
		}
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "SetJavaSettings", 1, 1, args.length);
	}
}