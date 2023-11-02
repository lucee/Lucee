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
 * Implements the CFML Function tobinary
 */
package lucee.runtime.functions.other;

import java.nio.charset.Charset;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class ToBinary implements Function {

	private static final long serialVersionUID = 4541724601337401920L;

	public static byte[] call(PageContext pc, Object data) throws PageException {
		return call(pc, data, null);
	}

	public static byte[] call(PageContext pc, Object data, String charset) throws PageException {
		if (!StringUtil.isEmpty(charset)) {
			charset = charset.trim().toLowerCase();
			Charset cs;
			if ("web".equalsIgnoreCase(charset)) cs = pc.getWebCharset();
			if ("resource".equalsIgnoreCase(charset)) cs = ((PageContextImpl) pc).getResourceCharset();
			else cs = CharsetUtil.toCharset(charset);

			String str = Caster.toString(data);
			return str.getBytes(cs);
		}
		return Caster.toBinary(data);
	}
}