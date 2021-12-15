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

import java.io.PrintWriter;
import java.util.Map;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.PageContext;

public class ConsoleExecutionLog extends ExecutionLogSupport {

	private PrintWriter pw;
	private PageContext pc;

	@Override
	protected void _init(PageContext pc, Map<String, String> arguments) {
		this.pc = pc;

		if (pw == null) {
			// stream type
			String type = arguments.get("stream-type");
			if (type != null && type.trim().equalsIgnoreCase("error")) pw = new PrintWriter(System.err);
			else pw = new PrintWriter(System.out);

		}
	}

	@Override
	protected void _log(int startPos, int endPos, long startTime, long endTime) {

		long diff = endTime - startTime;
		LogUtil.log(ThreadLocalPageContext.getConfig(pc), Log.LEVEL_INFO, Controler.class.getName(),
				pc.getId() + ":" + pc.getCurrentPageSource().getDisplayPath() + ":" + positons(startPos, endPos) + " > " + timeLongToString(diff));
	}

	@Override
	protected void _release() {
	}

	private static String positons(int startPos, int endPos) {
		if (startPos == endPos) return startPos + "";
		return startPos + ":" + endPos;
	}

}