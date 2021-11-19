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

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;

public class FileOpen {

	public static Object call(PageContext pc, String path) throws PageException {
		return call(pc, path, "read", ((PageContextImpl) pc).getResourceCharset().name(), false);
	}

	public static Object call(PageContext pc, String path, String mode) throws PageException {
		return call(pc, path, mode, ((PageContextImpl) pc).getResourceCharset().name(), false);
	}

	public static Object call(PageContext pc, String path, String strMode, String charset) throws PageException {
		return call(pc, path, strMode, charset, false);
	}

	public static Object call(PageContext pc, String path, String strMode, String charset, boolean seekable) throws PageException {

		strMode = strMode.trim().toLowerCase();
		if (StringUtil.isEmpty(charset, true)) charset = ((PageContextImpl) pc).getResourceCharset().name();
		// try {

		if ("read".equals(strMode)) {
			return new FileStreamWrapperRead(check(pc, ResourceUtil.toResourceExisting(pc, path)), charset, seekable);
		}
		if ("readbinary".equals(strMode)) {
			return new FileStreamWrapperReadBinary(check(pc, ResourceUtil.toResourceExisting(pc, path)), seekable);
		}
		if ("write".equals(strMode)) {
			return new FileStreamWrapperWrite(check(pc, ResourceUtil.toResourceNotExisting(pc, path)), charset, false, seekable);
		}
		if ("append".equals(strMode)) {
			return new FileStreamWrapperWrite(check(pc, ResourceUtil.toResourceNotExisting(pc, path)), charset, true, seekable);
		}
		if ("readwrite".equals(strMode)) {
			return new FileStreamWrapperReadWrite(check(pc, ResourceUtil.toResourceNotExisting(pc, path)), charset, seekable);
		}

		throw new FunctionException(pc, "FileOpen", 2, "mode", "invalid value [" + strMode + "], valid values for argument mode are [read,readBinary,append,write,readwrite]");

	}

	private static Resource check(PageContext pc, Resource res) throws PageException {
		pc.getConfig().getSecurityManager().checkFileLocation(res);
		return res;
	}

}