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

public abstract class CallerResponseStreamResult implements Callable<String> {

	private PageContext parent;
	private PageContextImpl pc;
	private ByteArrayOutputStream baos;

	public CallerResponseStreamResult(PageContext parent) {
		this.parent = parent;
		this.baos = new ByteArrayOutputStream();
		this.pc = ThreadUtil.clonePageContext(parent, baos, false, false, false);
	}

	@Override
	public final String call() throws PageException {
		ThreadLocalPageContext.register(pc);
		pc.getRootOut().setAllowCompression(false); // make sure content is not compressed
		String str = null;
		try {
			_call(parent, pc);
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
				LogUtil.log(pc, "concurrency", e);
			}
		}
		return str;
	}

	public abstract void _call(PageContext parent, PageContext pc) throws PageException;
	// public abstract void afterCleanup(PageContext parent, ByteArrayOutputStream baos);
}