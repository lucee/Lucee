/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.intergral.fusiondebug.server;

import java.util.ArrayList;
import java.util.List;

import com.intergral.fusiondebug.server.IFDController;
import com.intergral.fusiondebug.server.IFDStackFrame;
import com.intergral.fusiondebug.server.IFDThread;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.transformer.bytecode.util.ASMUtil;

public class FDThreadImpl implements IFDThread {

	private PageContextImpl pc;
	private String name;
	private FDControllerImpl engine;
	// private CFMLFactoryImpl factory;

	public FDThreadImpl(FDControllerImpl engine, CFMLFactoryImpl factory, String name, PageContextImpl pc) {
		this.engine = engine;
		// this.factory=factory;
		this.name = name;
		this.pc = pc;
	}

	@Override
	public String getName() {
		return name + ":" + pc.getCFID();
	}

	@Override
	public int id() {
		return pc.getId();
	}

	public static int id(PageContext pc) {
		return pc.getId();
	}

	@Override
	public void stop() {
		Log log = pc.getConfig().getLog("application");
		SystemUtil.stop(pc, true);
	}

	@Override
	public Thread getThread() {
		return pc.getThread();
	}

	@Override
	public String getOutputBuffer() {
		return pc.getRootOut().toString();
	}

	public List<IFDStackFrame> getStackFrames() {
		return getStack();
	}

	@Override
	public List<IFDStackFrame> getStack() {

		StackTraceElement[] traces = pc.getThread().getStackTrace();
		String template = "";
		StackTraceElement trace = null;
		ArrayList<IFDStackFrame> list = new ArrayList<IFDStackFrame>();
		PageSource ps;
		for (int i = traces.length - 1; i >= 0; i--) {
			trace = traces[i];
			if (trace.getLineNumber() <= 0) continue;
			template = trace.getFileName();
			if (template == null || ResourceUtil.getExtension(template, "").equals("java")) continue;
			ps = toPageSource(pc, template);
			FDStackFrameImpl frame = new FDStackFrameImpl(this, pc, trace, ps);
			if (ASMUtil.isOverfowMethod(trace.getMethodName())) list.set(0, frame);
			else list.add(0, frame);
		}
		return list;
	}

	public IFDStackFrame getTopStack() {
		return getTopStackFrame();
	}

	@Override
	public IFDStackFrame getTopStackFrame() {
		PageSource ps = pc.getCurrentPageSource();

		StackTraceElement[] traces = pc.getThread().getStackTrace();
		String template = "";
		StackTraceElement trace = null;

		for (int i = 0; i < traces.length; i++) {
			trace = traces[i];
			if (trace.getLineNumber() <= 0) continue;
			template = trace.getFileName();
			if (template == null || ResourceUtil.getExtension(template, "").equals("java")) continue;

			if (ps == null || !isEqual(ps, trace)) {
				ps = toPageSource(pc, template);
			}
			break;
		}
		return new FDStackFrameImpl(this, pc, trace, ps);
	}

	private PageSource toPageSource(PageContextImpl pc2, String template) {
		Resource res = ResourceUtil.toResourceNotExisting(pc, template);
		return pc.toPageSource(res, null);
	}

	private boolean isEqual(PageSource ps, StackTraceElement trace) {
		// class name do not match
		if (!ps.getClassName().equals(trace.getClassName())) return false;
		// filename to not match
		if (!ps.getResource().getAbsolutePath().equals(trace.getFileName())) return false;

		return true;
	}

	/**
	 * @return the engine
	 */
	public IFDController getController() {
		return engine;
	}

}