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

import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;

/**
 * class to handle thread local PageContext, do use pagecontext in classes that have no method
 * argument pagecontext
 */
public final class ThreadLocalPageContext {

	private static final Locale DEFAULT_LOCALE = Locale.getDefault();
	private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();
	private static ThreadLocal<PageContext> pcThreadLocal = new ThreadLocal<PageContext>();
	public final static CallOnStart callOnStart = new CallOnStart();

	/**
	 * register a pagecontext for he current thread
	 * 
	 * @param pc PageContext to register
	 */
	public static void register(PageContext pc) {// print.ds(Thread.currentThread().getName());
		if (pc == null) return; // TODO happens with Gateway, but should not!
		// TODO should i set the old one by "release"?
		Thread t = Thread.currentThread();
		t.setContextClassLoader(((ConfigPro) pc.getConfig()).getClassLoaderEnv());
		((PageContextImpl) pc).setThread(t);
		pcThreadLocal.set(pc);
	}

	/**
	 * returns pagecontext registered for the current thread
	 * 
	 * @return pagecontext for the current thread or null if no pagecontext is regisred for the current
	 *         thread
	 */
	public static PageContext get() {// print.dumpStack();
		return pcThreadLocal.get();
	}

	public static Config getConfig() {
		PageContext pc = get();
		if (pc != null) {
			return pc.getConfig();
		}
		return ThreadLocalConfig.get();

	}

	/**
	 * release the pagecontext for the current thread
	 */
	public static void release() {// print.ds(Thread.currentThread().getName());
		pcThreadLocal.set(null);
	}

	public static Config getConfig(PageContext pc) {
		if (pc == null) return getConfig();
		return pc.getConfig();
	}

	public static Config getConfig(Config config) {
		if (config == null) return getConfig();
		return config;
	}

	public static TimeZone getTimeZone(PageContext pc) {
		// pc
		pc = get(pc);
		if (pc != null) {
			if (pc.getTimeZone() != null) return pc.getTimeZone();
			return DEFAULT_TIMEZONE;
		}

		// config
		Config config = getConfig((Config) null);
		if (config != null && config.getTimeZone() != null) {
			return config.getTimeZone();
		}
		return DEFAULT_TIMEZONE;
	}

	public static Log getLog(PageContext pc, String logName) {
		// pc
		pc = get(pc);
		if (pc instanceof PageContextImpl) {
			return ((PageContextImpl) pc).getLog(logName);
		}

		// config
		Config config = getConfig(pc);
		if (config != null) {
			return config.getLog(logName);
		}
		return null;
	}

	public static Log getLog(Config config, String logName) {
		// pc
		if (config instanceof ConfigWeb) {
			PageContext pc = get(config);
			if (pc instanceof PageContextImpl) {
				return ((PageContextImpl) pc).getLog(logName);
			}
		}

		// config
		config = getConfig(config);
		if (config != null) {
			return config.getLog(logName);
		}
		return null;
	}

	public static Log getLog(String logName) {
		// pc
		PageContext pc = get();
		if (pc instanceof PageContextImpl) {
			return ((PageContextImpl) pc).getLog(logName);
		}

		// config
		Config config = getConfig();
		if (config != null) {
			return config.getLog(logName);
		}
		return null;
	}

	public static Locale getLocale() {
		return getLocale((PageContext) null);
	}

	public static Locale getLocale(Locale l) {
		if (l != null) return l;
		return getLocale((PageContext) null);
	}

	public static Locale getLocale(PageContext pc) {
		// pc
		pc = get(pc);
		if (pc != null) {
			if (pc.getLocale() != null) return pc.getLocale();
			return DEFAULT_LOCALE;
		}

		// config
		Config config = getConfig((Config) null);
		if (config != null && config.getLocale() != null) {
			return config.getLocale();
		}
		return DEFAULT_LOCALE;
	}

	public static TimeZone getTimeZone(Config config) {
		PageContext pc = get();
		if (pc != null && pc.getTimeZone() != null) return pc.getTimeZone();

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
}