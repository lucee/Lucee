/**
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
package lucee.runtime.net.http;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

/**
 * Minimal in-memory HttpSession for requests without a servlet container session (for example
 * internalRequest), so applications with sessionType "jee" get a real session id.
 */
public final class HttpSessionDummy implements HttpSession {

	private static final Map<String, HttpSessionDummy> sessions = new ConcurrentHashMap<>();
	private static final SecureRandom random = new SecureRandom();
	private static final char[] HEX = "0123456789ABCDEF".toCharArray();

	private volatile String id;
	private final long creationTime;
	private final Map<String, Object> attributes = new ConcurrentHashMap<>();
	private volatile long lastAccessedTime;
	private volatile int maxInactiveInterval = 1800;
	private volatile boolean isNew = true;
	private volatile boolean valid = true;

	private HttpSessionDummy() {
		this.id = createId();
		this.creationTime = this.lastAccessedTime = System.currentTimeMillis();
	}

	private static String createId() {
		byte[] bytes = new byte[16];
		random.nextBytes(bytes);
		char[] chars = new char[32];
		for (int i = 0; i < bytes.length; i++) {
			chars[i * 2] = HEX[(bytes[i] >> 4) & 0xF];
			chars[i * 2 + 1] = HEX[bytes[i] & 0xF];
		}
		return new String(chars);
	}

	/**
	 * creates a new session and registers it, so it can be found again with {@link #get(String)}
	 */
	public static HttpSessionDummy create() {
		removeExpired();
		HttpSessionDummy session = new HttpSessionDummy();
		sessions.put(session.id, session);
		return session;
	}

	/**
	 * returns the still valid session with the given id, or null
	 */
	public static HttpSessionDummy get(String id) {
		if (id == null || id.isEmpty()) return null;
		HttpSessionDummy session = sessions.get(id);
		if (session == null) return null;
		if (session.isExpired()) {
			session.expire();
			return null;
		}
		session.isNew = false;
		session.lastAccessedTime = System.currentTimeMillis();
		return session;
	}

	private static void removeExpired() {
		Iterator<HttpSessionDummy> it = sessions.values().iterator();
		HttpSessionDummy session;
		while (it.hasNext()) {
			session = it.next();
			if (session.isExpired()) session.expire();
		}
	}

	/**
	 * gives the session a new id (used by sessionRotate), the attributes stay untouched
	 * 
	 * @return the new id
	 */
	public synchronized String changeId() {
		checkValid();
		String newId = createId();
		sessions.remove(id, this);
		id = newId;
		sessions.put(newId, this);
		return newId;
	}

	/**
	 * @return false once the session is invalidated or expired
	 */
	public boolean isValid() {
		return valid;
	}

	private boolean isExpired() {
		return !valid || (maxInactiveInterval > 0 && System.currentTimeMillis() - lastAccessedTime > maxInactiveInterval * 1000L);
	}

	private void checkValid() {
		if (!valid) throw new IllegalStateException("session [" + id + "] is invalidated");
	}

	@Override
	public long getCreationTime() {
		checkValid();
		return creationTime;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getLastAccessedTime() {
		checkValid();
		return lastAccessedTime;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	@Override
	public Object getAttribute(String name) {
		checkValid();
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		checkValid();
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public void setAttribute(String name, Object value) {
		checkValid();
		if (value == null) {
			removeAttribute(name);
			return;
		}
		Object old = attributes.put(name, value);
		if (old == value) return;
		if (value instanceof HttpSessionBindingListener) ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
		if (old instanceof HttpSessionBindingListener) ((HttpSessionBindingListener) old).valueUnbound(new HttpSessionBindingEvent(this, name, old));
	}

	@Override
	public void removeAttribute(String name) {
		checkValid();
		Object old = attributes.remove(name);
		if (old instanceof HttpSessionBindingListener) ((HttpSessionBindingListener) old).valueUnbound(new HttpSessionBindingEvent(this, name, old));
	}

	@Override
	public void invalidate() {
		checkValid();
		expire();
	}

	private synchronized void expire() {
		if (!valid) return;
		valid = false;
		sessions.remove(id, this);
		Iterator<Map.Entry<String, Object>> it = attributes.entrySet().iterator();
		Map.Entry<String, Object> e;
		while (it.hasNext()) {
			e = it.next();
			it.remove();
			if (e.getValue() instanceof HttpSessionBindingListener) {
				((HttpSessionBindingListener) e.getValue()).valueUnbound(new HttpSessionBindingEvent(this, e.getKey(), e.getValue()));
			}
		}
	}

	@Override
	public boolean isNew() {
		checkValid();
		return isNew;
	}
}
