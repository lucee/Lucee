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

import java.util.Map;

import lucee.commons.collection.MapFactory;

public final class SMTPExceptionImpl extends SMTPException {

	private static Map<String, String> codes = MapFactory.<String, String>getConcurrentMap();

	static {
		codes.put("211", "System status, or system help reply");
		codes.put("214", " Help message (Information on how to use the receiver or the meaning of a particular non-standard command; this reply is useful only to the human user)");
		codes.put("220", "Service ready");
		codes.put("221", "Service closing transmission channel");
		codes.put("250", "Requested mail action okay, completed");
		codes.put("251", "User not local; will forward to");
		codes.put("354", "Start mail input; end with .");
		codes.put("421", "Service not available, closing transmission channel (This may be a reply to any command if the service knows it must shut down) ");
		codes.put("450", "Requested mail action not taken: mailbox unavailable (E.g., mailbox busy)");
		codes.put("451", "Requested action aborted: local error in processing");
		codes.put("452", "Requested action not taken: insufficient system storage");
		codes.put("500", "Syntax error, command unrecognized (This may include errors such as command line too long)");
		codes.put("501", "Syntax error in parameters or arguments");
		codes.put("502", "Command not implemented");
		codes.put("503", "Bad sequence of commands");
		codes.put("504", "Command parameter not implemented");
		codes.put("550", "Requested action not taken: mailbox unavailable (E.g., mailbox not found, no access)");
		codes.put("551", "User not local; please try");
		codes.put("552", "Requested mail action aborted: exceeded storage allocation");
		codes.put("553", "Requested action not taken: mailbox name not allowed (E.g., mailbox syntax incorrect) ");
		codes.put("554", "Transaction failed (Or, in the case of a connection-opening response, \"No SMTP service here\")");
		codes.put("252", "Cannot VRFY user, but will accept message and attempt delivery");
	}

	public SMTPExceptionImpl(String message) {
		super(message);
	}

	public SMTPExceptionImpl(int code) {
		this(doMessage(code));
	}

	private static String doMessage(int code) {
		String message = codes.get(String.valueOf(code));
		if (message == null) message = "SMTP Code " + code;
		else message = code + " - " + message;
		return message;
	}
}