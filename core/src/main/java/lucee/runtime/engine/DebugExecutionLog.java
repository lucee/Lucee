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

import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.debug.DebugEntry;

public class DebugExecutionLog extends ExecutionLogSupport {

	private PageContext pc;

	@Override
	protected void _init(PageContext pc, Map<String, String> arguments) {
		this.pc = pc;
	}

	@Override
	protected void _log(int startPos, int endPos, long startTime, long endTime) {

		if (!pc.getConfig().debug()) return;

		long diff = endTime - startTime;
		if (unit == UNIT_MICRO) diff /= 1000;
		else if (unit == UNIT_MILLI) diff /= 1000000;

		DebugEntry de = pc.getDebugger().getEntry(pc, pc.getCurrentPageSource(), startPos, endPos);
		de.updateExeTime((int) diff);
	}

	@Override
	protected void _release() {
	}

}