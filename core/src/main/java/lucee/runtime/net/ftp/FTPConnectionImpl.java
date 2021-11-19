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

package lucee.runtime.net.ftp;

/**
 *  
 */
public final class FTPConnectionImpl implements FTPConnection {

	private final String name;
	private final String server;
	private final String username;
	private final String password;
	private final int port;
	private final int timeout;
	private short transferMode;
	private final boolean passive;
	private final String proxyserver;
	private final int proxyport;
	private final String proxyuser;
	private final String proxypassword;
	private final String fingerprint;
	private final boolean stopOnError;
	private final boolean secure;
	private final String key;
	private final String passphrase;

	/**
	 *
	 * @param name
	 * @param server
	 * @param username
	 * @param password
	 * @param port
	 * @param timeout
	 * @param transferMode
	 * @param passive
	 * @param proxyserver
	 * @param proxyport
	 * @param proxyuser
	 * @param proxypassword
	 * @param fingerprint
	 * @param stopOnError
	 * @param secure
	 * @param key
	 * @param passphrase
	 */
	public FTPConnectionImpl(String name, String server, String username, String password, int port, int timeout, short transferMode, boolean passive, String proxyserver,
			int proxyport, String proxyuser, String proxypassword, String fingerprint, boolean stopOnError, boolean secure, String key, String passphrase) {

		this.name = (name == null) ? null : name.toLowerCase().trim();
		this.server = server;
		this.username = username;
		this.password = password;
		this.port = port;
		this.timeout = timeout;
		this.transferMode = transferMode;
		this.passive = passive;

		this.proxyserver = proxyserver;
		this.proxyport = proxyport;
		this.proxyuser = proxyuser;
		this.proxypassword = proxypassword;
		this.fingerprint = fingerprint;
		this.stopOnError = stopOnError;
		this.secure = secure;

		this.key = key;
		this.passphrase = passphrase;
	}

	/**
	 * Calls the first constructor and sets key and passphrase to null
	 *
	 * @param name
	 * @param server
	 * @param username
	 * @param password
	 * @param port
	 * @param timeout
	 * @param transferMode
	 * @param passive
	 * @param proxyserver
	 * @param proxyport
	 * @param proxyuser
	 * @param proxypassword
	 * @param fingerprint
	 * @param stopOnError
	 * @param secure
	 */
	public FTPConnectionImpl(String name, String server, String username, String password, int port, int timeout, short transferMode, boolean passive, String proxyserver,
			int proxyport, String proxyuser, String proxypassword, String fingerprint, boolean stopOnError, boolean secure) {

		this(name, server, username, password, port, timeout, transferMode, passive, proxyserver, proxyport, proxyuser, proxypassword, fingerprint, stopOnError, secure, null,
				null);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getServer() {
		return server;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean hasLoginData() {
		return server != null;// && username!=null && password!=null;
	}

	@Override
	public boolean hasName() {
		return name != null;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

	@Override
	public short getTransferMode() {
		return transferMode;
	}

	public void setTransferMode(short transferMode) {
		this.transferMode = transferMode;
	}

	@Override
	public boolean isPassive() {
		return passive;
	}

	@Override
	public boolean loginEquals(FTPConnection conn) {
		return server.equalsIgnoreCase(conn.getServer()) && username.equals(conn.getUsername()) && password.equals(conn.getPassword());
	}

	@Override
	public String getProxyPassword() {
		return proxypassword;
	}

	@Override
	public int getProxyPort() {
		return proxyport;
	}

	@Override
	public String getProxyServer() {
		return proxyserver;
	}

	@Override
	public String getProxyUser() {
		return proxyuser;
	}

	public boolean equal(Object o) {
		if (!(o instanceof FTPConnection)) return false;
		FTPConnection other = (FTPConnection) o;

		if (neq(other.getPassword(), getPassword())) return false;
		if (neq(other.getProxyPassword(), getProxyPassword())) return false;
		if (neq(other.getProxyServer(), getProxyServer())) return false;
		if (neq(other.getProxyUser(), getProxyUser())) return false;
		if (neq(other.getServer(), getServer())) return false;
		if (neq(other.getUsername(), getUsername())) return false;

		if (other.getPort() != getPort()) return false;
		if (other.getProxyPort() != getProxyPort()) return false;
		// if(other.getTimeout()!=getTimeout()) return false;
		if (other.getTransferMode() != getTransferMode()) return false;

		return true;
	}

	private boolean neq(String left, String right) {
		if (left == null) left = "";
		if (right == null) right = "";

		return !left.equals(right);
	}

	@Override
	public boolean secure() {
		return secure;
	}

	@Override
	public boolean getStopOnError() {
		return stopOnError;
	}

	@Override
	public String getFingerprint() {
		return fingerprint;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getPassphrase() {
		return passphrase;
	}

}