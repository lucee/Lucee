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
package lucee.runtime.functions.file;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSourcePool;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class FileWrite {

	public static String call(PageContext pc, Object obj, Object data) throws PageException {
		return call(pc, obj, data, ((PageContextImpl) pc).getResourceCharset().name());
	}

	public static String call(PageContext pc, Object obj, Object data, String charset) throws PageException {
		FileStreamWrapper fsw = null;
		boolean close = false;
		if (StringUtil.isEmpty(charset, true)) charset = ((PageContextImpl) pc).getResourceCharset().name();
		Resource res = null;

		try {
			if (obj instanceof FileStreamWrapper) {
				fsw = (FileStreamWrapper) obj;
			}
			else {
				close = true;
				res = Caster.toResource(pc, obj, false);
				pc.getConfig().getSecurityManager().checkFileLocation(res);
				// validate parent only works when you have access to the parent, that is not necessary a given for
				// all FS
				if ("file".equalsIgnoreCase(res.getResourceProvider().getScheme())) {
					Resource parent = res.getParentResource();
					if (parent != null && parent.canRead() && !parent.exists()) throw new ApplicationException("parent directory for [" + res + "] doesn't exist");
				}

				fsw = new FileStreamWrapperWrite(res, charset, false, false);
			}
			fsw.write(data);
		}
		catch (IOException e) {
			throw toApplicationException(pc, obj, charset, e);
		}
		finally {
			if (close) IOUtil.closeEL(fsw);
			if (fsw != null) PageSourcePool.flush(pc, fsw);
		}

		return null;
	}

	private static ApplicationException toApplicationException(PageContext pc, Object obj, String charset, IOException e) throws ExpressionException {
		ApplicationException ae;
		if (e instanceof UnsupportedEncodingException) {
			ae = new ApplicationException("Failed to write to file [" + Caster.toResource(pc, obj, false) + "], because the given charset [" + charset + "] is not supported");

		}
		else {
			String msg = e.getMessage();
			String appendix = StringUtil.isEmpty(msg, true) ? "" : (", caused by [" + msg + "]");
			ae = new ApplicationException("Failed to write to file [" + Caster.toResource(pc, obj, false) + "]" + appendix);

		}
		ExceptionUtil.initCauseEL(ae, e);
		return ae;
	}
}