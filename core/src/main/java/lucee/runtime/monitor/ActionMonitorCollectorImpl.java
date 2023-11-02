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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;

public class ActionMonitorCollectorImpl implements ActionMonitorCollector {

	private List<ActionMonitor> monitors;

	@Override
	public void addMonitor(ConfigServer cs, ActionMonitor monitor, String name, boolean log) throws IOException {
		monitor.init(cs, name, log);
		if (monitors == null) monitors = new ArrayList<ActionMonitor>();
		monitors.add(monitor);
	}

	/**
	 * logs certain action within a Request
	 * 
	 * @param pc
	 * @param ar
	 * @throws IOException
	 */
	@Override
	public void log(PageContext pc, String type, String label, long executionTime, Object data) {
		if (monitors == null) return;

		Iterator<ActionMonitor> it = monitors.iterator();
		while (it.hasNext()) {
			try {
				it.next().log(pc, type, label, executionTime, data);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
	}

	@Override
	public void log(ConfigWeb config, String type, String label, long executionTime, Object data) {
		if (monitors == null) return;

		Iterator<ActionMonitor> it = monitors.iterator();
		while (it.hasNext()) {
			try {
				it.next().log(config, type, label, executionTime, data);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
	}

	@Override
	public ActionMonitor getActionMonitor(String name) {
		if (monitors == null) return null;
		Iterator<ActionMonitor> it = monitors.iterator();
		ActionMonitor am;
		while (it.hasNext()) {
			am = it.next();
			if (name.equalsIgnoreCase(am.getName())) return am;
		}
		return null;
	}

}