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
 * Implements the CFML Function arrayToStruct
 */
package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public final class ArrayToStruct extends BIF {

	private static final long serialVersionUID = 2050803318757965798L;

	public static Struct call(PageContext pc, Array arr, boolean valueAsKey) throws PageException {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		int[] keys = arr.intKeys();
		for (int i = 0; i < keys.length; i++) {
			int key = keys[i];
			if (valueAsKey)
				sct.set(KeyImpl.toKey(arr.getE(key) + ""), key);
			else
				sct.set(KeyImpl.toKey(key + ""), arr.getE(key));
		}

		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toArray(args[0]), false);
		else if (args.length == 2) return call(pc, Caster.toArray(args[0]), Caster.toBooleanValue(args[1]));
		else throw new FunctionException(pc, "ArrayToStruct", 1, 2, args.length);
	}
}