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
 * Implements the CFML Function tostring
 */
package lucee.runtime.functions.string;

import java.nio.charset.Charset;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;

public final class ToString implements Function {
	public static String call(PageContext pc) {
		return "";
	}

	public static String call(PageContext pc, Object object) throws PageException {
		return call(pc, object, null);
	}

	public static String call(PageContext pc, Object object, String encoding) throws PageException {
		Charset charset;
		if (StringUtil.isEmpty(encoding)) {
			charset = ReqRspUtil.getCharacterEncoding(pc, pc.getResponse());
		}
		else charset = CharsetUtil.toCharset(encoding);

		if (object instanceof byte[]) {
			if (charset != null) return new String((byte[]) object, charset);
			return new String((byte[]) object);
		}
		return Caster.toString(object);
	}
}