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
package lucee.runtime.concurrency;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ExecutionLog;
import lucee.runtime.engine.ExecutionLogSupport;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.ParentException;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;

public final class UDFCaller2<P> implements Callable<Data<P>> {

	private PageContext parent;

	private UDF udf;
	private boolean doIncludePath;
	private Object[] arguments;
	private Struct namedArguments;
	private P passed;
	private ParentException parentException;

	private UDFCaller2(PageContext parent) {
		this.parent = parent;
	}

	public UDFCaller2(PageContext parent, ParentException parentException, UDF udf, Object[] arguments, P passed, boolean doIncludePath) {
		this(parent);
		this.udf = udf;
		this.parentException = parentException;
		this.arguments = arguments;
		this.doIncludePath = doIncludePath;
		this.passed = passed;
	}

	public UDFCaller2(PageContext parent, ParentException parentException, UDF udf, Struct namedArguments, P passed, boolean doIncludePath) {
		this(parent);
		this.udf = udf;
		this.parentException = parentException;
		this.namedArguments = namedArguments;
		this.doIncludePath = doIncludePath;
		this.passed = passed;
	}

	@Override
	public final Data<P> call() throws PageException {
		// reuse a clone from the operation's pool when available, otherwise fall back to a per-task clone
		PageContextPool pool = parent instanceof PageContextImpl ? ((PageContextImpl) parent).getParallelPool() : null;
		PageContextPool.Entry entry = null;
		PageContextImpl pc;
		ByteArrayOutputStream baos;
		if (pool != null) {
			entry = pool.borrow();
			pc = entry.pc;
			baos = entry.baos;
		}
		else {
			ThreadLocalPageContext.register(parent);
			baos = new ByteArrayOutputStream();
			pc = ThreadUtil.clonePageContext(parent, baos, false, false, false);
			// Capture spawn offset for execution log
			ExecutionLog execLog = pc.getExecutionLog();
			if (execLog instanceof ExecutionLogSupport) {
				((ExecutionLogSupport) execLog).setSpawnOffsetNano(System.nanoTime() - ((PageContextImpl) parent).getStartTimeNS());
			}
		}

		ThreadLocalPageContext.registerChild(pc);
		pc.getRootOut().setAllowCompression(false); // make sure content is not compressed
		String str = null;
		Object result = null;
		boolean succeeded = false;
		try {
			if (namedArguments != null) result = udf.callWithNamedValues(pc, namedArguments, doIncludePath);
			else result = udf.call(pc, arguments, doIncludePath);
			succeeded = true;
		}
		catch (PageException pe) {
			ExceptionUtil.initCauseEL(pe, parentException);

			throw pe;
		}
		finally {
			try {
				HttpServletResponseDummy rsp = (HttpServletResponseDummy) pc.getHttpServletResponse();

				Charset cs = ReqRspUtil.getCharacterEncoding(pc, rsp);
				// if(enc==null) enc="ISO-8859-1";

				pc.getOut().flush(); // make sure content is flushed

				str = IOUtil.toString((new ByteArrayInputStream(baos.toByteArray())), cs); // TODO add support for none string content

				if (pool != null) {
					// only hand a clean clone back for reuse; a failed task may leave inconsistent state,
					// so that clone stays tracked and is released when the pool is closed
					if (succeeded) pool.giveBack(entry);
				}
				else {
					pc.getConfig().getFactory().releasePageContext(pc);
				}
			}
			catch (Exception e) {
				LogUtil.log(pc, "loading", e);
			}
		}
		return new Data<P>(str, result, passed);
	}
}