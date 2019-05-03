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
package lucee.runtime.monitor;

import lucee.runtime.config.ConfigServer;

public abstract class MonitorWrap implements Monitor {
	private static final Object[] PARAMS_LOG = new Object[0];

	private ConfigServer configServer;
	protected Object monitor;
	private String name;
	private short type;
	private boolean logEnabled;

	public MonitorWrap(Object monitor, short type) {
		this.monitor = monitor;
		this.type = type;
	}

	@Override
	public void init(ConfigServer configServer, String name, boolean logEnabled) {
		this.configServer = configServer;
		this.name = name;
		this.logEnabled = logEnabled;
	}

	@Override
	public short getType() {
		return type;
	}

	public Object getMonitor() {
		return monitor;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isLogEnabled() {
		return logEnabled;
	}

	@Override
	public Class getClazz() {
		return monitor.getClass();
	}

}