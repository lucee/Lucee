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

import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class FileAppend {

	public static String call(PageContext pc, String path, Object data) throws PageException {
		return call(pc, path, data, ((PageContextImpl) pc).getResourceCharset().name());
	}

	public static String call(PageContext pc, String path, Object data, String charset) throws PageException {
		FileStreamWrapper fsw = null;
		if (StringUtil.isEmpty(charset, true)) charset = ((PageContextImpl) pc).getResourceCharset().name();

		try {
			Resource res = Caster.toResource(pc, path, false);
			pc.getConfig().getSecurityManager().checkFileLocation(res);
			fsw = new FileStreamWrapperWrite(res, charset, true, false);
			fsw.write(data);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		finally {
			closeEL(fsw);
		}
		return null;
	}

	private static void closeEL(FileStreamWrapper fsw) {
		if (fsw == null) return;
		try {
			fsw.close();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}
}