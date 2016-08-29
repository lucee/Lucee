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
package lucee.runtime.type.scope.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lucee.commons.collection.MapPro;
import lucee.commons.collection.concurrent.ConcurrentHashMapPro;
import lucee.commons.lang.RandomUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyAsStringIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.ValueIterator;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;
import lucee.runtime.type.wrap.MapAsStruct;

public abstract class StorageImpl extends StructSupport implements StorageScope {

	public static Collection.Key CFID=KeyConstants._cfid;
	public static Collection.Key CFTOKEN=KeyConstants._cftoken;
	public static Collection.Key URLTOKEN=KeyConstants._urltoken;
	public static Collection.Key LASTVISIT=KeyConstants._lastvisit;
	public static Collection.Key HITCOUNT=KeyConstants._hitcount;
	public static Collection.Key TIMECREATED=KeyConstants._timecreated;
	public static Collection.Key SESSION_ID=KeyConstants._sessionid;


	private static int _id=0;
	private int id=0;

	private static final long serialVersionUID = 7874930250042576053L;
	private static final StorageScopeItem NULL = new StorageScopeItem("null");
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
	protected MapPro<Collection.Key,StorageScopeItem> data;
	protected long lastvisit;
	protected DateTime _lastvisit;
	protected int hitcount=0;
	protected DateTime timecreated;
	private boolean hasChanges=false;
	private String strType;
	private int type;
	private long timeSpan=-1;
	private String storage;
	private Map<String, String> tokens; 
	
	
	/**
	 * Constructor of the class
	 * @param sct
	 * @param timecreated
	 * @param _lastvisit
	 * @param lastvisit
	 * @param hitcount
	 */
	public StorageImpl(MapPro<Collection.Key,StorageScopeItem> data, DateTime timecreated, DateTime _lastvisit, long lastvisit, int hitcount,String strType,int type) {
		this.data=data;
		this.timecreated=timecreated;
		if(_lastvisit==null)	this._lastvisit=timecreated;
		else 					this._lastvisit=_lastvisit;
		
		if(lastvisit==-1) 		this.lastvisit=this._lastvisit.getTime();
		else 					this.lastvisit=lastvisit;

		this.hitcount=hitcount;
		this.strType=strType;
		this.type=type;
        id=++_id;
	}
	
	/**
	 * Constructor of the class
	 * @param other
	 * @param deepCopy
	 */
	public StorageImpl(StorageImpl other, boolean deepCopy) {
		this.data=(MapPro<Collection.Key, StorageScopeItem>)Duplicator.duplicateMap(other.data, new ConcurrentHashMapPro<Collection.Key, StorageScopeItem>(), deepCopy);
		this.timecreated=other.timecreated;
		this._lastvisit=other._lastvisit;
		this.hitcount=other.hitcount;
		this.isinit=other.isinit;
		this.lastvisit=other.lastvisit;
		this.strType=other.strType;
		this.type=other.type;
		this.timeSpan=other.timeSpan;
        id=++_id;
	}

	@Override
	public void touchBeforeRequest(PageContext pc) {
		
		hasChanges=false;
		setTimeSpan(pc);
		
		
		//lastvisit=System.currentTimeMillis();
		if(data==null) data=new ConcurrentHashMapPro<Collection.Key, StorageScopeItem>();
		data.put(KeyConstants._cfid, new StorageScopeItem(pc.getCFID()));
		data.put(KeyConstants._cftoken, new StorageScopeItem(pc.getCFToken()));
		data.put(URLTOKEN, new StorageScopeItem(pc.getURLToken()));
		data.put(LASTVISIT, new StorageScopeItem(_lastvisit));
		_lastvisit=new DateTimeImpl(pc.getConfig());
		lastvisit=System.currentTimeMillis();
		
		if(type==SCOPE_CLIENT){
			data.put(HITCOUNT, new StorageScopeItem(new Double(hitcount++)));
		}
		else {
			data.put(SESSION_ID, new StorageScopeItem(pc.getApplicationContext().getName()+"_"+pc.getCFID()+"_"+pc.getCFToken()));
		}
		data.put(TIMECREATED, new StorageScopeItem(timecreated));
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
	
	@Override
	public final void initialize(PageContext pc) {
		// StorageScopes need only request initialisation no global init, they are not reused;
	}
	
	@Override
	public void touchAfterRequest(PageContext pc) {
		
		data.put(LASTVISIT, new StorageScopeItem(_lastvisit));
		data.put(TIMECREATED, new StorageScopeItem(timecreated));
		
		if(type==SCOPE_CLIENT){
			data.put(HITCOUNT, new StorageScopeItem(new Double(hitcount)));
		}
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
		StorageScopeItem v = data.g(key, NULL);
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
		StorageScopeItem existing = data.get(key);
		if(existing!=null) {
			return existing.remove();
		}
    	throw new ExpressionException("can't remove key ["+key.getString()+"] from map, key doesn't exist");
	}

	@Override
	public Object removeEL(Key key) {
		hasChanges=true;
		StorageScopeItem existing = data.get(key);
		if(existing!=null) {
			return existing.remove();
		}
		return null;
		
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		hasChanges=true;
		return data.put(key, new StorageScopeItem(value));
	}

	@Override
	public Object setEL(Key key, Object value) {
		hasChanges=true;
		return data.put(key, new StorageScopeItem(value));
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

	@Override
	public void store(Config config){
		//do nothing
	}

	@Override
	public void unstore(Config config){
		//do nothing
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
		Iterator<StorageScopeItem> it = data.values().iterator();
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
	public synchronized String generateToken(String key, boolean forceNew) {
        if(tokens==null) 
        	tokens = new HashMap<String,String>();
        
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
	public synchronized boolean verifyToken(String token, String key) {
		if(tokens==null) return false;
        String _token = tokens.get(key);
        return _token!=null && _token.equalsIgnoreCase(token);
    }

	public static void merge(MapPro<Key, StorageScopeItem> local,MapPro<Key, StorageScopeItem> storage) {
		Iterator<Entry<Key, StorageScopeItem>> it = local.entrySet().iterator();
		Entry<Key, StorageScopeItem> e;
		StorageScopeItem storageItem;
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

	public static MapPro<Key, StorageScopeItem> cleanRemoved(MapPro<Key, StorageScopeItem> local) {
		Iterator<Entry<Key, StorageScopeItem>> it = local.entrySet().iterator();
		Entry<Key, StorageScopeItem> e;
		while(it.hasNext()) {
			e=it.next();
			if(e.getValue().removed()) local.remove(e.getKey());
		}
		return local;
	}

	public static MapPro<Key, StorageScopeItem> prepareToStore(MapPro<Key, StorageScopeItem> local, Object oStorage,long lastModified) throws PageException {
		// cached data changed in meantime
		if(oStorage instanceof StorageVal) {
			StorageVal storage=(StorageVal) oStorage;
			if(storage.lastModified()>lastModified) {
				MapPro<Key, StorageScopeItem> trg = storage.getValue();
				StorageImpl.merge(local,trg);
				return trg;
			}
			else {
				return StorageImpl.cleanRemoved(local);
				
			}
		}
		return local;
		
	}
}