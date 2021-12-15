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

import java.io.IOException;
import java.util.Map;

import lucee.commons.io.log.Log;
import lucee.commons.lang.PageContextThread;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Query;

public class AsyncRequestMonitor implements RequestMonitorPro {

	private RequestMonitorPro monitor;
	private boolean logEnabled;

	public AsyncRequestMonitor(RequestMonitorPro monitor) {
		this.monitor = monitor;
	}

	@Override
	public void init(ConfigServer configServer, String name, boolean logEnabled) {
		monitor.init(configServer, name, logEnabled);
		this.logEnabled = logEnabled;
	}

	@Override
	public short getType() {
		return monitor.getType();
	}

	@Override
	public String getName() {
		return monitor.getName();
	}

	@Override
	public Class getClazz() {
		return monitor.getClazz();
	}

	@Override
	public boolean isLogEnabled() {
		return monitor.isLogEnabled();
	}

	@Override
	public Query getData(ConfigWeb config, Map<String, Object> arguments) throws PageException {
		return monitor.getData(config, arguments);
	}

	@Override
	public void init(PageContext pc) throws IOException {
		new _Log(monitor, pc, false, logEnabled, true).start();
	}

	@Override
	public void log(PageContext pc, boolean error) throws IOException {
		new _Log(monitor, pc, error, logEnabled, false).start();
	}

	static class _Log extends PageContextThread {
		private RequestMonitorPro monitor;
		private boolean error;
		private boolean logEnabled;
		private boolean init;

		public _Log(RequestMonitorPro monitor, PageContext pc, boolean error, boolean logEnabled, boolean init) {
			super(pc);
			this.monitor = monitor;
			this.error = error;
			this.logEnabled = logEnabled;
			this.init = init;
		}

		@Override
		public void run(PageContext pc) {
			try {
				ThreadLocalPageContext.register(pc);
				try {
					if (init) monitor.log(pc, error);
					else monitor.init(pc);
				}
				catch (IOException e) {
					if (logEnabled) {
						Log log = pc.getConfig().getLog("io");
						if (log != null) log.log(Log.LEVEL_ERROR, "io", e);
					}
				}
			}
			finally {
				ThreadLocalPageContext.release();
			}
		}
	}
}