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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public class FileRead {

	public static Object call(PageContext pc, Object path) throws PageException {
		return _call(pc, Caster.toResource(pc, path, true), ((PageContextImpl) pc).getResourceCharset().name());
	}

	public static Object call(PageContext pc, Object obj, Object charsetOrSize) throws PageException {
		if (charsetOrSize == null) return call(pc, obj);

		if (obj instanceof FileStreamWrapper) {
			return _call((FileStreamWrapper) obj, Caster.toIntValue(charsetOrSize));
		}
		Resource res = Caster.toResource(pc, obj, true);
		String charset = Caster.toString(charsetOrSize);
		if (Decision.isInteger(charset)) {
			charset = ((PageContextImpl) pc).getResourceCharset().name();
			return _call(pc, res, charset, Caster.toIntValue(charset));
		}

		return _call(pc, res, charset);
	}

	private static Object _call(FileStreamWrapper fs, int size) throws PageException {
		try {
			return fs.read(size);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	private static Object _call(PageContext pc, Resource res, String charset) throws PageException {
		pc.getConfig().getSecurityManager().checkFileLocation(res);
		try {
			return IOUtil.toString(res, charset);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	private static Object _call(PageContext pc, Resource res, String charset, int size) throws PageException {
		pc.getConfig().getSecurityManager().checkFileLocation(res);

		InputStream is = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			is = res.getInputStream();
			IOUtil.copy(is, baos, 0, size);
			return new String(baos.toByteArray(), charset);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		finally {
			try {
				IOUtil.close(is);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}

		// TODO Auto-generated method stub
	}

}