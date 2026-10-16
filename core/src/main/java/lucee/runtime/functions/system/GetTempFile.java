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
/**
 * Implements the CFML Function getTempFile
 */
package lucee.runtime.functions.system;

import java.io.IOException;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.security.SecurityManagerImpl;

public final class GetTempFile implements Function {

	private static final long serialVersionUID = -166719554831864953L;

	public static String call(PageContext pc, String strDir, String prefix) throws PageException {
		return call(pc, strDir, prefix, ".tmp");
	}

	public static String call(PageContext pc, String strDir, String prefix, String extension) throws PageException {
		Resource dir;
		if (StringUtil.isEmpty(strDir)) dir = pc.getConfig().getTempDirectory();
		else dir = ResourceUtil.toResourceExisting(pc, strDir);
		SecurityManagerImpl.checkFileLocation(pc, dir);
		try {
			return ResourceUtil.getUniqueTempFile(dir, prefix, extension).getCanonicalPath();
		}
		catch (IOException e) {
			ExpressionException ee = new ExpressionException("Unable to create temporary file", "In temp directory [" + strDir + "]"); // don't expose path in error message
			ExceptionUtil.initCauseEL(ee, e);
			throw ee;
		}
	}
}
