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

import java.io.File;

import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class GetFreeSpace {

	public static double call(PageContext pc, Object obj) throws PageException {
		Resource res = Caster.toResource(pc, obj, true, pc.getConfig().allowRealPath());
		if (!(res instanceof File)) throw new FunctionException(pc, "getFreeSpace", 1, "filepath", "this function is only supported for the local filesystem");
		File file = (File) res;
		return file.getFreeSpace();
	}
}