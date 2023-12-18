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

import java.util.concurrent.atomic.AtomicLong;

import lucee.runtime.PageContext;
import lucee.runtime.coder.Base64Util;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;

public final class CreateUniqueId extends BIF {

	private static AtomicLong counter = new AtomicLong(0);

	/**
	 * method to invoke the function
	 * 
	 * @param pc
	 * @return UUID String
	 */
	public static String call(PageContext pc) {
		return Base64Util.createUuidAsBase64();
	}

	public static String call(PageContext pc, String type) {
		if ("counter".equalsIgnoreCase(type)) return invoke();
		return Base64Util.createUuidAsBase64();
	}

	public static String invoke() {
		long value = counter.incrementAndGet();
		if (value < 0) counter.set(1);
		return Long.toString(value, Character.MAX_RADIX);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return invoke();
		if (args.length == 1) return call(pc, (String) args[0]);

		throw new FunctionException(pc, CreateUniqueId.class.getSimpleName(), 0, 1, args.length);
	}
}