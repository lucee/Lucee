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
package lucee.runtime.net.mail;

import java.io.Serializable;
import java.util.ArrayList;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.op.Caster;

/**
 * 
 */
public final class ServerImpl implements Server, Serializable {

	private static final long serialVersionUID = -3352908216814744100L;

	public static final int TYPE_GLOBAL = 1;
	public static final int TYPE_LOCAL = 2;

	private final int id;
	private String hostName;
	private String username;
	private String password;
	private int port = DEFAULT_PORT;
	private boolean readOnly = false;
	private boolean tls;
	private boolean ssl;
	private final boolean reuse;
	private final long life;
	private final long idle;
	private final int type;

	public static ServerImpl getInstance(String host, int defaultPort, String defaultUsername, String defaultPassword, long defaultLifeTimespan, long defaultIdleTimespan,
			boolean defaultTls, boolean defaultSsl) throws MailException {

		String userpass, user = defaultUsername, pass = defaultPassword, tmp;
		int port = defaultPort;

		// [user:password@]server[:port]
		int index = host.indexOf('@');

		// username:password
		if (index != -1) {
			userpass = host.substring(0, index);
			host = host.substring(index + 1);

			index = userpass.indexOf(':');
			if (index != -1) {
				user = userpass.substring(0, index).trim();
				pass = userpass.substring(index + 1).trim();
			}
			else user = userpass.trim();
		}

		// server:port
		index = host.indexOf(':');
		if (index != -1) {
			tmp = host.substring(index + 1).trim();
			if (!StringUtil.isEmpty(tmp)) {
				try {
					port = Caster.toIntValue(tmp);
				}
				catch (ExpressionException e) {
					throw new MailException("Mail server port definition is invalid [" + tmp + "]");
				}
			}
			host = host.substring(0, index).trim();
		}
		else host = host.trim();

		return new ServerImpl(-1, host, port, user, pass, defaultLifeTimespan, defaultIdleTimespan, defaultTls, defaultSsl, true, TYPE_LOCAL);
	}

	/*
	 * public ServerImpl(String server,int port) { this.hostName=server; this.port=port; }
	 */

	public ServerImpl(int id, String hostName, int port, String username, String password, long lifeTimespan, long idleTimespan, boolean tls, boolean ssl, boolean reuseConnections,
			int type) {
		this.id = id;
		this.hostName = hostName;
		this.username = username;
		this.password = password;
		this.life = lifeTimespan;
		this.idle = idleTimespan;
		this.port = port;
		this.tls = tls;
		this.ssl = ssl;
		this.reuse = reuseConnections;
		this.type = type;
	}

	@Override
	public String getPassword() {
		if (password == null && hasAuthentication()) return "";
		return password;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public String getHostName() {
		return hostName;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean hasAuthentication() {
		return username != null && username.length() > 0;
	}

	@Override
	public String toString() {
		if (username != null) {
			return username + ":" + password + "@" + hostName + ":" + port;
		}
		return hostName + ":" + port + ":" + ssl + ":" + tls + ":" + idle + ":" + life;
	}

	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}

	@Override
	public Server cloneReadOnly() {
		ServerImpl s = new ServerImpl(id, hostName, port, username, password, life, idle, tls, ssl, reuse, type);
		s.readOnly = true;
		return s;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public boolean verify() throws SMTPException {
		return SMTPVerifier.verify(hostName, username, password, port);
	}

	@Override
	public boolean isTLS() {
		return tls;
	}

	@Override
	public boolean isSSL() {
		return ssl;
	}

	public void setSSL(boolean ssl) {
		this.ssl = ssl;
	}

	public void setTLS(boolean tls) {
		this.tls = tls;
	}

	public long getLifeTimeSpan() {
		return life;
	}

	public long getIdleTimeSpan() {
		return idle;
	}

	public int getId() { // FUTURE add to interface
		return id;
	}

	public int getType() { // FUTURE add to interface
		return type;
	}

	public boolean reuseConnections() {
		return reuse;
	}

	public static lucee.runtime.net.mail.Server[] merge(lucee.runtime.net.mail.Server[] arr1, lucee.runtime.net.mail.Server[] arr2) {
		ArrayList<lucee.runtime.net.mail.Server> result = new ArrayList<Server>();

		// first we fill it with the left array
		for (int i = 0; i < arr1.length; i++) {
			result.add(arr1[i]);
		}

		// now we fill the second array, but only the one not existing yet
		for (int i = 0; i < arr2.length; i++) {
			if (!result.contains(arr2[i])) result.add(arr2[i]);
		}
		return result.toArray(new lucee.runtime.net.mail.Server[result.size()]);
	}
}