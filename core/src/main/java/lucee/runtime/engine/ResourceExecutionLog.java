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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.thread.ChildThread;
import lucee.runtime.thread.ChildThreadImpl;

public final class ResourceExecutionLog extends ExecutionLogSupport {

	private Resource file;
	private Resource tmpFile;
	private StringBuilder content;
	private PageContext pc;
	private StringBuilder header;
	private ArrayList<String> pathes = new ArrayList<String>();
	private HashMap<String, Integer> pathIndex = new HashMap<String, Integer>();
	private long start;
	private Resource dir;
	private static final int DEFAULT_BUFFER_SIZE = 100000; // 100K chars (~200KB)
	private int bufferSize = DEFAULT_BUFFER_SIZE;
	private static final long SERVER_START_TIME = CFMLEngineFactory.getInstance().uptime();

	@Override
	protected void _init(PageContext pc, Map<String, String> arguments) {
		this.pc = pc;

		// header
		HttpServletRequest req = pc.getHttpServletRequest();

		header = new StringBuilder();
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

		// request and PageContext IDs (all files)
		createHeader(header, "request-id", String.valueOf(((PageContextImpl) pc).getRequestId()));
		createHeader(header, "pc-id", String.valueOf(pc.getId()));

		// parent thread birth info (only child threads from cfthread)
		Thread currentThread = Thread.currentThread();
		if (currentThread instanceof ChildThread) {
			ChildThread childThread = (ChildThread) currentThread;
			if (childThread instanceof ChildThreadImpl) {
				ChildThreadImpl childThreadImpl = (ChildThreadImpl) childThread;
				PageContext parentPC = childThreadImpl.getParentPageContext();
				if (parentPC != null && parentPC instanceof PageContextImpl) {
					PageContextImpl parentPci = (PageContextImpl) parentPC;
					createHeader(header, "parent-request", String.valueOf(parentPci.getRequestId()));
					createHeader(header, "parent-pc", String.valueOf(parentPci.getId()));
					lucee.runtime.PageSource parentPs = parentPci.getCurrentPageSource(null);
					if (parentPs != null) {
						createHeader(header, "parent-path", parentPs.getDisplayPath());
					}
					long spawnOffsetNano = childThreadImpl.getSpawnOffsetNano();
					if (spawnOffsetNano > 0) {
						createHeader(header, "spawn-offset-nano", String.valueOf(spawnOffsetNano));
					}
				}
			}
		}

		// buffer-size
		String strBufferSize = arguments.get("buffer-size");
		if (!StringUtil.isEmpty(strBufferSize)) {
			Integer size = Caster.toInteger(strBufferSize, null);
			if (size != null && size > 0) {
				bufferSize = size;
			}
		}

		content = new StringBuilder(bufferSize);

		// directory
		String strDirectory = arguments.get("directory");
		if (dir == null) {
			if (StringUtil.isEmpty(strDirectory)) {
				dir = getTemp(pc);
			}
			else {
				try {
					dir = ResourceUtil.toResourceNotExisting(pc, null, strDirectory, false, false);
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
		file = dir.getRealResource((((PageContextImpl) pc).getRequestId()) + "-" + pc.getId() + "-" + SERVER_START_TIME + ".exl");
		// Always use local temp for buffer file
		Resource localTemp = getTemp(pc);
		tmpFile = localTemp.getRealResource(file.getName() + ".tmp");
		start = System.nanoTime();
	}

	private static Resource getTemp(PageContext pc) {
		Resource tmp = pc.getConfig().getConfigDir();
		Resource dir = tmp.getRealResource("execution-log");
		if (!dir.exists()) dir.mkdirs();
		return dir;
	}

	@Override
	protected void _release() {
		// Flush any remaining content
		flushContent();

		// execution time
		long executionTime = System.nanoTime() - start;
		createHeader(header, "execution-time", Caster.toString(convertTime(executionTime, unit)));

		// parent execution context (for parallel operations like arrayMap(..., true))
		PageContextImpl pci = (PageContextImpl) pc;
		PageContext parentPC = pci.getParentPageContext();
		if (parentPC != null && parentPC instanceof PageContextImpl) {
			PageContextImpl parentPci = (PageContextImpl) parentPC;
			createHeader(header, "parent-request", String.valueOf(parentPci.getRequestId()));
			createHeader(header, "parent-pc", String.valueOf(parentPci.getId()));
			lucee.runtime.PageSource parentPs = parentPci.getCurrentPageSource(null);
			if (parentPs != null) {
				createHeader(header, "parent-path", parentPs.getDisplayPath());
			}
			if (spawnOffsetNano > 0) {
				createHeader(header, "spawn-offset-nano", String.valueOf(spawnOffsetNano));
			}
		}

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

		// Write header and paths to final file, then append data from tmp file
		try {
			IOUtil.write(file, header.toString() + sb.toString(), (Charset) null, false);
			// Append data from tmp file if it exists
			if (tmpFile != null && tmpFile.exists()) {
				InputStream is = null;
				OutputStream os = null;
				try {
					is = tmpFile.getInputStream();
					os = file.getOutputStream(true);
					IOUtil.copy(is, os, true, true);
				}
				catch (IOException ioe) {
					IOUtil.closeEL(is);
					IOUtil.closeEL(os);
					throw ioe;
				}
				ResourceUtil.removeEL(tmpFile, true);
			}
		}
		catch (IOException ioe) {
			err(pc, ioe);
		}
	}

	private void createHeader(StringBuilder sb, String name, String value) {
		sb.append(name);
		sb.append(":");
		sb.append(StringUtil.emptyIfNull(value));
		sb.append("\n");
	}

	@Override
	protected void _log(int startPos, int endPos, long startTime, long endTime) {
		long diff = convertTime(endTime - startTime, unit);

		content.append(path(pc.getCurrentPageSource().getDisplayPath()));
		content.append("\t");
		content.append(startPos);
		content.append("\t");
		content.append(endPos);
		content.append("\t");
		content.append(diff);
		content.append("\n");

		// Flush content to file if it gets too large
		if (content.length() > bufferSize) {
			flushContent();
		}
	}

	private int path(String path) {
		Integer index = pathIndex.get(path);
		if (index == null) {
			index = pathes.size();
			pathes.add(path);
			pathIndex.put(path, index);
		}
		return index;
	}

	private void flushContent() {
		if (content.length() == 0) return;
		try {
			IOUtil.write(tmpFile, content.toString(), (Charset) null, true);
			content.setLength(0); // Clear the buffer
		}
		catch (IOException ioe) {
			err(pc, ioe);
		}
	}

	private void err(PageContext pc, String msg) {
		LogUtil.log(pc, Log.LEVEL_ERROR, ResourceExecutionLog.class.getName(), msg);
	}

	private void err(PageContext pc, Exception e) {
		LogUtil.log(pc, ResourceExecutionLog.class.getName(), e);
	}
}