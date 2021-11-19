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

import javax.crypto.KeyGenerator;

import lucee.runtime.PageContext;
import lucee.runtime.coder.Coder;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

/**
 * Generates a Secret Key
 */
public final class GenerateSecretKey implements Function {

	public static String call(PageContext pc, String algorithm) throws PageException {
		return call(pc, algorithm, 0);
	}

	public static String call(PageContext pc, String algorithm, double keySize) throws PageException {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm.toUpperCase());
			if (keySize > 0) keyGenerator.init(Caster.toIntValue(keySize));
			return Coder.encode(Coder.ENCODING_BASE64, keyGenerator.generateKey().getEncoded());
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

}