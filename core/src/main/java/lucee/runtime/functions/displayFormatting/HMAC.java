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
package lucee.runtime.functions.displayFormatting;

import java.nio.charset.Charset;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lucee.commons.digest.MD5;
import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public class HMAC implements Function {

	private static final long serialVersionUID = -1999122154087043893L;

	public static String call(PageContext pc, Object oMessage, Object oKey) throws PageException {
		return call(pc, oMessage, oKey, null, null);
	}

	public static String call(PageContext pc, Object oMessage, Object oKey, String algorithm) throws PageException {
		return call(pc, oMessage, oKey, algorithm, null);
	}

	public static String call(PageContext pc, Object oMessage, Object oKey, String algorithm, String charset) throws PageException {
		// charset
		Charset cs;
		if (StringUtil.isEmpty(charset, true)) cs = pc.getWebCharset();
		else cs = CharsetUtil.toCharset(charset);

		// message
		byte[] msg = toBinary(oMessage, cs);

		// message
		byte[] key = toBinary(oKey, cs);

		// algorithm
		if (StringUtil.isEmpty(algorithm, true)) algorithm = "HmacMD5";

		SecretKey sk = new SecretKeySpec(key, algorithm);
		try {
			Mac mac = Mac.getInstance(algorithm);
			mac.init(sk);
			mac.reset();
			mac.update(msg);
			msg = mac.doFinal();
			return MD5.stringify(msg).toUpperCase();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static byte[] toBinary(Object obj, Charset cs) throws PageException {
		if (Decision.isBinary(obj)) {
			return Caster.toBinary(obj);
		}
		return Caster.toString(obj).getBytes(cs);
	}
}