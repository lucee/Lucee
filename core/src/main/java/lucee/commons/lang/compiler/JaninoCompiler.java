package lucee.commons.lang.compiler;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.ICompiler;
import org.codehaus.commons.compiler.Location;
import org.codehaus.commons.compiler.util.resource.Resource;
import org.codehaus.commons.compiler.util.resource.ResourceFinder;
import org.codehaus.commons.compiler.util.resource.StringResource;
import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.CompilerFactory;

import lucee.print;
import lucee.commons.lang.compiler.janino.ResourceCreatorImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;

public class JaninoCompiler implements Compiler {

	public static void main(String[] args) throws Exception {
		File destinationDirectory = new File("/Users/mic/Tmp3/dest");
		File[] sourcePath = new File[0];
		File[] classPath = { new File(".") };
		File[] extDirs = new File[0];
		File[] bootClassPath = null;
		Charset encoding = Charset.defaultCharset();
		boolean verbose = true;
		boolean debugSource = true;
		boolean debugLines = true;
		boolean debugVars = false;
		boolean rebuild = false;

		// Process command line options.
		File[] sourceFiles = new File[] { new File("/Users/mic/Documents/workspaceLuna/Testx/src/org/lucee/test/Example.java") };

		ICompiler compiler = new CompilerFactory().newCompiler();

		compiler.setSourcePath(sourcePath);

		List<File> list = OSGiUtil.getClassPathAsListWithJarExtension();
		compiler.setClassPath(list.toArray(new File[list.size()]));
		compiler.setExtensionDirectories(extDirs);
		if (bootClassPath != null) compiler.setBootClassPath(bootClassPath); //
		compiler.setDestinationDirectory(destinationDirectory, rebuild);
		compiler.setVerbose(verbose);
		compiler.setDebugSource(debugSource);
		compiler.setDebugLines(debugLines);
		compiler.setDebugVars(debugVars);
		compiler.setTargetVersion(8);
		ResourceCreatorImpl resourceCreator = new ResourceCreatorImpl();
		compiler.setClassFileCreator(resourceCreator);
		// Compile source files.
		compiler.compile(new Resource[] {
				new StringResource("Example.java", "package org.lucee.test;\n import org.osgi.framework.BundleException; public class Example {\n" + "\n" + "}") });

		// compiler.setTargetVersion(0)

		System.out.println(resourceCreator.getBytes(true).length);
	}

	@Override
	public boolean supported() {
		return true;
	}

	@Override
	public byte[] compile(ConfigPro config, SourceCode sc) throws PageException, JavaCompilerException {
		// Create the compiler object.
		try {

			// ClassLoader cl =
			// CFMLEngineFactory.getInstance().getCFMLEngineFactory().getClass().getClassLoader();
			ClassLoader cl = config.getClassLoaderEnv();
			// ClassLoader cl = CFMLEngineFactory.getInstance().getClass().getClassLoader();
			DynamicClassLoader dcl = new DynamicClassLoader(cl);
			ClassLoaderIClassLoader clcl = new ClassLoaderIClassLoader(dcl);
			ResourceFinder rf = ResourceFinder.EMPTY_RESOURCE_FINDER;
			org.codehaus.janino.Compiler compiler = new org.codehaus.janino.Compiler(rf, clcl);
			// ICompiler compiler = new CompilerFactory().newCompiler();

			// List<File> list = OSGiUtil.getClassPathAsListWithJarExtension(); // TODO MUST we need an update
			// with the Janino, so it does not need a .jar extension
			// print.e(list);
			// compiler.setClassPath(list.toArray(new File[list.size()]));
			compiler.setVerbose(true);
			compiler.setDebugSource(true);
			compiler.setDebugLines(true);
			compiler.setDebugVars(true);
			compiler.setTargetVersion(8);
			ResourceCreatorImpl resourceCreator = new ResourceCreatorImpl();
			compiler.setClassFileCreator(resourceCreator);
			// print.e(">" + sc.getCharContent(true).toString() + "<");
			compiler.compile(new Resource[] { new StringResource(sc.getClassName(), sc.getCharContent(true).toString()) });
			return resourceCreator.getBytes(true);// TODO is there a more direct approch

		}
		catch (CompileException e) {
			Throwable cause = e.getCause();
			print.e(e);
			Location loc = e.getLocation();
			String msg = e.getLocalizedMessage();
			print.e(msg);
			int index = msg.indexOf(':');
			if (index != -1) msg = msg.substring(index + 1); // TODO is there a better way to do this?
			JavaCompilerException jce = new JavaCompilerException(msg, loc.getLineNumber(), loc.getColumnNumber(), null);
			if (cause != null) jce.initCause(cause);
			throw jce;
		}
		catch (Exception e) {
			print.e(e);
			throw Caster.toPageException(e);
		}
	}
}
