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
package lucee.runtime.listener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;

import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.MimeType;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Component;
import lucee.runtime.ComponentPageImpl;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.component.Member;
import lucee.runtime.config.Constants;
import lucee.runtime.debug.DebuggerImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.MissingIncludeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PostContentAbort;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.net.http.HttpServletRequestDummy;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.Duplicator;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Application;
import lucee.runtime.type.scope.ApplicationImpl;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.UndefinedImpl;
import lucee.runtime.type.scope.session.SessionMemory;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.UDFUtil;

public class ModernAppListener extends AppListenerSupport {

	public static final ModernAppListener instance = new ModernAppListener();

	private static final Collection.Key ON_REQUEST_START = KeyImpl.getInstance("onRequestStart");
	private static final Collection.Key ON_CFCREQUEST = KeyImpl.getInstance("onCFCRequest");
	private static final Collection.Key ON_REQUEST = KeyImpl.getInstance("onRequest");
	private static final Collection.Key ON_REQUEST_END = KeyImpl.getInstance("onRequestEnd");
	private static final Collection.Key ON_ABORT = KeyImpl.getInstance("onAbort");
	private static final Collection.Key ON_APPLICATION_START = KeyImpl.getInstance("onApplicationStart");
	private static final Collection.Key ON_APPLICATION_END = KeyImpl.getInstance("onApplicationEnd");
	private static final Collection.Key ON_SESSION_START = KeyImpl.getInstance("onSessionStart");
	private static final Collection.Key ON_SESSION_END = KeyImpl.getInstance("onSessionEnd");
	private static final Collection.Key ON_DEBUG = KeyImpl.getInstance("onDebug");
	private static final Collection.Key ON_ERROR = KeyConstants._onError;
	private static final Collection.Key ON_MISSING_TEMPLATE = KeyImpl.getInstance("onMissingTemplate");

	// private Map<String,Component> apps=new HashMap<String,Component>();// TODO no longer use this,
	// find a better way to store components for end methods
	protected int mode = MODE_CURRENT2ROOT;

	@Override
	public void onRequest(PageContext pc, PageSource requestedPage, RequestListener rl) throws PageException {
		// on requestStart
		Page appPS = AppListenerUtil.getApplicationPage(pc, requestedPage,
				pc.getRequestDialect() == CFMLEngine.DIALECT_CFML ? Constants.CFML_APPLICATION_EVENT_HANDLER : Constants.LUCEE_APPLICATION_EVENT_HANDLER, mode,
				ApplicationListener.TYPE_MODERN);
		_onRequest(pc, requestedPage, appPS, rl);
	}

	protected void _onRequest(PageContext pc, PageSource requestedPage, Page appP, RequestListener rl) throws PageException {
		PageContextImpl pci = (PageContextImpl) pc;
		pci.setAppListenerType(ApplicationListener.TYPE_MODERN);
		if (appP != null) {
			String callPath = appP.getPageSource().getComponentName();

			Component app = ComponentLoader.loadComponent(pci, appP, callPath, false, false, false, true);

			// init
			ModernApplicationContext appContext = initApplicationContext(pci, app);

			// apps.put(appContext.getName(), app);

			if (!pci.initApplicationContext(this)) return;

			if (rl != null) {
				requestedPage = rl.execute(pc, requestedPage);
				if (requestedPage == null) return;
			}

			String targetPage = requestedPage.getRealpathWithVirtual();
			RefBoolean goon = new RefBooleanImpl(true);

			// onRequestStart
			if (app.contains(pc, ON_REQUEST_START)) {
				try {
					Object rtn = call(app, pci, ON_REQUEST_START, new Object[] { targetPage }, false);
					if (!Caster.toBooleanValue(rtn, true)) return;
				}
				catch (PageException pe) {
					pe = handlePageException(pci, app, pe, requestedPage, targetPage, goon);
					if (pe != null) throw pe;
				}
			}

			// onRequest
			if (goon.toBooleanValue()) {

				boolean isComp = isComponent(pc, requestedPage);
				Object method;
				if (isComp && app.contains(pc, ON_CFCREQUEST) && (method = pc.urlFormScope().get(KeyConstants._method, null)) != null) {

					Struct url = (Struct) Duplicator.duplicate(pc.urlFormScope(), true);

					url.removeEL(KeyConstants._fieldnames);
					url.removeEL(KeyConstants._method);

					Object args = url.get(KeyConstants._argumentCollection, null);

					// url returnFormat
					Object oReturnFormat = url.removeEL(KeyConstants._returnFormat);
					int urlReturnFormat = -1;
					if (oReturnFormat != null) urlReturnFormat = UDFUtil.toReturnFormat(Caster.toString(oReturnFormat, null), -1);

					// request header accept
					List<MimeType> accept = ReqRspUtil.getAccept(pc);
					int headerReturnFormat = MimeType.toFormat(accept, -1, -1);

					Object queryFormat = url.removeEL(KeyConstants._queryFormat);

					if (args == null) {
						args = pc.getHttpServletRequest().getAttribute("argumentCollection");
					}

					if (args instanceof String) {
						args = new JSONExpressionInterpreter().interpret(pc, (String) args);
					}

					if (args != null) {
						if (Decision.isCastableToStruct(args)) {
							Struct sct = Caster.toStruct(args, false);
							// Key[] keys = url.keys();
							Iterator<Entry<Key, Object>> it = url.entryIterator();
							Entry<Key, Object> e;
							while (it.hasNext()) {
								e = it.next();
								sct.setEL(e.getKey(), e.getValue());
							}
							args = sct;
						}
						else if (Decision.isCastableToArray(args)) {
							args = Caster.toArray(args);
						}
						else {
							Array arr = new ArrayImpl();
							arr.appendEL(args);
							args = arr;
						}
					}
					else args = url;

					Object rtn = call(app, pci, ON_CFCREQUEST, new Object[] { requestedPage.getComponentName(), method, args }, true);

					if (rtn != null) {
						if (pc.getHttpServletRequest().getHeader("AMF-Forward") != null) {
							pc.variablesScope().setEL("AMF-Forward", rtn);
						}
						else {
							try {
								ComponentPageImpl.writeToResponseStream(pc, app, method.toString(), urlReturnFormat, headerReturnFormat, queryFormat, rtn);
							}
							catch (Exception e) {
								throw Caster.toPageException(e);
							}
						}
					}
				}
				else {
					try {
						if (!isComp && app.contains(pc, ON_REQUEST)) call(app, pci, ON_REQUEST, new Object[] { targetPage }, false);
						else pci._doInclude(new PageSource[] { requestedPage }, false, null);
					}
					catch (PageException pe) {
						pe = handlePageException(pci, app, pe, requestedPage, targetPage, goon);
						if (pe != null) throw pe;
					}
				}
			}
			// onRequestEnd
			if (goon.toBooleanValue() && app.contains(pc, ON_REQUEST_END)) {
				try {
					call(app, pci, ON_REQUEST_END, new Object[] { targetPage }, false);
				}
				catch (PageException pe) {
					pe = handlePageException(pci, app, pe, requestedPage, targetPage, goon);
					if (pe != null) throw pe;
				}
			}
		}
		else {
			// apps.put(pc.getApplicationContext().getName(), null);

			if (rl != null) {
				requestedPage = rl.execute(pc, requestedPage);
				if (requestedPage == null) return;
			}

			pci._doInclude(new PageSource[] { requestedPage }, false, null);
		}
	}

	private boolean isComponent(PageContext pc, PageSource requestedPage) {
		// CFML
		if (pc.getRequestDialect() == CFMLEngine.DIALECT_CFML) {
			return ResourceUtil.getExtension(requestedPage.getRealpath(), "").equalsIgnoreCase(Constants.getCFMLComponentExtension());
		}
		// Lucee
		return !PageSourceImpl.isTemplate(pc, requestedPage, true);
	}

	private PageException handlePageException(PageContextImpl pci, Component app, PageException pe, PageSource requestedPage, String targetPage, RefBoolean goon)
			throws PageException {
		PageException _pe = pe;
		if (pe instanceof ModernAppListenerException) {
			_pe = ((ModernAppListenerException) pe).getPageException();
		}

		if (!Abort.isSilentAbort(_pe)) {
			if (_pe instanceof MissingIncludeException) {
				if (((MissingIncludeException) _pe).getPageSource().equals(requestedPage)) {

					if (app.contains(pci, ON_MISSING_TEMPLATE)) {
						goon.setValue(false);
						if (!Caster.toBooleanValue(call(app, pci, ON_MISSING_TEMPLATE, new Object[] { targetPage }, true), true)) return pe;
					}
					else return pe;
				}
				else return pe;
			}
			else return pe;
		}
		else {
			if (!pci.isGatewayContext() && pci.getConfig().debug()) {
				((DebuggerImpl) pci.getDebugger()).setAbort(ExceptionUtil.getThrowingPosition(pci, _pe));
			}
			goon.setValue(false);
			if (app.contains(pci, ON_ABORT)) {
				call(app, pci, ON_ABORT, new Object[] { targetPage }, true);
			}
		}
		return null;
	}

	@Override
	public boolean onApplicationStart(PageContext pc) throws PageException {
		return onApplicationStart(pc, pc.applicationScope());
	}

	@Override
	public boolean onApplicationStart(PageContext pc, Application application) throws PageException {
		Component app = getComponent(pc);

		if (app != null && app.contains(pc, ON_APPLICATION_END)) {
			if (application instanceof ApplicationImpl) ((ApplicationImpl) application).setComponent(app);
		}

		if (app != null && app.contains(pc, ON_APPLICATION_START)) {
			Object rtn = call(app, pc, ON_APPLICATION_START, ArrayUtil.OBJECT_EMPTY, true);
			return Caster.toBooleanValue(rtn, true);
		}
		return true;
	}

	@Override
	public void onApplicationEnd(CFMLFactory factory, String applicationName) throws PageException {
		Component app = null;
		Application scope = ((CFMLFactoryImpl) factory).getScopeContext().getExistingApplicationScope(applicationName);

		if (scope instanceof ApplicationImpl) app = ((ApplicationImpl) scope).getComponent();

		if (app == null) return;

		PageContextImpl pc = (PageContextImpl) ThreadLocalPageContext.get();
		boolean createPc = pc == null;
		try {
			if (createPc) pc = createPageContext(factory, app, applicationName, null, ON_APPLICATION_END, true, -1);
			call(app, pc, ON_APPLICATION_END, new Object[] { pc.applicationScope() }, true);
		}
		finally {
			if (createPc && pc != null) {
				factory.releaseLuceePageContext(pc, createPc);
			}
		}
	}

	@Override
	public void onSessionStart(PageContext pc) throws PageException {
		onSessionStart(pc, pc.sessionScope());
	}

	@Override
	public void onSessionStart(PageContext pc, Session session) throws PageException {

		// component
		Component app = getComponent(pc);
		if (hasOnSessionStart(pc, app)) {
			call(app, pc, ON_SESSION_START, ArrayUtil.OBJECT_EMPTY, true);
		}
		if (hasOnSessionEnd(pc, app)) {
			if (session instanceof SessionMemory) ((SessionMemory) session).setComponent(app);
		}
	}

	@Override
	public void onSessionEnd(CFMLFactory factory, String applicationName, String cfid) throws PageException {

		Component app = null;
		Session scope = ((CFMLFactoryImpl) factory).getScopeContext().getExistingCFSessionScope(applicationName, cfid);
		if (scope instanceof SessionMemory) app = ((SessionMemory) scope).getComponent();

		if (app == null || !app.containsKey(ON_SESSION_END)) return;

		PageContextImpl pc = null;
		try {
			pc = createPageContext(factory, app, applicationName, cfid, ON_SESSION_END, true, -1);
			call(app, pc, ON_SESSION_END, new Object[] { pc.sessionScope(false), pc.applicationScope() }, true);
		}
		finally {
			if (pc != null) {
				factory.releaseLuceePageContext(pc, true);
			}
		}
	}

	private PageContextImpl createPageContext(CFMLFactory factory, Component app, String applicationName, String cfid, Collection.Key methodName, boolean register, long timeout)
			throws PageException {
		Resource root = factory.getConfig().getRootDirectory();
		String path = app.getPageSource().getRealpathWithVirtual();

		// Request
		HttpServletRequestDummy req = new HttpServletRequestDummy(root, "localhost", path, "", null, null, null, null, null, null);
		if (!StringUtil.isEmpty(cfid)) req.setCookies(new Cookie[] { new Cookie("cfid", cfid), new Cookie("cftoken", "0") });

		// Response
		OutputStream os = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM;

		HttpServletResponseDummy rsp = new HttpServletResponseDummy(os);

		// PageContext
		PageContextImpl pc = (PageContextImpl) factory.getLuceePageContext(factory.getServlet(), req, rsp, null, false, -1, false, register, timeout, true, false);

		// ApplicationContext
		ClassicApplicationContext ap = new ClassicApplicationContext(factory.getConfig(), applicationName, false,
				app == null ? null : ResourceUtil.getResource(pc, app.getPageSource(), null));
		initApplicationContext(pc, app);
		ap.setName(applicationName);
		ap.setSetSessionManagement(true);

		// Base
		pc.setBase(app.getPageSource());
		return pc;
	}

	@Override
	public void onDebug(PageContext pc) throws PageException {
		if (((PageContextImpl) pc).isGatewayContext() || !pc.getConfig().debug()) return;
		Component app = getComponent(pc);
		if (app != null && app.contains(pc, ON_DEBUG)) {
			call(app, pc, ON_DEBUG, new Object[] { pc.getDebugger().getDebuggingData(pc) }, true);
			return;
		}
		try {
			pc.getDebugger().writeOut(pc);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public void onError(PageContext pc, PageException pe) {
		Component app = getComponent(pc);
		if (app != null && app.containsKey(ON_ERROR) && !Abort.isSilentAbort(pe)) {
			try {
				String eventName = "";
				if (pe instanceof ModernAppListenerException) eventName = ((ModernAppListenerException) pe).getEventName();
				if (eventName == null) eventName = "";

				call(app, pc, ON_ERROR, new Object[] { pe.getCatchBlock(pc.getConfig()), eventName }, true);
				return;
			}
			catch (PageException _pe) {
				pe = _pe;
			}
		}
		pc.handlePageException(pe);
	}

	private Object call(Component app, PageContext pc, Collection.Key eventName, Object[] args, boolean catchAbort) throws PageException {
		try {
			return app.call(pc, eventName, args);
		}
		catch (PageException pe) {
			if (Abort.isSilentAbort(pe)) {
				if (catchAbort) return (pe instanceof PostContentAbort) ? Boolean.TRUE : Boolean.FALSE;

				throw pe;
			}
			throw new ModernAppListenerException(pe, eventName.getString());
		}
	}

	private ModernApplicationContext initApplicationContext(PageContextImpl pc, Component app) throws PageException {

		// use existing app context
		RefBoolean throwsErrorWhileInit = new RefBooleanImpl(false);
		ModernApplicationContext appContext = new ModernApplicationContext(pc, app, throwsErrorWhileInit);

		pc.setApplicationContext(appContext);

		// scope cascading
		if (pc.getRequestDialect() == CFMLEngine.DIALECT_CFML && ((UndefinedImpl) pc.undefinedScope()).getScopeCascadingType() != appContext.getScopeCascading()) {
			pc.undefinedScope().initialize(pc);
		}

		// ORM
		if (appContext.isORMEnabled()) {
			boolean hasError = throwsErrorWhileInit.toBooleanValue();
			if (hasError) pc.addPageSource(app.getPageSource(), true);
			try {
				ORMUtil.resetEngine(pc, false);
			}
			finally {
				if (hasError) pc.removeLastPageSource(true);
			}
		}
		return appContext;
	}

	private static Object get(Component app, Key name, String defaultValue) {
		Member mem = app.getMember(Component.ACCESS_PRIVATE, name, true, false);
		if (mem == null) return defaultValue;
		return mem.getValue();
	}

	@Override
	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public int getMode() {
		return mode;
	}

	@Override
	public String getType() {
		return "modern";
	}

	@Override
	public boolean hasOnSessionStart(PageContext pc) {
		return hasOnSessionStart(pc, getComponent(pc));
	}

	private boolean hasOnSessionStart(PageContext pc, Component app) {
		return app != null && app.contains(pc, ON_SESSION_START);
	}

	private boolean hasOnSessionEnd(PageContext pc, Component app) {
		return app != null && app.contains(pc, ON_SESSION_END);
	}

	public static ModernAppListener getInstance() {
		return instance;
	}

	private Component getComponent(PageContext pc) {
		ApplicationContext ac = ((PageContextImpl) pc).getApplicationContext();
		Component cfc = null;
		if (ac instanceof ModernApplicationContext) cfc = ((ModernApplicationContext) ac).getComponent();
		// if(cfc==null) cfc = apps.get(pc.getApplicationContext().getName());

		return cfc;
	}
}