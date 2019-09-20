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
 * Implements the CFML Function structkeyexists
 */
package lucee.runtime.functions.struct;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.query.QueryColumnExists;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.CollectionStruct;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.util.StructSupport;

public final class StructKeyExists extends BIF {

	private static final long serialVersionUID = 7659087310641834209L;

	public static boolean call(PageContext pc, lucee.runtime.type.Struct struct, String key) {
		return call(pc, struct, KeyImpl.init(key));
	}

	public static boolean call(PageContext pc, lucee.runtime.type.Struct struct, Collection.Key key) {
		if (struct instanceof CollectionStruct) {
			Collection c = ((CollectionStruct) struct).getCollection();
			if (c instanceof Query) {
				return QueryColumnExists.call(pc, (Query) c, key);
			}
		}
		if (struct instanceof StructSupport) { // FUTURE make available in Struct
			if (!((StructSupport) struct).containsKey(pc, key)) return false;
		}
		else {
			if (!struct.containsKey(key)) return false;
		}
		if (NullSupportHelper.full(pc)) return true;
		return struct.get(key, null) != null;// do not change, this has do be this way
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), Caster.toKey(args[1]));

		throw new FunctionException(pc, "StructKeyExists", 2, 2, args.length);
	}
}