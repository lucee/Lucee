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
package lucee.runtime;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspEngineInfo;

import lucee.cli.servlet.HTTPServletImpl;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SizeOf;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.JspEngineInfoImpl;
import lucee.runtime.engine.MonitorState;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageExceptionImpl;
import lucee.runtime.exp.RequestTimeoutException;
import lucee.runtime.functions.string.Hash;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.scope.LocalNotSupportedScope;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

/**
 * implements a JSP Factory, this class produces JSP compatible PageContext objects, as well as the
 * required ColdFusion specified interfaces
 */
public final class CFMLFactoryImpl extends CFMLFactory {

	private static final long MAX_AGE = 5 * 60000; // 5 minutes
	private static final int MAX_SIZE = 10000;
	private static final int MAX_NORMAL_PRIORITY = 0;
	private static final int MAX_NO_SLEEP = 0;
	private static final int SLEEP_TIME = 100;
	private static JspEngineInfo info = new JspEngineInfoImpl("1.0");
	private ConfigWebPro config;
	ConcurrentLinkedDeque<PageContextImpl> pcs = new ConcurrentLinkedDeque<PageContextImpl>();
	private final Map<Integer, PageContextImpl> runningPcs = new ConcurrentHashMap<Integer, PageContextImpl>();
	private final Map<Integer, PageContextImpl> runningChildPcs = new ConcurrentHashMap<Integer, PageContextImpl>();

	int idCounter = 1;
	private ScopeContext scopeContext = new ScopeContext(this);
	private HttpServlet _servlet;
	private URL url = null;
	private CFMLEngineImpl engine;
	private ArrayList<String> cfmlExtensions;
	private ArrayList<String> luceeExtensions;
	private ServletConfig servletConfig;
	private float memoryThreshold;
	private float cpuThreshold;
	private int concurrentReqThreshold;

	public CFMLFactoryImpl(CFMLEngineImpl engine, ServletConfig sg) {
		this.engine = engine;
		this.servletConfig = sg;
		memoryThreshold = getSystemPropOrEnvVarAsFloat("lucee.requesttimeout.memorythreshold");
		cpuThreshold = getSystemPropOrEnvVarAsFloat("lucee.requesttimeout.cputhreshold");
		concurrentReqThreshold = getSystemPropOrEnvVarAsInt("lucee.requesttimeout.concurrentrequestthreshold");
	}

	private static float getSystemPropOrEnvVarAsFloat(String name) {
		String str = SystemUtil.getSystemPropOrEnvVar(name, null);
		if (StringUtil.isEmpty(str)) return 0F;
		str = StringUtil.unwrap(str);
		if (StringUtil.isEmpty(str)) return 0F;

		float res = Caster.toFloatValue(str, 0F);
		if (res < 0F) return 0F;
		if (res > 1F) return 1F;
		return res;
	}

	private static int getSystemPropOrEnvVarAsInt(String name) {
		String str = SystemUtil.getSystemPropOrEnvVar(name, null);
		if (StringUtil.isEmpty(str)) return 0;
		str = StringUtil.unwrap(str);
		if (StringUtil.isEmpty(str)) return 0;
		int res = Caster.toIntValue(str, 0);
		if (res < 0) return 0;
		return res;
	}

	/**
	 * reset the PageContexes
	 */
	@Override
	public void resetPageContext() {
		LogUtil.log(config, Log.LEVEL_INFO, CFMLFactoryImpl.class.getName(), "Reset " + pcs.size() + " Unused PageContexts");
		pcs.clear();
		Iterator<PageContextImpl> it = runningPcs.values().iterator();
		while (it.hasNext()) {
			it.next().reset();
		}
	}

	@Override
	public javax.servlet.jsp.PageContext getPageContext(Servlet servlet, ServletRequest req, ServletResponse rsp, String errorPageURL, boolean needsSession, int bufferSize,
			boolean autoflush) {
		return getPageContextImpl((HttpServlet) servlet, (HttpServletRequest) req, (HttpServletResponse) rsp, errorPageURL, needsSession, bufferSize, autoflush, true, false, -1,
				true, false, false, null);
	}

	@Override
	@Deprecated
	public PageContext getLuceePageContext(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp, String errorPageURL, boolean needsSession, int bufferSize,
			boolean autoflush) {
		// runningCount++;
		return getPageContextImpl(servlet, req, rsp, errorPageURL, needsSession, bufferSize, autoflush, true, false, -1, true, false, false, null);
	}

	@Override
	public PageContext getLuceePageContext(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp, String errorPageURL, boolean needsSession, int bufferSize,
			boolean autoflush, boolean register, long timeout, boolean register2RunningThreads, boolean ignoreScopes) {
		// runningCount++;
		return getPageContextImpl(servlet, req, rsp, errorPageURL, needsSession, bufferSize, autoflush, register, false, timeout, register2RunningThreads, ignoreScopes, false,
				null);
	}

	public PageContextImpl getPageContextImpl(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp, String errorPageURL, boolean needsSession, int bufferSize,
			boolean autoflush, boolean register2Thread, boolean isChild, long timeout, boolean register2RunningThreads, boolean ignoreScopes, boolean createNew,
			PageContextImpl tmplPC) {
		PageContextImpl pc;

		if (!isChild) {
			String ra = req.getRemoteAddr();
			String tmp;
			if (ra != null) {
				boolean resetToNormPrio = true;
				int count = 0;
				for (PageContextImpl opc: runningPcs.values()) {
					tmp = opc.getHttpServletRequest().getRemoteAddr();
					if (ra.equals(tmp)) count++;
				}
				// has already running requests?
				if (count > 0) {

					// reached max amount of request for norm prio
					int maxNormPrio = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.request.limit.concurrent.maxnormprio", null), MAX_NORMAL_PRIORITY);
					if (maxNormPrio > 0 && count >= maxNormPrio) {
						for (PageContextImpl opc: runningPcs.values()) {
							opc.getThread().setPriority(Thread.MIN_PRIORITY);
						}
						Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
						resetToNormPrio = false;
						LogUtil.log(config, Log.LEVEL_INFO, "application", "cfml-factory", "reduce priority for user [" + ra + "]");
					}

					// reached max amount of request allowed in without a nap
					int maxNoSleep = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.request.limit.concurrent.maxnosleep", null), MAX_NO_SLEEP);
					if (maxNoSleep > 0 && count >= maxNoSleep) {
						int ms = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.request.limit.concurrent.sleeptime", null), SLEEP_TIME);
						if (ms > 0) {
							SystemUtil.sleep(ms);
							LogUtil.log(config, Log.LEVEL_INFO, "application", "cfml-factory", "force a nap for request from user [" + ra + "]");
						}
					}
				}
				if (resetToNormPrio && Thread.currentThread().getPriority() != Thread.NORM_PRIORITY) Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
			}
		}

		if (createNew || pcs.isEmpty()) {
			pc = null;
		}
		else {
			try {
				pc = pcs.pop();
			}
			catch (NoSuchElementException nsee) {
				pc = null;
			}
		}
		if (pc == null) pc = new PageContextImpl(scopeContext, config, idCounter++, servlet, ignoreScopes);

		if (timeout > 0) pc.setRequestTimeout(timeout);
		if (register2RunningThreads) {
			runningPcs.put(Integer.valueOf(pc.getId()), pc);
			if (isChild) runningChildPcs.put(Integer.valueOf(pc.getId()), pc);
		}
		this._servlet = servlet;
		if (register2Thread) ThreadLocalPageContext.register(pc);

		pc.initialize(servlet, req, rsp, errorPageURL, needsSession, bufferSize, autoflush, isChild, ignoreScopes, tmplPC);

		return pc;
	}

	@Override
	public void releasePageContext(javax.servlet.jsp.PageContext pc) {
		releaseLuceePageContext((PageContext) pc, true);
	}

	@Override
	public CFMLEngine getEngine() {
		return engine;
	}

	@Override
	@Deprecated
	public void releaseLuceePageContext(PageContext pc) {
		releaseLuceePageContext(pc, true);
	}

	/**
	 * Similar to the releasePageContext Method, but take lucee PageContext as entry
	 * 
	 * @param pc
	 */
	@Override
	public void releaseLuceePageContext(PageContext pc, boolean unregisterFromThread) {
		if (pc.getId() < 0) return;
		boolean isChild = pc.getParentPageContext() != null; // we need to get this check before release is executed

		// when pc was registered with an other thread, we register with this thread when calling release
		PageContext beforePC = ThreadLocalPageContext.get();
		boolean tmpRegister = false;
		if (beforePC != pc) {
			ThreadLocalPageContext.register(pc);
			tmpRegister = true;
		}
		boolean reuse = true;
		try {
			reuse = !pc.hasFamily(); // we do not recycle when still referenced by child threads
			pc.release();
		}
		catch (Exception e) {
			reuse = false;
			ThreadLocalPageContext.getLog(config, "application").error("release page context", e);
		}
		if (tmpRegister) ThreadLocalPageContext.register(beforePC);
		if (unregisterFromThread) ThreadLocalPageContext.release();

		runningPcs.remove(Integer.valueOf(pc.getId()));
		if (isChild) {
			runningChildPcs.remove(Integer.valueOf(pc.getId()));
		}
		if (pcs.size() < 100 && ((PageContextImpl) pc).getTimeoutStackTrace() == null && reuse)// not more than 100 PCs
			pcs.push((PageContextImpl) pc);

		if (runningPcs.size() > MAX_SIZE) clean(runningPcs);
		if (runningChildPcs.size() > MAX_SIZE) clean(runningChildPcs);
	}

	private void clean(Map<Integer, PageContextImpl> map) {
		Iterator<PageContextImpl> it = map.values().iterator();
		PageContextImpl pci;
		long now = System.currentTimeMillis();
		while (it.hasNext()) {
			pci = it.next();
			if (pci.isGatewayContext() || pci.getStartTime() + MAX_AGE > now) continue;
		}
	}

	/**
	 * check timeout of all running threads, downgrade also priority from all thread run longer than 10
	 * seconds
	 */
	@Override
	public void checkTimeout() {
		if (!engine.allowRequestTimeout()) return;

		// print.e(MonitorState.checkForBlockedThreads(runningPcs.values()));
		// print.e(MonitorState.checkForBlockedThreads(runningChildPcs.values()));

		// synchronized (runningPcs) {
		// int len=runningPcs.size();
		// we only terminate child threads

		Map<Integer, PageContextImpl> map = engine.exeRequestAsync() ? runningChildPcs : runningPcs;

		{
			Iterator<Entry<Integer, PageContextImpl>> it = map.entrySet().iterator();
			PageContextImpl pc;
			Entry<Integer, PageContextImpl> e;
			while (it.hasNext()) {
				e = it.next();
				pc = e.getValue();

				long timeout = pc.getRequestTimeout();
				// reached timeout
				if (pc.getStartTime() + timeout < System.currentTimeMillis() && Long.MAX_VALUE != timeout) {
					Log log = ThreadLocalPageContext.getLog(pc, "requesttimeout");
					if (reachedConcurrentReqThreshold() && reachedMemoryThreshold() && reachedCPUThreshold()) {
						if (log != null) {
							PageContext root = pc.getRootPageContext();
							log.log(Log.LEVEL_ERROR, "controller",
									"stop " + (root != null && root != pc ? "thread" : "request") + " (" + pc.getId() + ") because run into a timeout. ATM we have "
											+ getActiveRequests() + " active request(s) and " + getActiveThreads() + " active cfthreads " + getPath(pc) + "."
											+ MonitorState.getBlockedThreads(pc) + RequestTimeoutException.locks(pc),
									ExceptionUtil.toThrowable(pc.getThread().getStackTrace()));
						}
						terminate(pc, true);
						runningPcs.remove(Integer.valueOf(pc.getId()));
						it.remove();
					}
					else {
						if (log != null) {
							PageContext root = pc.getRootPageContext();
							log.log(Log.LEVEL_WARN, "controller", "reach request timeout with " + (root != null && root != pc ? "thread" : "request") + " [" + pc.getId()
									+ "], but the request is not killed because we did not reach all thresholds set. ATM we have " + getActiveRequests() + " active request(s) and "
									+ getActiveThreads() + " active cfthreads " + getPath(pc) + "." + MonitorState.getBlockedThreads(pc) + RequestTimeoutException.locks(pc),
									ExceptionUtil.toThrowable(pc.getThread().getStackTrace()));
						}
					}
				}
				// after 10 seconds downgrade priority of the thread
				else if (pc.getStartTime() + 10000 < System.currentTimeMillis() && pc.getThread().getPriority() != Thread.MIN_PRIORITY) {
					Log log = ThreadLocalPageContext.getLog(pc, "requesttimeout");
					if (log != null) {
						PageContext root = pc.getRootPageContext();
						log.log(Log.LEVEL_INFO, "controller", "downgrade priority of the a " + (root != null && root != pc ? "thread" : "request") + " at " + getPath(pc) + ". "
								+ MonitorState.getBlockedThreads(pc) + RequestTimeoutException.locks(pc), ExceptionUtil.toThrowable(pc.getThread().getStackTrace()));
					}
					try {
						pc.getThread().setPriority(Thread.MIN_PRIORITY);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
					}
				}
			}
		}
	}

	public boolean reachedConcurrentReqThreshold() {
		if (concurrentReqThreshold == 0) return true;
		return concurrentReqThreshold <= runningPcs.size();
	}

	public boolean reachedMemoryThreshold() {
		if (memoryThreshold == 0) return true;
		return memoryThreshold <= SystemUtil.getMemoryPercentage();
	}

	public boolean reachedCPUThreshold() {
		if (cpuThreshold == 0) return true;
		return cpuThreshold <= SystemUtil.getCpuPercentage();
	}

	public static void terminate(PageContextImpl pc, boolean async) {
		pc.getConfig().getThreadQueue().exit(pc);
		SystemUtil.stop(pc, async);
	}

	private static String getPath(PageContext pc) {
		try {
			String base = ResourceUtil.getResource(pc, pc.getBasePageSource()).getAbsolutePath();
			String current = ResourceUtil.getResource(pc, pc.getCurrentPageSource()).getAbsolutePath();
			if (base.equals(current)) return "path: " + base;
			return "path: " + base + " (" + current + ")";
		}
		catch (NullPointerException npe) {
			return "(no path available)";
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return "(fail to retrieve path:" + t.getClass().getName() + ":" + t.getMessage() + ")";
		}
	}

	@Override
	public JspEngineInfo getEngineInfo() {
		return info;
	}

	/**
	 * @return returns count of pagecontext in use
	 */
	@Override
	public int getUsedPageContextLength() {
		int length = 0;
		try {
			Iterator<PageContextImpl> it = runningPcs.values().iterator();
			while (it.hasNext()) {
				PageContextImpl pc = it.next();
				if (!pc.isGatewayContext()) length++;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return length;
		}
		return length;
	}

	/**
	 * @return Returns the config.
	 */
	@Override
	public ConfigWeb getConfig() {
		return config;
	}

	/**
	 * @return Returns the scopeContext.
	 */
	public ScopeContext getScopeContext() {
		return scopeContext;
	}

	/**
	 * @return label of the factory
	 */
	@Override
	public Object getLabel() {
		return getConfig().getLabel();
	}

	/**
	 * @param label
	 */
	@Override
	public void setLabel(String label) {
		// deprecated
	}

	@Override
	public URL getURL() {
		return url;
	}

	public void setURL(URL url) {
		this.url = url;
	}

	/**
	 * @return the servlet
	 */
	@Override
	public HttpServlet getServlet() {
		if (_servlet == null) _servlet = new HTTPServletImpl(servletConfig, servletConfig.getServletContext(), servletConfig.getServletName());
		return _servlet;
	}

	public void setConfig(ConfigWebPro config) {
		this.config = config;
	}

	public Map<Integer, PageContextImpl> getActivePageContexts() {
		return runningPcs;
	}

	public long getPageContextsSize() {
		return SizeOf.size(pcs);
	}

	public long getActiveRequests() {
		return runningPcs.size();
	}

	public long getActiveThreads() {
		return runningChildPcs.size();
	}

	public Array getInfo() {
		Array info = new ArrayImpl();

		// synchronized (runningPcs) {
		// int len=runningPcs.size();
		Iterator<PageContextImpl> it = runningPcs.values().iterator();
		PageContextImpl pc;
		Struct data, sctThread, scopes;
		Thread thread;
		Entry<Integer, PageContextImpl> e;
		ConfigWebPro cw;
		while (it.hasNext()) {
			pc = it.next();
			cw = (ConfigWebPro) pc.getConfig();
			data = new StructImpl();
			sctThread = new StructImpl();
			scopes = new StructImpl();
			data.setEL("thread", sctThread);
			data.setEL("scopes", scopes);

			if (pc.isGatewayContext()) continue;
			thread = pc.getThread();
			if (thread == Thread.currentThread()) continue;

			thread = pc.getThread();
			if (thread == Thread.currentThread()) continue;

			data.setEL("startTime", new DateTimeImpl(pc.getStartTime(), false));
			data.setEL("endTime", new DateTimeImpl(pc.getStartTime() + pc.getRequestTimeout(), false));
			data.setEL(KeyConstants._timeout, new Double(pc.getRequestTimeout()));

			// thread
			sctThread.setEL(KeyConstants._name, thread.getName());
			sctThread.setEL(KeyConstants._priority, Caster.toDouble(thread.getPriority()));
			sctThread.setEL(KeyConstants._state, thread.getState().name());

			StackTraceElement[] stes = thread.getStackTrace();
			data.setEL("TagContext", PageExceptionImpl.getTagContext(pc.getConfig(), stes));

			// Java Stacktrace
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			Throwable t = new Throwable();
			t.setStackTrace(stes);
			t.printStackTrace(pw);
			pw.close();
			data.setEL("JavaStackTrace", sw.toString());

			data.setEL(KeyConstants._urltoken, pc.getURLToken());
			try {
				if (pc.getConfig().debug()) data.setEL("debugger", pc.getDebugger().getDebuggingData(pc));
			}
			catch (PageException e2) {
			}

			try {
				data.setEL(KeyConstants._id, Hash.call(pc, pc.getId() + ":" + pc.getStartTime()));
			}
			catch (PageException e1) {
			}

			data.setEL(KeyConstants._hash, cw.getHash());
			data.setEL("contextId", cw.getIdentification().getId());
			data.setEL(KeyConstants._label, cw.getLabel());

			data.setEL("requestId", pc.getId());

			// Scopes
			scopes.setEL(KeyConstants._name, pc.getApplicationContext().getName());
			try {
				scopes.setEL(KeyConstants._application, pc.applicationScope());
			}
			catch (PageException pe) {
			}

			try {
				scopes.setEL(KeyConstants._session, pc.sessionScope());
			}
			catch (PageException pe) {
			}

			try {
				scopes.setEL(KeyConstants._client, pc.clientScope());
			}
			catch (PageException pe) {
			}
			scopes.setEL(KeyConstants._cookie, pc.cookieScope());
			scopes.setEL(KeyConstants._variables, pc.variablesScope());
			if (!(pc.localScope() instanceof LocalNotSupportedScope)) {
				scopes.setEL(KeyConstants._local, pc.localScope());
				scopes.setEL(KeyConstants._arguments, pc.argumentsScope());
			}
			scopes.setEL(KeyConstants._cgi, pc.cgiScope());
			scopes.setEL(KeyConstants._form, pc.formScope());
			scopes.setEL(KeyConstants._url, pc.urlScope());
			scopes.setEL(KeyConstants._request, pc.requestScope());

			info.appendEL(data);
		}
		return info;
		// }
	}

	public void stopThread(String threadId, String stopType) {
		// synchronized (runningPcs) {
		Iterator<PageContextImpl> it = runningPcs.values().iterator();
		PageContext pc;
		while (it.hasNext()) {
			pc = it.next();
			try {
				String id = Hash.call(pc, pc.getId() + ":" + pc.getStartTime());
				if (id.equals(threadId)) {
					stopType = stopType.trim();
					// Throwable t;
					if ("abort".equalsIgnoreCase(stopType) || "cfabort".equalsIgnoreCase(stopType)) throw new RuntimeException("type [" + stopType + "] is no longer supported");
					// t=new Abort(Abort.SCOPE_REQUEST);
					// else t=new RequestTimeoutException(pc.getThread(),"request has been forced to stop.");

					SystemUtil.stop(pc, true);
					SystemUtil.sleep(10);
					break;
				}
			}
			catch (PageException e1) {
			}

		}
		// }
	}

	@Override
	public JspApplicationContext getJspApplicationContext(ServletContext arg0) {
		throw new RuntimeException("not supported!");
	}

	@Override
	public int toDialect(String ext) {
		// MUST improve perfomance
		if (cfmlExtensions == null) _initExtensions();
		if (cfmlExtensions.contains(ext.toLowerCase())) return CFMLEngine.DIALECT_CFML;
		return CFMLEngine.DIALECT_CFML;
	}

	// FUTURE add to loader
	public int toDialect(String ext, int defaultValue) {
		if (ext == null) return defaultValue;
		if (cfmlExtensions == null) _initExtensions();
		if (cfmlExtensions.contains(ext = ext.toLowerCase())) return CFMLEngine.DIALECT_CFML;
		if (luceeExtensions.contains(ext)) return CFMLEngine.DIALECT_LUCEE;
		return defaultValue;
	}

	private void _initExtensions() {
		cfmlExtensions = new ArrayList<String>();
		luceeExtensions = new ArrayList<String>();
		try {

			Iterator<?> it = getServlet().getServletContext().getServletRegistrations().entrySet().iterator();
			Entry<String, ? extends ServletRegistration> e;
			String cn;
			while (it.hasNext()) {
				e = (Entry<String, ? extends ServletRegistration>) it.next();
				cn = e.getValue().getClassName();

				if (cn != null && cn.indexOf("LuceeServlet") != -1) {
					setExtensions(luceeExtensions, e.getValue().getMappings().iterator());
				}
				else if (cn != null && cn.indexOf("CFMLServlet") != -1) {
					setExtensions(cfmlExtensions, e.getValue().getMappings().iterator());
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			ArrayUtil.addAll(cfmlExtensions, Constants.getCFMLExtensions());
			ArrayUtil.addAll(luceeExtensions, Constants.getLuceeExtensions());
		}
	}

	private void setExtensions(ArrayList<String> extensions, Iterator<String> it) {
		String str, str2;
		Iterator<String> it2;
		while (it.hasNext()) {
			str = it.next();
			it2 = ListUtil.listToSet(str, ',', true).iterator();
			while (it2.hasNext()) {
				str2 = it2.next();
				extensions.add(str2.substring(2));// MUSTMUST better impl
			}
		}
	}

	@Override
	public Iterator<String> getCFMLExtensions() {
		if (cfmlExtensions == null) _initExtensions();
		return cfmlExtensions.iterator();
	}

	@Override
	public Iterator<String> getLuceeExtensions() {
		if (luceeExtensions == null) _initExtensions();
		return luceeExtensions.iterator();
	}

	public static RequestTimeoutException createRequestTimeoutException(PageContext pc) {
		return new RequestTimeoutException(pc, pc.getThread().getStackTrace());
	}
}