/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.net.ftp;

/**
 * represent a ftp connection
 */
public interface FTPConnection {

	/**
	 * @return Returns the name.
	 */
	public abstract String getName();

	/**
	 * @return Returns the password.
	 */
	public abstract String getPassword();

	/**
	 * @return Returns the server.
	 */
	public abstract String getServer();

	/**
	 * @return Returns the username.
	 */
	public abstract String getUsername();

	/**
	 * @return returns if has logindata or not
	 */
	public abstract boolean hasLoginData();

	/**
	 * @return has name
	 */
	public abstract boolean hasName();

	/**
	 * @return Returns the port.
	 */
	public abstract int getPort();

	/**
	 * @return Returns the timeout.
	 */
	public abstract int getTimeout();

	/**
	 * @return Returns the transferMode.
	 */
	public abstract short getTransferMode();

	/**
	 * @return Returns the passive.
	 */
	public abstract boolean isPassive();

	/**
	 * @param conn
	 * @return has equal login
	 */
	public abstract boolean loginEquals(FTPConnection conn);

	/**
	 * @return Returns the proxyserver.
	 */
	public String getProxyServer();

	public int getProxyPort();

	/**
	 * return the proxy username
	 * 
	 * @return proxy username
	 */
	public String getProxyUser();

	/**
	 * return the proxy password
	 * 
	 * @return proxy password
	 */
	public String getProxyPassword();

	public abstract boolean secure();

	public abstract boolean getStopOnError();

	public abstract String getFingerprint();

	public abstract String getKey();

	public abstract String getPassphrase();
}