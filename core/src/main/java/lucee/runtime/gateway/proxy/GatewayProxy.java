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
package lucee.runtime.gateway.proxy;

import java.io.IOException;
import java.util.Map;

import lucee.runtime.gateway.Gateway;
import lucee.runtime.gateway.GatewayEngine;

public class GatewayProxy implements Gateway {

	private final Gateway gateway;

	public GatewayProxy(Gateway gateway) {
		this.gateway = gateway;
	}

	@Override
	public void init(GatewayEngine engine, String id, String cfcPath, Map config) throws IOException {
		gateway.init(engine, id, cfcPath, config);
	}

	@Override
	public String getId() {
		return gateway.getId();
	}

	@Override
	public String sendMessage(Map data) throws IOException {
		return gateway.sendMessage(data);
	}

	@Override
	public Object getHelper() {
		return gateway.getHelper();
	}

	@Override
	public void doStart() throws IOException {
		gateway.doStart();
	}

	@Override
	public void doStop() throws IOException {
		gateway.doStop();
	}

	@Override
	public void doRestart() throws IOException {
		gateway.doRestart();
	}

	@Override
	public int getState() {
		return gateway.getState();
	}

	public Gateway getGateway() {
		return gateway;
	}
}