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
package lucee.commons.lang;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import lucee.commons.digest.Hash;

/**
 * class to create a MD5 sum
 */
public final class Md5 {

	/**
	 * @deprecated use instead <code>Hash.md5(String)</code> return md5 from string as string
	 * @param str plain string to get md5 from
	 * @return md5 from string
	 * @throws IOException
	 */
	@Deprecated
	public static String getDigestAsString(String str) throws IOException {
		try {
			return Hash.md5(str);
		}
		catch (NoSuchAlgorithmException e) {
			throw ExceptionUtil.toIOException(e);
		}
		// return new Md5 (str,"UTF-8").getDigest();
	}
}