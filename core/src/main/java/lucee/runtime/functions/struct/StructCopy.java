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
 * Implements the CFML Function structcopy
 */
package lucee.runtime.functions.struct;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

public final class StructCopy extends BIF {

	private static final long serialVersionUID = 4395420120630859733L;

	public static Object call(PageContext pc, Struct src) throws PageException {

		Collection trg = (Collection) Duplicator.duplicate(src, false);
		Collection.Key[] keys = CollectionUtil.keys(trg);
		Collection.Key key;
		Object o;
		for (int i = 0; i < keys.length; i++) {
			key = keys[i];
			o = src.get(key, null);
			if (o instanceof Array) trg.set(key, Duplicator.duplicate(o, false));
		}
		return trg;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "StructCopy", 1, 1, args.length);
	}
}