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

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.apache.commons.mail.DefaultAuthenticator;

import lucee.commons.lang.StringUtil;

/**
 * SMTP Server verifier
 */
public final class SMTPVerifier {

	/**
	 * verify mail server
	 * 
	 * @param host
	 * @param username
	 * @param password
	 * @param port
	 * @return are the setting ok
	 * @throws SMTPException
	 */
	public static boolean verify(String host, String username, String password, int port) throws SMTPException {
		try {
			return _verify(host, username, password, port);
		}
		catch (MessagingException e) {

			// check user
			if (!StringUtil.isEmpty(username)) {
				try {
					_verify(host, null, null, port);
					throw new SMTPExceptionImpl("Cannot connect to mail server, authentication settings are invalid");
				}
				catch (MessagingException e1) {

				}
			}
			// check port
			if (port > 0 && port != 25) {
				try {
					_verify(host, null, null, 25);
					throw new SMTPExceptionImpl("Cannot connect to mail server, port definition is invalid");
				}
				catch (MessagingException e1) {}
			}

			throw new SMTPExceptionImpl("can't connect to mail server");
		}
	}

	private static boolean _verify(String host, String username, String password, int port) throws MessagingException {
		boolean hasAuth = !StringUtil.isEmpty(username);

		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		if (hasAuth) props.put("mail.smtp.auth", "true");
		if (hasAuth) props.put("mail.smtp.user", username);
		if (hasAuth) props.put("mail.transport.connect-timeout", "30");
		if (port > 0) props.put("mail.smtp.port", String.valueOf(port));

		Authenticator auth = null;
		if (hasAuth) auth = new DefaultAuthenticator(username, password);
		Session session = Session.getInstance(props, auth);

		Transport transport = session.getTransport("smtp");
		if (hasAuth) transport.connect(host, username, password);
		else transport.connect();
		boolean rtn = transport.isConnected();
		transport.close();
		return rtn;

	}
}
