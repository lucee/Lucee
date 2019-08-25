/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.type.scope;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.storage.MemoryScope;
import lucee.runtime.type.scope.util.ScopeUtil;
import lucee.runtime.type.util.KeyConstants;

/**
 * 
 */
public final class JSession extends ScopeSupport implements Session, HttpSessionBindingListener, MemoryScope, CSRFTokenSupport {

	public static final Collection.Key SESSION_ID = KeyConstants._sessionid;
	private static Set<Collection.Key> FIX_KEYS = new HashSet<Collection.Key>();
	static {
		FIX_KEYS.add(KeyConstants._sessionid);
		FIX_KEYS.add(KeyConstants._urltoken);
	}

	private String name;
	private long timespan = -1;
	private transient HttpSession httpSession;
	private long lastAccess;
	private long created;
	private final Map<String, String> tokens = new StructImpl();

	/**
	 * constructor of the class
	 */
	public JSession() {
		super("session", SCOPE_SESSION, Struct.TYPE_LINKED);
		setDisplayName("Scope Session (Type JEE)");
		this.created = System.currentTimeMillis();
	}

	@Override
	public void touchBeforeRequest(PageContext pc) {

		ApplicationContext appContext = pc.getApplicationContext();
		timespan = appContext.getSessionTimeout().getMillis();
		this.name = appContext.getName();
		HttpSession hs = pc.getSession();
		String id = "";
		try {
			if (hs != null) this.httpSession = hs;
			if (httpSession != null) {
				id = httpSession.getId();
				int timeoutInSeconds = ((int) (timespan / 1000)) + 60;
				if (httpSession.getMaxInactiveInterval() < timeoutInSeconds) httpSession.setMaxInactiveInterval(timeoutInSeconds);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		lastAccess = System.currentTimeMillis();
		setEL(KeyConstants._sessionid, id);
		setEL(KeyConstants._urltoken, "CFID=" + pc.getCFID() + "&CFTOKEN=" + pc.getCFToken() + "&jsessionid=" + id);
	}

	@Override
	public void touchAfterRequest(PageContext pc) {

	}

	@Override
	public void release(PageContext pc) {
		if (httpSession != null) {
			try {
				Object key;
				Enumeration e = httpSession.getAttributeNames();
				while (e.hasMoreElements()) {
					// TODO set inative time new
					key = e.nextElement();
					if (key.equals(name)) httpSession.removeAttribute(name);
				}
				name = null;
				timespan = -1;
				httpSession = null;
				lastAccess = -1;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		super.release(pc);
	}

	@Override
	public long getLastAccess() {
		return lastAccess;
	}

	@Override
	public long getTimeSpan() {
		return timespan;
	}

	@Override
	public boolean isExpired() {
		return (getLastAccess() + getTimeSpan()) < System.currentTimeMillis();
	}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {

	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		clear();
	}

	@Override
	public void touch() {
		lastAccess = System.currentTimeMillis();
	}

	@Override
	public long getCreated() {
		return created;
	}

	@Override
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
	public void resetEnv(PageContext pc) {
		created = System.currentTimeMillis();
		lastAccess = System.currentTimeMillis();
		touchBeforeRequest(pc);
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