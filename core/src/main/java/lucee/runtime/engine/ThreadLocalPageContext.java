/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigServerPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.listener.ModernApplicationContext;
import lucee.runtime.thread.ThreadUtil;

/**
 * class to handle thread local PageContext, do use pagecontext in classes that have no method
 * argument pagecontext
 */
public final class ThreadLocalPageContext {
	private static final boolean INHERIT_ENABLED = false;
	private static final Locale DEFAULT_LOCALE = Locale.getDefault();
	private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();
	private static ThreadLocal<PageContext> pcThreadLocal = new ThreadLocal<PageContext>();
	private static InheritableThreadLocal<PageContext> pcThreadLocalInheritable = new InheritableThreadLocal<PageContext>();
	public final static CallOnStart callOnStart = new CallOnStart();
	private static ThreadLocal<Boolean> insideServerNewInstance = new ThreadLocal<Boolean>();
	private static ThreadLocal<Boolean> insideGateway = new ThreadLocal<Boolean>();
	private static ThreadLocal<Boolean> insideInheritableRegistration = new ThreadLocal<Boolean>();
	// general purpose, thread scoped switch (default enabled); while disabled the ambient (thread bound)
	// PageContext is not consulted as a fallback, see fallback(boolean)
	private static ThreadLocal<Boolean> fallbackTL = new ThreadLocal<Boolean>();
	// CCL_UNSET sentinel distinguishes "never saved" from "saved a null CCL"; some boot/gateway threads legitimately have a null context classloader.
	private static final ClassLoader CCL_UNSET = new ClassLoader(null) {};
	private static ThreadLocal<ClassLoader> prevCCL = ThreadLocal.withInitial(() -> CCL_UNSET);

	/**
	 * Register a NEW or PARENT PageContext for the current thread.
	 *
	 * <p>
	 * <b>Use this method for:</b>
	 * </p>
	 * <ul>
	 * <li>Newly created parent PageContexts (via ThreadUtil.createPageContext())</li>
	 * <li>Restoring an original PageContext after temporary operations</li>
	 * <li>HTTP request processing with fresh contexts</li>
	 * <li>Any context that may spawn child threads</li>
	 * </ul>
	 *
	 * <p>
	 * <b>DO NOT use this method for cloned contexts!</b> If the PageContext was created via
	 * {@code clonePageContext()}, use {@link #registerChild(PageContext)} instead for optimal
	 * performance.
	 * </p>
	 *
	 * <p>
	 * This method writes to both regular and inheritable ThreadLocals, enabling child threads to
	 * inherit the PageContext. This is necessary for parent contexts but wasteful for short-lived
	 * worker contexts.
	 * </p>
	 *
	 * @param pc PageContext to register (must be NEW or parent context, not a clone)
	 * @see #registerChild(PageContext) for cloned contexts (better performance)
	 */
	public static void register(PageContext pc) {// print.ds(Thread.currentThread().getName());
		if (pc == null) {
			return; // TODO happens with Gateway, but should not!
		}
		Thread t = Thread.currentThread();
		ClassLoader target = ((ConfigPro) pc.getConfig()).getClassLoaderEnv();
		ClassLoader current = t.getContextClassLoader();
		// fast path: skip CCL save/swap when already on target (re-entrant register, or reused worker thread)
		if (current != target) {
			// first register on this thread captures the pre-Lucee CCL; nested register with a different target keeps the original save
			if (prevCCL.get() == CCL_UNSET) {
				prevCCL.set(current);
			}
			t.setContextClassLoader(target);
		}
		((PageContextImpl) pc).setThread(t);
		pcThreadLocal.set(pc);
		pcThreadLocalInheritable.set(pc);
	}

	/**
	 * Register a CLONED PageContext for the current thread.
	 *
	 * <p>
	 * <b>CRITICAL: Use this method ONLY for cloned PageContexts created via
	 * {@code ThreadUtil.clonePageContext()} or {@code PageContext.clonePageContext()}.</b>
	 * </p>
	 *
	 * <h3>When to Use registerChild():</h3>
	 * <ul>
	 * <li>Parallel closure execution (arrayEach, structEach with parallel=true)</li>
	 * <li>CFThread daemon mode operations</li>
	 * <li>Background query spooler tasks</li>
	 * <li>Any worker thread receiving a cloned PageContext from its parent</li>
	 * </ul>
	 *
	 * <h3>When to Use register() Instead:</h3>
	 * <ul>
	 * <li>Creating a NEW parent PageContext (not a clone)</li>
	 * <li>Restoring an original PageContext after temporary work</li>
	 * <li>HTTP request processing with fresh contexts</li>
	 * </ul>
	 *
	 * <h3>Performance Impact:</h3>
	 * <p>
	 * This method skips writing to {@code pcThreadLocalInheritable} because cloned contexts are
	 * short-lived worker contexts that don't spawn child threads. This optimization:
	 * </p>
	 * <ul>
	 * <li>Eliminates branch mispredictions in register() hot path (+31.7% perf gain)</li>
	 * <li>Reduces ThreadLocal write overhead for worker threads</li>
	 * <li>Maintains proper context isolation for nested parallel operations</li>
	 * </ul>
	 *
	 * <h3>Example Usage:</h3>
	 * 
	 * <pre>
	 * // CORRECT - Clone from parent, use registerChild()
	 * PageContext childPC = ThreadUtil.clonePageContext(parent, baos, false, false, false);
	 * ThreadLocalPageContext.registerChild(childPC);
	 *
	 * // INCORRECT - New context should use register()
	 * PageContext newPC = ThreadUtil.createPageContext(...);
	 * ThreadLocalPageContext.register(newPC); // NOT registerChild()
	 * </pre>
	 *
	 * @param pc Cloned PageContext to register (must be created via clonePageContext)
	 * @see #register(PageContext) for parent/new contexts
	 * @see ThreadUtil#clonePageContext for creating cloned contexts
	 * @since Lucee 7.1 (LDEV-5923 - ThreadLocal optimization)
	 */
	public static void registerChild(PageContext pc) {
		if (pc == null) {
			return; // TODO happens with Gateway, but should not!
		}
		Thread t = Thread.currentThread();
		ClassLoader target = ((ConfigPro) pc.getConfig()).getClassLoaderEnv();
		ClassLoader current = t.getContextClassLoader();
		// fast path: skip CCL save/swap when already on target (re-entrant register, or reused worker thread)
		if (current != target) {
			// first register on this thread captures the pre-Lucee CCL; nested register with a different target keeps the original save
			if (prevCCL.get() == CCL_UNSET) {
				prevCCL.set(current);
			}
			t.setContextClassLoader(target);
		}
		((PageContextImpl) pc).setThread(t);
		pcThreadLocal.set(pc);
		// Skip pcThreadLocalInheritable - child contexts don't need inheritance
	}

	/**
	 * Controls whether the ambient (thread bound) PageContext is consulted as a fallback when no
	 * PageContext is passed in explicitly. This is a general purpose, thread scoped switch: while
	 * disabled every "grab the PageContext from the current thread" lookup behaves as if no
	 * PageContext is registered, so callers resolve deterministic config/server defaults instead of
	 * inheriting state from whatever request happens to run on the thread. Use it wherever the result
	 * must not depend on the calling request, e.g. while compiling or other cache shared computations.
	 *
	 * <p>
	 * The switch is sticky on the thread, so always restore the previous value in a finally block
	 * (this also makes it safe to nest):
	 * </p>
	 *
	 * <pre>
	 * boolean prev = ThreadLocalPageContext.fallback(false);
	 * try {
	 * 	...
	 * }
	 * finally {
	 * 	ThreadLocalPageContext.fallback(prev);
	 * }
	 * </pre>
	 *
	 * @param enable true to allow ambient lookups (the default), false to disable them
	 * @return the previous value, so it can be restored
	 */
	public static boolean fallback(boolean enable) {
		Boolean prev = fallbackTL.get();
		fallbackTL.set(enable ? Boolean.TRUE : Boolean.FALSE);
		return prev == null || prev.booleanValue();
	}

	private static boolean fallbackEnabled() {
		Boolean b = fallbackTL.get();
		return b == null || b.booleanValue();
	}

	// gated access to the thread bound PageContext, honoring fallback(boolean)
	private static PageContext pcCurrent() {
		return fallbackEnabled() ? pcThreadLocal.get() : null;
	}

	private static PageContext pcParent() {
		return fallbackEnabled() ? pcThreadLocalInheritable.get() : null;
	}

	public static PageContext get() {
		// print.ds(4, 2);
		return get(false);
	}

	/**
	 * returns pagecontext registered for the current thread
	 * 
	 * @return pagecontext for the current thread or null if no pagecontext is regisred for the current
	 *         thread
	 */
	public static PageContext get(boolean cloneParentIfNotExist) {
		PageContext pc = pcCurrent();
		if (cloneParentIfNotExist && pc == null) {
			PageContext pci = pcParent();
			// we have one from parent
			if (pci != null && pci.getRequest() != null) {
				try {
					// this is needed because clone below call this method a lot
					if (Boolean.TRUE.equals(insideInheritableRegistration.get())) return pci;
					insideInheritableRegistration.set(Boolean.TRUE);
					pc = ThreadUtil.clonePageContext(pci, new ByteArrayOutputStream(), true, false, false);
					// register as child since we cloned from parent
					registerChild(pc);
				}
				finally {
					insideInheritableRegistration.set(null);
				}

			}
		}
		return pc;
	}

	public static ConfigServerPro getConfigServer() {
		return ConfigServerImpl.instance;
	}

	public static Config getConfig() {
		PageContext pc = get(false);
		if (pc != null) {
			return pc.getConfig();
		}
		Config c = ThreadLocalConfig.get();
		if (c != null) return c;

		return ConfigServerImpl.instance;
	}

	public static ConfigWebPro getConfigWeb(PageContext pc) {
		if (pc != null) return (ConfigWebPro) pc.getConfig();
		return getConfigWeb();
	}

	public static ConfigWebPro getConfigWeb(Config c) {
		if (c instanceof ConfigWebPro) return (ConfigWebPro) c;
		if (c instanceof ConfigServer) {
			ConfigWeb[] webs = ((ConfigServer) c).getConfigWebs();
			if (webs.length == 1) return (ConfigWebPro) webs[0];
		}
		return getConfigWeb();
	}

	public static ConfigWebPro getConfigWeb() {
		PageContext pc = get(false);
		if (pc != null) {
			return (ConfigWebPro) pc.getConfig();
		}
		Config c = ThreadLocalConfig.get();
		if (c instanceof ConfigWebPro) return (ConfigWebPro) c;

		return null;
	}

	/**
	 * release the pagecontext for the current thread
	 */
	public static void release() {
		pcThreadLocal.set(null);
		pcThreadLocalInheritable.set(null);
		ClassLoader prev = prevCCL.get();
		if (prev != CCL_UNSET) {
			Thread.currentThread().setContextClassLoader(prev);
			prevCCL.remove();
		}
	}

	public static Config getConfig(PageContext pc) {
		if (pc == null) return getConfig();
		return pc.getConfig();
	}

	public static ConfigServerPro getConfigServer(PageContext pc) {
		if (pc == null) return getConfigServer();
		return ((ConfigWebImpl) pc.getConfig()).getConfigServerImpl();
	}

	public static Config getConfig(Config config) {
		if (config == null) return getConfig();
		return config;
	}

	public static ConfigServerPro getConfigServer(Config config) {
		if (config == null) return getConfigServer();
		if (config instanceof ConfigServerPro) return (ConfigServerPro) config;
		return ((ConfigWebImpl) config).getConfigServerImpl();
	}

	public static boolean preciseMath(PageContext pc) {
		// pc provided
		if (pc != null) return (pc.getApplicationContext()).getPreciseMath();

		if (ModernApplicationContext.hasCustomPreciseMath) {
			// pc from current thread
			pc = pcCurrent();
			if (pc != null) return (pc.getApplicationContext()).getPreciseMath();

			// pc from parent thread
			pc = pcParent();
			if (pc != null) return (pc.getApplicationContext()).getPreciseMath();
		}
		ConfigServerPro c = getConfigServer();
		if (c != null) return c.getPreciseMath();
		return true;
	}

	// LOG

	public static Log getLog(PageContext pc, String logName) {
		return getLog(pc, logName, true);
	}

	public static Log getLog(PageContext pc, String logName, boolean createIfNecessary) {
		// pc provided
		if (pc instanceof PageContextImpl) {
			return ((PageContextImpl) pc).getLog(logName, createIfNecessary);
		}
		// pc from current thread
		pc = pcCurrent();
		if (pc instanceof PageContextImpl) {
			return ((PageContextImpl) pc).getLog(logName, createIfNecessary);
		}

		// pc from parent thread
		pc = pcParent();
		if (pc instanceof PageContextImpl) {
			return ((PageContextImpl) pc).getLog(logName, createIfNecessary);
		}

		return getLog((Config) null, logName, createIfNecessary);
	}

	public static Log getLog(Config config, String logName) {
		return getLog(config, logName, true);
	}

	public static Log getLog(Config config, String logName, boolean createIfNecessary) {

		// config
		ConfigServerPro cs = getConfigServer(config);
		if (cs != null) {
			try {
				return cs.getLog(logName, createIfNecessary);
			}
			catch (Exception e) {
				return cs.getLog(logName);
			}
		}
		return null;
	}

	public static Log getLog(String logName) {
		return getLog((PageContext) null, logName, true);
	}

	public static Log getLog(String logName, boolean createIfNecessary) {
		return getLog((PageContext) null, logName, createIfNecessary);
	}

	// LOCALE

	public static Locale getLocale(PageContext pc) {
		// pc provided
		if (pc != null) {
			Locale l = pc.getLocale();
			if (l != null) return l;
			return DEFAULT_LOCALE;
		}
		// pc from current thread
		pc = pcCurrent();
		if (pc != null) {
			Locale l = pc.getLocale();
			if (l != null) return l;
			return DEFAULT_LOCALE;
		}

		// pc from parent thread
		pc = pcParent();
		if (pc != null) {
			Locale l = pc.getLocale();
			if (l != null) return l;
			return DEFAULT_LOCALE;
		}

		return getLocale((Config) null);
	}

	public static Locale getLocale(Config config) {
		// config
		ConfigServerPro cs = getConfigServer((Config) null);
		if (cs != null) {
			Locale l = cs.getLocale();
			if (l != null) return l;
		}
		return DEFAULT_LOCALE;
	}

	public static Locale getLocale() {
		return getLocale((PageContext) null);
	}

	public static Locale getLocale(Locale locale) {
		if (locale != null) return locale;
		return getLocale((PageContext) null);
	}

	// TIMEZONE

	public static TimeZone getTimeZone(PageContext pc) {
		// pc provided
		if (pc != null) {
			TimeZone tz = pc.getTimeZone();
			if (tz != null) return tz;
			return DEFAULT_TIMEZONE;
		}
		// pc from current thread
		pc = pcCurrent();
		if (pc != null) {
			TimeZone tz = pc.getTimeZone();
			if (tz != null) return tz;
			return DEFAULT_TIMEZONE;
		}

		// pc from parent thread
		pc = pcParent();
		if (pc != null) {
			TimeZone tz = pc.getTimeZone();
			if (tz != null) return tz;
			return DEFAULT_TIMEZONE;
		}

		return getTimeZone((Config) null);
	}

	public static TimeZone getTimeZone(Config config) {
		ConfigServerPro cs = getConfigServer(config);
		if (cs != null && cs.getTimeZone() != null) {
			return cs.getTimeZone();
		}
		return DEFAULT_TIMEZONE;
	}

	public static TimeZone getTimeZone(TimeZone timezone) {
		if (timezone != null) return timezone;
		return getTimeZone((PageContext) null);
	}

	public static TimeZone getTimeZone() {
		return getTimeZone((PageContext) null);
	}

	// CLASSLOADER

	public static ClassLoader getRPCClassLoader(PageContext pc, boolean reload) throws IOException {
		// pc provided
		if (pc != null) {
			ClassLoader cl = ((PageContextImpl) pc).getRPCClassLoader(reload);
			if (cl != null) return cl;
			return SystemUtil.getCoreClassLoader();
		}
		// pc from current thread
		pc = pcCurrent();
		if (pc != null) {
			ClassLoader cl = ((PageContextImpl) pc).getRPCClassLoader();
			if (cl != null) return cl;
			return SystemUtil.getCoreClassLoader();
		}

		// pc from parent thread
		pc = pcParent();
		if (pc != null) {
			ClassLoader cl = ((PageContextImpl) pc).getRPCClassLoader();
			if (cl != null) return cl;
			return SystemUtil.getCoreClassLoader();
		}

		return getRPCClassLoader((Config) null, reload);
	}

	public static ClassLoader getRPCClassLoader(Config config, boolean reload) throws IOException {
		ConfigServerPro cs = getConfigServer((Config) null);
		if (cs != null) {
			ClassLoader cl = cs.getRPCClassLoader(reload);
			if (cl != null) return cl;
		}
		return SystemUtil.getCoreClassLoader();
	}

	public static ClassLoader getRPCClassLoader(boolean reload) throws IOException {
		return getRPCClassLoader((PageContext) null, reload);
	}

	public static int getId() {
		PageContext pc = pcCurrent();
		if (pc != null) return pc.getId();
		throw new NullPointerException("cannot provide the id, because there is no PageContext for this thread");
	}

	public static int getId(PageContext pc) {
		if (pc != null) return pc.getId();
		return getId();
	}

	public static PageContext get(PageContext pc) {
		if (pc == null) {
			return get();
		}
		return pc;
	}

	public static PageContext get(Config config) {
		PageContext pc = get();
		if (pc != null && pc.getConfig() == config) return pc;
		return null;
	}

	public static class CallOnStart extends ThreadLocal<Boolean> {

		@Override
		protected Boolean initialValue() {
			return Boolean.TRUE;
		}

	}

	public static long getThreadId(PageContext pc) {
		if (pc != null) return pc.getThread().getId();
		return Thread.currentThread().getId();
	}

	public static boolean insideServerNewInstance() {
		Boolean b = insideServerNewInstance.get();
		return b != null && b.booleanValue();
	}

	public static void insideServerNewInstance(boolean inside) {
		insideServerNewInstance.set(inside);
	}

	public static boolean insideGateway() {
		Boolean b = insideGateway.get();
		return b != null && b.booleanValue();
	}

	public static void insideGateway(boolean inside) {
		insideGateway.set(inside);
	}
}