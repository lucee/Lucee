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
 * Implements the CFML Function decrypt
 */
package lucee.runtime.functions.other;

import lucee.runtime.PageContext;
import lucee.runtime.crypt.CFMXCompat;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class DecryptBinary implements Function {

	public static Object call(PageContext pc, Object oBytes, String key) throws PageException {
		return call(pc, Caster.toBinary(oBytes), key, CFMXCompat.ALGORITHM_NAME);
	}

	public static Object call(PageContext pc, Object oBytes, String key, String algorithm) throws PageException {
		return Decrypt.invoke(Caster.toBinary(oBytes), key, algorithm, null, 0);
	}

	public static Object call(PageContext pc, Object oBytes, String key, String algorithm, Object ivOrSalt) throws PageException {
		return Decrypt.invoke(Caster.toBinary(oBytes), key, algorithm, Caster.toBinary(ivOrSalt), 0);
	}

	public static Object call(PageContext pc, Object oBytes, String key, String algorithm, Object ivOrSalt, double iterations) throws PageException {
		return Decrypt.invoke(Caster.toBinary(oBytes), key, algorithm, Caster.toBinary(ivOrSalt), Caster.toInteger(iterations));
	}
}