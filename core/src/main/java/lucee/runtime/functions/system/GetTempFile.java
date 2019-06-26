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
 * Implements the CFML Function gettempfile
 */
package lucee.runtime.functions.system;

import java.io.IOException;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class GetTempFile implements Function {
	public static String call(PageContext pc, String strDir, String prefix, String extension) throws PageException {
		Resource dir = ResourceUtil.toResourceExisting(pc, strDir);
		pc.getConfig().getSecurityManager().checkFileLocation(dir);
		if (!dir.isDirectory()) throw new ExpressionException(strDir + " is not a directory");
		int count = 1;
		Resource file;
		String getExtens;
		if(extension == null || extension == ""){ extension = ".tmp"; }
		getExtens = extension.toLowerCase();
		extension = extension.toLowerCase();
		if(extension.charAt(0) != '.'){ extension = "." + extension; }
		if( ((!".tmp".equals(extension)) && (!".doc".equals(extension))  && (!".pdf".equals(extension)) && (!".docx".equals(extension)) &&
			(!".zip".equals(extension)) && (!".img".equals(extension)) && (!".jpeg".equals(extension))  && (!".jpg".equals(extension)) && 
			(!".png".equals(extension)) && (!".txt".equals(extension)) )) throw new ExpressionException("Type '" + getExtens + "' is not allowed. Valid types are [ txt,doc,docx,pdf,zip,img,png,tmp,jpeg,jpg ]");

		while ((file = dir.getRealResource(prefix + pc.getId() + count + extension )).exists()) {
			count++;
		}
		try {
			file.createFile(false);
			// file.createNewFile();
			return file.getCanonicalPath();
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}
}
