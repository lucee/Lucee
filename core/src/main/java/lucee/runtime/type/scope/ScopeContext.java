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

import lucee.print;
import lucee.commons.collection.MapFactory;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
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
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.client.ClientCache;
import lucee.runtime.type.scope.client.ClientCookie;
import lucee.runtime.type.scope.client.ClientDatasource;
import lucee.runtime.type.scope.client.ClientFile;
import lucee.runtime.type.scope.client.ClientMemory;
import lucee.runtime.type.scope.client.StorageClientCache;
import lucee.runtime.type.scope.session.SessionCache;
import lucee.runtime.type.scope.session.SessionCookie;
import lucee.runtime.type.scope.session.SessionDatasource;
import lucee.runtime.type.scope.session.SessionFile;
import lucee.runtime.type.scope.session.SessionMemory;
import lucee.runtime.type.scope.session.StorageSessionCache;
import lucee.runtime.type.scope.storage.MemoryScope;
import lucee.runtime.type.scope.storage.StorageScope;
import lucee.runtime.type.scope.storage.StorageScopeCleaner;
import lucee.runtime.type.scope.storage.StorageScopeEngine;
import lucee.runtime.type.scope.storage.clean.DatasourceStorageScopeCleaner;
import lucee.runtime.type.scope.storage.clean.FileStorageScopeCleaner;
import lucee.runtime.type.wrap.MapAsStruct;
import lucee.runtime.util.PageContextUtil;

/**
 * Scope Context handle Apllication and Session Scopes
 */
public final class ScopeContext {

	private static final int MINUTE = 60*1000;
	private static final long CLIENT_MEMORY_TIMESPAN =  5*MINUTE;
	private static final long SESSION_MEMORY_TIMESPAN =  5*MINUTE;
	private static final boolean INVIDUAL_STORAGE_KEYS;
	static {
		INVIDUAL_STORAGE_KEYS=Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("individualStorageKeys", null),false);
	}
	
	
	
	private Map<String,Map<String,Scope>> cfSessionContextes=MapFactory.<String,Map<String,Scope>>getConcurrentMap();
	private Map<String,Map<String,Scope>> cfClientContextes=MapFactory.<String,Map<String,Scope>>getConcurrentMap();
	private Map<String,Application> applicationContextes=MapFactory.<String,Application>getConcurrentMap();

	private int maxSessionTimeout=0;

	private static Cluster cluster;
	private static Server server=null;
	

	private StorageScopeEngine client;
	private StorageScopeEngine session;
	private CFMLFactoryImpl factory;
	private Log log;
	
	
	
	public ScopeContext(CFMLFactoryImpl factory) {
		this.factory=factory;
	}

	/**
	 * @return the log
	 */
	private Log getLog() {
		if(log==null) {
			this.log=((ConfigImpl)factory.getConfig()).getLog("scope");
			
		}
		return log;
	}
	
	public void info(String msg) {info(getLog(), msg);}
	public void error(String msg) {error(getLog(),msg);}
	public void error(Throwable t) {error(getLog(), t);}
	
	public static void info(Log log,String msg) {
		if(log!=null)log.log(Log.LEVEL_INFO,"scope-context", msg);
	}
	

	public static void error(Log log,String msg) {
		if(log!=null)log.log(Log.LEVEL_ERROR,"scope-context", msg);
	}
	
	public static void error(Log log,Throwable t) {
		if(log!=null)log.log(Log.LEVEL_ERROR,"scope-context",ExceptionUtil.getStacktrace(t, true));
	}
	

	/**
	 * return a map matching key from given map
	 * @param parent
	 * @param key key of the map
	 * @return matching map, if no map exist it willbe one created
	 */
	private Map<String,Scope> getSubMap(Map<String,Map<String,Scope>> parent, String key) {
			
		Map<String,Scope> context=parent.get(key);
		if(context!=null) return context;
		
		context = MapFactory.<String,Scope>getConcurrentMap();
		parent.put(key,context);
		return context;
		
	}
	
	/**
	 * return the server Scope for this context
	 * @param pc
	 * @return server scope
	 */
	public static Server getServerScope(PageContext pc, boolean jsr223) {
	    if(server==null) {
	    	server=new ServerImpl(pc,jsr223);
	    }
		return server;
	}
	
	/* *
	 * Returns the current Cluster Scope, if there is no current Cluster Scope, this method returns null. 
	 * @param pc
	 * @param create
	 * @return
	 * @throws SecurityException
	 * /
	public static Cluster getClusterScope() {
	    return cluster;
	}*/

	/**
	 * Returns the current Cluster Scope, if there is no current Cluster Scope and create is true, returns a new Cluster Scope.
	 * If create is false and the request has no valid Cluster Scope, this method returns null. 
	 * @param pc
	 * @param create
	 * @return
	 * @throws PageException 
	 */
	public static Cluster getClusterScope(Config config, boolean create) throws PageException {
		if(cluster==null && create) {
	    	cluster=((ConfigImpl)config).createClusterScope();
	    	 
	    }
		return cluster;
	}
	
	public static void clearClusterScope() {
		cluster=null;
	}
	
	
	public Client getClientScope(PageContext pc) throws PageException {
		ApplicationContext appContext = pc.getApplicationContext(); 
		// get Context
			Map<String, Scope> context = getSubMap(cfClientContextes,appContext.getName());
			
		// get Client
			boolean isMemory=false;
			String storage = appContext.getClientstorage();
			if(StringUtil.isEmpty(storage,true)){
				storage=ConfigImpl.DEFAULT_STORAGE_CLIENT;
			}
			else if("ram".equalsIgnoreCase(storage)) {
				storage="memory";
				isMemory=true;
			}
			else if("registry".equalsIgnoreCase(storage)) {
				storage="file";
			}
			else {
				storage=storage.toLowerCase();
				if("memory".equals(storage))isMemory=true;
			}
			
			Client existing=(Client) context.get(pc.getCFID());
			Client client=appContext.getClientCluster()?null:existing;
			//final boolean doMemory=isMemory || !appContext.getClientCluster();
			//client=doMemory?(Client) context.get(pc.getCFID()):null;
			if(client==null || client.isExpired() || !client.getStorage().equalsIgnoreCase(storage)) {
				if("file".equals(storage)){
					client=ClientFile.getInstance(appContext.getName(),pc,getLog());
				}
				else if("cookie".equals(storage))
					client=ClientCookie.getInstance(appContext.getName(),pc,getLog());
				else if("memory".equals(storage)) {
					if(existing!=null) client=existing;
					client=ClientMemory.getInstance(pc,getLog());
				}
				else{
					DataSource ds = pc.getDataSource(storage,null);
					if(ds!=null)client=ClientDatasource.getInstance(storage,pc,getLog());
					else {
						if(INVIDUAL_STORAGE_KEYS) client=StorageClientCache.getInstance(storage,appContext.getName(),pc,existing,getLog(),null);
						else client=ClientCache.getInstance(storage,appContext.getName(),pc,existing,getLog(),null);
					}

					if(client==null){
						// datasource not enabled for storage
						if(ds!=null)
							throw new ApplicationException("datasource ["+storage+"] is not enabled to be used as session/client storage, you have to enable it in the Lucee administrator.");
						
						CacheConnection cc = CacheUtil.getCacheConnection(pc,storage,null);
						if(cc!=null) 
							throw new ApplicationException("cache ["+storage+"] is not enabled to be used  as a session/client storage, you have to enable it in the Lucee administrator.");
						
						throw new ApplicationException("there is no cache or datasource with name ["+storage+"] defined.");
					}
					
				}
				client.setStorage(storage);
				context.put(pc.getCFID(),client);
			}
			else
				getLog().log(Log.LEVEL_INFO,"scope-context", "use existing client scope for "+appContext.getName()+"/"+pc.getCFID()+" from storage "+storage);
			
			client.touchBeforeRequest(pc);
			return client;
	}
	
	public Client getClientScopeEL(PageContext pc) {
		try {
			return getClientScope(pc);
		} catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}
	
	/*public ClientPlus getClientScopeEL(PageContext pc) {
		ClientPlus client=null;
		ApplicationContext appContext = pc.getApplicationContext(); 
		// get Context
			Map context=getSubMap(cfClientContextes,appContext.getName());
			
		// get Client
			String storage = appContext.getClientstorage();
			if(!StringUtil.isEmpty(storage))storage=storage.toLowerCase();
			else storage="";
			
			client=(ClientPlus) context.get(pc.getCFID());
			if(client==null || client.isExpired() || !client.getStorageType().equalsIgnoreCase(storage)) {
				if(StringUtil.isEmpty(storage) || "file".equals(storage) || "registry".equals(storage)){
					storage="file";
					client=ClientFile.getInstance(appContext.getName(),pc,getLog());
				}
				else if("cookie".equals(storage))
					client=ClientCookie.getInstance(appContext.getName(),pc,getLog());
				else if("memory".equals(storage) || "ram".equals(storage)){
					//storage="ram";
					client=ClientMemory.getInstance(pc,getLog());
				}
				else{
					DataSource ds = ((ConfigImpl)pc.getConfig()).getDataSource(storage,null);
					if(ds!=null)client=ClientDatasource.getInstanceEL(storage,pc,getLog());
					else client=ClientCache.getInstanceEL(storage,appContext.getName(),pc,getLog());

				}
				client.setStorage(storage);
				context.put(pc.getCFID(),client);
			}
			else
				getLog().info("scope-context", "use existing client scope for "+appContext.getName()+"/"+pc.getCFID()+" from storage "+storage);
			
			
			client.initialize(pc);
			return client;
	}*/



	/**
	 * return the session count of all application contextes
	 * @return
	 */
	public int getSessionCount(PageContext pc) {
		if(pc.getSessionType()==Config.SESSION_TYPE_JEE) return 0;
		
		Iterator<Entry<String, Map<String, Scope>>> it = cfSessionContextes.entrySet().iterator();
		Entry<String, Map<String, Scope>> entry;
		int count=0;
		while(it.hasNext()) {
			entry = it.next();
			count+=getSessionCount(entry.getValue());
		}
		return count;
	}
	
	/**
	 * return the session count of this application context
	 * @return
	 */
	public int getAppContextSessionCount(PageContext pc) {
		ApplicationContext appContext = pc.getApplicationContext(); 
		if(pc.getSessionType()==Config.SESSION_TYPE_JEE) return 0;

		Map<String, Scope> context = getSubMap(cfSessionContextes,appContext.getName());
		return getSessionCount(context);
	}
	
	private int getSessionCount(Map<String, Scope> context) {
		Iterator<Entry<String, Scope>> it = context.entrySet().iterator();
		Entry<String, Scope> entry;
		int count=0;
		Session s;
		while(it.hasNext()) {
			entry = it.next();
			s=(Session)entry.getValue();
			if(!s.isExpired())
				count++;
		}
		return count;
	}
	


	/**
	 * return all session context of this application context
	 * @param pc
	 * @return
	 */
	public Struct getAllSessionScopes(PageContext pc) {
		return getAllSessionScopes(pc.getApplicationContext().getName());
	}
	
	public Struct getAllApplicationScopes() {
		Struct trg=new StructImpl();
		StructImpl.copy(MapAsStruct.toStruct(applicationContextes, true), trg, false);
		return trg;
	}
	
	public Struct getAllCFSessionScopes() {
		Struct trg=new StructImpl();
		StructImpl.copy(MapAsStruct.toStruct(this.cfSessionContextes, true), trg, false);
		return trg;
	}
	
	/**
	 * return the size in bytes of all session contextes
	 * @return size in bytes
	 * @throws ExpressionException 
	 */
	public long getScopesSize(int scope) throws ExpressionException {
		if(scope==Scope.SCOPE_APPLICATION)return SizeOf.size(applicationContextes);
		if(scope==Scope.SCOPE_CLUSTER)return SizeOf.size(cluster);
		if(scope==Scope.SCOPE_SERVER)return SizeOf.size(server);
		if(scope==Scope.SCOPE_SESSION)return SizeOf.size(this.cfSessionContextes);
		if(scope==Scope.SCOPE_CLIENT)return SizeOf.size(this.cfClientContextes);
		
		throw new ExpressionException("can only return information of scope that are not request dependent");
	}
	
	/**
	 * get all session contexts of given applicaton name
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
	 * @param pc
	 * @param appName
	 * @return
	 */
	public Struct getAllSessionScopes(String appName) {
        //if(pc.getSessionType()==Config.SESSION_TYPE_J2EE)return new StructImpl();
		return getAllSessionScopes(getSubMap(cfSessionContextes,appName),appName);
	}
	
	private Struct getAllSessionScopes(Map<String,Scope> context, String appName) {
		Iterator<Entry<String, Scope>> it = context.entrySet().iterator();
		Entry<String, Scope> entry;
		Struct sct=new StructImpl();
		Session s;
		while(it.hasNext()) {
			entry = it.next();
			s=(Session)entry.getValue();
			if(!s.isExpired())
				sct.setEL(KeyImpl.init(appName+"_"+entry.getKey()+"_0"), s);
		}
		return sct;
	}

	/**
	 * return the session Scope for this context (cfid,cftoken,contextname)
	 * @param pc PageContext 
	 * @return session matching the context
	 * @throws PageException
	 */
	public Session getSessionScope(PageContext pc,RefBoolean isNew) throws PageException {
        if(pc.getSessionType()==Config.SESSION_TYPE_APPLICATION)return getCFSessionScope(pc,isNew);
		return getJSessionScope(pc,isNew);
	}
	
	public boolean hasExistingSessionScope(PageContext pc) {
        if(pc.getSessionType()==Config.SESSION_TYPE_APPLICATION)return hasExistingCFSessionScope(pc);
		return hasExistingJSessionScope(pc);
	}
	
	private boolean hasExistingJSessionScope(PageContext pc) {
		HttpSession httpSession=pc.getSession();
        if(httpSession==null) return false;
        
        Session session=(Session) httpSession.getAttribute(pc.getApplicationContext().getName());
        return session instanceof JSession;
	}
	
	private boolean hasExistingCFSessionScope(PageContext pc, String cfid) {
		ApplicationContext appContext = pc.getApplicationContext(); 
		Map<String, Scope> context = getSubMap(cfSessionContextes,appContext.getName());
		return context.containsKey(cfid);
	}
	
	private boolean hasExistingClientScope(PageContext pc, String cfid) {
		ApplicationContext appContext = pc.getApplicationContext(); 
		Map<String, Scope> context = getSubMap(cfClientContextes,appContext.getName());
		return context.containsKey(cfid);
	}
	
	public boolean hasExistingCFID(PageContext pc, String cfid) {
		if(hasExistingCFSessionScope(pc,cfid)) return true;
		return hasExistingClientScope(pc,cfid);
	}
	
	
	private boolean hasExistingCFSessionScope(PageContext pc) {
		
		ApplicationContext appContext = pc.getApplicationContext(); 
		// get Context
			Map<String, Scope> context = getSubMap(cfSessionContextes,appContext.getName());
			
		// get Session
			String storage = appContext.getSessionstorage();
			if(StringUtil.isEmpty(storage,true))storage="memory";
			else if("ram".equalsIgnoreCase(storage)) storage="memory";
			else if("registry".equalsIgnoreCase(storage)) storage="file";
			else storage=storage.toLowerCase();
			
			
			
			Session session=(Session) context.get(pc.getCFID());
			
			if(!(session instanceof StorageScope) || session.isExpired() || !((StorageScope)session).getStorage().equalsIgnoreCase(storage)) {
				
				if("memory".equals(storage)) return false;
				else if("file".equals(storage))
					return SessionFile.hasInstance(appContext.getName(),pc);
				else if("cookie".equals(storage))
					return SessionCookie.hasInstance(appContext.getName(),pc);
				else {
					DataSource ds = ((ConfigImpl)pc.getConfig()).getDataSource(storage,null);
					if(ds!=null && ds.isStorage()){
						if(SessionDatasource.hasInstance(storage,pc)) return true;
					}
					if(INVIDUAL_STORAGE_KEYS)
						return StorageSessionCache.hasInstance(storage, appContext.getName(), pc);
					return  SessionCache.hasInstance(storage,appContext.getName(),pc);
				}
			}
			return true;
	}
	
	
	/**
	 * return cf session scope
	 * @param pc PageContext
	 * @param checkExpires 
	 * @param listener 
	 * @return cf session matching the context
	 * @throws PageException 
	 */
	private Session getCFSessionScope(PageContext pc, RefBoolean isNew) throws PageException {
		
		ApplicationContext appContext = pc.getApplicationContext(); 
		// get Context
			Map<String, Scope> context = getSubMap(cfSessionContextes,appContext.getName());
			
		// get Session
			boolean isMemory=false;
			String storage = appContext.getSessionstorage();
			if(StringUtil.isEmpty(storage,true)){
				storage=ConfigImpl.DEFAULT_STORAGE_SESSION;
				isMemory=true;
			}
			else if("ram".equalsIgnoreCase(storage)) {
				storage="memory";
				isMemory=true;
			}
			else if("registry".equalsIgnoreCase(storage)) {
				storage="file";
			}
			else {
				storage=storage.toLowerCase();
				if("memory".equals(storage))isMemory=true;
			}
			
			Session existing=(Session) context.get(pc.getCFID());
			if(existing!=null && (existing.isExpired() || !(existing instanceof StorageScope))) existing=null; // second should not happen
			
			Session session=appContext.getSessionCluster()?null:existing;

			if(session==null || !(session instanceof StorageScope) || !((StorageScope)session).getStorage().equalsIgnoreCase(storage)) {
				// not necessary to check session in the same way, because it is overwritten anyway
				if(isMemory){
					if(existing!=null) session=existing;
					else session=SessionMemory.getInstance(pc,isNew,getLog());
				}
				else if("file".equals(storage)){
					session=SessionFile.getInstance(appContext.getName(),pc,getLog());
				}
				else if("cookie".equals(storage))
					session=SessionCookie.getInstance(appContext.getName(),pc,getLog());
				else{
					DataSource ds = pc.getDataSource(storage,null);
					if(ds!=null && ds.isStorage())session=SessionDatasource.getInstance(storage,pc,getLog(),null);
					else {
						if(INVIDUAL_STORAGE_KEYS) session=StorageSessionCache.getInstance(storage,appContext.getName(),pc,existing,getLog(),null);
						else session=SessionCache.getInstance(storage,appContext.getName(),pc,existing,getLog(),null);
					}

					if(session==null){
						// datasource not enabled for storage
						if(ds!=null)
							throw new ApplicationException(
									"datasource ["+storage+"] is not enabled to be used as session/client storage, " +
									"you have to enable it in the Lucee administrator or define key \"storage=true\" for datasources defined in the application event handler.");
						
						CacheConnection cc = CacheUtil.getCacheConnection(pc,storage,null);
						if(cc!=null) 
							throw new ApplicationException("cache ["+storage+"] is not enabled to be used  as a session/client storage, you have to enable it in the Lucee administrator.");
						
						throw new ApplicationException("there is no cache or datasource with name ["+storage+"] defined.");
					}
				}
				if(session instanceof StorageScope)((StorageScope)session).setStorage(storage);
				context.put(pc.getCFID(),session);
				isNew.setValue(true);
			}
			else {
				getLog().log(Log.LEVEL_INFO,"scope-context", "use existing session scope for "+appContext.getName()+"/"+pc.getCFID()+" from storage "+storage);
			}
			session.touchBeforeRequest(pc);
			return session;
	}
	
	public void removeSessionScope(PageContext pc) throws PageException {
		
		//CFSession
		Session sess = getCFSessionScope(pc, new RefBooleanImpl());
		ApplicationContext appContext = pc.getApplicationContext(); 
		Map<String, Scope> context = getSubMap(cfSessionContextes,appContext.getName());
		if(context!=null) {
			context.remove(pc.getCFID());
			if(sess instanceof StorageScope)((StorageScope)sess).unstore(pc.getConfig());
		}
		
		// JSession
		HttpSession httpSession=pc.getSession();
        if(httpSession!=null) {
        	httpSession.removeAttribute(appContext.getName());
        }
	}
	
	public void removeClientScope(PageContext pc) throws PageException {
		Client cli = getClientScope(pc);
		ApplicationContext appContext = pc.getApplicationContext(); 
		Map<String, Scope> context = getSubMap(cfClientContextes,appContext.getName());
		if(context!=null) {
			context.remove(pc.getCFID());
			if(cli!=null)cli.unstore(pc.getConfig());
		}
	}
	

	public boolean remove(int type, String appName, String cfid) {
		Map<String, Map<String, Scope>> contextes = type==Scope.SCOPE_CLIENT?cfClientContextes:cfSessionContextes;
		Map<String, Scope> context = getSubMap(contextes,appName);
		Object res = context.remove(cfid);
		getLog().log(Log.LEVEL_INFO,"scope-context", "remove "+VariableInterpreter.scopeInt2String(type)+" scope "+appName+"/"+cfid+" from memory");
		
		return res!=null;
	}

	/**
	 * return j session scope
	 * @param pc PageContext
	 * @param listener 
	 * @return j session matching the context
	 * @throws PageException
	 */
	private Session getJSessionScope(PageContext pc, RefBoolean isNew) throws PageException {
        HttpSession httpSession=pc.getSession();
        ApplicationContext appContext = pc.getApplicationContext(); 
        Object session=null;// this is from type object, because it is possible that httpSession return object from prior restart
		
        int s=(int) appContext.getSessionTimeout().getSeconds();
        if(maxSessionTimeout<s)maxSessionTimeout=s;
        
        if(httpSession!=null) {
        	httpSession.setMaxInactiveInterval(maxSessionTimeout+60);
        	session= httpSession.getAttribute(appContext.getName());
        }
        else {
        	Map<String, Scope> context = getSubMap(cfSessionContextes,appContext.getName());
        	session=context.get(pc.getCFID());
        }
        
        JSession jSession=null;
		if(session instanceof JSession) {
			jSession=(JSession) session;
            try {
                if(jSession.isExpired()) {
                	jSession.touch();
                }
                info(getLog(), "use existing JSession for "+appContext.getName()+"/"+pc.getCFID());
                
            }
            catch(ClassCastException cce) {
            	error(getLog(), cce);
            	// if there is no HTTPSession
    			if(httpSession==null) return getCFSessionScope(pc, isNew);
    			
                jSession=new JSession();
                httpSession.setAttribute(appContext.getName(),jSession);
				isNew.setValue(true);
            }
		}
		else {
			// if there is no HTTPSession
			if(httpSession==null) return getCFSessionScope(pc, isNew);
			
			info(getLog(), "create new JSession for "+appContext.getName()+"/"+pc.getCFID());
			jSession=new JSession();
		    httpSession.setAttribute(appContext.getName(),jSession);
			isNew.setValue(true);
			Map<String, Scope> context = getSubMap(cfSessionContextes,appContext.getName());
			context.put(pc.getCFID(),jSession);
		}
		jSession.touchBeforeRequest(pc);
		return jSession;    
	}

	/**
	 * return the application Scope for this context (cfid,cftoken,contextname)
	 * @param pc PageContext 
	 * @param listener 
	 * @param isNew 
	 * @return session matching the context
	 * @throws PageException 
	 */
	public Application getApplicationScope(PageContext pc, RefBoolean isNew) {
		ApplicationContext appContext = pc.getApplicationContext(); 
		// getApplication Scope from Context
			ApplicationImpl application;
			Object objApp=applicationContextes.get(appContext.getName());
			if(objApp!=null) {
			    application=(ApplicationImpl)objApp;
			    if(application.isExpired()) {
			    	application.release(pc);	
			    	isNew.setValue(true);
			    }
			}
			else {
				application=new ApplicationImpl();
				applicationContextes.put(appContext.getName(),application);	
		    	isNew.setValue(true);
			}
			application.touchBeforeRequest(pc);
			//if(newApplication)listener.onApplicationStart(pc);
			
			return application;
	}
	
	public void removeApplicationScope(PageContext pc) {
		applicationContextes.remove(pc.getApplicationContext().getName());
	}


    /**
     * remove all unused scope objects
     */
    public void clearUnused() {
    	
    	Log log=getLog();
    	try{
    	// create cleaner engine for session/client scope
    	if(session==null)session=new StorageScopeEngine(factory,log,new StorageScopeCleaner[]{
			new FileStorageScopeCleaner(Scope.SCOPE_SESSION, null)//new SessionEndListener())
			,new DatasourceStorageScopeCleaner(Scope.SCOPE_SESSION, null)//new SessionEndListener())
			//,new CacheStorageScopeCleaner(Scope.SCOPE_SESSION, new SessionEndListener())
    	});
    	if(client==null)client=new StorageScopeEngine(factory,log,new StorageScopeCleaner[]{
    			new FileStorageScopeCleaner(Scope.SCOPE_CLIENT, null)
    			,new DatasourceStorageScopeCleaner(Scope.SCOPE_CLIENT, null)
    			//,new CacheStorageScopeCleaner(Scope.SCOPE_CLIENT, null) //Cache storage need no control, if there is no listener
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
    	catch(Throwable t){
    		error(t);
    	}
    }
    
    /**
     * remove all scope objects
     */
    public void clear() {
    	try{
	    	Scope scope;
	    	//Map.Entry entry,e;
	    	//Map context;
	    	
	    	// release all session scopes
	    	Iterator<Entry<String, Map<String, Scope>>> sit = cfSessionContextes.entrySet().iterator();
	    	Entry<String, Map<String, Scope>> sentry;
	    	Map<String, Scope> context;
	    	Iterator<Entry<String, Scope>> itt;
	    	Entry<String, Scope> e;
	    	PageContext pc = ThreadLocalPageContext.get();
			while(sit.hasNext()){
	    		sentry=sit.next();
	    		context = sentry.getValue();
	    		itt = context.entrySet().iterator();
	    		while(itt.hasNext()){
	    			e = itt.next();
	    			scope=e.getValue();
	    			scope.release(pc);
	    		}
	    	}
	        cfSessionContextes.clear();
	    	
	    	// release all application scopes
	    	Iterator<Entry<String, Application>> ait = applicationContextes.entrySet().iterator();
	    	Entry<String, Application> aentry;
	    	while(ait.hasNext()){
	    		aentry = ait.next();
	    		scope=aentry.getValue();
	    		scope.release(pc);
	    	}
	        applicationContextes.clear();
	    	
	    	// release server scope
	    	if(server!=null){
	    		server.release(pc);
	    		server=null;
	    	}
    	
    	}
    	catch(Throwable t){t.printStackTrace();}
    }

    

	private void storeUnusedStorageScope(CFMLFactoryImpl cfmlFactory, int type) {
        Map<String, Map<String, Scope>> contextes = type==Scope.SCOPE_CLIENT?cfClientContextes:cfSessionContextes;
		long timespan = type==Scope.SCOPE_CLIENT?CLIENT_MEMORY_TIMESPAN:SESSION_MEMORY_TIMESPAN;
		String strType=VariableInterpreter.scopeInt2String(type);
		
		if(contextes.size()==0)return;
		long now = System.currentTimeMillis();
		Object[] arrContextes= contextes.keySet().toArray();
		Object applicationName,cfid,o;
		Map<String, Scope> fhm;
		for(int i=0;i<arrContextes.length;i++) {
			
			applicationName=arrContextes[i];
            fhm = contextes.get(applicationName);
            if(fhm.size()>0){
    			Object[] arrClients= fhm.keySet().toArray();
                int count=arrClients.length;
                for(int y=0;y<arrClients.length;y++) {
                	cfid=arrClients[y];
                	o=fhm.get(cfid);
                	if(!(o instanceof StorageScope)) continue;
    				StorageScope scope=(StorageScope)o;
    				if(scope.lastVisit()+timespan<now && !(scope instanceof MemoryScope)) {
    					getLog().log(Log.LEVEL_INFO,"scope-context", "remove from memory "+strType+" scope for "+applicationName+"/"+cfid+" from storage "+scope.getStorage());
    					
        				fhm.remove(arrClients[y]);
        				count--;
    				}
    			}
                if(count==0)contextes.remove(arrContextes[i]);
            }
		}
	}
    
	/**
	 * @param cfmlFactory 
	 * 
	 */
	private void clearUnusedMemoryScope(CFMLFactoryImpl cfmlFactory, int type) {
        Map<String, Map<String, Scope>> contextes = type==Scope.SCOPE_CLIENT?cfClientContextes:cfSessionContextes;
		if(contextes.size()==0)return;
		
		
		
        Object[] arrContextes= contextes.keySet().toArray();
		ApplicationListener listener = cfmlFactory.getConfig().getApplicationListener();
		Object applicationName,cfid,o;
		Map<String, Scope> fhm;
		
		for(int i=0;i<arrContextes.length;i++) {
			applicationName=arrContextes[i];
            fhm = contextes.get(applicationName);

			if(fhm.size()>0){
    			Object[] cfids= fhm.keySet().toArray();
                int count=cfids.length;
                for(int y=0;y<cfids.length;y++) {
                	cfid=cfids[y];
                	o=fhm.get(cfid);
                	if(!(o instanceof MemoryScope)) continue;
                	MemoryScope scope=(MemoryScope) o;
    				// close
    				if(scope.isExpired()) {
    					// TODO macht das sinn? ist das nicht kopierleiche?
    					ApplicationImpl application=(ApplicationImpl) applicationContextes.get(applicationName);
    					long appLastAccess=0;
    					if(application!=null){
    						appLastAccess=application.getLastAccess();
    						application.touch();
    					}
    					scope.touch();
                        
    					try {
    						if(type==Scope.SCOPE_SESSION)listener.onSessionEnd(cfmlFactory,(String)applicationName,(String)cfid);
    					} 
    					catch (Throwable t) {t.printStackTrace();
    						ExceptionHandler.log(cfmlFactory.getConfig(),Caster.toPageException(t));
    					}
    					finally {
    						if(application!=null)application.setLastAccess(appLastAccess);
    						fhm.remove(cfids[y]);
        					scope.release(ThreadLocalPageContext.get());
        					getLog().log(Log.LEVEL_INFO,"scope-context", "remove memory based "+VariableInterpreter.scopeInt2String(type)+" scope for "+applicationName+"/"+cfid);
        					count--;
    					}
    				}
    			}
                if(count==0)contextes.remove(arrContextes[i]);
            }
		}
	}
	
	private void clearUnusedApplications(CFMLFactoryImpl jspFactory) {
        
        if(applicationContextes.size()==0)return;
		
		long now=System.currentTimeMillis();
		Object[] arrContextes= applicationContextes.keySet().toArray();
		ApplicationListener listener = jspFactory.getConfig().getApplicationListener();
		for(int i=0;i<arrContextes.length;i++) {
            Application application=applicationContextes.get(arrContextes[i]);
			
			if(application.getLastAccess()+application.getTimeSpan()<now) {
                //SystemOut .printDate(jspFactory.getConfigWebImpl().getOut(),"Clear application scope:"+arrContextes[i]+"-"+this);
                application.touch();
				try {
					listener.onApplicationEnd(jspFactory,(String)arrContextes[i]);
				} 
				catch (Throwable t) {
					ExceptionHandler.log(jspFactory.getConfig(),Caster.toPageException(t));
				}
				finally {
					applicationContextes.remove(arrContextes[i]);
					application.release(ThreadLocalPageContext.get());
				}
				
			}
		}
	}
	

	public void clearApplication(PageContext pc) throws PageException {
        
        if(applicationContextes.size()==0) throw new ApplicationException("there is no application context defined");
		
        String name = pc.getApplicationContext().getName();
        CFMLFactoryImpl jspFactory = (CFMLFactoryImpl)pc.getCFMLFactory();
		
        Application application=applicationContextes.get(name);
        if(application==null) throw new ApplicationException("there is no application context defined with name ["+name+"]");
        ApplicationListener listener = PageContextUtil.getApplicationListener(pc);
        application.touch();
		try {
			listener.onApplicationEnd(jspFactory,name);
		} 
		finally {
			applicationContextes.remove(name);
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

	public void invalidateUserScope(PageContextImpl pc,boolean migrateSessionData,boolean migrateClientData) throws PageException {
		ApplicationContext appContext = pc.getApplicationContext();

		// get in memory scopes
		Map<String, Scope> clientContext = getSubMap(cfClientContextes,appContext.getName());
		UserScope clientScope = (UserScope) clientContext.get(pc.getCFID());
		Map<String, Scope> sessionContext = getSubMap(cfSessionContextes,appContext.getName());
		UserScope sessionScope = (UserScope) sessionContext.get(pc.getCFID());
		
		// remove  Scopes completly
		removeSessionScope(pc);
		removeClientScope(pc);
		
		pc.resetIdAndToken();

		_migrate(pc,clientContext,clientScope,migrateClientData);
		_migrate(pc,sessionContext,sessionScope,migrateSessionData);
	}

	private static void _migrate(PageContextImpl pc, Map<String, Scope> context, UserScope scope, boolean migrate) {
		if(scope==null) return;
		if(!migrate) scope.clear();
		scope.resetEnv(pc);
		context.put(pc.getCFID(), scope);
	}
	

}