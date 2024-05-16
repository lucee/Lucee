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

/**
 * DTO of a single Mailserver
 */
public interface Server {

	public static final int DEFAULT_PORT = 25;

	/**
	 * @return Returns the password.
	 */
	public abstract String getPassword();

	/**
	 * @return Returns the port.
	 */
	public abstract int getPort();

	/**
	 * @return Returns the server.
	 */
	public abstract String getHostName();

	/**
	 * @return Returns the username.
	 */
	public abstract String getUsername();

	/**
	 * @return Returns if it has authentication or not
	 */
	public abstract boolean hasAuthentication();

	/**
	 * @return clone the DataSource as ReadOnly
	 */
	public abstract Server cloneReadOnly();

	/**
	 * @return Returns the readOnly.
	 */
	public abstract boolean isReadOnly();

	/**
	 * verify the server properties
	 * 
	 * @return is ok
	 * @throws SMTPException SMTP Exception
	 */
	public abstract boolean verify() throws SMTPException;

	/**
	 * @return is tls
	 */
	public abstract boolean isTLS();

	/**
	 * @return is ssl
	 */
	public abstract boolean isSSL();

}