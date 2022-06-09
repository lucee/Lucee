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
 * Implements the CFML Function listrest
 */
package lucee.runtime.functions.list;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ListUtil;

public final class ListRemoveDuplicates extends BIF {

	private static final long serialVersionUID = -6596215135126751629L;

	public static String call(PageContext pc, String list) throws PageException {
		return call(pc, list, ",", false);
	}

	public static String call(PageContext pc, String list, String delimiter) throws PageException {
		return call(pc, list, delimiter, false);
	}

	public static String call(PageContext pc, String list, String delimiter, boolean ignoreCase) throws PageException {
		if (list == null) return "";
		if (delimiter == null) delimiter = ",";
		Array array = ListUtil.listToArrayRemoveEmpty(list, delimiter);

		Set<String> existing;
		if (ignoreCase) existing = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		else existing = new HashSet<String>();

		StringBuilder sb = new StringBuilder();
		// Key[] keys = array.keys();
		Iterator<Object> it = array.valueIterator();
		String value;

		while (it.hasNext()) {

			value = Caster.toString(it.next());

			if (!existing.contains(value)) {
				if (sb.length() > 0) sb.append(delimiter);

				sb.append(value);
				existing.add(value);
			}
		}

		return sb.toString();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]));

		throw new FunctionException(pc, "ListRemoveDuplicates", 1, 3, args.length);
	}
}