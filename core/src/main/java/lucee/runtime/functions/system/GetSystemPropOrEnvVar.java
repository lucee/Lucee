package lucee.runtime.functions.system;

import java.nio.charset.StandardCharsets;

import java.io.InputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.interpreter.JSONExpressionInterpreter;

public final class GetSystemPropOrEnvVar implements Function {

	private static final long serialVersionUID = 3459096452887146460L;

	public static Object call(PageContext pc) throws PageException {
		InputStream is = null;
		String sysProps = null;
		try {
			is = PageSourceImpl.class.getClassLoader().getResourceAsStream("/resource/setting/sysprop-envvar.json");
			sysProps = IOUtil.toString(is, StandardCharsets.UTF_8);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		finally {
			IOUtil.closeEL(is);
		}
		if (sysProps == null) throw new ExpressionException("Failed to read [/resource/setting/sysprop-envvar.json]");
		Object result = new JSONExpressionInterpreter(false, JSONExpressionInterpreter.FORMAT_JSON5).interpret(pc, sysProps);
		return result;
	}

	public static Object call(PageContext pc, String sysprop) {
		return SystemUtil.getSystemPropOrEnvVar(sysprop, "");
	}

}