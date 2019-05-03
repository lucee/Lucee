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
 * Implements the CFML Function structappend
 */
package lucee.runtime.functions.struct;

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;

public final class StructAppend extends BIF {

	private static final long serialVersionUID = 6131382324325758447L;

	public static boolean call(PageContext pc, Struct struct1, Struct struct2) throws PageException {
		return call(pc, struct1, struct2, true);
	}

	public static boolean call(PageContext pc, Struct struct1, Struct struct2, boolean overwrite) throws PageException {
		Iterator<Key> it = struct2.keyIterator();
		Key key;
		while (it.hasNext()) {
			key = KeyImpl.toKey(it.next());
			if (overwrite || struct1.get(key, null) == null) struct1.setEL(key, struct2.get(key, null));
		}
		return true;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), Caster.toStruct(args[1]), Caster.toBooleanValue(args[2]));
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), Caster.toStruct(args[1]));
		throw new FunctionException(pc, "StructAppend", 2, 3, args.length);
	}

}