package lucee.commons.lang.compiler;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.ApplicationException;

public final class CompilerFactory {
	public static Compiler getInstance(ConfigPro config) throws ApplicationException {
		JVMCompiler jvm = new JVMCompiler();
		if (jvm.supported()) return jvm;

		JaninoCompiler janino = new JaninoCompiler(config);
		if (janino.supported()) return janino;

		ApplicationException ae = new ApplicationException("Java compiling is not suppprted with your current JVM Environment (" + System.getProperty("java.vendor") + " "
				+ System.getProperty("java.version")
				+ "). Update to a newer version or add a tools.jar to the environment. Read more here: https://stackoverflow.com/questions/15513330/toolprovider-getsystemjavacompiler-returns-null-usable-with-only-jre-installed");
		ExceptionUtil.initCauseEL(ae, janino.getLoadFailure());
		throw ae;
	}
}
