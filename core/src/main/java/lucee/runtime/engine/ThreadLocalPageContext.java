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
import lucee.runtime.config.ConfigServerImpl;
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
	// CCL_UNSET sentinel distinguishes "never saved" from "saved a null CCL"; some boot/gateway threads legitimately have a null context classloader.
	private static final ClassLoader CCL_UNSET = new ClassLoader(null) {};
	private static ThreadLocal<ClassLoader> prevCCL = ThreadLocal.withInitial(() -> CCL_UNSET);

	/**
	 * Register a NEW or PARENT PageContext for the current thread.
	 *
	 * <p><b>Use this method for:</b></p>
	 * <ul>
	 *   <li>Newly created parent PageContexts (via ThreadUtil.createPageContext())</li>
	 *   <li>Restoring an original PageContext after temporary operations</li>
	 *   <li>HTTP request processing with fresh contexts</li>
	 *   <li>Any context that may spawn child threads</li>
	 * </ul>
	 *
	 * <p><b>DO NOT use this method for cloned contexts!</b> If the PageContext was created via
	 * {@code clonePageContext()}, use {@link #registerChild(PageContext)} instead for optimal
	 * performance.</p>
	 *
	 * <p>This method writes to both regular and inheritable ThreadLocals, enabling child threads
	 * to inherit the PageContext. This is necessary for parent contexts but wasteful for
	 * short-lived worker contexts.</p>
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
	 * <p><b>CRITICAL: Use this method ONLY for cloned PageContexts created via
	 * {@code ThreadUtil.clonePageContext()} or {@code PageContext.clonePageContext()}.</b></p>
	 *
	 * <h3>When to Use registerChild():</h3>
	 * <ul>
	 *   <li>Parallel closure execution (arrayEach, structEach with parallel=true)</li>
	 *   <li>CFThread daemon mode operations</li>
	 *   <li>Background query spooler tasks</li>
	 *   <li>Any worker thread receiving a cloned PageContext from its parent</li>
	 * </ul>
	 *
	 * <h3>When to Use register() Instead:</h3>
	 * <ul>
	 *   <li>Creating a NEW parent PageContext (not a clone)</li>
	 *   <li>Restoring an original PageContext after temporary work</li>
	 *   <li>HTTP request processing with fresh contexts</li>
	 * </ul>
	 *
	 * <h3>Performance Impact:</h3>
	 * <p>This method skips writing to {@code pcThreadLocalInheritable} because cloned contexts
	 * are short-lived worker contexts that don't spawn child threads. This optimization:</p>
	 * <ul>
	 *   <li>Eliminates branch mispredictions in register() hot path (+31.7% perf gain)</li>
	 *   <li>Reduces ThreadLocal write overhead for worker threads</li>
	 *   <li>Maintains proper context isolation for nested parallel operations</li>
	 * </ul>
	 *
	 * <h3>Example Usage:</h3>
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

	public static PageContext get() {
		return get(false);
	}

	/**
	 * returns pagecontext registered for the current thread
	 * 
	 * @return pagecontext for the current thread or null if no pagecontext is regisred for the current
	 *         thread
	 */
	public static PageContext get(boolean cloneParentIfNotExist) {
		PageContext pc = pcThreadLocal.get();
		if (cloneParentIfNotExist && pc == null) {
			PageContext pci = pcThreadLocalInheritable.get();
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

	public static Config getConfig() {
		PageContext pc = get(false);
		if (pc != null) {
			return pc.getConfig();
		}
		Config c = ThreadLocalConfig.get();
		if (c != null) return c;

		return ConfigServerImpl.instance;
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

	public static Config getConfig(Config config) {
		if (config == null) return getConfig();
		return config;
	}

	public static boolean preciseMath(PageContext pc) {
		// pc provided
		if (pc != null) return (pc.getApplicationContext()).getPreciseMath();

		// pc from current thread
		pc = pcThreadLocal.get();
		if (pc != null) return (pc.getApplicationContext()).getPreciseMath();

		// pc from parent thread
		pc = pcThreadLocalInheritable.get();
		if (pc != null) return (pc.getApplicationContext()).getPreciseMath();

		Config c = ThreadLocalConfig.get();
		if (c instanceof ConfigPro) return ((ConfigPro) c).getPreciseMath();
		return true;
	}

	public static TimeZone getTimeZone(PageContext pc) {
		// pc provided
		if (pc != null) {
			TimeZone tz = pc.getTimeZone();
			if (tz != null) return tz;
			return DEFAULT_TIMEZONE;
		}
		// pc from current thread
		pc = pcThreadLocal.get();
		if (pc != null) {
			TimeZone tz = pc.getTimeZone();
			if (tz != null) return tz;
			return DEFAULT_TIMEZONE;
		}

		// pc from parent thread
		pc = pcThreadLocalInheritable.get();
		if (pc != null) {
			TimeZone tz = pc.getTimeZone();
			if (tz != null) return tz;
			return DEFAULT_TIMEZONE;
		}

		// config
		Config config = getConfig((Config) null);
		if (config != null) {
			TimeZone tz = config.getTimeZone();
			if (tz != null) return tz;
		}
		return DEFAULT_TIMEZONE;
	}

	public static Log getLog(PageContext pc, String logName) {
		return getLog(pc, logName, true);
	}

	public static Log getLog(PageContext pc, String logName, boolean createIfNecessary) {
		// pc provided
		if (pc instanceof PageContextImpl) {
			return ((PageContextImpl) pc).getLog(logName, createIfNecessary);
		}
		// pc from current thread
		pc = pcThreadLocal.get();
		if (pc instanceof PageContextImpl) {
			return ((PageContextImpl) pc).getLog(logName, createIfNecessary);
		}

		// pc from parent thread
		pc = pcThreadLocalInheritable.get();
		if (pc instanceof PageContextImpl) {
			return ((PageContextImpl) pc).getLog(logName, createIfNecessary);
		}

		// config
		Config config = getConfig(pc);
		if (config != null) {
			try {
				return ((ConfigPro) config).getLog(logName, createIfNecessary);
			}
			catch (Exception e) {
				return config.getLog(logName);
			}
		}
		return null;
	}

	public static Log getLog(Config config, String logName) {
		return getLog(config, logName, true);
	}

	public static Log getLog(Config config, String logName, boolean createIfNecessary) {

		// pc from current thread
		PageContext pc = pcThreadLocal.get();
		if (pc instanceof PageContextImpl && pc.getConfig() == config) {
			return ((PageContextImpl) pc).getLog(logName, createIfNecessary);
		}

		// pc from parent thread
		pc = pcThreadLocalInheritable.get();
		if (pc instanceof PageContextImpl && pc.getConfig() == config) {
			return ((PageContextImpl) pc).getLog(logName, createIfNecessary);
		}

		// config
		config = getConfig(config);
		if (config != null) {
			if (config instanceof ConfigPro) {
				try {
					return ((ConfigPro) config).getLog(logName, createIfNecessary);
				}
				catch (Exception e) {
					return config.getLog(logName);
				}
			}
			return config.getLog(logName);
		}
		return null;
	}

	public static Log getLog(String logName) {
		return getLog((PageContext) null, logName, true);
	}

	public static Log getLog(String logName, boolean createIfNecessary) {
		return getLog((PageContext) null, logName, createIfNecessary);
	}

	public static Locale getLocale() {
		return getLocale((PageContext) null);
	}

	public static Locale getLocale(Locale l) {
		if (l != null) return l;
		return getLocale((PageContext) null);
	}

	public static Locale getLocale(PageContext pc) {
		// pc provided
		if (pc != null) {
			Locale l = pc.getLocale();
			if (l != null) return l;
			return DEFAULT_LOCALE;
		}
		// pc from current thread
		pc = pcThreadLocal.get();
		if (pc != null) {
			Locale l = pc.getLocale();
			if (l != null) return l;
			return DEFAULT_LOCALE;
		}

		// pc from parent thread
		pc = pcThreadLocalInheritable.get();
		if (pc != null) {
			Locale l = pc.getLocale();
			if (l != null) return l;
			return DEFAULT_LOCALE;
		}

		// config
		Config config = getConfig((Config) null);
		if (config != null) {
			Locale l = config.getLocale();
			if (l != null) return l;
		}
		return DEFAULT_LOCALE;
	}

	public static TimeZone getTimeZone(Config config) {
		// pc from current thread
		PageContext pc = pcThreadLocal.get();
		if (pc instanceof PageContextImpl && pc.getConfig() == config) {
			TimeZone tz = pc.getTimeZone();
			if (tz != null) return tz;
			return DEFAULT_TIMEZONE;
		}

		// pc from parent thread
		pc = pcThreadLocalInheritable.get();
		if (pc instanceof PageContextImpl && pc.getConfig() == config) {
			TimeZone tz = pc.getTimeZone();
			if (tz != null) return tz;
			return DEFAULT_TIMEZONE;
		}

		config = getConfig(config);
		if (config != null && config.getTimeZone() != null) {
			return config.getTimeZone();
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

	public static ClassLoader getRPCClassLoader(PageContext pc, boolean reload) throws IOException {
		// pc provided
		if (pc != null) {
			ClassLoader cl = ((PageContextImpl) pc).getRPCClassLoader(reload);
			if (cl != null) return cl;
			return SystemUtil.getCoreClassLoader();
		}
		// pc from current thread
		pc = pcThreadLocal.get();
		if (pc != null) {
			ClassLoader cl = ((PageContextImpl) pc).getRPCClassLoader();
			if (cl != null) return cl;
			return SystemUtil.getCoreClassLoader();
		}

		// pc from parent thread
		pc = pcThreadLocalInheritable.get();
		if (pc != null) {
			ClassLoader cl = ((PageContextImpl) pc).getRPCClassLoader();
			if (cl != null) return cl;
			return SystemUtil.getCoreClassLoader();
		}

		// config
		Config config = getConfig((Config) null);
		if (config != null) {
			ClassLoader cl = config.getRPCClassLoader(reload);
			if (cl != null) return cl;
		}
		return SystemUtil.getCoreClassLoader();
	}

	public static ClassLoader getRPCClassLoader(boolean reload) throws IOException {
		return getRPCClassLoader((PageContext) null, reload);
	}

	public static int getId() {
		PageContext pc = pcThreadLocal.get();
		if (pc != null) return pc.getId();
		throw new NullPointerException("cannot provide the id, because there is no PageContext for this thread");
	}

	public static int getId(PageContext pc) {
		if (pc != null) return pc.getId();
		return getId();
	}

	public static PageContext get(PageContext pc) {
		if (pc == null) return get();
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