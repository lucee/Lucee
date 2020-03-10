/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.instrumentation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;

/**
 * Factory for obtaining an {@link Instrumentation} instance.
 */
public class InstrumentationFactory {
	// private static final String _name = InstrumentationFactory.class.getName();
	private static final String SEP = File.separator;
	private static final String TOOLS_VERSION = "7u25";
	private static final String AGENT_CLASS_NAME = "lucee.runtime.instrumentation.ExternalAgent";
	public static final Object lockToken = new Object();

	private static Instrumentation _instr;
	private static boolean init = false;

	public static Instrumentation getInstrumentation(final Config config) {
		if (!init) {
			_getInstrumentation(config);
			init = true;
		}
		return _instr;
	}

	private static synchronized Instrumentation _getInstrumentation(final Config config) {

		final Log log = config.getLog("application");
		// final CFMLEngine engine = ConfigWebUtil.getEngine(config);
		Instrumentation instr = _getInstrumentation(log, config);

		// agent already exist
		if (instr != null) return instr;

		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			@Override
			public Object run() {
				ClassLoader ccl = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
				try {

					JavaVendor vendor = JavaVendor.getCurrentVendor();
					Resource toolsJar = null;
					// When running on IBM, the attach api classes are packaged in vm.jar which is a part
					// of the default vm classpath.
					RefBoolean useOurOwn = new RefBooleanImpl(true);
					// if (!vendor.isIBM()) {
					// If we can't find the tools.jar and we're not on IBM we can't load the agent.
					toolsJar = findToolsJar(config, log, useOurOwn);
					if (toolsJar == null) {
						return null;
					}
					// }
					log.info("Instrumentation", "tools.jar used:" + toolsJar);

					// add the attach native library
					if (useOurOwn.toBooleanValue()) addAttachIfNecessary(config, log);

					Class<?> vmClass = loadVMClass(toolsJar, log, vendor);
					log.info("Instrumentation", "loaded VirtualMachine class:" + (vmClass == null ? "null" : vmClass.getName()));
					if (vmClass == null) {
						return null;
					}
					String agentPath = createAgentJar(log, config).getAbsolutePath();
					if (agentPath == null) {
						return null;
					}
					log.info("Instrumentation", "try to load agent (path:" + agentPath + ")");
					loadAgent(config, log, agentPath, vmClass);
					// log.info("Instrumentation","agent loaded (path:"+agentPath+")");

				}
				catch (IOException ioe) {
					log.log(Log.LEVEL_INFO, "Instrumentation", ioe);
				}
				finally {
					Thread.currentThread().setContextClassLoader(ccl);
				}
				return null;
			}// end run()
		});
		// If the load(...) agent call was successful, this variable will no
		// longer be null.
		instr = _getInstrumentation(log, config);
		if (instr == null) {
			instr = InstrumentationFactoryExternal.install();
		}
		if (instr == null) {
			try {
				boolean allowAttachSelf = Caster.toBooleanValue(System.getProperty("jdk.attach.allowAttachSelf"), false);
				Resource agentJar = createAgentJar(log, config);

				throw new PageRuntimeException(new ApplicationException(
						Constants.NAME + " was not able to load an Agent dynamically! " + "You may add this manually by adding the following to your JVM arguments [-javaagent:\""
								+ (agentJar) + "\"] " + (allowAttachSelf ? "." : "or supply -Djdk.attach.allowAttachSelf as system property.")));
			}
			catch (IOException ioe) {
				LogUtil.log(ThreadLocalPageContext.getConfig(config), InstrumentationFactory.class.getName(), ioe);
			}
		}
		return instr;
	}

	private static Instrumentation _getInstrumentation(Log log, Config config) {
		if (_instr != null) return _instr;

		// try to get from different Classloaders
		_instr = _getInstrumentation(ClassLoader.getSystemClassLoader(), log);
		if (_instr != null) return _instr;

		_instr = _getInstrumentation(CFMLEngineFactory.class.getClassLoader(), log);
		if (_instr != null) return _instr;

		_instr = _getInstrumentation(config.getClassLoader(), log);
		return _instr;
	}

	private static Instrumentation _getInstrumentation(ClassLoader cl, Log log) {
		// get Class
		Class<?> clazz = ClassUtil.loadClass(cl, AGENT_CLASS_NAME, null);
		if (clazz != null) {
			log.info("Instrumentation", "found [lucee.runtime.instrumentation.ExternalAgent] in ClassLoader [" + clazz.getClassLoader() + "]");
		}
		else {
			log.info("Instrumentation", "not found [lucee.runtime.instrumentation.ExternalAgent] in ClassLoader [" + cl + "]");
			return null;
		}

		try {
			Method m = clazz.getMethod("getInstrumentation", new Class[0]);
			_instr = (Instrumentation) m.invoke(null, new Object[0]);

			log.info("Instrumentation", "ExternalAgent does " + (_instr != null ? "" : "not ") + "contain an Instrumentation instance");

			return _instr;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log.log(Log.LEVEL_INFO, "Instrumentation", t);
		}
		return null;
	}

	private static Resource createAgentJar(Log log, Config c) throws IOException {
		Resource trg = getDeployDirectory(c).getRealResource("lucee-external-agent.jar");

		if (!trg.exists() || trg.length() == 0) {
			log.info("Instrumentation", "create " + trg);
			InputStream jar = InfoImpl.class.getResourceAsStream("/resource/lib/lucee-external-agent.jar");
			if (jar == null) {
				throw new IOException("could not load jar [/resource/lib/lucee-external-agent.jar]");
			}

			IOUtil.copy(jar, trg, true);
		}
		return trg;
	}

	private static Resource createToolsJar(Config config) throws IOException {
		Resource dir = getDeployDirectory(config);

		String os = "bsd"; // used for Mac OS X
		if (SystemUtil.isWindows()) {
			os = "windows";
		}
		else if (SystemUtil.isLinux()) { // not MacOSX
			os = "linux";
		}
		else if (SystemUtil.isSolaris()) {
			os = "solaris";
		}
		String name = "tools-" + os + "-" + TOOLS_VERSION + ".jar";
		Resource trg = dir.getRealResource(name);

		if (!trg.exists() || trg.length() == 0) {

			InputStream jar = InfoImpl.class.getResourceAsStream("/resource/lib/" + name);
			IOUtil.copy(jar, trg, true);
		}
		return trg;
	}

	private static Resource getDeployDirectory(Config config) {
		Resource dir = ConfigWebUtil.getConfigServerDirectory(config);
		if (dir == null || !dir.isWriteable() || !dir.isReadable()) dir = ResourceUtil.toResource(CFMLEngineFactory.getClassLoaderRoot(SystemUtil.getLoaderClassLoader()));

		return dir;
	}

	private static Resource getBinDirectory(Config config) {
		Resource dir = ConfigWebUtil.getConfigServerDirectory(config);
		if (dir == null || !dir.isWriteable() || !dir.isReadable()) dir = ResourceUtil.toResource(CFMLEngineFactory.getClassLoaderRoot(SystemUtil.getLoaderClassLoader()));
		else {
			dir = dir.getRealResource("bin");
			if (!dir.exists()) dir.mkdir();
		}
		return dir;
	}

	/**
	 * This private worker method attempts to find [java_home]/lib/tools.jar. Note: The tools.jar is a
	 * part of the SDK, it is not present in the JRE.
	 * 
	 * @return If tools.jar can be found, a File representing tools.jar. <BR>
	 *         If tools.jar cannot be found, null.
	 */
	private static Resource findToolsJar(Config config, Log log, RefBoolean useOurOwn) {
		log.info("Instrumentation", "looking for tools.jar");
		String javaHome = System.getProperty("java.home");
		Resource javaHomeFile = ResourcesImpl.getFileResourceProvider().getResource(javaHome);

		Resource toolsJarFile = javaHomeFile.getRealResource("lib" + File.separator + "tools.jar");
		if (toolsJarFile.exists()) {
			useOurOwn.setValue(false);
			return toolsJarFile;
		}
		log.info("Instrumentation", "couldn't find tools.jar at: " + toolsJarFile.getAbsolutePath());

		// If we're on an IBM SDK, then remove /jre off of java.home and try again.
		if (javaHomeFile.getAbsolutePath().endsWith(SEP + "jre")) {
			javaHomeFile = javaHomeFile.getParentResource();
			toolsJarFile = javaHomeFile.getRealResource("lib" + SEP + "tools.jar");
			if (!toolsJarFile.exists()) {
				log.info("Instrumentation", "for IBM SDK couldn't find " + toolsJarFile.getAbsolutePath());
			}
			else {
				useOurOwn.setValue(false);
				return toolsJarFile;
			}
		}
		else if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
			// If we're on a Mac, then change the search path to use ../Classes/classes.jar.
			if (javaHomeFile.getAbsolutePath().endsWith(SEP + "Home")) {
				javaHomeFile = javaHomeFile.getParentResource();
				toolsJarFile = javaHomeFile.getRealResource("Classes" + SEP + "classes.jar");
				if (!toolsJarFile.exists()) {
					log.info("Instrumentation", "for Mac OS couldn't find " + toolsJarFile.getAbsolutePath());
				}
				else {
					useOurOwn.setValue(false);
					return toolsJarFile;
				}
			}
		}

		// if the engine could not find the tools.jar it is using it's own version
		try {
			toolsJarFile = createToolsJar(config);
		}
		catch (IOException e) {
			log.log(Log.LEVEL_INFO, "Instrumentation", e);
		}

		if (!toolsJarFile.exists()) {
			log.info("Instrumentation", "could not be created " + toolsJarFile.getAbsolutePath());
			return null;
		}
		log.info("Instrumentation", "found " + toolsJarFile.getAbsolutePath());
		return toolsJarFile;

	}

	/**
	 * Attach and load an agent class.
	 * 
	 * @param log Log used if the agent cannot be loaded.
	 * @param agentJar absolute path to the agent jar.
	 * @param vmClass VirtualMachine.class from tools.jar.
	 */
	private static void loadAgent(Config config, Log log, String agentJar, Class<?> vmClass) {
		try {

			// addAttach(config,log);

			// first obtain the PID of the currently-running process
			// ### this relies on the undocumented convention of the
			// RuntimeMXBean's
			// ### name starting with the PID, but there appears to be no other
			// ### way to obtain the current process' id, which we need for
			// ### the attach process
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			String pid = runtime.getName();
			if (pid.indexOf("@") != -1) pid = pid.substring(0, pid.indexOf("@"));
			log.info("Instrumentation", "pid:" + pid);
			// JDK1.6: now attach to the current VM so we can deploy a new agent
			// ### this is a Sun JVM specific feature; other JVMs may offer
			// ### this feature, but in an implementation-dependent way
			Object vm = vmClass.getMethod("attach", new Class<?>[] { String.class }).invoke(null, new Object[] { pid });
			// now deploy the actual agent, which will wind up calling
			// agentmain()
			vmClass.getMethod("loadAgent", new Class[] { String.class }).invoke(vm, new Object[] { agentJar });
			vmClass.getMethod("detach", new Class[] {}).invoke(vm, new Object[] {});
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			// Log the message from the exception. Don't log the entire
			// stack as this is expected when running on a JDK that doesn't
			// support the Attach API.
			log.log(Log.LEVEL_INFO, "Instrumentation", t);

		}
	}

	private static void addAttachIfNecessary(Config config, Log log) {

		String srcName = null, trgName = null;
		String archBits = (SystemUtil.getJREArch() == SystemUtil.ARCH_64) ? "64" : "32";

		// Windows
		if (SystemUtil.isWindows()) {
			trgName = "attach.dll";
			srcName = "windows" + archBits + "/" + trgName;
		}
		// Linux
		else if (SystemUtil.isLinux()) {
			trgName = "libattach.so";
			srcName = "linux" + archBits + "/" + trgName;
		}
		// Solaris
		else if (SystemUtil.isSolaris()) {
			trgName = "libattach.so";
			srcName = "solaris" + archBits + "/" + trgName;
		}
		// Mac OSX
		else if (SystemUtil.isMacOSX()) {
			trgName = "libattach.dylib";
			srcName = "macosx" + archBits + "/" + trgName;
		}

		if (srcName != null) {

			// create dll if necessary
			Resource binDir = getBinDirectory(config);
			Resource trg = binDir.getRealResource(trgName);
			if (!trg.exists() || trg.length() == 0) {
				log.info("Instrumentation", "deploy /resource/bin/" + srcName + " to " + trg);
				InputStream src = InfoImpl.class.getResourceAsStream("/resource/bin/" + srcName);
				try {
					IOUtil.copy(src, trg, true);
				}
				catch (IOException e) {
					log.log(Log.LEVEL_INFO, "Instrumentation", e);
				}
			}

			// set directory to library path
			SystemUtil.addLibraryPathIfNoExist(binDir, log);
		}
	}

	/**
	 * If <b>ibm</b> is false, this private method will create a new URLClassLoader and attempt to load
	 * the com.sun.tools.attach.VirtualMachine class from the provided toolsJar file.
	 * 
	 * <p>
	 * If <b>ibm</b> is true, this private method will ignore the toolsJar parameter and load the
	 * com.ibm.tools.attach.VirtualMachine class.
	 * 
	 * 
	 * @return The AttachAPI VirtualMachine class <br>
	 *         or null if something unexpected happened.
	 */
	private static Class<?> loadVMClass(Resource toolsJar, Log log, JavaVendor vendor) {
		try {
			ClassLoader loader = ClassLoader.getSystemClassLoader();
			String cls = vendor.getVirtualMachineClassName();
			// if (!vendor.isIBM()) {
			loader = new URLClassLoader(new URL[] { ((FileResource) toolsJar).toURI().toURL() }, loader);
			// }
			return loader.loadClass(cls);
		}
		catch (Exception e) {
			log.log(Log.LEVEL_INFO, "Instrumentation", e);

		}
		return null;
	}
}