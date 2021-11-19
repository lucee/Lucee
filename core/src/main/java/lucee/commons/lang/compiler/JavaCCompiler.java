package lucee.commons.lang.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageSource;
import lucee.runtime.osgi.OSGiUtil;

/**
 * Compile Java sources in-memory
 */
public class JavaCCompiler {

	public static JavaFunction compile(PageSource parent, SourceCode sc) throws JavaCompilerException {

		ClassLoader cl = CFMLEngineFactory.getInstance().getCFMLEngineFactory().getClass().getClassLoader();

		Collection<SourceCode> compilationUnits = new ArrayList<>();
		compilationUnits.add(sc);
		DynamicClassLoader dcl = new DynamicClassLoader(cl);
		javax.tools.JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
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
		return new JavaFunction(parent, sc, dcl.getCompiledCode(sc.getClassName()).getByteCode());
	}
}
