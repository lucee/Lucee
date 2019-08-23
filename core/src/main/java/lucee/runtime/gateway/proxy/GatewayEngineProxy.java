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

import java.util.Map;

import lucee.runtime.gateway.Gateway;
import lucee.runtime.gateway.GatewayEngine;
import lucee.runtime.gateway.GatewayEngineImpl;

public class GatewayEngineProxy implements GatewayEngine {

	private GatewayEngineImpl engine;

	public GatewayEngineProxy(GatewayEngineImpl engine) {
		this.engine = engine;
	}

	@Override
	public boolean invokeListener(Gateway gateway, String method, Map data) {
		return engine.invokeListener(gateway.getId(), method, data);
	}

	@Override
	public void log(Gateway gateway, int level, String message) {
		engine.log(gateway.getId(), level, message);
	}

	public GatewayEngineImpl getEngine() {
		return engine;
	}

}