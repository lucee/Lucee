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
import lucee.commons.io.res.util.ResourceSnippet;
import lucee.commons.io.res.util.ResourceSnippetsMap;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.PageContextImpl;
import lucee.runtime.op.Caster;

public class LogExecutionLog extends ExecutionLogSupport {

	private PageContext pc;
	private ResourceSnippetsMap snippetsMap = new ResourceSnippetsMap(1024, 128);
	private boolean snippet = false;
	private int logLevel;
	private String logName;

	@Override
	protected void _init(PageContext pc, Map<String, String> arguments) {
		this.pc = pc;
		if (Caster.toBooleanValue(arguments.get("snippet"), false)) snippet = true;
		String type = arguments.get("log-level");
		logLevel = LogUtil.toLevel(type, Log.LEVEL_TRACE);
		logName = StringUtil.toString(arguments.get("log-name"), Controler.class.getName());
	}

	@Override
	protected void _log(int startPos, int endPos, long startTime, long endTime) {
		PageSource ps = pc.getCurrentPageSource(null);
		if (ps == null) return;
		long diff = endTime - startTime;
		String log = pc.getId() + ":" + ps.getDisplayPath() + ":";
		if (snippet) {
			ResourceSnippet snippet = snippetsMap.getSnippet(ps, startPos, endPos, ((PageContextImpl) pc).getResourceCharset().name());
			log += positions(snippet.getEndLine(), snippet.getEndLine()) + " > " + timeLongToString(diff) + " [" + snippet.getContent() + "]";
		} else {
			log += positions(startPos, endPos) + " > " + timeLongToString(diff);
		}
		LogUtil.log(pc, logLevel, Controler.class.getName(), log);
	}

	@Override
	protected void _release() {
		snippetsMap = null;
	}

	private static String positions(int startPos, int endPos) {
		if (startPos == endPos) return startPos + "";
		return startPos + ":" + endPos;
	}

}