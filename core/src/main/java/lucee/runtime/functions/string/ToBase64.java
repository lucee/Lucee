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
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.coder.Base64Util;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import java.io.UnsupportedEncodingException;

/**
 * Implements the CFML Function toBase64()
 */
public final class ToBase64 implements Function {
	/**
	 * @param pc
	 * @param object
	 * @return base64 value as string
	 * @throws PageException
	 */
	public static String call(PageContext pc, Object object) throws PageException {
		return call(pc, object, ReqRspUtil.getCharacterEncoding(pc, pc.getHttpServletResponse()).name());
	}

	/**
	 * @param pc
	 * @param object
	 * @param encoding
	 * @return base 64 value as string
	 * @throws PageException
	 */
	public static String call(PageContext pc, Object object, String encoding) throws PageException {
		if (Decision.isBoolean(encoding)) {
			return call(pc, object, ReqRspUtil.getCharacterEncoding(pc, pc.getHttpServletResponse()).name(), Caster.toBoolean(encoding));
		}

		return Caster.toBase64(object, (String)encoding);
	}

	/**
	 * @param pc
	 * @param object
	 * @param encoding
	 * @return base 64 value as string
	 * @throws PageException
	 */
	public static String call(PageContext pc, Object object, String encoding, Boolean urlSafe) throws PageException {
		if (!urlSafe) return Caster.toBase64(object, encoding);

		byte[] barr;
		if (object instanceof CharSequence) {
			try {
				barr = object.toString().getBytes(encoding);
			}
			catch (UnsupportedEncodingException ex) {
				throw Caster.toPageException(ex);
			}
		}
		else {
			barr = Caster.toBinary(object);
		}

		return Base64Util.base64EncodeToString(barr, urlSafe);
	}
}