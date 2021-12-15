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

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.XMLConfigWebFactory;
import lucee.runtime.config.XMLConfigWebFactory.MonitorTemp;

public class ActionMonitorFatory {
	public static ActionMonitorCollector getActionMonitorCollector() {
		if (SystemUtil.getLoaderVersion() > 4) return new ActionMonitorCollectorImpl();
		return new ActionMonitorCollectorRefImpl();
	}

	public static ActionMonitorCollector getActionMonitorCollector(ConfigServer cs, XMLConfigWebFactory.MonitorTemp[] temps) throws IOException {
		// try to load with interface
		try {
			ActionMonitorCollector collector = new ActionMonitorCollectorImpl();
			addMonitors(collector, cs, temps);
			return collector;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			ActionMonitorCollector collector = new ActionMonitorCollectorRefImpl();
			addMonitors(collector, cs, temps);
			return collector;
		}
	}

	private static void addMonitors(ActionMonitorCollector collector, ConfigServer cs, MonitorTemp[] temps) throws IOException {
		MonitorTemp temp;
		for (int i = 0; i < temps.length; i++) {
			temp = temps[i];
			collector.addMonitor(cs, temp.am, temp.name, temp.log);
		}
	}
}