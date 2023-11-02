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
 * Implements the CFML Function structkeylist
 */
package lucee.runtime.functions.struct;

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;

public final class StructKeyList extends BIF {

	private static final long serialVersionUID = 6256709521354910213L;

	public static String call(PageContext pc, Struct struct) {
		return call(pc, struct, ",");// KeyImpl.toUpperCaseList(struct.keys(), ",");
	}

	public static String call(PageContext pc, Struct struct, String delimiter) {
		// return KeyImpl.toList(CollectionUtil.keys(struct), delimiter);

		if (struct == null) return "";
		Iterator<Key> it = struct.keyIterator();

		// first
		if (!it.hasNext()) return "";
		StringBuilder sb = new StringBuilder();
		sb.append(it.next().getString());

		// rest
		if (delimiter.length() == 1) {
			char c = delimiter.charAt(0);
			while (it.hasNext()) {
				sb.append(c);
				sb.append(it.next().getString());
			}
		}
		else {
			while (it.hasNext()) {
				sb.append(delimiter);
				sb.append(it.next().getString());
			}
		}

		return sb.toString();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]));
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "StructKeyList", 1, 2, args.length);
	}
}