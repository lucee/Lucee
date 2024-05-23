/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
package lucee.runtime.thread;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.debug.DebugEntryTemplate;
import lucee.runtime.debug.DebuggerImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.ParentException;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.net.http.HttpUtil;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.ArgumentThreadImpl;
import lucee.runtime.type.scope.Local;
import lucee.runtime.type.scope.LocalImpl;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.scope.UndefinedImpl;
import lucee.runtime.type.util.KeyConstants;

public class ChildThreadImpl extends ChildThread implements Serializable {

	private static final long serialVersionUID = -8902836175312356628L;

	private static final Collection.Key KEY_ATTRIBUTES = KeyConstants._attributes;

	// private static final Set EMPTY = new HashSet();

	private int threadIndex;
	private PageContextImpl pc = null;
	// PageContextImpl pc =null;
	private final String tagName;
	private long start;
	private long endTime;
	private ThreadsImpl scope;

	// accesible from scope
	Struct content = new StructImpl();
	Struct catchBlock;
	boolean terminated;
	boolean completed;
	ByteArrayOutputStream output;

	// only used for type daemon
	private Page page;

	// only used for type task, demon attrs are not Serializable
	private Struct attrs;
	private SerializableCookie[] cookies;
	private String serverName;
	private String queryString;
	private Pair<String, String>[] parameters;
	private String requestURI;
	private Pair<String, String>[] headers;
	private Struct attributes;
	private String template;
	private long requestTimeout;

	private boolean serializable;

	String contentType;

	String contentEncoding;

	private Object threadScope;

	private ParentException parentException;

	private final boolean separateScopes;

	public ChildThreadImpl(PageContextImpl parent, Page page, String tagName, int threadIndex, Struct attrs, boolean serializable, boolean separateScopes) {
		this.serializable = serializable;
		this.tagName = tagName;
		this.threadIndex = threadIndex;
		this.parentException = new ParentException();
		this.separateScopes = separateScopes;
		start = System.currentTimeMillis();
		if (attrs == null) this.attrs = new StructImpl();
		else this.attrs = attrs;

		if (!serializable) {
			this.page = page;
			if (parent != null) {
				output = new ByteArrayOutputStream();
				try {
					this.pc = ThreadUtil.clonePageContext(parent, output, false, false, true);
				}
				catch (ConcurrentModificationException e) {// MUST search for:hhlhgiug
					this.pc = ThreadUtil.clonePageContext(parent, output, false, false, true);
				}
				// tag names
				this.pc.setTagName(tagName);
				this.pc.addParentTag(parent.getTagName());
				if (!separateScopes) {
					this.pc.undefinedScope().setMode(((UndefinedImpl) parent.undefinedScope()).getMode());
					this.pc.setFunctionScopes(parent.localScope(), parent.argumentsScope());
				}
			}
		}
		else {
			this.template = page.getPageSource().getRealpathWithVirtual();
			HttpServletRequest req = parent.getHttpServletRequest();
			serverName = req.getServerName();
			queryString = ReqRspUtil.getQueryString(req);
			cookies = SerializableCookie.toSerializableCookie(ReqRspUtil.getCookies(req, parent.getWebCharset()));
			parameters = HttpUtil.cloneParameters(req);
			requestURI = req.getRequestURI();
			headers = HttpUtil.cloneHeaders(req);
			attributes = HttpUtil.getAttributesAsStruct(req);
			requestTimeout = parent.getRequestTimeout();
			// MUST here ist sill a mutch state values missing
		}
	}

	@Override
	public void run() {
		execute(null);
	}

	public PageException execute(Config config) {
		PageContext oldPc = ThreadLocalPageContext.get();
		Page p = page;
		PageContextImpl pc = null;
		DebugEntryTemplate debugEntry = null;
		long time = System.nanoTime();
		try {
			// daemon
			if (this.pc != null) {
				pc = this.pc;
				ThreadLocalPageContext.register(pc);
			}
			// task
			else {
				ConfigWebPro cwi;
				try {
					cwi = (ConfigWebPro) config;
					DevNullOutputStream os = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM;
					HttpSession session = oldPc != null && oldPc.getSessionType() == Config.SESSION_TYPE_JEE ? oldPc.getSession() : null;
					pc = ThreadUtil.createPageContext(cwi, os, serverName, requestURI, queryString, SerializableCookie.toCookies(cookies), headers, null, parameters, attributes,
							true, -1, session);
					pc.setRequestTimeout(requestTimeout);
					p = PageSourceImpl.loadPage(pc, cwi.getPageSources(oldPc == null ? pc : oldPc, null, template, false, false, true));
					// p=cwi.getPageSources(oldPc,null, template, false,false,true).loadPage(cwi);
				}
				catch (PageException e) {
					return e;
				}
				pc.addPageSource(p.getPageSource(), true);
			}

			ConfigWebPro ci = (ConfigWebPro) pc.getConfig();
			if (!pc.isGatewayContext() && ci.debug()) {
				((DebuggerImpl) pc.getDebugger()).setThreadName(tagName);
				if (pc.hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) debugEntry = pc.getDebugger().getEntry(pc, page.getPageSource());
			}

			threadScope = pc.getCFThreadScope();
			pc.setCurrentThreadScope(new ThreadsImpl(this));
			pc.setThread(Thread.currentThread());

			// String encodings = pc.getHttpServletRequest().getHeader("Accept-Encoding");

			Undefined undefined = pc.us();

			int oldMode = -1;
			LocalImpl newLocal = null;
			Argument oldArgs = null;
			Local oldLocal = null;
			if (separateScopes) {

				// arguments
				oldArgs = pc.argumentsScope();
				Argument newArgs = new ArgumentThreadImpl((Struct) Duplicator.duplicate(attrs, false));
				Iterator<Entry<Key, Object>> it = attrs.entryIterator();
				Entry<Key, Object> e;
				while (it.hasNext()) {
					e = it.next();
					newArgs.setEL(e.getKey(), e.getValue());
				}

				// local
				newLocal = pc.getScopeFactory().getLocalInstance();
				oldLocal = pc.localScope();
				newLocal.setEL(KEY_ATTRIBUTES, newArgs);

				oldMode = undefined.setMode(Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS);
				pc.setFunctionScopes(newLocal, newArgs);
			}

			try {
				p.threadCall(pc, threadIndex);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (!Abort.isSilentAbort(t)) {
					ConfigWeb c = pc.getConfig();
					ExceptionUtil.initCauseEL(t, parentException);
					Log log = ThreadLocalPageContext.getLog(c, "thread");
					if (log != null) {
						try {
							log.log(Log.LEVEL_ERROR, this.getName(), t);
						}
						catch (Exception ex) {
						}
					}

					PageException pe = Caster.toPageException(t);
					if (!serializable) {
						catchBlock = pe.getCatchBlock(pc.getConfig());
					}

					return pe;
				}
			}
			finally {
				completed = true;
				if (separateScopes) {
					pc.setFunctionScopes(oldLocal, oldArgs);
					undefined.setMode(oldMode);
					if (newLocal != null) pc.getScopeFactory().recycle(pc, newLocal);
				}
				// pc.getScopeFactory().recycle(newArgs);

				if (pc.getHttpServletResponse() instanceof HttpServletResponseDummy) {
					HttpServletResponseDummy rsp = (HttpServletResponseDummy) pc.getHttpServletResponse();
					pc.flush();
					contentType = rsp.getContentType();
					Pair<String, Object>[] _headers = rsp.getHeaders();
					if (_headers != null) for (int i = 0; i < _headers.length; i++) {
						if (_headers[i].getName().equalsIgnoreCase("Content-Encoding")) contentEncoding = Caster.toString(_headers[i].getValue(), null);
					}
				}
				if (scope != null) scope.uncouple();
			}
		}
		finally {
			if (debugEntry != null) debugEntry.updateExeTime(System.nanoTime() - time);
			pc.setEndTimeNS(System.nanoTime());
			endTime = System.currentTimeMillis();
			pc.getConfig().getFactory().releaseLuceePageContext(pc, true);
			pc = null;
			if (oldPc != null) ThreadLocalPageContext.register(oldPc);
		}
		return null;
	}

	@Override
	public String getTagName() {
		return tagName;
	}

	@Override
	public long getStartTime() {
		return start;
	}

	/*
	 * public Threads getThreadScopeX() { if(scope==null) scope=new ThreadsImpl(this); return scope; }
	 */

	public long getEndTime() {
		if (endTime == 0) return System.currentTimeMillis(); // endTime = 0 means the thread is still running
		return endTime;
	}

	public Object getThreads() {
		return threadScope;
	}

	@Override
	public void terminated() {
		terminated = true;
	}

	/**
	 * @return the pageSource
	 */
	public String getTemplate() {
		return template;
	}

	public void setScope(ThreadsImpl scope) {
		this.scope = scope;
	}

}