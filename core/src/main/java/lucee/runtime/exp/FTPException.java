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

import org.apache.commons.net.ftp.FTPClient;

import lucee.runtime.config.Config;
import lucee.runtime.net.ftp.AFTPClient;
import lucee.runtime.op.Caster;

public class FTPException extends ApplicationException {

	private int code;
	private String msg;

	public FTPException(String action, FTPClient client) {
		super("action [" + action + "] from tag ftp failed", client.getReplyString());
		// setAdditional("ReplyCode",Caster.toDouble(client.getReplyCode()));
		// setAdditional("ReplyMessage",client.getReplyString());
		code = client.getReplyCode();
		msg = client.getReplyString();
	}

	public FTPException(String action, AFTPClient client) {
		super("action [" + action + "] from tag ftp failed", client.getReplyString());
		// setAdditional("ReplyCode",Caster.toDouble(client.getReplyCode()));
		// setAdditional("ReplyMessage",client.getReplyString());
		code = client.getReplyCode();
		msg = client.getReplyString();
	}

	@Override
	public CatchBlock getCatchBlock(Config config) {
		CatchBlock cb = super.getCatchBlock(config);
		cb.setEL("Cause", msg);
		cb.setEL("Code", Caster.toDouble(code));
		return cb;
	}
}