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
package lucee.runtime.gateway;

import lucee.commons.lang.ExceptionUtil;

public class GatewayThread extends Thread {

	public static final int START = 0;
	public static final int STOP = 1;
	public static final int RESTART = 2;

	private GatewayEngine engine;
	private Gateway gateway;
	private int action;

	public GatewayThread(GatewayEngine engine, Gateway gateway, int action) {
		this.engine = engine;
		this.gateway = gateway;
		this.action = action;
		this.setName("EventGateway-" + gateway.getId()); // name the thread
		if (gateway instanceof GatewaySupport) ((GatewaySupport) gateway).setThread(this);
	}

	@Override
	public void run() {
		// MUST handle timeout
		try {
			if (action == START) gateway.doStart();
			else if (action == STOP) gateway.doStop();
			else if (action == RESTART) gateway.doRestart();
		}
		catch (Throwable ge) {
			ExceptionUtil.rethrowIfNecessary(ge);
			engine.log(gateway, GatewayEngine.LOGLEVEL_ERROR, ge.getMessage());
		}
	}
}