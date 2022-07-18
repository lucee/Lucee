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
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;

public class UDFCaller2<P> implements Callable<Data<P>> {

	private PageContext parent;
	private PageContextImpl pc;
	private ByteArrayOutputStream baos;

	private UDF udf;
	private boolean doIncludePath;
	private Object[] arguments;
	private Struct namedArguments;
	private P passed;

	private UDFCaller2(PageContext parent) {
		this.parent = parent;
		this.baos = new ByteArrayOutputStream();

	}

	public UDFCaller2(PageContext parent, UDF udf, Object[] arguments, P passed, boolean doIncludePath) {
		this(parent);
		this.udf = udf;
		this.arguments = arguments;
		this.doIncludePath = doIncludePath;
		this.passed = passed;
	}

	public UDFCaller2(PageContext parent, UDF udf, Struct namedArguments, P passed, boolean doIncludePath) {
		this(parent);
		this.udf = udf;
		this.namedArguments = namedArguments;
		this.doIncludePath = doIncludePath;
		this.passed = passed;
	}

	@Override
	public final Data<P> call() throws PageException {
		if (this.pc == null) {
			ThreadLocalPageContext.register(parent);
			this.pc = ThreadUtil.clonePageContext(parent, baos, false, false, false);
		}
		ThreadLocalPageContext.register(pc);
		pc.getRootOut().setAllowCompression(false); // make sure content is not compressed
		String str = null;
		Object result = null;
		try {
			if (namedArguments != null) result = udf.callWithNamedValues(pc, namedArguments, doIncludePath);
			else result = udf.call(pc, arguments, doIncludePath);

		}
		finally {
			try {
				HttpServletResponseDummy rsp = (HttpServletResponseDummy) pc.getHttpServletResponse();

				Charset cs = ReqRspUtil.getCharacterEncoding(pc, rsp);
				// if(enc==null) enc="ISO-8859-1";

				pc.getOut().flush(); // make sure content is flushed

				pc.getConfig().getFactory().releasePageContext(pc);
				str = IOUtil.toString((new ByteArrayInputStream(baos.toByteArray())), cs); // TODO add support for none string content
			}
			catch (Exception e) {
				LogUtil.log(ThreadLocalPageContext.getConfig(pc), "loading", e);
			}
		}
		return new Data<P>(str, result, passed);
	}
}