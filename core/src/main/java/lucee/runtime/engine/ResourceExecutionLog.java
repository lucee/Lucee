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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.functions.other.CreateUUID;
import lucee.runtime.op.Caster;

public class ResourceExecutionLog extends ExecutionLogSupport {

	private static int count = 1;
	private Resource file;
	private StringBuffer content;
	private PageContext pc;
	private StringBuffer header;
	private ArrayList<String> pathes = new ArrayList<String>();
	private long start;
	private Resource dir;

	@Override
	protected void _init(PageContext pc, Map<String, String> arguments) {
		this.pc = pc;

		// header
		HttpServletRequest req = pc.getHttpServletRequest();

		header = new StringBuffer();
		createHeader(header, "context-path", req.getContextPath());
		createHeader(header, "remote-user", req.getRemoteUser());
		createHeader(header, "remote-addr", req.getRemoteAddr());
		createHeader(header, "remote-host", req.getRemoteHost());
		createHeader(header, "script-name", StringUtil.emptyIfNull(req.getContextPath()) + StringUtil.emptyIfNull(req.getServletPath()));
		createHeader(header, "server-name", req.getServerName());
		createHeader(header, "protocol", req.getProtocol());
		createHeader(header, "server-port", Caster.toString(req.getServerPort()));
		createHeader(header, "path-info", StringUtil.replace(StringUtil.emptyIfNull(req.getRequestURI()), StringUtil.emptyIfNull(req.getServletPath()), "", true));
		// createHeader(header,"path-translated",pc.getBasePageSource().getDisplayPath());
		createHeader(header, "query-string", req.getQueryString());
		createHeader(header, "unit", unitShortToString(unit));
		createHeader(header, "min-time-nano", min + "");

		content = new StringBuffer();

		// directory
		String strDirectory = arguments.get("directory");
		if (dir == null) {
			if (StringUtil.isEmpty(strDirectory)) {
				dir = getTemp(pc);
			}
			else {
				try {
					dir = ResourceUtil.toResourceNotExisting(pc, strDirectory, false, false);
					if (!dir.exists()) {
						dir.createDirectory(true);
					}
					else if (dir.isFile()) {
						err(pc, "can not create directory [" + dir + "], there is already a file with same name.");
					}
				}
				catch (Exception t) {
					err(pc, t);
					dir = getTemp(pc);
				}
			}
		}
		file = dir.getRealResource((pc.getId()) + "-" + CreateUUID.call(pc) + ".exl");
		file.createNewFile();
		start = System.currentTimeMillis();
	}

	private static Resource getTemp(PageContext pc) {
		Resource tmp = pc.getConfig().getConfigDir();
		Resource dir = tmp.getRealResource("execution-log");
		if (!dir.exists()) dir.mkdirs();
		return dir;
	}

	@Override
	protected void _release() {

		// execution time
		createHeader(header, "execution-time", Caster.toString(System.currentTimeMillis() - start));
		header.append("\n");

		// path
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = pathes.iterator();
		int count = 0;
		while (it.hasNext()) {
			sb.append(count++);
			sb.append(":");
			sb.append(it.next());
			sb.append("\n");
		}
		sb.append("\n");
		try {
			IOUtil.write(file, header + sb.toString() + content.toString(), (Charset) null, false);
		}
		catch (IOException ioe) {
			err(pc, ioe);
		}
	}

	private void createHeader(StringBuffer sb, String name, String value) {
		sb.append(name);
		sb.append(":");
		sb.append(StringUtil.emptyIfNull(value));
		sb.append("\n");
	}

	@Override
	protected void _log(int startPos, int endPos, long startTime, long endTime) {
		long diff = endTime - startTime;
		if (unit == UNIT_MICRO) diff /= 1000;
		else if (unit == UNIT_MILLI) diff /= 1000000;

		content.append(path(pc.getCurrentPageSource().getDisplayPath()));
		content.append("\t");
		content.append(startPos);
		content.append("\t");
		content.append(endPos);
		content.append("\t");
		content.append(diff);
		content.append("\n");
	}

	private int path(String path) {
		int index = pathes.indexOf(path);
		if (index == -1) {
			pathes.add(path);
			return pathes.size() - 1;
		}
		return index;
	}

	private void err(PageContext pc, String msg) {
		LogUtil.log(pc, Log.LEVEL_ERROR, ResourceExecutionLog.class.getName(), msg);
	}

	private void err(PageContext pc, Exception e) {
		LogUtil.log(pc, ResourceExecutionLog.class.getName(), e);
	}
}