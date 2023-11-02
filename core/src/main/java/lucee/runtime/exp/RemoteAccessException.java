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
package lucee.runtime.exp;

import lucee.runtime.config.RemoteClient;

public class RemoteAccessException extends ApplicationException {

	public RemoteAccessException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RemoteAccessException(RemoteClient client, PageException pe) {
		super(createMessage(client, pe));
	}

	private static String createMessage(RemoteClient client, PageException pe) {

		return "Recieved the exception [" + pe.getMessage() + "] while accessing the remote client [" + client.getUrl() + "]";
	}

}