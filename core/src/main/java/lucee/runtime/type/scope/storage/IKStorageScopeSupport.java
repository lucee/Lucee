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

import lucee.commons.collection.MapFactory;
import lucee.commons.io.log.Log;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
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
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.ValueIterator;
import lucee.runtime.type.scope.CSRFTokenSupport;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.client.IKStorageScopeClient;
import lucee.runtime.type.scope.session.IKStorageScopeSession;
import lucee.runtime.type.scope.util.ScopeUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;

public abstract class IKStorageScopeSupport extends StructSupport implements StorageScope, CSRFTokenSupport {

	protected static final IKStorageScopeItem ONE = new IKStorageScopeItem("1");

	private static int _id = 0;
	private int id = 0;

	private static final long serialVersionUID = 7874930250042576053L;
	private static final IKStorageScopeItem NULL = new IKStorageScopeItem("null");
	private static Set<Collection.Key> FIX_KEYS = new HashSet<Collection.Key>();
	static {
		FIX_KEYS.add(KeyConstants._cfid);
		FIX_KEYS.add(KeyConstants._cftoken);
		FIX_KEYS.add(KeyConstants._urltoken);
		FIX_KEYS.add(KeyConstants._lastvisit);
		FIX_KEYS.add(KeyConstants._hitcount);
		FIX_KEYS.add(KeyConstants._timecreated);
	}

	protected static Set<Collection.Key> ignoreSet = new HashSet<Collection.Key>();
	static {
		ignoreSet.add(KeyConstants._cfid);
		ignoreSet.add(KeyConstants._cftoken);
		ignoreSet.add(KeyConstants._urltoken);
	}

	protected boolean isinit = true;
	protected Map<Collection.Key, IKStorageScopeItem> data0;
	protected long lastvisit;
	protected DateTime _lastvisit;
	protected int hitcount = 0;
	protected DateTime timecreated;
	private boolean hasChanges = false;
	protected String strType;
	protected int type;
	private long timeSpan = -1;
	private String storage;
	private final Map<Collection.Key, String> tokens = new ConcurrentHashMap<Collection.Key, String>();
	private long lastModified;

	private IKHandler handler;
	private String appName;
	private String name;
	private String cfid;

	public IKStorageScopeSupport(PageContext pc, IKHandler handler, String appName, String name, String strType, int type, Map<Collection.Key, IKStorageScopeItem> data,
			long lastModified, long timeSpan) {
		// !!! do not store the pagecontext or config object, this object is Serializable !!!
		Config config = ThreadLocalPageContext.getConfig(pc);
		this.data0 = data;

		timecreated = doNowIfNull(config, Caster.toDate(data.getOrDefault(KeyConstants._timecreated, null), false, pc.getTimeZone(), null));
		_lastvisit = doNowIfNull(config, Caster.toDate(data.getOrDefault(KeyConstants._lastvisit, null), false, pc.getTimeZone(), null));

		if (_lastvisit == null) _lastvisit = timecreated;
		lastvisit = _lastvisit == null ? 0 : _lastvisit.getTime();

		this.hitcount = (type == SCOPE_CLIENT) ? Caster.toIntValue(data.getOrDefault(KeyConstants._hitcount, ONE), 1) : 1;
		this.strType = strType;
		this.type = type;
		this.lastModified = lastModified;
		this.handler = handler;
		this.appName = appName;
		this.name = name;
		this.cfid = pc.getCFID();
		id = ++_id;
		this.timeSpan = timeSpan;
	}

	/**
	 * Constructor of the class
	 * 
	 * @param other
	 * @param deepCopy
	 */
	protected IKStorageScopeSupport(IKStorageScopeSupport other, boolean deepCopy) {
		this.data0 = Duplicator.duplicateMap(other.data0, MapFactory.getConcurrentMap(), deepCopy);
		this.timecreated = other.timecreated;
		this._lastvisit = other._lastvisit;
		this.hitcount = other.hitcount;
		this.isinit = other.isinit;
		this.lastvisit = other.lastvisit;
		this.strType = other.strType;
		this.type = other.type;
		this.timeSpan = other.timeSpan;
		id = ++_id;
		this.lastModified = other.lastModified;

		this.handler = other.handler;
		this.appName = other.appName;
		this.name = other.name;
		this.cfid = other.cfid;
	}

	public static Scope getInstance(int scope, IKHandler handler, String appName, String name, PageContext pc, Scope existing, Log log) throws PageException {
		IKStorageValue sv = null;
		if (Scope.SCOPE_SESSION == scope) sv = handler.loadData(pc, appName, name, "session", Scope.SCOPE_SESSION, log);
		else if (Scope.SCOPE_CLIENT == scope) sv = handler.loadData(pc, appName, name, "client", Scope.SCOPE_CLIENT, log);

		if (sv != null) {
			long time = sv.lastModified();

			if (existing instanceof IKStorageScopeSupport) {
				IKStorageScopeSupport tmp = ((IKStorageScopeSupport) existing);
				if (tmp.lastModified() >= time && name.equalsIgnoreCase(tmp.getStorage())) {
					return existing;
				}
			}

			if (Scope.SCOPE_SESSION == scope) return new IKStorageScopeSession(pc, handler, appName, name, sv.getValue(), time, getSessionTimeout(pc));
			else if (Scope.SCOPE_CLIENT == scope) return new IKStorageScopeClient(pc, handler, appName, name, sv.getValue(), time, getClientTimeout(pc));
		}
		else if (existing instanceof IKStorageScopeSupport) {
			IKStorageScopeSupport tmp = ((IKStorageScopeSupport) existing);
			if (name.equalsIgnoreCase(tmp.getStorage())) {
				return existing;
			}
		}

		IKStorageScopeSupport rtn = null;
		Map<Key, IKStorageScopeItem> map = MapFactory.getConcurrentMap();
		if (Scope.SCOPE_SESSION == scope) rtn = new IKStorageScopeSession(pc, handler, appName, name, map, 0, getSessionTimeout(pc));
		else if (Scope.SCOPE_CLIENT == scope) rtn = new IKStorageScopeClient(pc, handler, appName, name, map, 0, getClientTimeout(pc));

		rtn.store(pc);
		return rtn;
	}

	private static long getClientTimeout(PageContext pc) {
		pc = ThreadLocalPageContext.get(pc);
		ApplicationContext ac = pc == null ? null : pc.getApplicationContext();
		TimeSpan timeout = ac == null ? null : ac.getClientTimeout();
		return timeout == null ? 0 : timeout.getMillis();
	}

	private static long getSessionTimeout(PageContext pc) {
		pc = ThreadLocalPageContext.get(pc);
		ApplicationContext ac = pc == null ? null : pc.getApplicationContext();
		TimeSpan timeout = ac == null ? null : ac.getSessionTimeout();
		return timeout == null ? 0 : timeout.getMillis();
	}

	public static Scope getInstance(int scope, IKHandler handler, String appName, String name, PageContext pc, Session existing, Log log, Session defaultValue) {
		try {
			return getInstance(scope, handler, appName, name, pc, existing, log);
		}
		catch (PageException e) {}
		return defaultValue;
	}

	public static boolean hasInstance(int scope, IKHandler handler, String appName, String name, PageContext pc) {
		try {
			if (Scope.SCOPE_SESSION == scope) return handler.loadData(pc, appName, name, "session", Scope.SCOPE_SESSION, null) != null;
			else if (Scope.SCOPE_CLIENT == scope) return handler.loadData(pc, appName, name, "client", Scope.SCOPE_CLIENT, null) != null;
			return false;
		}
		catch (PageException e) {
			return false;
		}
	}

	@Override
	public void touchBeforeRequest(PageContext pc) {

		hasChanges = false;
		setTimeSpan(pc);

		// lastvisit=System.currentTimeMillis();
		if (data0 == null) data0 = MapFactory.getConcurrentMap();
		data0.put(KeyConstants._cfid, new IKStorageScopeItem(pc.getCFID()));
		data0.put(KeyConstants._cftoken, new IKStorageScopeItem(pc.getCFToken()));
		data0.put(KeyConstants._urltoken, new IKStorageScopeItem(pc.getURLToken()));
		data0.put(KeyConstants._lastvisit, new IKStorageScopeItem(_lastvisit));
		_lastvisit = new DateTimeImpl(pc.getConfig());
		lastvisit = System.currentTimeMillis();

		if (type == SCOPE_CLIENT) {
			data0.put(KeyConstants._hitcount, new IKStorageScopeItem(new Double(hitcount++)));
		}
		else {
			data0.put(KeyConstants._sessionid, new IKStorageScopeItem(pc.getApplicationContext().getName() + "_" + pc.getCFID() + "_" + pc.getCFToken()));
		}
		data0.put(KeyConstants._timecreated, new IKStorageScopeItem(timecreated));
	}

	public void resetEnv(PageContext pc) {
		_lastvisit = new DateTimeImpl(pc.getConfig());
		timecreated = new DateTimeImpl(pc.getConfig());
		touchBeforeRequest(pc);

	}

	void setTimeSpan(PageContext pc) {
		ApplicationContext ac = pc.getApplicationContext();
		this.timeSpan = getType() == SCOPE_SESSION ? ac.getSessionTimeout().getMillis() : ac.getClientTimeout().getMillis();
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.timeSpan = interval * 1000L;
	}

	@Override
	public int getMaxInactiveInterval() {
		return (int) (this.timeSpan / 1000L);
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
		data0.put(KeyConstants._lastvisit, new IKStorageScopeItem(_lastvisit));
		data0.put(KeyConstants._timecreated, new IKStorageScopeItem(timecreated));

		if (type == SCOPE_CLIENT) {
			data0.put(KeyConstants._hitcount, new IKStorageScopeItem(new Double(hitcount)));
		}
		store(pc);
	}

	@Override
	public final void release(PageContext pc) {
		clear();
		isinit = false;
	}

	/**
	 * @return returns if the scope is empty or not, this method ignore the "constant" entries of the
	 *         scope (cfid,cftoken,urltoken)
	 */
	public boolean hasContent() {
		if (size() == (type == SCOPE_CLIENT ? 6 : 5) && containsKey(KeyConstants._urltoken) && containsKey(KeyConstants._cftoken) && containsKey(KeyConstants._cfid)) {
			return false;
		}
		return true;
	}

	@Override
	public void clear() {
		Iterator<Key> it = data0.keySet().iterator();
		Key k;
		while (it.hasNext()) {
			k = it.next();
			removeEL(k);
		}
	}

	@Override
	public final boolean containsKey(Key key) {
		IKStorageScopeItem v = data0.getOrDefault(key, NULL);
		return v != NULL && !v.removed();
	}

	@Override
	public final boolean containsKey(PageContext pc, Key key) {
		IKStorageScopeItem v = data0.getOrDefault(key, NULL);
		return v != NULL && !v.removed();
	}

	@Override
	public final Object get(Key key) throws PageException {
		return get((PageContext) null, key);
	}

	@Override
	public final Object get(PageContext pc, Key key) throws PageException {
		IKStorageScopeItem v = data0.getOrDefault(key, null);
		if (v == null) throw StructSupport.invalidKey(data0, key, false);

		if (v.removed()) {
			StringBuilder sb = new StringBuilder();
			Iterator<?> it = keySet().iterator();
			Object k;
			while (it.hasNext()) {
				k = it.next();
				if (sb.length() > 0) sb.append(',');
				sb.append(k.toString());
			}
			return new ExpressionException("key [" + key + "] doesn't exist (existing keys:" + sb.toString() + ")");
		}
		return v.getValue();
	}

	@Override
	public final Object get(Key key, Object defaultValue) {
		return get((PageContext) null, key, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Key key, Object defaultValue) {
		IKStorageScopeItem v = data0.getOrDefault(key, NULL);
		if (v == NULL || v.removed()) return defaultValue;
		return v.getValue();
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return keySet().iterator();
	}

	@Override
	public Set<Collection.Key> keySet() {
		Set<Collection.Key> keys = new HashSet<Collection.Key>();
		Iterator<Entry<Key, IKStorageScopeItem>> it = data0.entrySet().iterator();
		Entry<Key, IKStorageScopeItem> e;
		IKStorageScopeItem v;
		while (it.hasNext()) {
			e = it.next();
			v = e.getValue();
			if (v != NULL && !v.removed()) keys.add(e.getKey());
		}
		return keys;
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
		hasChanges = true;
		IKStorageScopeItem existing = data0.get(key);
		if (existing != null) {
			return existing.remove();
		}
		throw new ExpressionException("can't remove key [" + key.getString() + "] from map, key doesn't exist");
	}

	@Override
	public Object removeEL(Key key) {
		hasChanges = true;
		IKStorageScopeItem existing = data0.get(key);
		if (existing != null) {
			return existing.remove();
		}
		return null;
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		hasChanges = true;
		return data0.put(key, new IKStorageScopeItem(value));
	}

	@Override
	public Object setEL(Key key, Object value) {
		hasChanges = true;
		return data0.put(key, new IKStorageScopeItem(value));
	}

	@Override
	public long lastVisit() {
		return lastvisit;
	}

	public Collection.Key[] pureKeys() {
		List<Collection.Key> keys = new ArrayList<Collection.Key>();
		Iterator<Key> it = keyIterator();
		Collection.Key key;
		while (it.hasNext()) {
			key = it.next();
			if (!FIX_KEYS.contains(key)) keys.add(key);
		}
		return keys.toArray(new Collection.Key[keys.size()]);
	}

	@Override
	public int size() {
		int size = 0;
		Iterator<Entry<Key, IKStorageScopeItem>> it = data0.entrySet().iterator();
		Entry<Key, IKStorageScopeItem> e;
		IKStorageScopeItem v;
		while (it.hasNext()) {
			e = it.next();
			v = e.getValue();
			if (v != NULL && !v.removed()) size++;
		}
		return size;
	}

	public void store(PageContext pc) { // FUTURE add to interface
		handler.store(this, pc, appName, name, cfid, data0, ThreadLocalPageContext.getConfig(pc).getLog("scope"));
	}

	public void unstore(PageContext pc) {
		handler.unstore(this, pc, appName, name, cfid, ThreadLocalPageContext.getConfig(pc).getLog("scope"));
	}

	@Override
	public void store(Config config) {
		store(ThreadLocalPageContext.get());
	}

	@Override
	public void unstore(Config config) {
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
		Iterator<?> it = values().iterator();
		while (it.hasNext()) {
			if (it.next().equals(value)) return true;
		}
		return false;
	}

	@Override
	public java.util.Collection values() {
		java.util.Collection<Object> res = new ArrayList<Object>();
		Iterator<IKStorageScopeItem> it = data0.values().iterator();
		IKStorageScopeItem v;
		while (it.hasNext()) {
			v = it.next();
			if (v != NULL && !v.removed()) res.add(v.getValue());
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
		return StructUtil.toDumpTable(this, StringUtil.ucFirst(getTypeAsString()) + " Scope (" + getStorageType() + ")", pageContext, maxlevel, dp);
	}

	@Override
	public long getLastAccess() {
		return lastvisit;
	}

	@Override
	public long getTimeSpan() {
		return timeSpan;
	}

	@Override
	public void touch() {
		lastvisit = System.currentTimeMillis();
		_lastvisit = new DateTimeImpl(ThreadLocalPageContext.getConfig());
	}

	@Override
	public boolean isExpired() {
		return (getLastAccess() + getTimeSpan()) < System.currentTimeMillis();
	}

	@Override
	public void setStorage(String storage) {
		this.storage = storage;
	}

	@Override
	public String getStorage() {
		return storage;
	}

	public static String encode(String input) {
		int len = input.length();
		StringBuilder sb = new StringBuilder();
		char c;
		for (int i = 0; i < len; i++) {
			c = input.charAt(i);
			if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '-') sb.append(c);
			else {
				sb.append('$');
				sb.append(Integer.toString((c), Character.MAX_RADIX));
				sb.append('$');
			}
		}

		return sb.toString();
	}

	public static String decode(String input) {
		int len = input.length();
		StringBuilder sb = new StringBuilder();
		char c;
		int ni;
		for (int i = 0; i < len; i++) {
			c = input.charAt(i);
			if (c == '$') {
				ni = input.indexOf('$', i + 1);
				sb.append((char) Integer.parseInt(input.substring(i + 1, ni), Character.MAX_RADIX));
				i = ni;
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
		return timecreated == null ? 0 : timecreated.getTime();
	}

	@Override
	public String generateToken(String key, boolean forceNew) {
		return ScopeUtil.generateCsrfToken(tokens, key, forceNew);
	}

	@Override
	public boolean verifyToken(String token, String key) {
		return ScopeUtil.verifyCsrfToken(tokens, token, key);
	}

	public static void merge(Map<Key, IKStorageScopeItem> local, Map<Key, IKStorageScopeItem> storage) {
		Iterator<Entry<Key, IKStorageScopeItem>> it = local.entrySet().iterator();
		Entry<Key, IKStorageScopeItem> e;
		IKStorageScopeItem storageItem;
		while (it.hasNext()) {
			e = it.next();

			storageItem = storage.get(e.getKey());
			// this entry not exist in the storage
			if (storageItem == null) {
				if (!e.getValue().removed()) storage.put(e.getKey(), e.getValue());
			}
			// local is newer than storage
			else if (e.getValue().lastModified() > storageItem.lastModified()) {
				if (e.getValue().removed()) storage.remove(e.getKey());
				else {
					storage.put(e.getKey(), e.getValue());
				}
			}
			// local is older than storage is ignored?

		}

	}

	public static Map<Key, IKStorageScopeItem> cleanRemoved(Map<Key, IKStorageScopeItem> local) {
		Iterator<Entry<Key, IKStorageScopeItem>> it = local.entrySet().iterator();
		Entry<Key, IKStorageScopeItem> e;
		while (it.hasNext()) {
			e = it.next();
			if (e.getValue().removed()) local.remove(e.getKey());
		}
		return local;
	}

	public static Map<Key, IKStorageScopeItem> prepareToStore(Map<Key, IKStorageScopeItem> local, Object oStorage, long lastModified) throws PageException {
		// cached data changed in meantime
		if (oStorage instanceof IKStorageValue) {
			IKStorageValue storage = (IKStorageValue) oStorage;
			if (storage.lastModified() > lastModified) {
				Map<Key, IKStorageScopeItem> trg = storage.getValue();
				IKStorageScopeSupport.merge(local, trg);
				return trg;
			}
			else {
				return IKStorageScopeSupport.cleanRemoved(local);

			}
		}
		else if (oStorage instanceof byte[][]) {
			byte[][] barrr = (byte[][]) oStorage;
			if (IKStorageValue.toLong(barrr[1]) > lastModified) {
				if (barrr[0] == null || barrr[0].length == 0) return local;
				Map<Key, IKStorageScopeItem> trg = IKStorageValue.deserialize(barrr[0]);
				IKStorageScopeSupport.merge(local, trg);
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

	protected static DateTime doNowIfNull(Config config, DateTime dt) {
		if (dt == null) return new DateTimeImpl(config);
		return dt;
	}

	// protected abstract IKStorageValue loadData(PageContext pc, String appName, String name,String
	// strType,int type, Log log) throws PageException;

}