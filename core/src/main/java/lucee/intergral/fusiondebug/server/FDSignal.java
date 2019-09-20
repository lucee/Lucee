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
package lucee.intergral.fusiondebug.server;

import java.util.ArrayList;
import java.util.List;

import com.intergral.fusiondebug.server.FDSignalException;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.NativeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.op.Caster;
import lucee.transformer.bytecode.util.ASMUtil;

public class FDSignal {
	private static ThreadLocal hash = new ThreadLocal();

	public static void signal(PageException pe, boolean caught) {
		try {
			String id = pe.hashCode() + ":" + caught;
			if (Caster.toString(hash.get(), "").equals(id)) return;

			List stack = createExceptionStack(pe);
			if (stack.size() > 0) {
				FDSignalException se = new FDSignalException();
				se.setExceptionStack(stack);
				se.setRuntimeExceptionCaughtStatus(caught);
				se.setRuntimeExceptionExpression(createRuntimeExceptionExpression(pe));
				if (pe instanceof NativeException) se.setRuntimeExceptionType("native");
				else se.setRuntimeExceptionType(pe.getTypeAsString());
				se.setStackTrace(pe.getStackTrace());
				hash.set(id);
				throw se;
			}

		}
		catch (FDSignalException fdse) {
			// do nothing - will be processed by JDI and handled by FD
		}
	}

	public static String createRuntimeExceptionExpression(PageException pe) {
		if (!StringUtil.isEmpty(pe.getDetail())) return pe.getMessage() + " " + pe.getDetail();
		return pe.getMessage();
	}

	public static List createExceptionStack(PageException pe) {
		StackTraceElement[] traces = pe.getStackTrace();
		PageContextImpl pc = (PageContextImpl) ThreadLocalPageContext.get();
		String template = "";
		StackTraceElement trace = null;
		List list = new ArrayList();
		Resource res;
		PageSource ps;
		FDStackFrameImpl frame;

		for (int i = traces.length - 1; i >= 0; i--) {
			trace = traces[i];
			ps = null;
			if (trace.getLineNumber() <= 0) continue;
			template = trace.getFileName();
			if (template == null || ResourceUtil.getExtension(template, "").equals("java")) continue;

			res = ResourceUtil.toResourceNotExisting(pc, template);
			ps = pc.toPageSource(res, null);

			frame = new FDStackFrameImpl(null, pc, trace, ps);
			if (ASMUtil.isOverfowMethod(trace.getMethodName())) list.set(0, frame);
			else list.add(0, frame);

		}
		if (pe instanceof TemplateException) {
			TemplateException te = (TemplateException) pe;
			if (te.getPageSource() != null) list.add(0, new FDStackFrameImpl(null, pc, te.getPageSource(), te.getLine()));
		}

		return list;
	}
}