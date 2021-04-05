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
import java.io.IOException;
import java.io.InputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.converter.JavaConverter;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public class ObjectLoad {
	public static Object call(PageContext pc, Object input) throws PageException {
		InputStream is;
		boolean closeStream = true;
		if (Decision.isBinary(input)) {
			is = new ByteArrayInputStream(Caster.toBinary(input));
		}
		else if (input instanceof InputStream) {
			is = (InputStream) input;
			closeStream = false;
		}
		else {
			Resource res = ResourceUtil.toResourceExisting(pc, Caster.toString(input));
			pc.getConfig().getSecurityManager().checkFileLocation(res);
			try {
				is = res.getInputStream();
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}

		try {
			return JavaConverter.deserialize(is);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		finally {
			if (closeStream) {
				try {
					IOUtil.close(is);
				}
				catch (IOException e) {
					throw Caster.toPageException(e);
				}
			}
		}
	}

}