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
package lucee.runtime.engine;

import lucee.commons.io.log.LogUtil;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigServerImpl;

/**
 * own thread how check the main thread and his data
 */
public final class Monitor extends Thread {

	private static final long INTERVALL = 5000;
	private final ConfigServerImpl configServer;
	private final ControllerState state;

	/**
	 * @param contextes
	 * @param interval
	 * @param run
	 */
	public Monitor(ConfigServer configServer, ControllerState state) {

		this.state = state;
		this.configServer = (ConfigServerImpl) configServer;

	}

	@Override
	public void run() {
		short tries = 0;
		while (state.active()) {
			try {
				sleep(INTERVALL);
			}
			catch (InterruptedException e) {
				LogUtil.log(ThreadLocalPageContext.getConfig(configServer), Monitor.class.getName(), e);
			}

			if (!configServer.isMonitoringEnabled()) return;
			lucee.runtime.monitor.IntervallMonitor[] monitors = configServer.getIntervallMonitors();

			int logCount = 0;
			if (monitors != null) for (int i = 0; i < monitors.length; i++) {
				if (monitors[i].isLogEnabled()) {
					logCount++;
					try {
						monitors[i].log();
					}
					catch (Exception e) {
						LogUtil.log(ThreadLocalPageContext.getConfig(configServer), Monitor.class.getName(), e);
					}
				}
			}

			if (logCount == 0) {
				tries++;
				if (tries >= 10) return;
			}
		}
	}

}