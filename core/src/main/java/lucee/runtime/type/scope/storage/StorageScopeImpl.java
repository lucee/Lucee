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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.scope.CSRFTokenSupport;
import lucee.runtime.type.scope.util.ScopeUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;

public abstract class StorageScopeImpl extends StructSupport implements StorageScope, CSRFTokenSupport {

	public static Collection.Key CFID = KeyConstants._cfid;
	public static Collection.Key CFTOKEN = KeyConstants._cftoken;
	public static Collection.Key URLTOKEN = KeyConstants._urltoken;
	public static Collection.Key LASTVISIT = KeyConstants._lastvisit;
	public static Collection.Key HITCOUNT = KeyConstants._hitcount;
	public static Collection.Key TIMECREATED = KeyConstants._timecreated;
	public static Collection.Key SESSION_ID = KeyConstants._sessionid;

	private static int _id = 0;
	private int id = 0;

	private static final long serialVersionUID = 7874930250042576053L;
	private static Set<Collection.Key> FIX_KEYS = new HashSet<Collection.Key>();
	static {
		FIX_KEYS.add(KeyConstants._cfid);
		FIX_KEYS.add(KeyConstants._cftoken);
		FIX_KEYS.add(KeyConstants._urltoken);
		FIX_KEYS.add(KeyConstants._lastvisit);
		FIX_KEYS.add(KeyConstants._hitcount);
		FIX_KEYS.add(KeyConstants._timecreated);
	}

	public static Set<Collection.Key> KEYS = new HashSet<Collection.Key>();
	static {
		KEYS.add(KeyConstants._cfid);
		KEYS.add(KeyConstants._cftoken);
		KEYS.add(KeyConstants._urltoken);
		KEYS.add(KeyConstants._lastvisit);
		KEYS.add(KeyConstants._hitcount);
		KEYS.add(KeyConstants._timecreated);
		KEYS.add(KeyConstants._sessionid);
	}

	protected static Set<Collection.Key> ignoreSet = new HashSet<Collection.Key>();
	static {
		ignoreSet.add(KeyConstants._cfid);
		ignoreSet.add(KeyConstants._cftoken);
		ignoreSet.add(KeyConstants._urltoken);
	}

	protected boolean isinit = true;
	protected Struct sct;
	protected long lastvisit;
	protected DateTime _lastvisit;
	protected int hitcount = 0;
	protected DateTime timecreated;
	private boolean hasChanges = false;
	private String strType;
	private int type;
	private long timeSpan = -1;
	private String storage;

	private final Map<Collection.Key, String> tokens = new ConcurrentHashMap<Collection.Key, String>();

	/**
	 * Constructor of the class
	 * 
	 * @param sct
	 * @param timecreated
	 * @param _lastvisit
	 * @param lastvisit
	 * @param hitcount
	 */
	public StorageScopeImpl(Struct sct, DateTime timecreated, DateTime _lastvisit, long lastvisit, int hitcount, String strType, int type) {
		this.sct = sct;
		this.timecreated = timecreated;
		if (_lastvisit == null) this._lastvisit = timecreated;
		else this._lastvisit = _lastvisit;

		if (lastvisit == -1) this.lastvisit = this._lastvisit.getTime();
		else this.lastvisit = lastvisit;

		this.hitcount = hitcount;
		this.strType = strType;
		this.type = type;
		id = ++_id;
	}

	/**
	 * Constructor of the class
	 * 
	 * @param other
	 * @param deepCopy
	 */
	public StorageScopeImpl(StorageScopeImpl other, boolean deepCopy) {
		this.sct = (Struct) Duplicator.duplicate(other.sct, deepCopy);
		this.timecreated = other.timecreated;
		this._lastvisit = other._lastvisit;
		this.hitcount = other.hitcount;
		this.isinit = other.isinit;
		this.lastvisit = other.lastvisit;
		this.strType = other.strType;
		this.type = other.type;
		this.timeSpan = other.timeSpan;
		id = ++_id;
	}

	@Override
	public void touchBeforeRequest(PageContext pc) {

		hasChanges = false;
		setTimeSpan(pc);

		// lastvisit=System.currentTimeMillis();
		if (sct == null) sct = new StructImpl();
		sct.setEL(KeyConstants._cfid, pc.getCFID());
		sct.setEL(KeyConstants._cftoken, pc.getCFToken());
		sct.setEL(URLTOKEN, pc.getURLToken());
		sct.setEL(LASTVISIT, _lastvisit);
		_lastvisit = new DateTimeImpl(pc.getConfig());
		lastvisit = System.currentTimeMillis();

		if (type == SCOPE_CLIENT) {
			sct.setEL(HITCOUNT, new Double(hitcount++));
		}
		else {
			sct.setEL(SESSION_ID, pc.getApplicationContext().getName() + "_" + pc.getCFID() + "_" + pc.getCFToken());
		}
		sct.setEL(TIMECREATED, timecreated);
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

	@Override
	public final void initialize(PageContext pc) {
		// StorageScopes need only request initialisation no global init, they are not reused;
	}

	@Override
	public void touchAfterRequest(PageContext pc) {

		sct.setEL(LASTVISIT, _lastvisit);
		sct.setEL(TIMECREATED, timecreated);

		if (type == SCOPE_CLIENT) {
			sct.setEL(HITCOUNT, new Double(hitcount));
		}
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
		if (sct.size() == (type == SCOPE_CLIENT ? 6 : 5) && sct.containsKey(URLTOKEN) && sct.containsKey(KeyConstants._cftoken) && sct.containsKey(KeyConstants._cfid)) {
			return false;
		}
		return true;
	}

	@Override
	public void clear() {
		sct.clear();
	}

	@Override
	public final boolean containsKey(Key key) {
		return sct.containsKey(key);
	}

	@Override
	public final boolean containsKey(PageContext pc, Key key) {
		return sct instanceof StructSupport ? ((StructSupport) sct).containsKey(pc, key) : sct.containsKey(key);
	}

	@Override
	public final Object get(Key key) throws PageException {
		return sct.get(key);
	}

	@Override
	public final Object get(PageContext pc, Key key) throws PageException {
		return sct.get(key);
	}

	@Override
	public final Object get(Key key, Object defaultValue) {
		return sct.get(key, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Key key, Object defaultValue) {
		return sct.get(pc, key, defaultValue);
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return sct.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return sct.keysAsStringIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return sct.entryIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return sct.valueIterator();
	}

	@Override
	public lucee.runtime.type.Collection.Key[] keys() {
		return CollectionUtil.keys(this);
	}

	@Override
	public Object remove(Key key) throws PageException {
		hasChanges = true;
		return sct.remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		hasChanges = true;
		return sct.removeEL(key);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		hasChanges = true;
		return sct.set(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		hasChanges = true;
		return sct.setEL(key, value);
	}

	@Override
	public int size() {
		return sct.size();
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return sct.castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return sct.castToBoolean(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return sct.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return sct.castToDateTime(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return sct.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return sct.castToDoubleValue(defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return sct.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return sct.castToString(defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return sct.compareTo(b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return sct.compareTo(dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return sct.compareTo(d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return sct.compareTo(str);
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
	public final void store(Config config) {
		store(ThreadLocalPageContext.get(config));
	}

	@Override
	public final void unstore(Config config) {
		unstore(ThreadLocalPageContext.get(config));
	}

	public void store(PageContext pc) {}

	public void unstore(PageContext pc) {}

	/**
	 * @return the hasChanges
	 */
	public boolean hasChanges() {
		return hasChanges;
	}

	@Override
	public boolean containsValue(Object value) {
		return sct.containsValue(value);
	}

	@Override
	public java.util.Collection values() {
		return sct.values();
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
}