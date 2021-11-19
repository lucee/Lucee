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
package lucee.runtime.tag;

import lucee.runtime.net.mail.MailClient;

/**
 * Retrieves and deletes e-mail messages from a POP mail server.
 */
public final class Pop extends _Mail {

	@Override
	protected int getDefaultPort() {
		if (isSecure()) return 995;
		return 110;
	}

	@Override
	protected String getTagName() {
		return "Pop";
	}

	@Override
	protected int getType() {
		return MailClient.TYPE_POP3;
	}
}