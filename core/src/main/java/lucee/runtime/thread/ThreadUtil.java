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
package lucee.runtime.thread;

import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lucee.aprint;
import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.SystemUtil;
import lucee.runtime.op.Caster;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Constants;
import lucee.runtime.net.http.HTTPServletRequestWrap;
import lucee.runtime.net.http.HttpServletRequestDummy;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.type.Struct;

public final class ThreadUtil {

	// virtual threads are safe from Java 25+ (JEP 491: synchronized no longer pins virtual threads).
	// Default off — virtual threads on Java 25+ are experimental. Opt-in via lucee.allow.virtual.threads.
	private static final boolean ALLOW_VIRTUAL_THREADS =
			SystemUtil.JAVA_VERSION >= SystemUtil.JAVA_VERSION_25
			&& Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.allow.virtual.threads", "false"), false);
	// private static final Class<?> THREAD_CLASS = Thread.class;
	private static final Class<?> RUNNABLE_CLASS = Runnable.class;
	private static Class<?> threadBuilderClass;
	private static boolean virtualDisabled = false;

	public static Class<?> getThreadBuilderClass() throws ClassNotFoundException {
		if (threadBuilderClass == null) {
			threadBuilderClass = Class.forName("java.lang.Thread$Builder$OfVirtual");
		}
		return threadBuilderClass;
	}

	// do not change, used in Redis extension
	public static PageContextImpl clonePageContext(PageContext pc, OutputStream os, boolean stateless, boolean register2Thread, boolean register2RunningThreads) {
		// TODO stateless
		CFMLFactoryImpl factory = (CFMLFactoryImpl) pc.getConfig().getFactory();
		HttpServletRequest req = new HTTPServletRequestWrap(cloneHttpServletRequest(pc));
		HttpServletResponse rsp = createHttpServletResponse(os);

		// copy state
		PageContextImpl pci = (PageContextImpl) pc;
		PageContextImpl dest = factory.getPageContextImpl(factory.getServlet(), req, rsp, null, false, -1, false, register2Thread, true, pc.getRequestTimeout(),
				register2RunningThreads, false, false, stateless ? null : pci);
		return dest;
	}

	public static PageContextImpl createPageContext(ConfigWeb config, String serverName, String requestURI, String queryString, byte[] body, boolean register, long timeout) {
		return createPageContext(config, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, serverName, requestURI, queryString, new Cookie[] {}, new Pair[] {}, body, new Pair[] {}, null,
				register, timeout, null, null);
	}

	// since 7.1
	public static PageContextImpl createPageContext(ConfigWeb config, OutputStream os, String serverName, String requestURI, String queryString, byte[] body, boolean register,
			long timeout) {
		return createPageContext(config, os, serverName, requestURI, queryString, new Cookie[] {}, new Pair[] {}, body, new Pair[] {}, null, register, timeout, null, null);
	}

	@Deprecated
	public static PageContextImpl createPageContext(ConfigWeb config, OutputStream os, String serverName, String requestURI, String queryString, Cookie[] cookies, Pair[] headers,
			byte[] body, Pair[] parameters, Struct attributes, boolean register, long timeout) {
		return createPageContext(config, os, serverName, requestURI, queryString, cookies, headers, body, parameters, attributes, register, timeout, null, null);
	}

	@Deprecated
	public static PageContextImpl createPageContext(ConfigWeb config, OutputStream os, String serverName, String requestURI, String queryString, Cookie[] cookies, Pair[] headers,
			byte[] body, Pair[] parameters, Struct attributes, boolean register, long timeout, HttpSession session) {

		return createPageContext(config, os, serverName, requestURI, queryString, cookies, headers, body, parameters, attributes, register, timeout, session, null);
	}

	public static PageContextImpl createPageContext(ConfigWeb config, OutputStream os, String serverName, String requestURI, String queryString, Cookie[] cookies, Pair[] headers,
			byte[] body, Pair[] parameters, Struct attributes, boolean register, long timeout, HttpSession session, String method) {

		CFMLFactory factory = config.getFactory();
		HttpServletRequest req = new HttpServletRequestDummy(config.getRootDirectory(), serverName, requestURI, queryString, cookies, headers, parameters, attributes, session,
				body);

		if (!StringUtil.isEmpty(method, true)) ((HttpServletRequestDummy) req).setMethod(method);

		req = new HTTPServletRequestWrap(req);
		HttpServletResponse rsp = createHttpServletResponse(os);

		return (PageContextImpl) factory.getLuceePageContext(factory.getServlet(), req, rsp, null, false, -1, false, register, timeout, false, false);

	}

	public static PageContextImpl createDummyPageContext(ConfigWeb config) {
		return createPageContext(config, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, Constants.NAME, "/", "", null, null, null, null, null, true, -1, null, null).setDummy(true);
	}

	/**
	 * 
	 * @param factory
	 * @param rootDirectory
	 * @param os
	 * @param serverName
	 * @param requestURI
	 * @param queryString
	 * @param cookies
	 * @param headers
	 * @param parameters
	 * @param attributes
	 * @param register
	 * @param timeout in ms, if the value is smaller than 1 it is ignored and the value comming from the
	 *            context is used
	 * @return
	 */
	public static PageContextImpl createPageContext(CFMLFactory factory, Resource rootDirectory, OutputStream os, String serverName, String requestURI, String queryString,
			Cookie[] cookies, Pair[] headers, Pair[] parameters, Struct attributes, boolean register, long timeout) {
		HttpServletRequest req = createHttpServletRequest(rootDirectory, serverName, requestURI, queryString, cookies, headers, parameters, attributes, null);
		HttpServletResponse rsp = createHttpServletResponse(os);

		return (PageContextImpl) factory.getLuceePageContext(factory.getServlet(), req, rsp, null, false, -1, false, register, timeout, false, false);

	}

	public static HttpServletRequest createHttpServletRequest(Resource contextRoot, String serverName, String scriptName, String queryString, Cookie[] cookies, Pair[] headers,
			Pair[] parameters, Struct attributes, HttpSession session) {
		return new HTTPServletRequestWrap(new HttpServletRequestDummy(contextRoot, serverName, scriptName, queryString, cookies, headers, parameters, attributes, null, null));
	}

	public static HttpServletRequest cloneHttpServletRequest(PageContext pc) {
		Config config = pc.getConfig();
		HttpServletRequest req = pc.getHttpServletRequest();
		HttpServletRequestDummy dest = HttpServletRequestDummy.clone(pc, config.getRootDirectory(), req);
		return dest;
	}

	public static HttpServletResponse createHttpServletResponse(OutputStream os) {
		if (os == null) os = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM;

		HttpServletResponseDummy dest = new HttpServletResponseDummy(os);
		return dest;
	}

	/**
	 * return priority as a String representation
	 * 
	 * @param priority Thread priority
	 * @return String definition of priority (null when input is invalid)
	 */
	public static String toStringPriority(int priority) {
		if (priority == Thread.NORM_PRIORITY) return "NORMAL";
		if (priority == Thread.MAX_PRIORITY) return "HIGH";
		if (priority == Thread.MIN_PRIORITY) return "LOW";
		return null;
	}

	/**
	 * return priority as an int representation
	 * 
	 * @param strPriority Thread priority as String definition
	 * @return int definition of priority (-1 when input is invalid)
	 */
	public static int toIntPriority(String strPriority) {
		strPriority = strPriority.trim().toLowerCase();

		if ("low".equals(strPriority)) return Thread.MIN_PRIORITY;
		if ("min".equals(strPriority)) return Thread.MIN_PRIORITY;
		if ("high".equals(strPriority)) return Thread.MAX_PRIORITY;
		if ("max".equals(strPriority)) return Thread.MAX_PRIORITY;
		if ("normal".equals(strPriority)) return Thread.NORM_PRIORITY;
		if ("norm".equals(strPriority)) return Thread.NORM_PRIORITY;
		return -1;
	}

	public static void printThreads() {
		Iterator<Entry<Thread, StackTraceElement[]>> it = Thread.getAllStackTraces().entrySet().iterator();
		Entry<Thread, StackTraceElement[]> e;
		while (it.hasNext()) {
			e = it.next();
			aprint.e(e.getKey().getName());
			aprint.e(ExceptionUtil.toString(e.getValue()));
		}
	}

	public static boolean isInNativeMethod(Thread thread, boolean defaultValue) {
		if (thread == null) return defaultValue;
		StackTraceElement[] stes = thread.getStackTrace();
		if (stes == null || stes.length == 0) return defaultValue;
		StackTraceElement ste = stes[0];
		return ste.isNativeMethod();
	}

	public static Thread getThread(Runnable task) {
		return getThread(task, ALLOW_VIRTUAL_THREADS);

	}

	public static Thread getThread(Runnable task, boolean allowVirtual) {
		if (allowVirtual && SystemUtil.JAVA_VERSION >= SystemUtil.JAVA_VERSION_19) {

			try {
				// Get Thread.ofVirtual()
				MethodHandles.Lookup lookup = MethodHandles.lookup();
				MethodHandle ofVirtualHandle = lookup.findStatic(Thread.class, "ofVirtual", MethodType.methodType(getThreadBuilderClass()));
				MethodHandle unstartedHandle = lookup.findVirtual(Class.forName("java.lang.Thread$Builder"), "unstarted", MethodType.methodType(Thread.class, Runnable.class));
				return (Thread) unstartedHandle.bindTo(ofVirtualHandle.invoke()).invoke(task);
			}
			catch (Throwable e) {
				ExceptionUtil.rethrowIfNecessary(e);
				LogUtil.log("threading", e);
			}
		}
		return new Thread(task);
	}

	public static ExecutorService createExecutorService(int maxThreads) {
		return createExecutorService(maxThreads, ALLOW_VIRTUAL_THREADS);
	}

	public static ExecutorService createExecutorService(int maxThreads, boolean allowVirtual) {
		if (!virtualDisabled) {
			if (allowVirtual && SystemUtil.JAVA_VERSION >= SystemUtil.JAVA_VERSION_19) {
				// FUTURE use newVirtualThreadPerTaskExecutor natively
				try {
					MethodHandles.Lookup lookup = MethodHandles.lookup();
					MethodType methodType = MethodType.methodType(ExecutorService.class);
					MethodHandle methodHandle = lookup.findStatic(Executors.class, "newVirtualThreadPerTaskExecutor", methodType);
					ExecutorService vtExec = (ExecutorService) methodHandle.invoke();
					// VT-per-task has no parallelism cap — wrap to honour maxThreads contract
					return new SemaphoreBoundedExecutor(vtExec, maxThreads);
				}
				catch (Throwable e) {
					virtualDisabled = true;
					ExceptionUtil.rethrowIfNecessary(e);
					LogUtil.log("threading", e);
				}
			}
		}
		return Executors.newFixedThreadPool(maxThreads);
	}

	public static ExecutorService createExecutorService() {
		if (SystemUtil.JAVA_VERSION >= SystemUtil.JAVA_VERSION_19) {
			// FUTURE use newVirtualThreadPerTaskExecutor natively
			try {
				MethodHandles.Lookup lookup = MethodHandles.lookup();
				MethodType methodType = MethodType.methodType(ExecutorService.class);
				MethodHandle methodHandle = lookup.findStatic(Executors.class, "newVirtualThreadPerTaskExecutor", methodType);
				return (ExecutorService) methodHandle.invoke();
			}
			catch (Throwable e) {
				ExceptionUtil.rethrowIfNecessary(e);
				LogUtil.log("threading", e);
			}
		}
		return Executors.newSingleThreadExecutor();
	}

	// Wait for any in-flight futures to finish so orphan workers can't race the caller's
	// post-parallel mutations (LDEV-6395). Swallows per-future exceptions: caller has already
	// surfaced the relevant one via afterCall.
	public static void drainFutures(List<? extends Future<?>> futures) {
		if (futures == null) return;
		for (Future<?> f : futures) {
			if (!f.isDone()) {
				try {
					f.get();
				}
				catch (Exception ignored) {}
			}
		}
	}

	// Shared cleanup for the parallel section of Each/Map/Filter/Some/Every (LDEV-6395):
	// restore the parent's thread, shut down the executor, drain any remaining futures.
	// Safe to call when parts were never set up — null pc/thread/es/futures are no-ops.
	public static void finishParallelSection(PageContext pc, ExecutorService es, Thread thread, List<? extends Future<?>> futures) {
		if (pc != null && thread != null) ((PageContextImpl) pc).setThread(thread);
		if (es != null) {
			es.shutdown();
			drainFutures(futures);
		}
	}

	/**
	 * Closes an ExecutorService, mimicking the behavior of ExecutorService.close() from Java 21.
	 * <p>
	 * This utility method provides backward compatibility for Java 11 environments by:
	 * <ul>
	 * <li>Using AutoCloseable.close() if the executor implements it (for future compatibility)
	 * <li>Otherwise performing a graceful shutdown, waiting indefinitely for tasks to complete
	 * <li>Handling interruptions by initiating an immediate shutdown
	 * <li>Preserving the interrupted status of the current thread if interruption occurred
	 * </ul>
	 * </p>
	 *
	 * @param executor the ExecutorService to close
	 * @throws Exception if an exception occurs during the close operation
	 */
	public static void close(ExecutorService executor) throws Exception {
		if (executor instanceof AutoCloseable) {
			((AutoCloseable) executor).close();
		}
		else {
			boolean terminated = executor.isTerminated();
			if (!terminated) {
				executor.shutdown();
				boolean interrupted = false;
				while (!terminated) {
					try {
						terminated = executor.awaitTermination(1L, TimeUnit.DAYS);
					}
					catch (InterruptedException e) {
						if (!interrupted) {
							executor.shutdownNow();
							interrupted = true;
						}
					}
				}
				if (interrupted) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

}