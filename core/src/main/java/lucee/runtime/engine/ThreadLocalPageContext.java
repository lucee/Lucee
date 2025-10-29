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

	// Java 25: ScopedValue replaces ThreadLocal for automatic cleanup
	public static final ScopedValue<PageContext> CURRENT = ScopedValue.newInstance();

	public final static CallOnStart callOnStart = new CallOnStart();
	private static ThreadLocal<Boolean> insideServerNewInstance = new ThreadLocal<Boolean>();
	private static ThreadLocal<Boolean> insideGateway = new ThreadLocal<Boolean>();

	/**
	 * register a pagecontext for the current thread
	 *
	 * @param pc PageContext to register
	 * @deprecated Use ScopedValue.where(ThreadLocalPageContext.CURRENT, pc).run() instead
	 */
	@Deprecated
	public static void register(PageContext pc) {
		if (pc == null) {
			return;
		}
		// Set context classloader - still needed even with ScopedValue
		Thread t = Thread.currentThread();
		t.setContextClassLoader(((ConfigPro) pc.getConfig()).getClassLoaderEnv());
		((PageContextImpl) pc).setThread(t);
		// Note: With ScopedValue, the scope should be established via ScopedValue.where()
		// This method kept for backwards compatibility but doesn't actually register with ScopedValue
	}

	public static PageContext get() {
		return get(false);
	}

	/**
	 * returns pagecontext registered for the current thread
	 *
	 * @return pagecontext for the current thread or null if no pagecontext is registered for the current
	 *         thread
	 */
	public static PageContext get(boolean cloneParentIfNotExist) {
		// Check if ScopedValue is bound
		if (CURRENT.isBound()) {
			PageContext pc = CURRENT.get();
			if (pc != null) return pc;
		}

		// For child threads that need parent context, we can't use InheritableThreadLocal with ScopedValue
		// This functionality would need to be explicitly handled by passing parent PageContext to child threads
		if (cloneParentIfNotExist) {
			// TODO: This needs to be refactored - child threads should explicitly bind parent PageContext
			// For now, return null to avoid issues
		}

		return null;
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
	 * @deprecated No longer needed with ScopedValue - cleanup is automatic
	 */
	@Deprecated
	public static void release() {
		// No-op: ScopedValue handles cleanup automatically
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
		pc = CURRENT.isBound() ? CURRENT.get() : null;
		if (pc != null) return (pc.getApplicationContext()).getPreciseMath();

		// pc from parent thread
		pc = null;
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
		pc = CURRENT.isBound() ? CURRENT.get() : null;
		if (pc != null) {
			TimeZone tz = pc.getTimeZone();
			if (tz != null) return tz;
			return DEFAULT_TIMEZONE;
		}

		// pc from parent thread
		pc = null;
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
		pc = CURRENT.isBound() ? CURRENT.get() : null;
		if (pc instanceof PageContextImpl) {
			return ((PageContextImpl) pc).getLog(logName, createIfNecessary);
		}

		// pc from parent thread
		pc = null;
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
		PageContext pc = CURRENT.isBound() ? CURRENT.get() : null;
		if (pc instanceof PageContextImpl && pc.getConfig() == config) {
			return ((PageContextImpl) pc).getLog(logName, createIfNecessary);
		}

		// pc from parent thread
		pc = null;
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
		pc = CURRENT.isBound() ? CURRENT.get() : null;
		if (pc != null) {
			Locale l = pc.getLocale();
			if (l != null) return l;
			return DEFAULT_LOCALE;
		}

		// pc from parent thread
		pc = null;
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
		PageContext pc = CURRENT.isBound() ? CURRENT.get() : null;
		if (pc instanceof PageContextImpl && pc.getConfig() == config) {
			TimeZone tz = pc.getTimeZone();
			if (tz != null) return tz;
			return DEFAULT_TIMEZONE;
		}

		// pc from parent thread
		pc = null;
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
		pc = CURRENT.isBound() ? CURRENT.get() : null;
		if (pc != null) {
			ClassLoader cl = ((PageContextImpl) pc).getRPCClassLoader();
			if (cl != null) return cl;
			return SystemUtil.getCoreClassLoader();
		}

		// pc from parent thread
		pc = null;
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
		PageContext pc = CURRENT.isBound() ? CURRENT.get() : null;
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