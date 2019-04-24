/**
 * Copyright (c) 2017, Lucee Assosication Switzerland
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
package lucee.runtime.type.scope.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.collection.MapPro;
import lucee.commons.collection.concurrent.ConcurrentHashMapPro;
import lucee.commons.io.log.Log;
import lucee.commons.lang.RandomUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.db.DataSource;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.ValueIterator;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.client.IKStorageScopeClient;
import lucee.runtime.type.scope.session.IKStorageScopeSession;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;
import lucee.runtime.type.wrap.MapAsStruct;

public abstract class IKStorageScopeSupport extends StructSupport implements StorageScope {

	//public static int STORAGE_TYPE_DATASOURCE=1;
	//public static int STORAGE_TYPE_CACHE=2;
	
	public static Collection.Key CFID=KeyConstants._cfid;
	public static Collection.Key CFTOKEN=KeyConstants._cftoken;
	public static Collection.Key URLTOKEN=KeyConstants._urltoken;
	public static Collection.Key LASTVISIT=KeyConstants._lastvisit;
	public static Collection.Key HITCOUNT=KeyConstants._hitcount;
	public static Collection.Key TIMECREATED=KeyConstants._timecreated;
	public static Collection.Key SESSION_ID=KeyConstants._sessionid;
	public static Collection.Key CSRF_TOKEN=KeyConstants._csrf_token;
	
	protected static final IKStorageScopeItem ONE = new IKStorageScopeItem("1");


	private static int _id=0;
	private int id=0;

	private static final long serialVersionUID = 7874930250042576053L;
	private static final IKStorageScopeItem NULL = new IKStorageScopeItem("null");
	private static Set<Collection.Key> FIX_KEYS=new HashSet<Collection.Key>();
	static {
		FIX_KEYS.add(CFID);
		FIX_KEYS.add(CFTOKEN);
		FIX_KEYS.add(URLTOKEN);
		FIX_KEYS.add(LASTVISIT);
		FIX_KEYS.add(HITCOUNT);
		FIX_KEYS.add(TIMECREATED);
	}
	

	protected static Set<Collection.Key> ignoreSet=new HashSet<Collection.Key>();
	static {
		ignoreSet.add(CFID);
		ignoreSet.add(CFTOKEN);
		ignoreSet.add(URLTOKEN);
	}
	
	
	protected boolean isinit=true;
	protected MapPro<Collection.Key,IKStorageScopeItem> data;
	protected long lastvisit;
	protected DateTime _lastvisit;
	protected int hitcount=0;
	protected DateTime timecreated;
	private boolean hasChanges=false;
	protected String strType;
	protected int type;
	private long timeSpan=-1;
	private String storage;
	private Map<String, String> tokens=new ConcurrentHashMap<String, String>();
	private long lastModified;
	
	private IKHandler handler;
	private String appName;
	private String name;
	private String cfid; 
	
	
	public IKStorageScopeSupport(PageContext pc, IKHandler handler, String appName,String name,String strType,int type,MapPro<Collection.Key,IKStorageScopeItem> data, long lastModified) { 
		// !!! do not store the pagecontext or config object, this object is Serializable !!!
		Config config = ThreadLocalPageContext.getConfig(pc);
		this.data=data;
		
		timecreated=doNowIfNull(config,Caster.toDate(data.g(TIMECREATED,null),false,pc.getTimeZone(),null));
		_lastvisit=doNowIfNull(config,Caster.toDate(data.g(LASTVISIT,null),false,pc.getTimeZone(),null));
		
		if(_lastvisit==null) _lastvisit=timecreated;
		lastvisit=_lastvisit==null?0:_lastvisit.getTime();
		
		if(pc.getApplicationContext().getSessionCluster() && isSessionStorageDatasource(pc)) {
			IKStorageScopeItem csrfTokens = this.data.g(CSRF_TOKEN, null);
			if(csrfTokens instanceof Map) {
				this.tokens = (Map<String, String>) csrfTokens.getValue();
			}
		}
			
		this.hitcount=(type==SCOPE_CLIENT)?Caster.toIntValue(data.g(HITCOUNT,ONE),1):1;
		this.strType=strType;
		this.type=type;
        this.lastModified=lastModified;
		this.handler=handler;
		this.appName=appName;
		this.name=name;
		this.cfid=pc.getCFID();
		id=++_id;
	}
	
	
	/**
	 * Constructor of the class
	 * @param other
	 * @param deepCopy
	 */
	protected IKStorageScopeSupport(IKStorageScopeSupport other, boolean deepCopy) {
		this.data=(MapPro<Collection.Key, IKStorageScopeItem>)
				Duplicator.duplicateMap(other.data, new ConcurrentHashMapPro<Collection.Key, IKStorageScopeItem>(), deepCopy);
		this.timecreated=other.timecreated;
		this._lastvisit=other._lastvisit;
		this.hitcount=other.hitcount;
		this.isinit=other.isinit;
		this.lastvisit=other.lastvisit;
		this.strType=other.strType;
		this.type=other.type;
		this.timeSpan=other.timeSpan;
        id=++_id;
        this.lastModified=other.lastModified;
        
        this.handler=other.handler;
        this.appName=other.appName;
        this.name=other.name;
        this.cfid=other.cfid;
	}
	
	
	public static Scope getInstance(int scope, IKHandler handler, String appName, String name, PageContext pc, Scope existing, Log log) throws PageException {
		IKStorageValue sv=null;
		if(Scope.SCOPE_SESSION==scope)		sv= handler.loadData(pc, appName,name, "session",Scope.SCOPE_SESSION, log);
		else if(Scope.SCOPE_CLIENT==scope)	sv= handler.loadData(pc, appName,name, "client",Scope.SCOPE_CLIENT, log);
		
		
		
		if(sv!=null) {
			long time = sv.lastModified();
			if(existing instanceof IKStorageScopeSupport) {
				IKStorageScopeSupport tmp = ((IKStorageScopeSupport)existing);
				if(tmp.lastModified()>=time && name.equalsIgnoreCase(tmp.getStorage())) {
					return existing;
				}
			}
			
			if(Scope.SCOPE_SESSION==scope) 		return new IKStorageScopeSession(pc,handler,appName,name,sv.getValue(),time);
			else if(Scope.SCOPE_CLIENT==scope)	return new IKStorageScopeClient(pc,handler,appName,name,sv.getValue(),time);
		}
		else if(existing instanceof IKStorageScopeSupport) {
			IKStorageScopeSupport tmp = ((IKStorageScopeSupport)existing);
			if(name.equalsIgnoreCase(tmp.getStorage())) {
				return existing;
			}
		}
		
		IKStorageScopeSupport rtn=null;
		ConcurrentHashMapPro<Key, IKStorageScopeItem> map = new ConcurrentHashMapPro<Collection.Key,IKStorageScopeItem>();
		if(Scope.SCOPE_SESSION==scope) rtn= new IKStorageScopeSession(pc,handler,appName,name,map,0);
		else if(Scope.SCOPE_CLIENT==scope) rtn= new IKStorageScopeClient(pc,handler,appName,name,map,0);
		
		rtn.store(pc);
		return rtn;
	}
	
	public static Scope getInstance(int scope, IKHandler handler, String appName, String name, PageContext pc, Session existing, Log log, Session defaultValue) {
		try {
			return getInstance(scope, handler, appName, name, pc,existing, log);
		}
		catch (PageException e) {}
		return defaultValue;
	}

	
	public static boolean hasInstance(int scope, IKHandler handler, String appName, String name, PageContext pc) {
		try {
			if(Scope.SCOPE_SESSION==scope)		return handler.loadData(pc, appName,name, "session",Scope.SCOPE_SESSION, null)!=null;
			else if(Scope.SCOPE_CLIENT==scope)	return handler.loadData(pc, appName,name, "client",Scope.SCOPE_CLIENT, null)!=null;
			return false;
		} 
		catch (PageException e) {
			return false;
		}
	}
	
	@Override
	public void touchBeforeRequest(PageContext pc) {
		
		hasChanges=false;
		setTimeSpan(pc);
		
		
		//lastvisit=System.currentTimeMillis();
		if(data==null) data=new ConcurrentHashMapPro<Collection.Key, IKStorageScopeItem>();
		data.put(KeyConstants._cfid, new IKStorageScopeItem(pc.getCFID()));
		data.put(KeyConstants._cftoken, new IKStorageScopeItem(pc.getCFToken()));
		data.put(URLTOKEN, new IKStorageScopeItem(pc.getURLToken()));
		data.put(LASTVISIT, new IKStorageScopeItem(_lastvisit));
		_lastvisit=new DateTimeImpl(pc.getConfig());
		lastvisit=System.currentTimeMillis();
		
		if(type==SCOPE_CLIENT){
			data.put(HITCOUNT, new IKStorageScopeItem(new Double(hitcount++)));
		}
		else {
			data.put(SESSION_ID, new IKStorageScopeItem(pc.getApplicationContext().getName()+"_"+pc.getCFID()+"_"+pc.getCFToken()));
		}
		
		if(pc.getApplicationContext().getSessionCluster() && isSessionStorageDatasource(pc)) {
			data.put(KeyConstants._csrf_token, new IKStorageScopeItem(MapAsStruct.toStruct(tokens, false)));
		}
		
		data.put(TIMECREATED, new IKStorageScopeItem(timecreated));
	}
	
	private Boolean isSessionStorageDatasource(PageContext pc) {
		String storage = pc.getApplicationContext().getSessionstorage();
		DataSource ds = pc.getDataSource(storage, null);
		if(ds != null && ds.isStorage())
			return true;
		else 
			return false;
		
	}

	public void resetEnv(PageContext pc){
		_lastvisit=new DateTimeImpl(pc.getConfig());
		timecreated=new DateTimeImpl(pc.getConfig());
		touchBeforeRequest(pc);
		
	}
	
	void setTimeSpan(PageContext pc) {
		ApplicationContext ac=pc.getApplicationContext();
		this.timeSpan=getType()==SCOPE_SESSION?
				ac.getSessionTimeout().getMillis():
				ac.getClientTimeout().getMillis();
	}
	
	@Override
	public void setMaxInactiveInterval(int interval) {
		this.timeSpan=interval*1000L;
	}

	@Override
	public int getMaxInactiveInterval() {
		return (int)(this.timeSpan/1000L);
	}
	
	@Override
	public final boolean isInitalized() {
		return isinit;
	}
	
	public long lastModified() {
		return lastModified;
	}
	
	@Override
	public final void initialize(PageContext pc) {
		// StorageScopes need only request initialisation no global init, they are not reused;
	}
	
	@Override
	public void touchAfterRequest(PageContext pc) {
		
		setTimeSpan(pc);
		data.put(LASTVISIT, new IKStorageScopeItem(_lastvisit));
		data.put(TIMECREATED, new IKStorageScopeItem(timecreated));
		
		if(type==SCOPE_CLIENT){
			data.put(HITCOUNT, new IKStorageScopeItem(new Double(hitcount)));
		}
		store(pc);
	}

	@Override
	public final void release(PageContext pc) {
		clear();
		isinit=false;
	}
	
	
	/**
	 * @return returns if the scope is empty or not, this method ignore the "constant" entries of the scope (cfid,cftoken,urltoken)
	 */
	public boolean hasContent() {
		if(data.size()==(type==SCOPE_CLIENT?6:5) && data.containsKey(URLTOKEN) && data.containsKey(KeyConstants._cftoken) && data.containsKey(KeyConstants._cfid)) {
			return false;
		}
		return true;
	}
	
	@Override
	public void  clear() {
		data.clear();
	}

	@Override
	public boolean containsKey(Key key) {
		return data.containsKey(key);
	}

	@Override
	public Object get(Key key) throws PageException {
		return data.g(key).getValue();
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		IKStorageScopeItem v = data.g(key, NULL);
		if(v==NULL) return defaultValue;
		return v.getValue();
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return data.keySet().iterator();
	}
    
	
	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}
	
	@Override
	public Iterator<Object> valueIterator() {
		return new ValueIterator(this, keys()); // TODO use or make a faster iterator
	}

	@Override
	public lucee.runtime.type.Collection.Key[] keys() {
		return CollectionUtil.keys(this);
	}


	@Override
	public Object remove(Key key) throws PageException {
		hasChanges=true;
		IKStorageScopeItem existing = data.get(key);
		if(existing!=null) {
			return existing.remove();
		}
    	throw new ExpressionException("can't remove key ["+key.getString()+"] from map, key doesn't exist");
	}

	@Override
	public Object removeEL(Key key) {
		hasChanges=true;
		IKStorageScopeItem existing = data.get(key);
		if(existing!=null) {
			return existing.remove();
		}
		return null;
		
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		hasChanges=true;
		return data.put(key, new IKStorageScopeItem(value));
	}

	@Override
	public Object setEL(Key key, Object value) {
		hasChanges=true;
		return data.put(key, new IKStorageScopeItem(value));
	}

	@Override
	public long lastVisit() {
		return lastvisit;
	}

	public Collection.Key[] pureKeys() {
		List<Collection.Key> keys=new ArrayList<Collection.Key>();
		Iterator<Key> it = keyIterator();
		Collection.Key key;
		while(it.hasNext()){
			key=it.next();
			if(!FIX_KEYS.contains(key))keys.add(key);
		}
		return keys.toArray(new Collection.Key[keys.size()]);
	}
	
	@Override
	public int size() {
		return data.size();
	}

	
	public void store(PageContext pc){ // FUTURE add to interface
		handler.store(this, pc, appName, name, cfid, data,ThreadLocalPageContext.getConfig(pc).getLog("scope"));
	}

	public void unstore(PageContext pc){
		handler.unstore(this, pc, appName, name, cfid,ThreadLocalPageContext.getConfig(pc).getLog("scope"));
	}
	
	public void store(Config config){ 
		store(ThreadLocalPageContext.get());
	}

	public void unstore(Config config){
		unstore(ThreadLocalPageContext.get());
	}
	
	/**
	 * @return the hasChanges
	 */
	public boolean hasChanges() {
		return hasChanges;
	}
	

	@Override
	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	@Override
	public java.util.Collection values() {
		java.util.Collection<Object> res=new ArrayList<Object>();
		Iterator<IKStorageScopeItem> it = data.values().iterator();
		while(it.hasNext()) {
			res.add(it.next().getValue());
		}
		return res;
	}

	@Override
	public final int getType() {
		return type;
	}

	@Override
	public final String getTypeAsString() {
		return strType;
	}
	
	

	@Override
	public final DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return StructUtil.toDumpTable(this, StringUtil.ucFirst(getTypeAsString())+" Scope ("+getStorageType()+")", pageContext, maxlevel, dp);
	}
	

	@Override
	public long getLastAccess() { return lastvisit;}
	@Override
	public long getTimeSpan() { return timeSpan;}
	
	
	@Override
	public void touch() {
		lastvisit=System.currentTimeMillis();
		_lastvisit=new DateTimeImpl(ThreadLocalPageContext.getConfig());
	}
	
	@Override
	public boolean isExpired() {
	    return (getLastAccess()+getTimeSpan())<System.currentTimeMillis();
    }


	
	@Override
	public void setStorage(String storage) {
		this.storage=storage;
	}

	@Override
	public String getStorage() {
		return storage;
	}
	
	public static String encode(String input) {
		int len=input.length();
		StringBuilder sb=new StringBuilder();
		char c;
		for(int i=0;i<len;i++){
			c=input.charAt(i);
			if((c>='0' && c<='9') || (c>='a' && c<='z') || (c>='A' && c<='Z') || c=='_' || c=='-')
				sb.append(c);
			else {
				sb.append('$');
				sb.append(Integer.toString((c),Character.MAX_RADIX));
				sb.append('$');
			}
		}
		
		return sb.toString();
	}

	public static String decode(String input) {
		int len=input.length();
		StringBuilder sb=new StringBuilder();
		char c;
		int ni;
		for(int i=0;i<len;i++){
			c=input.charAt(i);
			if(c=='$') {
				ni=input.indexOf('$',i+1);
				sb.append((char)Integer.parseInt(input.substring(i+1,ni),Character.MAX_RADIX));
				i=ni;
			}
				
			else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
    public int _getId() {
        return id;
    }
    

	
	@Override
	public long getCreated() {
		return timecreated==null?0:timecreated.getTime();
	}
	
	@Override
	public String generateToken(String key, boolean forceNew) {
        
        // get existing
        String token;
        if(!forceNew) {
        	token = tokens.get(key);
        	if(token!=null) return token;
        }
        
        // create new one
        token = RandomUtil.createRandomStringLC(40);
        tokens.put(key, token);
        return token;
    }
	
	@Override
	public boolean verifyToken(String token, String key) {
		String _token = tokens.get(key);
        return _token!=null && _token.equalsIgnoreCase(token);
    }

	public static void merge(MapPro<Key, IKStorageScopeItem> local,MapPro<Key, IKStorageScopeItem> storage) {
		Iterator<Entry<Key, IKStorageScopeItem>> it = local.entrySet().iterator();
		Entry<Key, IKStorageScopeItem> e;
		IKStorageScopeItem storageItem;
		while(it.hasNext()) {
			e = it.next();
			
			storageItem = storage.get(e.getKey());
			// this entry not exist in the storage
			if(storageItem==null) {
				if(!e.getValue().removed()) storage.put(e.getKey(), e.getValue());
			}
			// local is newer than storage
			else if(e.getValue().lastModified()>storageItem.lastModified()) {
				if(e.getValue().removed()) storage.remove(e.getKey());
				else {
					storage.put(e.getKey(), e.getValue());
				}
			}
			// local is older than storage is ignored? 
			
		}
		
		
	}

	public static MapPro<Key, IKStorageScopeItem> cleanRemoved(MapPro<Key, IKStorageScopeItem> local) {
		Iterator<Entry<Key, IKStorageScopeItem>> it = local.entrySet().iterator();
		Entry<Key, IKStorageScopeItem> e;
		while(it.hasNext()) {
			e=it.next();
			if(e.getValue().removed()) local.remove(e.getKey());
		}
		return local;
	}

	public static MapPro<Key, IKStorageScopeItem> prepareToStore(MapPro<Key, IKStorageScopeItem> local, 
			Object oStorage,long lastModified) throws PageException {
		// cached data changed in meantime
		if(oStorage instanceof IKStorageValue) {
			IKStorageValue storage=(IKStorageValue) oStorage;
			if(storage.lastModified()>lastModified) {
				MapPro<Key, IKStorageScopeItem> trg = storage.getValue();
				IKStorageScopeSupport.merge(local,trg);
				return trg;
			}
			else {
				return IKStorageScopeSupport.cleanRemoved(local);
				
			}
		}
		else if(oStorage instanceof byte[][]) {
			byte[][] barrr=(byte[][]) oStorage;
			if(IKStorageValue.toLong(barrr[1])>lastModified) {
				if(barrr[0]==null || barrr[0].length==0) return local;
				MapPro<Key, IKStorageScopeItem> trg = IKStorageValue.deserialize(barrr[0]);
				IKStorageScopeSupport.merge(local,trg);
				return trg;
			}
			else {
				return IKStorageScopeSupport.cleanRemoved(local);
				
			}
		}
		return local;
		
	}

	@Override
	public final String getStorageType() {
		return handler.getType();
	}

	protected static DateTime doNowIfNull(Config config,DateTime dt) {
		if(dt==null)return new DateTimeImpl(config);
		return dt;
	}
		
	//protected abstract IKStorageValue loadData(PageContext pc, String appName, String name,String strType,int type, Log log) throws PageException;
		
}
