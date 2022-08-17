package lucee.runtime.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import lucee.commons.io.log.Log;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;

public class LogClassLoading {
	private static Log log;
	private static String logName;

	public static void enable(Config config) {
		enable(config, null);
	}

	public static void enable(Config config, String logName) {
		if (StringUtil.isEmpty(logName)) logName = "application";
		if (LogClassLoading.logName == null) {
			InstrumentationFactory.getInstrumentation(config).addTransformer(new LogClassFileTransformer());
			log = ThreadLocalPageContext.getLog(config, logName);
		}
		else if (!LogClassLoading.logName.equalsIgnoreCase(logName)) {
			log = ThreadLocalPageContext.getLog(config, logName);
		}

	}

	private static class LogClassFileTransformer implements ClassFileTransformer {

		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
				throws IllegalClassFormatException {
			String line = "{'loader':'" + loader + "','class':'" + className + "','classBeingRedefined':'" + (classBeingRedefined == null ? "" : classBeingRedefined.getName())
					+ "'}";
			log.info("class-loading", line);
			return null;
		}

		/*
		 * public byte[] transform(Module module, ClassLoader loader, String className, Class<?>
		 * classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws
		 * IllegalClassFormatException { print.e(loader + ":" + className); return null; }
		 */

	}
}
