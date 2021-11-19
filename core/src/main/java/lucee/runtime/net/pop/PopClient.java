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
package lucee.runtime.net.pop;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.sun.mail.pop3.POP3Folder;

import lucee.runtime.net.mail.MailClient;

public final class PopClient extends MailClient {

	public PopClient(String server, int port, String username, String password, boolean secure) {
		super(server, port, username, password, secure);
	}

	@Override
	protected String _getId(Folder folder, Message message) throws MessagingException {

		return ((POP3Folder) folder).getUID(message);
	}

	@Override
	protected String getTypeAsString() {
		return "pop3";
	}

	@Override
	protected int getType() {
		return TYPE_POP3;
	}

}