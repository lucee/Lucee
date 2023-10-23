package lucee.runtime.engine;

import lucee.runtime.config.ConfigServer;

/**
 * class to handle thread local PageContext, do use pagecontext in classes that have no method
 * argument pagecontext
 */
public final class ThreadLocalConfigServer {

	private static ThreadLocal<ConfigServer> cThreadLocal = new ThreadLocal<ConfigServer>();

	/**
	 * register a Config for he current thread
	 * 
	 * @param config Config to register
	 */
	public static void register(ConfigServer config) {// DO NOT CHANGE, used in Ortus extension via reflection
		cThreadLocal.set(config);
	}

	/**
	 * returns Config registered for the current thread
	 * 
	 * @return Config for the current thread or null
	 */
	public static ConfigServer get() {
		return cThreadLocal.get();
	}

	/**
	 * release the pagecontext for the current thread
	 */
	public static void release() {// DO NOT CHANGE, used in Ortus extension via reflection
		cThreadLocal.set(null);
	}
}