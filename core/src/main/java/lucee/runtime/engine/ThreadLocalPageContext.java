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
import lucee.runtime.config.RuntimeProfile;
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

	/**
	 * register a pagecontext for he current thread
	 * 
	 * @param pc PageContext to register
	 */
	public static void register(PageContext pc) {// print.ds(Thread.currentThread().getName());
		if (pc == null) {
			return; // TODO happens with Gateway, but should not!
		}
		// TODO should i set the old one by "release"?
		Thread t = Thread.currentThread();
		t.setContextClassLoader(((ConfigPro) pc.getConfig()).getClassLoaderEnv());
		((PageContextImpl) pc).setThread(t);
		pcThreadLocal.set(pc);
		pcThreadLocalInheritable.set(pc);
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
		// Global control - when set to false, completely bypass all precise math
		if (!RuntimeProfile.ALLOW_PRECISE_MATH) return false;

		// Try provided PC or get from ThreadLocal
		if (pc == null) pc = get();

		// Use PageContext cached value (provides thread isolation)
		if (pc != null) return ((PageContextImpl) pc).getPreciseMath();

		// Fallback to config if no PC available
		Config c = ThreadLocalConfig.get();
		if (c instanceof ConfigPro) return ((ConfigPro) c).getPreciseMath();
		return false;
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