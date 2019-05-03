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
package lucee.runtime.functions.other;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.converter.JavaConverter;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class ObjectSave {

	public static Object call(PageContext pc, Object input) throws PageException {
		return call(pc, input, null);
	}

	public static Object call(PageContext pc, Object input, String filepath) throws PageException {
		if (!(input instanceof Serializable)) throw new ApplicationException("can only serialize object from type Serializable");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			JavaConverter.serialize((Serializable) input, baos);

			byte[] barr = baos.toByteArray();

			// store to file
			if (!StringUtil.isEmpty(filepath, true)) {
				Resource res = ResourceUtil.toResourceNotExisting(pc, filepath);
				pc.getConfig().getSecurityManager().checkFileLocation(res);
				IOUtil.copy(new ByteArrayInputStream(barr), res, true);
			}
			return barr;

		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}
}