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
package lucee.runtime.type.scope;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import lucee.commons.collection.MapFactory;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SizeOf;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.db.DataSource;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExceptionHandler;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.client.ClientCache;
import lucee.runtime.type.scope.client.ClientCookie;
import lucee.runtime.type.scope.client.ClientDatasource;
import lucee.runtime.type.scope.client.ClientFile;
import lucee.runtime.type.scope.client.ClientMemory;
import lucee.runtime.type.scope.session.SessionCache;
import lucee.runtime.type.scope.session.SessionCookie;
import lucee.runtime.type.scope.session.SessionDatasource;
import lucee.runtime.type.scope.session.SessionFile;
import lucee.runtime.type.scope.session.SessionMemory;
import lucee.runtime.type.scope.storage.IKHandlerCache;
import lucee.runtime.type.scope.storage.IKHandlerDatasource;
import lucee.runtime.type.scope.storage.IKStorageScopeSupport;
import lucee.runtime.type.scope.storage.MemoryScope;
import lucee.runtime.type.scope.storage.StorageScope;
import lucee.runtime.type.scope.storage.StorageScopeCleaner;
import lucee.runtime.type.scope.storage.StorageScopeEngine;
import lucee.runtime.type.scope.storage.StorageScopeImpl;
import lucee.runtime.type.scope.storage.clean.DatasourceStorageScopeCleaner;
import lucee.runtime.type.scope.storage.clean.FileStorageScopeCleaner;
import lucee.runtime.type.wrap.MapAsStruct;
import lucee.runtime.util.PageContextUtil;

/**
 * handles the Scopes, e.g. Application, Session, etc., for a ServletContext
 */
public final class ScopeContext {

	private static final int MINUTE = 60 * 1000;
	private static final long CLIENT_MEMORY_TIMESPAN = 5 * MINUTE;
	private static final long SESSION_MEMORY_TIMESPAN = 5 * MINUTE;
	private static final boolean INVIDUAL_STORAGE_KEYS;

	static {
		INVIDUAL_STORAGE_KEYS = true;// Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("individualStorageKeys", null),false);
	}

	private Map<String, Map<String, Scope>> cfSessionContexts = MapFactory.<String, Map<String, Scope>>getConcurrentMap();
	private Map<String, Map<String, Scope>> cfClientContexts = MapFactory.<String, Map<String, Scope>>getConcurrentMap();
	private Map<String, Application> applicationContexts = MapFactory.<String, Application>getConcurrentMap();

	private int maxSessionTimeout = 0;

	private static Cluster cluster;
	private static Server server = null;

	private StorageScopeEngine client;
	private StorageScopeEngine session;
	private CFMLFactoryImpl factory;
	private Log log;

	public ScopeContext(CFMLFactoryImpl factory) {
		this.factory = factory;
	}

	/**
	 * @return the log
	 */
	private Log getLog() {
		if (log == null) {
			this.log = factory.getConfig().getLog("scope");

		}
		return log;
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
		if (log != null) log.log(Log.LEVEL_DEBUG, "scope-context", msg);
		else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, "scope", msg);
	}

	public static void info(Log log, String msg) {
		if (log != null) log.log(Log.LEVEL_INFO, "scope-context", msg);
		else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, "scope", msg);
	}

	public static void error(Log log, String msg) {
		if (log != null) log.log(Log.LEVEL_ERROR, "scope-context", msg);
		else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, "scope", msg);
	}

	public static void error(Log log, Throwable t) {
		if (log != null) log.log(Log.LEVEL_ERROR, "scope-context", ExceptionUtil.getStacktrace(t, true));
		else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "scope", (Exception) t);
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
		if (context != null) return context;

		context = MapFactory.<String, Scope>getConcurrentMap();
		parent.put(key, context);
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

	/*
	 * * Returns the current Cluster Scope, if there is no current Cluster Scope, this method returns
	 * null.
	 *
	 * @param pc
	 *
	 * @param create
	 *
	 * @return
	 *
	 * @throws SecurityException / public static Cluster getClusterScope() { return cluster; }
	 */

	/**
	 * Returns the current Cluster Scope, if there is no current Cluster Scope and create is true,
	 * returns a new Cluster Scope. If create is false and the request has no valid Cluster Scope, this
	 * method returns null.
	 *
	 * @param config
	 * @param create
	 * @return
	 * @throws PageException
	 */
	public static Cluster getClusterScope(Config config, boolean create) throws PageException {
		if (cluster == null && create) {
			cluster = ((ConfigImpl) config).createClusterScope();

		}
		return cluster;
	}

	public static void clearClusterScope() {
		cluster = null;
	}

	public Client getClientScope(PageContext pc) throws PageException {
		ApplicationContext appContext = pc.getApplicationContext();
		// get Context
		Map<String, Scope> context = getSubMap(cfClientContexts, appContext.getName());

		// get Client
		boolean isMemory = false;
		String storage = appContext.getClientstorage();
		if (StringUtil.isEmpty(storage, true)) {
			storage = ConfigImpl.DEFAULT_STORAGE_CLIENT;
		}
		else if ("ram".equalsIgnoreCase(storage)) {
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

		Client existing = (Client) context.get(pc.getCFID());
		Client client = appContext.getClientCluster() ? null : existing;
		// final boolean doMemory=isMemory || !appContext.getClientCluster();
		// client=doMemory?(Client) context.get(pc.getCFID()):null;
		if (client == null || client.isExpired() || !client.getStorage().equalsIgnoreCase(storage)) {
			if ("file".equals(storage)) {
				client = ClientFile.getInstance(appContext.getName(), pc, getLog());
			}
			else if ("cookie".equals(storage)) client = ClientCookie.getInstance(appContext.getName(), pc, getLog());
			else if ("memory".equals(storage)) {
				if (existing != null) client = existing;
				client = ClientMemory.getInstance(pc, getLog());
			}
			else {
				DataSource ds = pc.getDataSource(storage, null);
				if (ds != null) {
					if (INVIDUAL_STORAGE_KEYS) {
						try {
							client = (Client) IKStorageScopeSupport.getInstance(Scope.SCOPE_CLIENT, new IKHandlerDatasource(), appContext.getName(), storage, pc, existing,
									getLog());
						}
						catch (PageException pe) {
							// code above could fail when an old scope is loaded, remember client scope can be easy be
							// 180 days old
							client = ClientDatasource.getInstance(storage, pc, getLog());
						}
					}
					else client = ClientDatasource.getInstance(storage, pc, getLog());
				}
				else {
					if (INVIDUAL_STORAGE_KEYS) {
						try {
							client = (Client) IKStorageScopeSupport.getInstance(Scope.SCOPE_CLIENT, new IKHandlerCache(), appContext.getName(), storage, pc, existing, getLog());
						}
						catch (PageException pe) {
							// code above could fail when an old scope is loaded, remember client scope can be easy be
							// 180 days old
							client = ClientCache.getInstance(storage, appContext.getName(), pc, existing, getLog(), null);
						}
					}
					else client = ClientCache.getInstance(storage, appContext.getName(), pc, existing, getLog(), null);
				}

				if (client == null) {
					// datasource not enabled for storage
					if (ds != null) {
						if (!ds.isStorage()) throw new ApplicationException(
								"datasource [" + storage + "] is not enabled to be used as client storage, you have to enable it in the Lucee administrator.");
						throw new ApplicationException("datasource [" + storage
								+ "] could not be reached for client storage. Please make sure the datasource settings are correct, and the datasource is available.");
					}
					CacheConnection cc = CacheUtil.getCacheConnection(pc, storage, null);
					if (cc != null) throw new ApplicationException(
							"cache [" + storage + "] is not enabled to be used  as a session/client storage, you have to enable it in the Lucee administrator.");

					throw new ApplicationException("there is no cache or datasource with name [" + storage + "] defined.");
				}

			}
			client.setStorage(storage);
			context.put(pc.getCFID(), client);
		}
		else getLog().log(Log.LEVEL_DEBUG, "scope-context", "use existing client scope for " + appContext.getName() + "/" + pc.getCFID() + " from storage " + storage);

		client.touchBeforeRequest(pc);
		return client;
	}

	public Client getClientScopeEL(PageContext pc) {
		try {
			return getClientScope(pc);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	/*
	 * public ClientPlus getClientScopeEL(PageContext pc) { ClientPlus client=null; ApplicationContext
	 * appContext = pc.getApplicationContext(); // get Context Map
	 * context=getSubMap(cfClientContexts,appContext.getName());
	 *
	 * // get Client String storage = appContext.getClientstorage();
	 * if(!StringUtil.isEmpty(storage))storage=storage.toLowerCase(); else storage="";
	 *
	 * client=(ClientPlus) context.get(pc.getCFID()); if(client==null || client.isExpired() ||
	 * !client.getStorageType().equalsIgnoreCase(storage)) { if(StringUtil.isEmpty(storage) ||
	 * "file".equals(storage) || "registry".equals(storage)){ storage="file";
	 * client=ClientFile.getInstance(appContext.getName(),pc,getLog()); } else
	 * if("cookie".equals(storage)) client=ClientCookie.getInstance(appContext.getName(),pc,getLog());
	 * else if("memory".equals(storage) || "ram".equals(storage)){ //storage="ram";
	 * client=ClientMemory.getInstance(pc,getLog()); } else{ DataSource ds =
	 * ((ConfigImpl)pc.getConfig()).getDataSource(storage,null);
	 * if(ds!=null)client=ClientDatasource.getInstanceEL(storage,pc,getLog()); else
	 * client=ClientCache.getInstanceEL(storage,appContext.getName(),pc,getLog());
	 *
	 * } client.setStorage(storage); context.put(pc.getCFID(),client); } else
	 * getLog().info("scope-context",
	 * "use existing client scope for "+appContext.getName()+"/"+pc.getCFID()+" from storage "+storage);
	 *
	 *
	 * client.initialize(pc); return client; }
	 */

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
		Struct trg = new StructImpl();
		StructImpl.copy(MapAsStruct.toStruct(applicationContexts, true), trg, false);
		return trg;
	}

	public Struct getAllCFSessionScopes() {
		Struct trg = new StructImpl();
		StructImpl.copy(MapAsStruct.toStruct(this.cfSessionContexts, true), trg, false);
		return trg;
	}

	/**
	 * return the size in bytes of all session contexts
	 *
	 * @return size in bytes
	 * @throws ExpressionException
	 */
	public long getScopesSize(int scope) throws ExpressionException {
		if (scope == Scope.SCOPE_APPLICATION) return SizeOf.size(applicationContexts);
		if (scope == Scope.SCOPE_CLUSTER) return SizeOf.size(cluster);
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
		Struct sct = new StructImpl();
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
	public Session getSessionScope(PageContext pc, RefBoolean isNew) throws PageException {
		if (pc.getSessionType() == Config.SESSION_TYPE_APPLICATION) return getCFSessionScope(pc, isNew);
		return getJSessionScope(pc, isNew);
	}

	public boolean hasExistingSessionScope(PageContext pc) {
		if (pc.getSessionType() == Config.SESSION_TYPE_APPLICATION) return hasExistingCFSessionScope(pc);
		return hasExistingJSessionScope(pc);
	}

	private boolean hasExistingJSessionScope(PageContext pc) {
		HttpSession httpSession = pc.getSession();
		if (httpSession == null) return false;

		Session session = (Session) httpSession.getAttribute(pc.getApplicationContext().getName());
		return session instanceof JSession;
	}

	private boolean hasExistingCFSessionScope(PageContext pc, String cfid) {
		ApplicationContext appContext = pc.getApplicationContext();
		Map<String, Scope> context = getSubMap(cfSessionContexts, appContext.getName());
		return context.containsKey(cfid);
	}

	private boolean hasExistingClientScope(PageContext pc, String cfid) {
		ApplicationContext appContext = pc.getApplicationContext();
		Map<String, Scope> context = getSubMap(cfClientContexts, appContext.getName());
		return context.containsKey(cfid);
	}

	public boolean hasExistingCFID(PageContext pc, String cfid) {
		if (hasExistingCFSessionScope(pc, cfid)) return true;
		return hasExistingClientScope(pc, cfid);
	}

	private boolean hasExistingCFSessionScope(PageContext pc) {

		ApplicationContext appContext = pc.getApplicationContext();
		// get Context
		Map<String, Scope> context = getSubMap(cfSessionContexts, appContext.getName());

		// get Session
		String storage = appContext.getSessionstorage();
		if (StringUtil.isEmpty(storage, true)) storage = "memory";
		else if ("ram".equalsIgnoreCase(storage)) storage = "memory";
		else if ("registry".equalsIgnoreCase(storage)) storage = "file";
		else storage = storage.toLowerCase();

		Session session = (Session) context.get(pc.getCFID());

		if (!(session instanceof StorageScope) || session.isExpired() || !((StorageScope) session).getStorage().equalsIgnoreCase(storage)) {

			if ("memory".equals(storage)) return false;
			else if ("file".equals(storage)) return SessionFile.hasInstance(appContext.getName(), pc);
			else if ("cookie".equals(storage)) return SessionCookie.hasInstance(appContext.getName(), pc);
			else {
				DataSource ds = pc.getConfig().getDataSource(storage, null);
				if (ds != null && ds.isStorage()) {
					if (INVIDUAL_STORAGE_KEYS) {
						return IKStorageScopeSupport.hasInstance(Scope.SCOPE_SESSION, new IKHandlerDatasource(), appContext.getName(), storage, pc);
					}
					else {
						if (SessionDatasource.hasInstance(storage, pc)) return true;
					}
				}
				if (INVIDUAL_STORAGE_KEYS) return IKStorageScopeSupport.hasInstance(Scope.SCOPE_SESSION, new IKHandlerCache(), appContext.getName(), storage, pc);
				return SessionCache.hasInstance(storage, appContext.getName(), pc);
			}
		}
		return true;
	}

	public Session getExistingCFSessionScope(String applicationName, String cfid) throws PageException {
		Map<String, Scope> context = getSubMap(cfSessionContexts, applicationName);
		if (context != null) {
			return (Session) context.get(cfid);
		}
		return null;
	}

	/**
	 * return cf session scope
	 *
	 * @param pc PageContext
	 * @param isNew
	 * @return cf session matching the context
	 * @throws PageException
	 */
	private Session getCFSessionScope(PageContext pc, RefBoolean isNew) throws PageException {

		ApplicationContext appContext = pc.getApplicationContext();
		// get Context
		Map<String, Scope> context = getSubMap(cfSessionContexts, appContext.getName());

		// get Session
		boolean isMemory = false;
		String storage = appContext.getSessionstorage();
		if (StringUtil.isEmpty(storage, true)) {
			storage = ConfigImpl.DEFAULT_STORAGE_SESSION;
			isMemory = true;
		}
		else if ("ram".equalsIgnoreCase(storage)) {
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

		Session existing = (Session) context.get(pc.getCFID());
		if (existing != null && (existing.isExpired() || !(existing instanceof StorageScope))) existing = null; // second should not happen

		Session session = appContext.getSessionCluster() ? null : existing;

		if (session == null || !(session instanceof StorageScope) || !((StorageScope) session).getStorage().equalsIgnoreCase(storage)) {
			// not necessary to check session in the same way, because it is overwritten anyway
			if (isMemory) {
				if (existing != null) session = existing;
				else session = SessionMemory.getInstance(pc, isNew, getLog());
			}
			else if ("file".equals(storage)) {
				session = SessionFile.getInstance(appContext.getName(), pc, getLog());
			}
			else if ("cookie".equals(storage)) session = SessionCookie.getInstance(appContext.getName(), pc, getLog());
			else {
				DataSource ds = pc.getDataSource(storage, null);
				if (ds != null && ds.isStorage()) {
					if (INVIDUAL_STORAGE_KEYS) {
						try {
							session = (Session) IKStorageScopeSupport.getInstance(Scope.SCOPE_SESSION, new IKHandlerDatasource(), appContext.getName(), storage, pc, existing,
									getLog());
						}
						catch (PageException pe) {
							session = SessionDatasource.getInstance(storage, pc, getLog(), null);
						}
					}
					else session = SessionDatasource.getInstance(storage, pc, getLog(), null);
				}
				else {
					if (INVIDUAL_STORAGE_KEYS) {
						try {
							session = (Session) IKStorageScopeSupport.getInstance(Scope.SCOPE_SESSION, new IKHandlerCache(), appContext.getName(), storage, pc, existing, getLog());
						}
						catch (PageException pe) {
							session = SessionCache.getInstance(storage, appContext.getName(), pc, existing, getLog(), null);
						}
					}
					else session = SessionCache.getInstance(storage, appContext.getName(), pc, existing, getLog(), null);
				}

				if (session == null) {
					// datasource not enabled for storage
					if (ds != null) {
						if (!ds.isStorage()) throw new ApplicationException("datasource [" + storage + "] is not enabled to be used as session storage, "
								+ "you have to enable it in the Lucee administrator or define key \"storage=true\" for datasources defined in the application event handler.");
						throw new ApplicationException("datasource [" + storage
								+ "] could not be reached for session storage. Please make sure the datasource settings are correct, and the datasource is available.");
					}
					CacheConnection cc = CacheUtil.getCacheConnection(pc, storage, null);
					if (cc != null) throw new ApplicationException(
							"cache [" + storage + "] is not enabled to be used  as a session/client storage, you have to enable it in the Lucee administrator.");

					throw new ApplicationException("there is no cache or datasource with name [" + storage + "] defined.");
				}
			}
			if (session instanceof StorageScope) ((StorageScope) session).setStorage(storage);
			context.put(pc.getCFID(), session);
			isNew.setValue(true);
		}
		else {
			getLog().log(Log.LEVEL_DEBUG, "scope-context", "use existing session scope for " + appContext.getName() + "/" + pc.getCFID() + " from storage " + storage);
		}
		session.touchBeforeRequest(pc);
		return session;
	}

	public void removeSessionScope(PageContext pc) throws PageException {
		removeCFSessionScope(pc);
		removeJSessionScope(pc);
	}

	public void removeJSessionScope(PageContext pc) throws PageException {
		HttpSession httpSession = pc.getSession();
		if (httpSession != null) {
			ApplicationContext appContext = pc.getApplicationContext();
			httpSession.removeAttribute(appContext.getName());
		}
	}

	public void removeCFSessionScope(PageContext pc) throws PageException {
		Session sess = getCFSessionScope(pc, new RefBooleanImpl());
		ApplicationContext appContext = pc.getApplicationContext();
		Map<String, Scope> context = getSubMap(cfSessionContexts, appContext.getName());
		if (context != null) {
			context.remove(pc.getCFID());
			if (sess instanceof StorageScope) ((StorageScope) sess).unstore(pc.getConfig());
		}
	}

	public void removeClientScope(PageContext pc) throws PageException {
		Client cli = getClientScope(pc);
		ApplicationContext appContext = pc.getApplicationContext();
		Map<String, Scope> context = getSubMap(cfClientContexts, appContext.getName());
		if (context != null) {
			context.remove(pc.getCFID());
			if (cli != null) cli.unstore(pc.getConfig());
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
	private Session getJSessionScope(PageContext pc, RefBoolean isNew) throws PageException {
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
					jSession.touch();
				}
				debug(getLog(), "use existing JSession for " + appContext.getName() + "/" + pc.getCFID());

			}
			catch (ClassCastException cce) {
				error(getLog(), cce);
				// if there is no HTTPSession
				if (httpSession == null) return getCFSessionScope(pc, isNew);

				jSession = new JSession();
				httpSession.setAttribute(appContext.getName(), jSession);
				isNew.setValue(true);
			}
		}
		else {
			// if there is no HTTPSession
			if (httpSession == null) return getCFSessionScope(pc, isNew);

			debug(getLog(), "create new JSession for " + appContext.getName() + "/" + pc.getCFID());
			jSession = new JSession();
			httpSession.setAttribute(appContext.getName(), jSession);
			isNew.setValue(true);
			Map<String, Scope> context = getSubMap(cfSessionContexts, appContext.getName());
			context.put(pc.getCFID(), jSession);
		}
		jSession.touchBeforeRequest(pc);
		return jSession;
	}

	/**
	 * return the application Scope for this context (cfid,cftoken,contextname)
	 *
	 * @param pc PageContext
	 * @param isNew
	 * @return session matching the context
	 * @throws PageException
	 */
	public Application getApplicationScope(PageContext pc, RefBoolean isNew) {
		ApplicationContext appContext = pc.getApplicationContext();
		// getApplication Scope from Context
		ApplicationImpl application;
		Object objApp = applicationContexts.get(appContext.getName());
		if (objApp != null) {
			application = (ApplicationImpl) objApp;
			if (application.isExpired()) {
				application.release(pc);
				isNew.setValue(true);
			}
		}
		else {
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
			storeUnusedStorageScope(factory, Scope.SCOPE_CLIENT);
			storeUnusedStorageScope(factory, Scope.SCOPE_SESSION);

			// remove unused memory based client/session scope (invoke onSessonEnd)
			clearUnusedMemoryScope(factory, Scope.SCOPE_CLIENT);
			clearUnusedMemoryScope(factory, Scope.SCOPE_SESSION);

			// session must be executed first, because session creates a reference from client scope
			session.clean();
			client.clean();

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

	private void storeUnusedStorageScope(CFMLFactoryImpl cfmlFactory, int type) {
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
					if (scope.lastVisit() + timespan < now && !(scope instanceof MemoryScope)) {
						getLog().log(Log.LEVEL_INFO, "scope-context",
								"remove from memory " + strType + " scope for " + applicationName + "/" + cfid + " from storage " + scope.getStorage());

						fhm.remove(arrClients[y]);
						count--;
					}
				}
				if (count == 0) contexts.remove(arrContexts[i]);
			}
		}
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
							if (type == Scope.SCOPE_SESSION) listener.onSessionEnd(cfmlFactory, (String) applicationName, (String) cfid);
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
									"remove memory based " + VariableInterpreter.scopeInt2String(type) + " scope for " + applicationName + "/" + cfid);
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

	/**
	 * @return returns a new CFIs
	 */
	public static String getNewCFId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * @return returns a new CFToken
	 */
	public static String getNewCFToken() {
		return "0";
	}

	public void invalidateUserScope(PageContextImpl pc, boolean migrateSessionData, boolean migrateClientData) throws PageException {
		ApplicationContext appContext = pc.getApplicationContext();
		RefBoolean isNew = new RefBooleanImpl();

		// get in memory scopes
		Map<String, Scope> clientContext = getSubMap(cfClientContexts, appContext.getName());
		UserScope oldClient = (UserScope) clientContext.get(pc.getCFID());
		Map<String, Scope> sessionContext = getSubMap(cfSessionContexts, appContext.getName());
		UserScope oldSession = (UserScope) sessionContext.get(pc.getCFID());

		// remove Scopes completly
		removeCFSessionScope(pc);
		removeClientScope(pc);

		pc.resetIdAndToken();
		pc.resetSession();
		pc.resetClient();

		if (oldSession != null) migrate(pc, oldSession, getCFSessionScope(pc, isNew), migrateSessionData);
		if (oldClient != null) migrate(pc, oldClient, getClientScope(pc), migrateClientData);

	}

	private static void migrate(PageContextImpl pc, UserScope oldScope, UserScope newScope, boolean migrate) {
		if (oldScope == null || newScope == null) return;
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
			if (newScope instanceof StorageScope) ((StorageScope) newScope).store(pc.getConfig());
		}
	}
}