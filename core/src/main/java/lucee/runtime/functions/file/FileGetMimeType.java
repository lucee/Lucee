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

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class FileGetMimeType {
	public static String call(PageContext pc, Object oSrc) throws PageException {
		return call(pc, oSrc, true);
	}

	public static String call(PageContext pc, Object oSrc, boolean checkHeader) throws PageException {
		Resource src = null;
		byte[] barr = null;
		try {
			src = Caster.toResource(pc, oSrc, false);
		}
		catch (ExpressionException e) {
			barr = Caster.toBinary(oSrc, null);
			if (barr == null) throw e;

		}
		if (barr != null) {
			String mimeType = IOUtil.getMimeType(barr, null);
			if (StringUtil.isEmpty(mimeType, true)) return "application/octet-stream";
			return mimeType;
		}

		if (!src.exists()) {
			if (checkHeader) {
				throw new FunctionException(pc, "FileGetMimeType", 1, "file", "File [" + src + "] does not exist, strict was true");
			}
			else {
				String mimeType = IOUtil.getMimeType(src.getName(), null);
				if (!StringUtil.isEmpty(mimeType)) return mimeType;
				throw new FunctionException(pc, "FileGetMimeType", 1, "file", "File [" + src + "] does not exist and couldn't detect mimetype from the file extension.");
			}
		}
		pc.getConfig().getSecurityManager().checkFileLocation(src);

		if (checkHeader && src.length() == 0) throw new FunctionException(pc, "FileGetMimeType", 1, "file", "File [" + src + "] was empty, strict was true");

		String mimeType = ResourceUtil.getMimeType(src, null);
		if (StringUtil.isEmpty(mimeType, true)) return "application/octet-stream";
		return mimeType;
	}
}