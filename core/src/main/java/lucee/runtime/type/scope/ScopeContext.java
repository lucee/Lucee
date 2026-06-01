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
package lucee.runtime.type.scope;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.servlet.http.HttpSession;
import lucee.commons.collection.MapFactory;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SizeOf;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.db.DataSource;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExceptionHandler;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.client.ClientCookie;
import lucee.runtime.type.scope.client.ClientFile;
import lucee.runtime.type.scope.client.ClientMemory;
import lucee.runtime.type.scope.session.SessionFile;
import lucee.runtime.type.scope.session.SessionMemory;
import lucee.runtime.type.scope.storage.IKHandlerCache;
import lucee.runtime.type.scope.storage.IKHandlerDatasource;
import lucee.runtime.type.scope.storage.IKStorageScopeItem;
import lucee.runtime.type.scope.storage.IKStorageScopeSupport;
import lucee.runtime.type.scope.storage.MemoryScope;
import lucee.runtime.type.scope.storage.StorageScope;
import lucee.runtime.type.scope.storage.StorageScopeCleaner;
import lucee.runtime.type.scope.storage.StorageScopeEngine;
import lucee.runtime.type.scope.storage.StorageScopeImpl;
import lucee.runtime.type.scope.storage.clean.DatasourceStorageScopeCleaner;
import lucee.runtime.type.scope.storage.clean.FileStorageScopeCleaner;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.wrap.MapAsStruct;
import lucee.runtime.util.PageContextUtil;

/**
 * handles the Scopes, e.g. Application, Session, etc., for a ServletContext
 */
public final class ScopeContext {

	private static final int MINUTE = 60 * 1000;
	private static final long CLIENT_MEMORY_TIMESPAN = 1 * MINUTE;
	private static final long SESSION_MEMORY_TIMESPAN = 1 * MINUTE;

	private final static Set<Key> IGNORE_SESSION = new HashSet<>();
	private final static Set<Key> IGNORE_CLIENT = new HashSet<>();
	static {
		IGNORE_SESSION.add(KeyConstants._csrf_token);
		IGNORE_SESSION.add(KeyConstants._cfid);
		IGNORE_SESSION.add(KeyConstants._cftoken);
		IGNORE_SESSION.add(KeyConstants._urltoken);
		IGNORE_SESSION.add(KeyConstants._timecreated);
		IGNORE_SESSION.add(KeyConstants._lastvisit);
		IGNORE_SESSION.add(KeyConstants._sessionid);

		IGNORE_CLIENT.add(KeyConstants._csrf_token);
		IGNORE_CLIENT.add(KeyConstants._cfid);
		IGNORE_CLIENT.add(KeyConstants._cftoken);
		IGNORE_CLIENT.add(KeyConstants._urltoken);
		IGNORE_CLIENT.add(KeyConstants._timecreated);
		IGNORE_CLIENT.add(KeyConstants._lastvisit);
		IGNORE_CLIENT.add(KeyConstants._hitcount);
	}

	private Map<String, Map<String, Scope>> cfSessionContexts = MapFactory.<String, Map<String, Scope>>getConcurrentMap();
	private Map<String, Map<String, Scope>> cfClientContexts = MapFactory.<String, Map<String, Scope>>getConcurrentMap();
	private Map<String, Application> applicationContexts = MapFactory.<String, Application>getConcurrentMap();

	private int maxSessionTimeout = 0;

	private static Server server = null;

	private StorageScopeEngine client;
	private StorageScopeEngine session;
	private CFMLFactoryImpl factory;

	public ScopeContext(CFMLFactoryImpl factory) {
		this.factory = factory;
	}

	/**
	 * @return the log
	 */
	private Log getLog() {
		return ThreadLocalPageContext.getLog(factory.getConfig(), "scope");
	}

	public void debug(String msg) {
		debug(getLog(), msg);
	}

	public void info(String msg) {
		info(getLog(), msg);
	}

	public void error(String msg) {
		error(getLog(), msg);
	}

	public void error(Throwable t) {
		error(getLog(), t);
	}

	public static void debug(Log log, String msg) {
		if (LogUtil.doesDebug(log)) log.log(Log.LEVEL_DEBUG, "scope-context", msg + "; " + ExceptionUtil.getTagContextLine(null));
	}

	public static void info(Log log, String msg) {
		if (LogUtil.doesInfo(log)) log.log(Log.LEVEL_INFO, "scope-context", msg + "; " + ExceptionUtil.getTagContextLine(null));
	}

	public static void error(Log log, String msg) {
		if (LogUtil.doesError(log)) log.log(Log.LEVEL_ERROR, "scope-context", msg + "; " + ExceptionUtil.getTagContextLine(null));
	}

	public static void error(Log log, Throwable t) {
		if (LogUtil.doesError(log)) log.log(Log.LEVEL_ERROR, "scope-context", t);
	}

	/**
	 * return a map matching key from given map
	 *
	 * @param parent
	 * @param key key of the map
	 * @return matching map, if no map exist it willbe one created
	 */
	private Map<String, Scope> getSubMap(Map<String, Map<String, Scope>> parent, String key) {

		Map<String, Scope> context = parent.get(key);
		if (context == null) {
			synchronized (SystemUtil.createToken("getSubMap", key)) {
				context = parent.get(key);
				if (context == null) {
					context = MapFactory.<String, Scope>getConcurrentMap();
					parent.put(key, context);
				}
			}
		}
		return context;

	}

	/**
	 * return the server Scope for this context
	 *
	 * @param pc
	 * @return server scope
	 */
	public static Server getServerScope(PageContext pc, boolean jsr223) {
		if (server == null) {
			server = new ServerImpl(pc, jsr223);
		}
		return server;
	}

	public Client getClientScope(PageContext pc, boolean createIfNeeded) throws PageException {
		return (Client) getCFScope(pc, createIfNeeded, Scope.SCOPE_CLIENT);
	}

	private StorageScope getCFScope(PageContext pc, boolean createIfNeeded, int scopeType) throws PageException {
		// if there is no CFID, there can be no existing session or client scope
		if (!createIfNeeded && !((PageContextImpl) pc).hasCFID()) return null;

		if (scopeType != Scope.SCOPE_SESSION && scopeType != Scope.SCOPE_CLIENT)
			throw new ApplicationException("invalid scope defintion [" + scopeType + "], valid scopes are [Scope.SCOPE_CLIENT,Scope.SCOPE_SESSION]");
		boolean isSession = scopeType == Scope.SCOPE_SESSION;

		ApplicationContext appContext = pc.getApplicationContext();
		// get Context
		Map<String, Scope> context = getSubMap(isSession ? cfSessionContexts : cfClientContexts, appContext.getName());

		// set default
		boolean isMemory = false;
		String storage = isSession ? appContext.getSessionstorage() : appContext.getClientstorage();
		if (StringUtil.isEmpty(storage, true)) {
			storage = isSession ? ConfigPro.DEFAULT_STORAGE_SESSION : ConfigPro.DEFAULT_STORAGE_CLIENT;
		}
		else {
			storage = storage.trim();
		}

		// clean up storage type
		if ("ram".equalsIgnoreCase(storage)) {
			storage = "memory";
			isMemory = true;
		}
		else if ("registry".equalsIgnoreCase(storage)) {
			storage = "file";
		}
		else {
			storage = storage.toLowerCase();
			if ("memory".equals(storage)) isMemory = true;
		}

		StorageScope existing = (StorageScope) context.get(pc.getCFID());
		if (existing != null && existing.isExpired()) {
			existing = null;
		}
		boolean cluster = isSession ? appContext.getSessionCluster() : appContext.getClientCluster();
		StorageScope scope = cluster ? null : existing;

		if (scope == null || !scope.getStorage().equalsIgnoreCase(storage)) {
			synchronized (SystemUtil.createToken("getCFScope", pc.getCFID())) {
				if (scope == null || !scope.getStorage().equalsIgnoreCase(storage)) {
					// memory
					if (isMemory) {
						if (existing != null) scope = existing;
						else if (createIfNeeded) {
							if (isSession) scope = (StorageScope) SessionMemory.getInstance(pc, getLog());
							else scope = ClientMemory.getInstance(pc, getLog());
						}
						else {
							scope = null;
						}
					}

					// file
					else if ("file".equals(storage)) {
						if (isSession) scope = (StorageScope) SessionFile.getInstance(appContext.getName(), pc, createIfNeeded, getLog());
						else scope = ClientFile.getInstance(appContext.getName(), pc, createIfNeeded, getLog());
					}

					// cookie
					else if ("cookie".equals(storage)) {
						if (isSession) {
							throw new ApplicationException("sessionStorage cookie is no longer supported");
						}
						else scope = ClientCookie.getInstance(appContext.getName(), pc, createIfNeeded, getLog());
					}

					// cache/datasource
					else {
						DataSource ds = pc.getDataSource(storage, null);
						if (ds != null && ds.isStorage()) {
							try {
								scope = (StorageScope) IKStorageScopeSupport.getInstance(scopeType, new IKHandlerDatasource(), appContext.getName(), storage, pc, existing,
										createIfNeeded, getLog());
							}
							catch (Exception ex) {
								LogUtil.log("scope-context", ex);
							}
						}
						else {
							scope = (StorageScope) IKStorageScopeSupport.getInstance(scopeType, new IKHandlerCache(), appContext.getName(), storage, pc, existing, createIfNeeded,
									getLog());
						}

						if (createIfNeeded && scope == null) {
							// datasource not enabled for storage
							if (ds != null) {
								if (!ds.isStorage()) throw new ApplicationException("datasource [" + storage + "] is not enabled to be used as client/session storage, "
										+ "you have to enable it in the Lucee administrator or define key \"storage=true\" for datasources defined in the application event handler.");
								throw new ApplicationException("datasource [" + storage
										+ "] could not be reached for client/session storage. Please make sure the datasource settings are correct, and the datasource is available.");
							}
							CacheConnection cc = CacheUtil.getCacheConnection(pc, storage, null);
							if (cc != null) throw new ApplicationException(
									"cache [" + storage + "] is not enabled to be used  as a session/client storage, you have to enable it in the Lucee administrator.");

							throw new ApplicationException("there is no cache or datasource with name [" + storage + "] defined.");
						}

					}
					if (!createIfNeeded && scope == null) return null;
					scope.setStorage(storage);
					context.put(pc.getCFID(), scope);

				}
			}
		}
		else {
			getLog().log(Log.LEVEL_INFO, "scope-context",
					"use existing " + (isSession ? "session" : "client") + " scope for " + appContext.getName() + "/" + pc.getCFID() + " from storage " + storage);
		}
		scope.touchBeforeRequest(pc);
		return scope;
	}

	/**
	 * return the session count of all application contexts
	 *
	 * @return
	 */
	public int getSessionCount(PageContext pc) {
		if (pc.getSessionType() == Config.SESSION_TYPE_JEE) return 0;

		return getSessionCount();
	}

	public int getSessionCount() {
		Iterator<Entry<String, Map<String, Scope>>> it = cfSessionContexts.entrySet().iterator();
		Entry<String, Map<String, Scope>> entry;
		int count = 0;
		while (it.hasNext()) {
			entry = it.next();
			count += getCount(entry.getValue());
		}
		return count;
	}

	public int getClientCount() {
		Iterator<Entry<String, Map<String, Scope>>> it = cfClientContexts.entrySet().iterator();
		Entry<String, Map<String, Scope>> entry;
		int count = 0;
		while (it.hasNext()) {
			entry = it.next();
			count += getCount(entry.getValue());
		}
		return count;
	}

	/**
	 * return the session count of this application context
	 *
	 * @return
	 */
	public int getAppContextSessionCount(PageContext pc) {
		ApplicationContext appContext = pc.getApplicationContext();
		if (pc.getSessionType() == Config.SESSION_TYPE_JEE) return 0;

		Map<String, Scope> context = getSubMap(cfSessionContexts, appContext.getName());
		return getCount(context);
	}

	public int getAppContextCount() {
		return this.applicationContexts.size();
	}

	private int getCount(Map<String, Scope> context) {
		Iterator<Entry<String, Scope>> it = context.entrySet().iterator();
		Entry<String, Scope> entry;
		int count = 0;
		StorageScope s;
		while (it.hasNext()) {
			entry = it.next();
			if (entry.getValue() instanceof StorageScope) {
				s = (StorageScope) entry.getValue();
				if (!s.isExpired()) count++;
			}
		}
		return count;
	}

	/**
	 * return all session context of this application context
	 *
	 * @param pc
	 * @return
	 */
	public Struct getAllSessionScopes(PageContext pc) {
		return getAllSessionScopes(pc.getApplicationContext().getName());
	}

	public Struct getAllApplicationScopes() {
		return StructImpl.copy(MapAsStruct.toStruct(applicationContexts, true), false);
	}

	public Struct getAllCFSessionScopes() {
		return StructImpl.copy(MapAsStruct.toStruct(this.cfSessionContexts, true), false);
	}

	/**
	 * return the size in bytes of all session contexts
	 *
	 * @return size in bytes
	 * @throws ExpressionException
	 */
	public long getScopesSize(int scope) throws ExpressionException {
		if (scope == Scope.SCOPE_APPLICATION) return SizeOf.size(applicationContexts);
		if (scope == Scope.SCOPE_SERVER) return SizeOf.size(server);
		if (scope == Scope.SCOPE_SESSION) return SizeOf.size(this.cfSessionContexts);
		if (scope == Scope.SCOPE_CLIENT) return SizeOf.size(this.cfClientContexts);

		throw new ExpressionException("can only return information of scope that are not request dependent");
	}

	/**
	 * get all session contexts of given applicaton name
	 *
	 * @param pc
	 * @param appName
	 * @return
	 * @deprecated use instead getAllSessionScopes(String appName)
	 */
	@Deprecated
	public Struct getAllSessionScopes(PageContext pc, String appName) {
		return getAllSessionScopes(appName);
	}

	/**
	 * get all session contexts of given applicaton name
	 *
	 * @param appName
	 * @return
	 */
	public Struct getAllSessionScopes(String appName) {
		// if(pc.getSessionType()==Config.SESSION_TYPE_J2EE)return new StructImpl();
		return getAllSessionScopes(getSubMap(cfSessionContexts, appName), appName);
	}

	private Struct getAllSessionScopes(Map<String, Scope> context, String appName) {
		Iterator<Entry<String, Scope>> it = context.entrySet().iterator();
		Entry<String, Scope> entry;
		Struct sct = new StructImpl(Struct.TYPE_SYNC);
		Session s;
		while (it.hasNext()) {
			entry = it.next();
			s = (Session) entry.getValue();
			if (!s.isExpired()) sct.setEL(KeyImpl.init(appName + "_" + entry.getKey() + "_0"), s);
		}
		return sct;
	}

	/**
	 * return the session Scope for this context (cfid,cftoken,contextname)
	 *
	 * @param pc PageContext
	 * @return session matching the context
	 * @throws PageException
	 */
	public Session getSessionScope(PageContext pc) throws PageException {
		if (pc.getSessionType() == Config.SESSION_TYPE_APPLICATION) return (Session) getCFScope(pc, true, Scope.SCOPE_SESSION);
		return getJSessionScope(pc);
	}

	public Object getExistingSessionScope(PageContext pc) {
		if (pc.getSessionType() == Config.SESSION_TYPE_APPLICATION) {
			try {
				return getCFScope(pc, false, Scope.SCOPE_SESSION);
			}
			catch (Exception e) {
				return null;
			}
		}
		return hasExistingJSessionScope(pc);
	}

	/**
	 * this method still is used in some extension, so keep it in place
	 */
	@Deprecated
	public boolean hasExistingSessionScope(PageContext pc) {
		return getExistingSessionScope(pc) != null;
	}

	private Object hasExistingJSessionScope(PageContext pc) {
		HttpSession httpSession = ((PageContextImpl) pc).getHttpServletRequest().getSession(false);
		if (httpSession == null) return null;

		Session session = (Session) httpSession.getAttribute(pc.getApplicationContext().getName());
		if (session instanceof JSession && !session.isExpired()) {
			return session;
		}
		return null;
	}

	public Session getExistingCFSessionScope(String applicationName, String cfid) {
		Map<String, Scope> context = getSubMap(cfSessionContexts, applicationName);
		if (context != null) {
			return (Session) context.get(cfid);
		}
		return null;
	}

	public void removeSessionScope(PageContext pc) {
		removeCFSessionScope(pc);
		removeJSessionScope(pc);
	}

	public void removeJSessionScope(PageContext pc) {
		HttpSession httpSession = pc.getSession();
		if (httpSession != null) {
			ApplicationContext appContext = pc.getApplicationContext();
			httpSession.removeAttribute(appContext.getName());
		}
	}

	public void removeCFSessionScope(PageContext pc) {

		ApplicationContext appContext = pc.getApplicationContext();
		Map<String, Scope> context = getSubMap(cfSessionContexts, appContext.getName());
		if (context != null) {
			// LDEV-6046: Get scope BEFORE removing from memory, so we can unstore it
			// Don't use getCFScope() as it would reload from DB and put back in memory
			Scope scope = context.remove(pc.getCFID());
			if (scope instanceof StorageScope) {
				((StorageScope) scope).unstore(pc.getConfig());
			}
		}
	}

	public void removeClientScope(PageContext pc) {
		ApplicationContext appContext = pc.getApplicationContext();
		Map<String, Scope> context = getSubMap(cfClientContexts, appContext.getName());
		if (context != null) {
			// LDEV-6046: Get scope BEFORE removing from memory, so we can unstore it
			// Don't use getCFScope() as it would reload from DB and put back in memory
			Scope scope = context.remove(pc.getCFID());
			if (scope instanceof StorageScope) {
				((StorageScope) scope).unstore(pc.getConfig());
			}
		}
	}

	public boolean remove(int type, String appName, String cfid) {
		Map<String, Map<String, Scope>> contexts = type == Scope.SCOPE_CLIENT ? cfClientContexts : cfSessionContexts;
		Map<String, Scope> context = getSubMap(contexts, appName);
		Object res = context.remove(cfid);
		getLog().log(Log.LEVEL_INFO, "scope-context", "remove " + VariableInterpreter.scopeInt2String(type) + " scope " + appName + "/" + cfid + " from memory");

		return res != null;
	}

	/**
	 * return j session scope
	 *
	 * @param pc PageContext
	 * @param isNew
	 * @return j session matching the context
	 * @throws PageException
	 */
	private Session getJSessionScope(PageContext pc) throws PageException {
		HttpSession httpSession = pc.getSession();
		ApplicationContext appContext = pc.getApplicationContext();
		Object session = null;// this is from type object, because it is possible that httpSession return object from
		// prior restart

		int s = (int) appContext.getSessionTimeout().getSeconds();
		if (maxSessionTimeout < s) maxSessionTimeout = s;

		if (httpSession != null) {
			httpSession.setMaxInactiveInterval(maxSessionTimeout + 60);
			session = httpSession.getAttribute(appContext.getName());
		}
		else {
			Map<String, Scope> context = getSubMap(cfSessionContexts, appContext.getName());
			session = context.get(pc.getCFID());
		}

		JSession jSession = null;
		if (session instanceof JSession) {
			jSession = (JSession) session;
			try {
				if (jSession.isExpired()) {
					if (httpSession == null) jSession.touch();
					else jSession = createNewJSession(pc, httpSession);

				}
				info(getLog(), "use existing JSession for " + appContext.getName() + "/" + pc.getCFID());

			}
			catch (ClassCastException cce) {
				error(getLog(), cce);
				// if there is no HTTPSession
				if (httpSession == null) return (Session) getCFScope(pc, true, Scope.SCOPE_SESSION);

				jSession = new JSession();
				httpSession.setAttribute(appContext.getName(), jSession);
			}
		}
		else {
			// if there is no HTTPSession
			if (httpSession == null) return (Session) getCFScope(pc, true, Scope.SCOPE_SESSION);
			jSession = createNewJSession(pc, httpSession);
		}
		jSession.touchBeforeRequest(pc);
		return jSession;
	}

	private JSession createNewJSession(PageContext pc, HttpSession httpSession) {
		ApplicationContext appContext = pc.getApplicationContext();
		debug(getLog(), "create new JSession for " + appContext.getName() + "/" + pc.getCFID());
		JSession jSession = new JSession();
		httpSession.setAttribute(appContext.getName(), jSession);
		Map<String, Scope> context = getSubMap(cfSessionContexts, appContext.getName());
		context.put(pc.getCFID(), jSession);
		return jSession;
	}

	/**
	 * return the application Scope for this context (cfid,cftoken,contextname)
	 *
	 * @param pc PageContext
	 * @param createUpdateIfNotExist
	 * @param isNew
	 * @return session matching the context
	 */
	public Application getApplicationScope(PageContext pc, boolean createUpdateIfNotExist, RefBoolean isNew) {
		ApplicationContext appContext = pc.getApplicationContext();
		// getApplication Scope from Context
		ApplicationImpl application;
		Object objApp = applicationContexts.get(appContext.getName());
		if (objApp != null) {
			application = (ApplicationImpl) objApp;
			if (application.isExpired()) {
				if (!createUpdateIfNotExist) return null;
				application.release(pc);
				isNew.setValue(true);
			}
		}
		else {
			if (!createUpdateIfNotExist) return null;
			application = new ApplicationImpl();
			applicationContexts.put(appContext.getName(), application);
			isNew.setValue(true);
		}
		application.touchBeforeRequest(pc);
		// if(newApplication)listener.onApplicationStart(pc);

		return application;
	}

	public void removeApplicationScope(PageContext pc) {
		applicationContexts.remove(pc.getApplicationContext().getName());
	}

	public Application getExistingApplicationScope(String applicationName) {
		return applicationContexts.get(applicationName);
	}

	/**
	 * remove all unused scope objects
	 */
	public void clearUnused() {
		clearUnused(false);
	}

	public void clearUnused(boolean force) {
		Log log = getLog();
		try {
			// create cleaner engine for session/client scope
			if (session == null) session = new StorageScopeEngine(factory, log, new StorageScopeCleaner[] { new FileStorageScopeCleaner(Scope.SCOPE_SESSION, null)// new
					// SessionEndListener())
					, new DatasourceStorageScopeCleaner(Scope.SCOPE_SESSION, null)// new
					// SessionEndListener())
					// ,new CacheStorageScopeCleaner(Scope.SCOPE_SESSION, new SessionEndListener())
			});
			if (client == null) client = new StorageScopeEngine(factory, log,
					new StorageScopeCleaner[] { new FileStorageScopeCleaner(Scope.SCOPE_CLIENT, null), new DatasourceStorageScopeCleaner(Scope.SCOPE_CLIENT, null)
					// ,new CacheStorageScopeCleaner(Scope.SCOPE_CLIENT, null) //Cache storage need no control, if
					// there is no listener
					});

			// store session/client scope and remove from memory
			storeUnusedStorageScope(factory, Scope.SCOPE_CLIENT, force);
			storeUnusedStorageScope(factory, Scope.SCOPE_SESSION, force);

			// remove unused memory based client/session scope (invoke onSessonEnd)
			clearUnusedMemoryScope(factory, Scope.SCOPE_CLIENT);
			clearUnusedMemoryScope(factory, Scope.SCOPE_SESSION);

			// session must be executed first, because session creates a reference from client scope
			session.clean(force);
			client.clean(force);

			// clean all unused application scopes
			clearUnusedApplications(factory);
		}
		catch (Exception t) {
			error(t);
		}
	}

	/**
	 * remove all scope objects
	 */
	public void clear() {
		try {
			Scope scope;
			// Map.Entry entry,e;
			// Map context;

			// release all session scopes
			Iterator<Entry<String, Map<String, Scope>>> sit = cfSessionContexts.entrySet().iterator();
			Entry<String, Map<String, Scope>> sentry;
			Map<String, Scope> context;
			Iterator<Entry<String, Scope>> itt;
			Entry<String, Scope> e;
			PageContext pc = ThreadLocalPageContext.get();
			while (sit.hasNext()) {
				sentry = sit.next();
				context = sentry.getValue();
				itt = context.entrySet().iterator();
				while (itt.hasNext()) {
					e = itt.next();
					scope = e.getValue();
					scope.release(pc);
				}
			}
			cfSessionContexts.clear();

			// release all application scopes
			Iterator<Entry<String, Application>> ait = applicationContexts.entrySet().iterator();
			Entry<String, Application> aentry;
			while (ait.hasNext()) {
				aentry = ait.next();
				scope = aentry.getValue();
				scope.release(pc);
			}
			applicationContexts.clear();

			// release server scope
			if (server != null) {
				server.release(pc);
				server = null;
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	private void storeUnusedStorageScope(CFMLFactoryImpl cfmlFactory, int type, boolean force) {
		Map<String, Map<String, Scope>> contexts = type == Scope.SCOPE_CLIENT ? cfClientContexts : cfSessionContexts;
		long timespan = type == Scope.SCOPE_CLIENT ? CLIENT_MEMORY_TIMESPAN : SESSION_MEMORY_TIMESPAN;
		String strType = VariableInterpreter.scopeInt2String(type);

		if (contexts.size() == 0) return;
		long now = System.currentTimeMillis();
		Object[] arrContexts = contexts.keySet().toArray();
		Object applicationName, cfid, o;
		Map<String, Scope> fhm;
		for (int i = 0; i < arrContexts.length; i++) {

			applicationName = arrContexts[i];
			fhm = contexts.get(applicationName);
			if (fhm.size() > 0) {
				Object[] arrClients = fhm.keySet().toArray();
				int count = arrClients.length;
				for (int y = 0; y < arrClients.length; y++) {
					cfid = arrClients[y];
					o = fhm.get(cfid);
					if (!(o instanceof StorageScope)) continue;
					StorageScope scope = (StorageScope) o;
					if (scope.lastVisit() + timespan < now || (force && scope.isExpired())) {
						if (!(scope instanceof MemoryScope)) {
							getLog().log(Log.LEVEL_INFO, "scope-context",
									"remove " + strType + " scope [" + applicationName + "/" + cfid + "] from memory, it remain in storage [" + scope.getStorage() + "]");
							fhm.remove(arrClients[y]);
							count--;
						}
						else if (!((MemoryScope) scope).hasContent()) {
							getLog().log(Log.LEVEL_INFO, "scope-context", "remove " + strType + " scope [" + applicationName + "/" + cfid + "] from memory, because it is empty.");
							fhm.remove(arrClients[y]);
							count--;
						}
					}
				}
				if (count == 0) contexts.remove(arrContexts[i]);
			}
		}
	}

	/**
	 * Determines if a storage scope contains user-defined content beyond system keys.
	 * 
	 * This method excludes internal system keys (cfid, cftoken, lastvisit, etc.) when determining if
	 * the scope has meaningful content. Importantly, it INCLUDES keys that have been deleted from
	 * storage but are still present in memory marked as deleted - these deleted keys are considered
	 * "content" for merge decision purposes.
	 * 
	 * @param scope the session or client scope to check
	 * @return true if scope contains user-defined content (including deleted keys), false if only
	 *         system keys
	 */
	public static boolean hasContent(Scope scope) {
		int size = scope.size();
		if (size == 0) return false;
		if (size > 7) return true;
		if (size == 7 && !scope.containsKey(KeyConstants._csrf_token)) return true;
		return !(scope.containsKey(KeyConstants._cfid) && scope.containsKey(KeyConstants._cftoken) && scope.containsKey(KeyConstants._urltoken)
				&& scope.containsKey(KeyConstants._timecreated) && scope.containsKey(KeyConstants._lastvisit)
				&& (scope.getType() == Scope.SCOPE_CLIENT ? scope.containsKey(KeyConstants._hitcount) : scope.containsKey(KeyConstants._sessionid)));
	}

	public static boolean hasContent(Map<Key, IKStorageScopeItem> map, int type) {
		int size = map.size();
		if (size == 0) return false;
		if (size > 7) return true;
		if (size == 7 && !map.containsKey(KeyConstants._csrf_token)) return true;
		return !(map.containsKey(KeyConstants._cfid) && map.containsKey(KeyConstants._cftoken) && map.containsKey(KeyConstants._urltoken)
				&& map.containsKey(KeyConstants._timecreated) && map.containsKey(KeyConstants._lastvisit)
				&& (type == Scope.SCOPE_CLIENT ? map.containsKey(KeyConstants._hitcount) : map.containsKey(KeyConstants._sessionid)));
	}

	public static int hash(Map<Key, IKStorageScopeItem> map, int type, boolean ignoreSimpleValues) {
		int result = 1;
		for (Entry<Key, IKStorageScopeItem> e: map.entrySet()) {
			if (type == Scope.SCOPE_CLIENT ? IGNORE_CLIENT.contains(e.getKey()) : IGNORE_SESSION.contains(e.getKey())) {
				continue;
			}
			if (ignoreSimpleValues && Decision.isSimpleValue(e.getValue().getValue())) {
				continue;
			}
			result = 31 * result + (e.getValue() == null ? 0 : e.getValue().hashCode());
		}
		return result;
	}

	/**
	 * @param cfmlFactory
	 *
	 */
	private void clearUnusedMemoryScope(CFMLFactoryImpl cfmlFactory, int type) {
		Map<String, Map<String, Scope>> contexts = type == Scope.SCOPE_CLIENT ? cfClientContexts : cfSessionContexts;
		if (contexts.size() == 0) return;
		Object[] arrContexts = contexts.keySet().toArray();
		ApplicationListener listener = cfmlFactory.getConfig().getApplicationListener();
		Object applicationName, cfid, o;
		Map<String, Scope> fhm;

		for (int i = 0; i < arrContexts.length; i++) {
			applicationName = arrContexts[i];
			fhm = contexts.get(applicationName);
			if (fhm.size() > 0) {
				Object[] cfids = fhm.keySet().toArray();
				int count = cfids.length;

				for (int y = 0; y < cfids.length; y++) {
					cfid = cfids[y];
					o = fhm.get(cfid);
					if (!(o instanceof MemoryScope)) continue;
					MemoryScope scope = (MemoryScope) o;

					// close
					if (scope.isExpired()) {
						// TODO macht das sinn? ist das nicht kopierleiche?
						ApplicationImpl application = (ApplicationImpl) applicationContexts.get(applicationName);
						long appLastAccess = 0;
						if (application != null) {
							appLastAccess = application.getLastAccess();
							application.touch();
						}
						scope.touch();
						try {
							if (type == Scope.SCOPE_SESSION) {
								listener.onSessionEnd(cfmlFactory, (String) applicationName, (String) cfid);
							}
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
							ExceptionHandler.log(cfmlFactory.getConfig(), Caster.toPageException(t));
						}
						finally {
							if (application != null) application.setLastAccess(appLastAccess);
							fhm.remove(cfids[y]);
							scope.release(ThreadLocalPageContext.get());
							getLog().log(Log.LEVEL_INFO, "scope-context",
									"remove memory based " + VariableInterpreter.scopeInt2String(type) + " scope for [" + applicationName + "/" + cfid + "]");
							count--;
						}
					}
				}
				if (count == 0) contexts.remove(arrContexts[i]);
			}
		}
	}

	private void clearUnusedApplications(CFMLFactoryImpl jspFactory) {
		if (applicationContexts.size() == 0) return;

		long now = System.currentTimeMillis();
		Object[] arrContexts = applicationContexts.keySet().toArray();
		ApplicationListener listener = jspFactory.getConfig().getApplicationListener();
		for (int i = 0; i < arrContexts.length; i++) {
			Application application = applicationContexts.get(arrContexts[i]);

			if (application.getLastAccess() + application.getTimeSpan() < now) {
				application.touch();
				try {
					listener.onApplicationEnd(jspFactory, (String) arrContexts[i]);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					ExceptionHandler.log(jspFactory.getConfig(), Caster.toPageException(t));
				}
				finally {
					applicationContexts.remove(arrContexts[i]);
					application.release(ThreadLocalPageContext.get());
				}

			}
		}
	}

	public void clearApplication(PageContext pc) throws PageException {

		if (applicationContexts.size() == 0) throw new ApplicationException("there is no application context defined");

		String name = pc.getApplicationContext().getName();
		CFMLFactoryImpl jspFactory = (CFMLFactoryImpl) pc.getCFMLFactory();

		Application application = applicationContexts.get(name);
		if (application == null) throw new ApplicationException("there is no application context defined with name [" + name + "]");
		ApplicationListener listener = PageContextUtil.getApplicationListener(pc);
		application.touch();
		try {
			listener.onApplicationEnd(jspFactory, name);
		}
		finally {
			applicationContexts.remove(name);
			application.release(pc);
		}
	}

	public void invalidateUserScope(PageContextImpl pc, boolean migrateSessionData, boolean migrateClientData) throws PageException {
		ApplicationContext appContext = pc.getApplicationContext();

		boolean hasClientManagement = appContext.isSetClientManagement();
		boolean hasSessionManagement = appContext.isSetSessionManagement();
		boolean isJ2EESession = pc.getSessionType() == Config.SESSION_TYPE_JEE;

		// get in memory scopes
		UserScope oldClient = null;
		if (hasClientManagement) {
			Map<String, Scope> clientContext = getSubMap(cfClientContexts, appContext.getName());
			oldClient = (UserScope) clientContext.get(pc.getCFID());
		}
		UserScope oldSession = null;
		if (hasSessionManagement) {
			if (isJ2EESession) {
				// For J2EE sessions, try the HttpSession attribute first
				HttpSession httpSession = pc.getSession();
				if (httpSession != null) {
					Object session = httpSession.getAttribute(appContext.getName());
					if (session instanceof JSession) {
						oldSession = (JSession) session;
					}
				}
				// Fall back to cfSessionContexts (used by JSR-223/script-runner where httpSession is null)
				if (oldSession == null) {
					Map<String, Scope> sessionContext = getSubMap(cfSessionContexts, appContext.getName());
					oldSession = (UserScope) sessionContext.get(pc.getCFID());
				}
			}
			else {
				Map<String, Scope> sessionContext = getSubMap(cfSessionContexts, appContext.getName());
				oldSession = (UserScope) sessionContext.get(pc.getCFID());
			}
		}

		if (hasSessionManagement) {
			ApplicationListener listener = factory.getConfig().getApplicationListener();
			try {
				listener.onSessionEnd(factory, appContext.getName(), pc.getCFID());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				ExceptionHandler.log(pc.getConfig(), Caster.toPageException(t));
			}
		}

		// remove Scopes completely
		if (hasSessionManagement) removeCFSessionScope(pc);
		if (hasClientManagement) removeClientScope(pc);

		// For J2EE sessions, handle the servlet container's session (JSESSIONID)
		if (isJ2EESession && hasSessionManagement) {
			HttpSession httpSession = pc.getSession();
			if (httpSession != null) {
				if (migrateSessionData) {
					// sessionRotate: rotate to a new session ID but keep session alive
					pc.getHttpServletRequest().changeSessionId();
				}
				else {
					// sessionInvalidate: completely destroy the JEE session (LDEV-3248)
					httpSession.invalidate();
				}
			}
		}

		pc.resetIdAndToken();
		// For J2EE sessionRotate with a real httpSession (Tomcat), don't reset session - we already called
		// changeSessionId() and want to keep the data
		// But for JSR-223 (where httpSession is null), we need to reset to create a new session
		HttpSession httpSessionForReset = pc.getSession();
		if (!(isJ2EESession && migrateSessionData && httpSessionForReset != null)) {
			pc.resetSession();
		}
		pc.resetClient();

		if (oldSession != null) {
			UserScope newSession;
			if (isJ2EESession) {
				newSession = getSessionScope(pc);
			}
			else {
				newSession = (UserScope) getCFScope(pc, true, Scope.SCOPE_SESSION);
			}
			migrate(pc, oldSession, newSession, migrateSessionData);
		}
		if (oldClient != null) migrate(pc, oldClient, (UserScope) getCFScope(pc, true, Scope.SCOPE_CLIENT), migrateClientData);

	}

	private static void migrate(PageContextImpl pc, UserScope oldScope, UserScope newScope, boolean migrate) {
		if (oldScope == null || newScope == null) return;
		// For J2EE sessions with changeSessionId(), old and new are the same object - no migration needed
		if (oldScope == newScope) return;
		if (!migrate) oldScope.clear();
		oldScope.resetEnv(pc);
		Iterator<Entry<Key, Object>> it = oldScope.entryIterator();
		Entry<Key, Object> e;
		if (migrate) {
			while (it.hasNext()) {
				e = it.next();
				if (StorageScopeImpl.KEYS.contains(e.getKey())) continue;
				newScope.setEL(e.getKey(), e.getValue());
			}
			if (newScope instanceof StorageScope) {
				((StorageScope) newScope).store(pc.getConfig());
				if (oldScope instanceof StorageScope) {
					((StorageScope) newScope).setTokens(((StorageScope) oldScope).getTokens());
				}
			}
			else if (newScope instanceof JSession && oldScope instanceof JSession) {
				// JSession doesn't implement StorageScope but has its own token handling
				((JSession) newScope).setTokens(((JSession) oldScope).getTokens());
			}

		}
	}
}