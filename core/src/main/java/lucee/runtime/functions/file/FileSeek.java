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

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class FileSeek {
	public static String call(PageContext pc, Object fileObj, double pos) throws PageException {

		if (!(fileObj instanceof FileStreamWrapper))
			throw new FunctionException(pc, "FileSeek", 1, "fileObj", "invalid type [" + Caster.toTypeName(fileObj) + "], only File Object produced by FileOpen supported");
		FileStreamWrapper fs = (FileStreamWrapper) fileObj;
		fs.seek((long) pos);
		return null;

	}
}