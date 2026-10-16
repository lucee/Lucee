package lucee.commons.lang.compiler;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicReference;

import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.PageException;
import lucee.runtime.mvn.POM;
import lucee.runtime.op.Caster;

public final class JaninoCompiler implements Compiler {

	private static final String GROUP_ID = "org.codehaus.janino";
	private static final String ARTIFACT_JANINO = "janino";
	private static final String ARTIFACT_CC = "commons-compiler";
	private static final String VERSION = "3.1.12";

	private static final AtomicReference<ClassLoader> janinoLoader = new AtomicReference<>();

	private ConfigPro config;
	private Exception loadFailure;

	public JaninoCompiler(ConfigPro config) {
		this.config = config;
		try {
			loadJanino(config);
		}
		catch (Exception e) {
			this.loadFailure = e;
		}
	}

	private static ClassLoader loadJanino(ConfigPro config) throws Exception {
		ClassLoader existing = janinoLoader.get();
		if (existing != null) return existing;

		Resource mavenDir = config.getMavenDir();
		mavenDir.mkdirs();

		POM pomJanino = POM.getInstance(mavenDir, GROUP_ID, ARTIFACT_JANINO, VERSION, LogUtil.getLog(config, "compiler", "application"));
		POM pomCC = POM.getInstance(mavenDir, GROUP_ID, ARTIFACT_CC, VERSION, LogUtil.getLog(config, "compiler", "application"));

		Resource[] jarsJanino = pomJanino.getJars();
		Resource[] jarsCC = pomCC.getJars();

		URL[] urls = new URL[jarsJanino.length + jarsCC.length];
		int i = 0;
		for (Resource r: jarsJanino)
			urls[i++] = ResourceUtil.toURL(r);
		for (Resource r: jarsCC)
			urls[i++] = ResourceUtil.toURL(r);

		ClassLoader cl = new URLClassLoader(urls, JaninoCompiler.class.getClassLoader());
		janinoLoader.compareAndSet(null, cl);
		return janinoLoader.get();
	}

	public Exception getLoadFailure() {
		return loadFailure;
	}

	@Override
	public boolean supported() {
		return loadFailure == null;
	}

	@Override
	public byte[] compile(ConfigPro config, SourceCode sc) throws PageException, JavaCompilerException {
		try {
			ClassLoader cl = loadJanino(config);

			// org.codehaus.commons.compiler.util.resource.ResourceFinder.EMPTY_RESOURCE_FINDER
			Class<?> rfClass = cl.loadClass("org.codehaus.commons.compiler.util.resource.ResourceFinder");
			Object emptyRF = rfClass.getField("EMPTY_RESOURCE_FINDER").get(null);

			// new ClassLoaderIClassLoader(new DynamicClassLoader(config.getClassLoaderEnv()))
			DynamicClassLoader dcl = new DynamicClassLoader(config.getClassLoaderEnv());
			Class<?> clcl = cl.loadClass("org.codehaus.janino.ClassLoaderIClassLoader");
			Object iclassLoader = clcl.getConstructor(ClassLoader.class).newInstance(dcl);

			// new org.codehaus.janino.Compiler(rf, clcl)
			Class<?> compilerClass = cl.loadClass("org.codehaus.janino.Compiler");
			Object compiler = compilerClass.getConstructor(rfClass, cl.loadClass("org.codehaus.janino.IClassLoader")).newInstance(emptyRF, iclassLoader);

			compilerClass.getMethod("setVerbose", boolean.class).invoke(compiler, true);
			compilerClass.getMethod("setDebugSource", boolean.class).invoke(compiler, true);
			compilerClass.getMethod("setDebugLines", boolean.class).invoke(compiler, true);
			compilerClass.getMethod("setDebugVars", boolean.class).invoke(compiler, true);
			compilerClass.getMethod("setTargetVersion", int.class).invoke(compiler, 8);

			// ResourceCreator via dynamic proxy
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Class<?> resourceCreatorIface = cl.loadClass("org.codehaus.commons.compiler.util.resource.ResourceCreator");
			Object resourceCreator = Proxy.newProxyInstance(cl, new Class<?>[] { resourceCreatorIface }, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					if ("createResource".equals(method.getName())) return baos;
					if ("deleteResource".equals(method.getName())) return Boolean.TRUE;
					return null;
				}
			});
			compilerClass.getMethod("setClassFileCreator", resourceCreatorIface).invoke(compiler, resourceCreator);

			// new StringResource(className, source)
			Class<?> stringResourceClass = cl.loadClass("org.codehaus.commons.compiler.util.resource.StringResource");
			Object stringResource = stringResourceClass.getConstructor(String.class, String.class).newInstance(sc.getClassName(), sc.getCharContent(true).toString());

			Class<?> resourceClass = cl.loadClass("org.codehaus.commons.compiler.util.resource.Resource");
			Object resourceArray = java.lang.reflect.Array.newInstance(resourceClass, 1);
			java.lang.reflect.Array.set(resourceArray, 0, stringResource);

			compilerClass.getMethod("compile", resourceArray.getClass()).invoke(compiler, resourceArray);

			return baos.toByteArray();

		}
		catch (JavaCompilerException jce) {
			throw jce;
		}
		catch (java.lang.reflect.InvocationTargetException ite) {
			Throwable cause = ite.getCause();
			// org.codehaus.commons.compiler.CompileException
			if (cause != null && cause.getClass().getName().equals("org.codehaus.commons.compiler.CompileException")) {
				try {
					Object loc = cause.getClass().getMethod("getLocation").invoke(cause);
					int line = (int) loc.getClass().getMethod("getLineNumber").invoke(loc);
					int col = (int) loc.getClass().getMethod("getColumnNumber").invoke(loc);
					String msg = cause.getLocalizedMessage();
					int idx = msg.indexOf(':');
					if (idx != -1) msg = msg.substring(idx + 1);
					JavaCompilerException jce = new JavaCompilerException(msg, line, col, null);
					jce.initCause(cause.getCause());
					throw jce;
				}
				catch (JavaCompilerException jce) {
					throw jce;
				}
				catch (Exception e2) {
					throw Caster.toPageException(cause);
				}
			}
			throw Caster.toPageException(cause != null ? cause : ite);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
}
