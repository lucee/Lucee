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
package lucee.runtime.net.smtp;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import lucee.commons.lang.ExceptionUtil;

public class SMTPConnectionPool {

	private static Map<String, Stack<SessionAndTransport>> sessions = new HashMap<String, Stack<SessionAndTransport>>();

	public static SessionAndTransport getSessionAndTransport(Properties props, String key, Authenticator auth, long lifeTimespan, long idleTimespan) throws MessagingException {

		// Session
		SessionAndTransport sat = null;
		Stack<SessionAndTransport> satStack = getSATStack(key);
		sat = pop(satStack);

		// when sat still valid return it
		if (sat != null) {
			if (isValid(sat, lifeTimespan, idleTimespan)) {
				return sat.touch();
			}
			disconnect(sat.transport);
		}

		return new SessionAndTransport(key, props, auth, lifeTimespan, idleTimespan);
	}

	private static boolean isValid(SessionAndTransport sat, long lifeTimespan, long idleTimespan) {

		return (idleTimespan <= 0 || sat.lastAccess + idleTimespan > System.currentTimeMillis()) && (lifeTimespan <= 0 || sat.created + lifeTimespan > System.currentTimeMillis());
	}

	public static void releaseSessionAndTransport(SessionAndTransport sat) {
		getSATStack(sat.key).add(sat.touch());
	}

	public static String listSessions() {
		Iterator<Entry<String, Stack<SessionAndTransport>>> it = sessions.entrySet().iterator();
		Entry<String, Stack<SessionAndTransport>> entry;
		Stack<SessionAndTransport> stack;
		StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			entry = it.next();
			sb.append(entry.getKey()).append('\n');
			stack = entry.getValue();
			if (stack.isEmpty()) continue;
			listSessions(sb, stack);
		}
		return sb.toString();
	}

	private static void listSessions(StringBuilder sb, Stack<SessionAndTransport> stack) {
		Iterator<SessionAndTransport> it = stack.iterator();
		while (it.hasNext()) {
			SessionAndTransport sat = it.next();
			sb.append("- " + sat.key + ":" + new Date(sat.lastAccess)).append('\n');
		}
	}

	public static void closeSessions() {
		Iterator<Entry<String, Stack<SessionAndTransport>>> it = sessions.entrySet().iterator();
		Entry<String, Stack<SessionAndTransport>> entry;
		Stack<SessionAndTransport> oldStack;
		Stack<SessionAndTransport> newStack;
		while (it.hasNext()) {
			entry = it.next();
			oldStack = entry.getValue();
			if (oldStack.isEmpty()) continue;
			newStack = new Stack<SMTPConnectionPool.SessionAndTransport>();
			entry.setValue(newStack);
			closeSessions(oldStack, newStack);
		}
	}

	private static void closeSessions(Stack<SessionAndTransport> oldStack, Stack<SessionAndTransport> newStack) {
		SessionAndTransport sat;
		while ((sat = pop(oldStack)) != null) {
			if (!isValid(sat, sat.lifeTimespan, sat.idleTimespan)) {

				disconnect(sat.transport);
			}
			else newStack.add(sat);
		}
	}

	static void disconnect(Transport transport) {
		if (transport != null && transport.isConnected()) {
			try {
				transport.close();
			}
			catch (MessagingException e) {
			}
		}
	}

	private static Stack<SessionAndTransport> getSATStack(String key) {
		Stack<SessionAndTransport> stack;
		synchronized (sessions) {
			stack = sessions.get(key);
			if (stack == null) {
				stack = new Stack<SessionAndTransport>();
				sessions.put(key, stack);
			}
		}
		return stack;
	}

	private static Session createSession(String key, Properties props, Authenticator auth) {
		if (auth != null) return Session.getInstance(props, auth);
		return Session.getInstance(props);
	}

	private static SessionAndTransport pop(Stack<SessionAndTransport> satStack) {
		try {
			return satStack.pop();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return null;
	}

	public static class SessionAndTransport {
		public final Session session;
		public final Transport transport;
		public final String key;
		private long lastAccess;
		public final long created;
		public final long lifeTimespan;
		public final long idleTimespan;

		SessionAndTransport(String key, Properties props, Authenticator auth, long lifeTimespan, long idleTimespan) throws NoSuchProviderException {
			this.key = key;
			this.session = createSession(key, props, auth);
			this.transport = session.getTransport("smtp");
			this.created = System.currentTimeMillis();
			this.lifeTimespan = lifeTimespan;
			this.idleTimespan = idleTimespan;
			touch();
		}

		private SessionAndTransport touch() {
			this.lastAccess = System.currentTimeMillis();
			return this;
		}
	}

}