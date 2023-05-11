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
package lucee.commons.io.res.type.ftp;

import lucee.commons.lang.StringUtil;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;

public final class FTPConnectionData {

	public String username = "";
	public String password = "";
	public String host = "localhost";
	public int port = 21;

	private boolean customHostPort;
	private boolean customUserPass;

	ProxyData data;

	public FTPConnectionData() {
	}

	public FTPConnectionData(String host, String username, String password, int port) {
		this(host, username, password, port, false, false);
	}

	public FTPConnectionData(String host, String username, String password, int port, boolean customHostPort, boolean customUserPass) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.port = port;
		this.customHostPort = customHostPort;
		this.customUserPass = customUserPass;
	}

	public static DataAndPath load(FTPConnectionData base, String path) {

		String username = base == null ? "" : base.username;
		String password = base == null ? "" : base.password;
		String host = base == null ? "localhost" : base.host;
		int port = base == null ? 21 : base.port;

		boolean customUserPass = false;
		boolean customHostPort = false;

		int atIndex = path.indexOf('@');
		int slashIndex = path.indexOf('/');
		if (slashIndex == -1) {
			slashIndex = path.length();
			path += "/";
		}
		int index;

		// username/password
		if (atIndex != -1) {
			customUserPass = true;
			index = path.indexOf(':');
			if (index != -1 && index < atIndex) {
				username = path.substring(0, index);
				password = path.substring(index + 1, atIndex);
			}
			else {
				username = path.substring(0, atIndex);
				password = "";
			}
		}
		// host port
		if (slashIndex > atIndex + 1) {
			customHostPort = true;
			index = path.indexOf(':', atIndex + 1);
			if (index != -1 && index > atIndex && index < slashIndex) {
				host = path.substring(atIndex + 1, index);
				port = Integer.parseInt(path.substring(index + 1, slashIndex));
			}
			else {
				host = path.substring(atIndex + 1, slashIndex);
				port = 21;
			}
		}
		return new DataAndPath(new FTPConnectionData(host, username, password, port, customHostPort, customUserPass), path.substring(slashIndex));
	}

	public static class DataAndPath {

		public FTPConnectionData data;
		public String path;

		public DataAndPath(FTPConnectionData data, String path) {
			this.data = data;
			this.path = path;
		}

	}

	@Override
	public String toString() {
		return new StringBuilder().append("username:").append(username).append(";password:").append(password).append(";hostname:").append(host).append(";port:").append(port)
				.toString();
	}

	public String key() {
		StringBuilder sb = new StringBuilder();

		if (!StringUtil.isEmpty(username) && customUserPass) sb.append(username).append(":").append(password).append("@");

		if (customHostPort) sb.append(host).append(_port());

		return sb.toString();
	}

	private String _port() {
		if (port > 0) return ":" + port;
		return "";
	}

	public boolean hasProxyData() {
		return ProxyDataImpl.isValid(data);
	}

	public ProxyData getProxyData() {
		return data;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FTPConnectionData)) return false;
		return toString().equals(((FTPConnectionData) obj).toString());
	}
}