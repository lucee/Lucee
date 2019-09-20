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

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * This is a very simple authentication object that can be used for any transport needing basic
 * userName and password type authentication.
 *
 */
public final class SMTPAuthenticator extends Authenticator {
	/** Stores the login information for authentication */
	private PasswordAuthentication authentication;

	/**
	 * Default constructor
	 *
	 * @param userName user name to use when authentication is requested
	 * @param password password to use when authentication is requested
	 *
	 */
	public SMTPAuthenticator(String userName, String password) {
		this.authentication = new PasswordAuthentication(userName, password);
	}

	/**
	 * Gets the authentication object that will be used to login to the mail server.
	 *
	 * @return A <code>PasswordAuthentication</code> object containing the login information.
	 *
	 */
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return this.authentication;
	}
}