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
 * Implements the CFML Function structfind
 */
package lucee.runtime.functions.struct;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;

public final class StructFind extends BIF {

	private static final long serialVersionUID = 6251275814429295997L;

	public static Object call(PageContext pc, Struct struct, String key) throws PageException {
		return struct.get(KeyImpl.init(key));
	}

	public static Object call(PageContext pc, Struct struct, Collection.Key key) throws PageException {
		return struct.get(key);
	}

	public static Object call(PageContext pc, Struct struct, String key, Object defaultValue) throws PageException {

		return struct.get(Caster.toKey(key), defaultValue);
	}

	public static Object call(PageContext pc, Struct struct, Collection.Key key, Object defaultValue) {

		return struct.get(key, defaultValue);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), Caster.toKey(args[1]), args[2]);
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), Caster.toKey(args[1]));
		throw new FunctionException(pc, "StructFind", 2, 3, args.length);
	}
}