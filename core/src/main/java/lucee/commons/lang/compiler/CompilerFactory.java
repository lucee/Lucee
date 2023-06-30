package lucee.commons.lang.compiler;

import lucee.runtime.exp.ApplicationException;

public class CompilerFactory {
	public static Compiler getInstance() throws ApplicationException {
		JVMCompiler jvm = new JVMCompiler();
		if (false && jvm.supported()) {
			return jvm;
		}

		JaninoCompiler janino = new JaninoCompiler();

		if (janino.supported()) return janino;

		throw new ApplicationException("Java compiling is not suppprted with your current JVM Environment (" + System.getProperty("java.vendor") + " "
				+ System.getProperty("java.version")
				+ "). Update to a newer version or add a tools.jar to the environment. Read more here: https://stackoverflow.com/questions/15513330/toolprovider-getsystemjavacompiler-returns-null-usable-with-only-jre-install");

	}
}
