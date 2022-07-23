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
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
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
		try {
			try {
				if (obj instanceof FileStreamWrapper) {
					fsw = (FileStreamWrapper) obj;
				}
				else {
					close = true;
					Resource res = Caster.toResource(pc, obj, false);
					pc.getConfig().getSecurityManager().checkFileLocation(res);
					Resource parent = res.getParentResource();
					//if (parent != null && !parent.exists())  throw new FunctionException(pc, "FileWrite", 1, "source", "parent directory for [" + res + "] doesn't exist");
					fsw = new FileStreamWrapperWrite(res, charset, false, false);
				}
				fsw.write(data);
				/* see LDEV-4081
				try { 
					fsw.write(data);
				}
				catch (IOException e) {
					throw new FunctionException(pc, "FileWrite", 1, "source", "Invalid file [" + Caster.toResource(pc, obj, false)  + "]",e.getMessage());
				}
				*/
			}
			finally {
				if (close && fsw != null) fsw.close();
			}

		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return null;
	}
}