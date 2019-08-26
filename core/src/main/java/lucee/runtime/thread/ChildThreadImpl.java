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
package lucee.runtime.thread;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.net.http.HttpUtil;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.ArgumentThreadImpl;
import lucee.runtime.type.scope.Local;
import lucee.runtime.type.scope.LocalImpl;
import lucee.runtime.type.scope.Threads;
import lucee.runtime.type.scope.Undefined;

public class ChildThreadImpl extends ChildThread implements Serializable {

	private static final long serialVersionUID = -8902836175312356628L;

	private static final Collection.Key KEY_ATTRIBUTES = KeyImpl.intern("attributes");

	// private static final Set EMPTY = new HashSet();

	private int threadIndex;
	private PageContextImpl pc = null;
	// PageContextImpl pc =null;
	private final String tagName;
	private long start;
	private Threads scope;

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

	public ChildThreadImpl(PageContextImpl parent, Page page, String tagName, int threadIndex, Struct attrs, boolean serializable) {
		this.serializable = serializable;
		this.tagName = tagName;
		this.threadIndex = threadIndex;
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
		try {
			// daemon
			if (this.pc != null) {
				pc = this.pc;
				ThreadLocalPageContext.register(pc);
			}
			// task
			else {
				ConfigWebImpl cwi;
				try {
					cwi = (ConfigWebImpl) config;
					DevNullOutputStream os = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM;
					pc = ThreadUtil.createPageContext(cwi, os, serverName, requestURI, queryString, SerializableCookie.toCookies(cookies), headers, null, parameters, attributes,
							true, -1);
					pc.setRequestTimeout(requestTimeout);
					p = PageSourceImpl.loadPage(pc, cwi.getPageSources(oldPc == null ? pc : oldPc, null, template, false, false, true));
					// p=cwi.getPageSources(oldPc,null, template, false,false,true).loadPage(cwi);
				}
				catch (PageException e) {
					return e;
				}
				pc.addPageSource(p.getPageSource(), true);
			}

			threadScope = pc.getCFThreadScope();
			pc.setCurrentThreadScope(new ThreadsImpl(this));
			pc.setThread(Thread.currentThread());

			// String encodings = pc.getHttpServletRequest().getHeader("Accept-Encoding");

			Undefined undefined = pc.us();

			Argument newArgs = new ArgumentThreadImpl((Struct) Duplicator.duplicate(attrs, false));
			LocalImpl newLocal = pc.getScopeFactory().getLocalInstance();
			// Key[] keys = attrs.keys();
			Iterator<Entry<Key, Object>> it = attrs.entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				newArgs.setEL(e.getKey(), e.getValue());
			}

			newLocal.setEL(KEY_ATTRIBUTES, newArgs);

			Argument oldArgs = pc.argumentsScope();
			Local oldLocal = pc.localScope();

			int oldMode = undefined.setMode(Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS);
			pc.setFunctionScopes(newLocal, newArgs);

			try {
				p.threadCall(pc, threadIndex);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (!Abort.isSilentAbort(t)) {
					ConfigWeb c = pc.getConfig();
					if (c instanceof ConfigImpl) {
						ConfigImpl ci = (ConfigImpl) c;
						Log log = ci.getLog("thread");
						if (log != null) log.log(Log.LEVEL_ERROR, this.getName(), t);
					}
					PageException pe = Caster.toPageException(t);
					if (!serializable) catchBlock = pe.getCatchBlock(pc.getConfig());
					return pe;
				}
			}
			finally {
				completed = true;
				pc.setFunctionScopes(oldLocal, oldArgs);
				undefined.setMode(oldMode);
				// pc.getScopeFactory().recycle(newArgs);
				pc.getScopeFactory().recycle(pc, newLocal);

				if (pc.getHttpServletResponse() instanceof HttpServletResponseDummy) {
					HttpServletResponseDummy rsp = (HttpServletResponseDummy) pc.getHttpServletResponse();
					pc.flush();
					contentType = rsp.getContentType();
					Pair<String, Object>[] _headers = rsp.getHeaders();
					if (_headers != null) for (int i = 0; i < _headers.length; i++) {
						if (_headers[i].getName().equalsIgnoreCase("Content-Encoding")) contentEncoding = Caster.toString(_headers[i].getValue(), null);
					}
				}
			}
		}
		finally {
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

}