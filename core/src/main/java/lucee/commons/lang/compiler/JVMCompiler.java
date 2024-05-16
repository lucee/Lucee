package lucee.commons.lang.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.osgi.OSGiUtil;

public class JVMCompiler implements Compiler {

	@Override
	public byte[] compile(ConfigPro config, SourceCode sc) throws ApplicationException, JavaCompilerException {
		ClassLoader cl = config.getClassLoaderEnv();

		// ClassLoader cl =
		// CFMLEngineFactory.getInstance().getCFMLEngineFactory().getClass().getClassLoader();
		Collection<SourceCode> compilationUnits = new ArrayList<>();
		compilationUnits.add(sc);
		DynamicClassLoader dcl = new DynamicClassLoader(cl);
		javax.tools.JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
		if (javac == null) {
			throw new ApplicationException("Java compiling is not suppprted with your current JVM Environment (" + System.getProperty("java.vendor") + " "
					+ System.getProperty("java.version")
					+ "). Update to a newer version or add a tools.jar to the environment. Read more here: https://stackoverflow.com/questions/15513330/toolprovider-getsystemjavacompiler-returns-null-usable-with-only-jre-install");
		}
		List<String> options = new ArrayList<String>();

		// TODO MUST better way to do this!!!
		options.add("-classpath");
		options.add(OSGiUtil.getClassPath());

		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();

		ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), dcl);
		javax.tools.JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null, compilationUnits);
		boolean result = task.call();

		if (!result || collector.getDiagnostics().size() > 0) {
			StringBuilder exceptionMsg = new StringBuilder();
			exceptionMsg.append("Unable to compile the source");
			boolean hasWarnings = false;
			boolean hasErrors = false;
			for (Diagnostic<? extends JavaFileObject> d: collector.getDiagnostics()) {
				switch (d.getKind()) {
				case NOTE:
				case MANDATORY_WARNING:
				case WARNING:
					hasWarnings = true;
					break;
				case OTHER:
				case ERROR:
				default:
					hasErrors = true;
					break;
				}
				if (hasErrors) throw new JavaCompilerException(d.getMessage(Locale.US), d.getLineNumber(), d.getColumnNumber(), d.getKind());
			}
		}
		return dcl.getCompiledCode(sc.getClassName()).getByteCode();
	}

	@Override
	public boolean supported() {
		return ToolProvider.getSystemJavaCompiler() != null;
	}
}
