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
package lucee.runtime.functions.list;

import java.io.IOException;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.util.ListParser;
import lucee.runtime.type.util.ListParserConsumer;

/**
 * Implements the CFML Function listqualify
 */
public final class ListQualifiedToArray extends BIF {

	private static final long serialVersionUID = 8140873337224497863L;

	public static Array call(PageContext pc, String list) throws PageException {
		return _call(pc, list, ',', '"', false, false);
	}

	public static Array call(PageContext pc, String list, String delimiter) throws PageException {
		return _call(pc, list, toDelimeter(pc, delimiter), '"', false, false);
	}

	public static Array call(PageContext pc, String list, String delimiter, String qualifier) throws PageException {
		return _call(pc, list, toDelimeter(pc, delimiter), toQualifier(pc, qualifier), false, false);
	}

	public static Array call(PageContext pc, String list, String delimiter, String qualifier, boolean qualifierRequired) throws PageException {
		return _call(pc, list, toDelimeter(pc, delimiter), toQualifier(pc, qualifier), qualifierRequired, false);
	}

	public static Array call(PageContext pc, String list, String delimiter, String qualifier, boolean qualifierRequired, boolean includeEmptyFields) throws PageException {
		return _call(pc, list, toDelimeter(pc, delimiter), toQualifier(pc, qualifier), qualifierRequired, includeEmptyFields);
	}

	private static Array _call(PageContext pc, String list, char del, char qual, boolean qualifierRequired, boolean includeEmptyFields) throws PageException {
		try {
			ArrayConsumer consumer = new ArrayConsumer();
			new ListParser(list, consumer, del, qual, !includeEmptyFields, qualifierRequired).parse();
			return consumer.getArray();
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 5) return _call(pc, Caster.toString(args[0]), toDelimeter(pc, Caster.toString(args[1])), toQualifier(pc, Caster.toString(args[2])),
				Caster.toBooleanValue(args[3]), Caster.toBooleanValue(args[4]));
		if (args.length == 4)
			return _call(pc, Caster.toString(args[0]), toDelimeter(pc, Caster.toString(args[1])), toQualifier(pc, Caster.toString(args[2])), Caster.toBooleanValue(args[3]), false);
		if (args.length == 3) return _call(pc, Caster.toString(args[0]), toDelimeter(pc, Caster.toString(args[1])), toQualifier(pc, Caster.toString(args[2])), false, false);
		if (args.length == 2) return _call(pc, Caster.toString(args[0]), toDelimeter(pc, Caster.toString(args[1])), '"', false, false);
		if (args.length == 1) return _call(pc, Caster.toString(args[0]), ',', '"', false, false);

		throw new FunctionException(pc, "ListQualifiedToArray", 1, 5, args.length);
	}

	private static char toDelimeter(PageContext pc, String delimeter) throws FunctionException {
		if (delimeter == null || StringUtil.isEmpty(delimeter)) return ',';
		if (delimeter.length() != 1)
			throw new FunctionException(pc, "ListQualifiedToArray", 2, "delimeter", "qualifier can only be a single character, now is [" + delimeter + "]");
		return delimeter.charAt(0);
	}

	private static char toQualifier(PageContext pc, String qualifier) throws FunctionException {
		if (qualifier == null || StringUtil.isEmpty(qualifier)) return '"';
		if (qualifier.length() != 1)
			throw new FunctionException(pc, "ListQualifiedToArray", 3, "qualifier", "qualifier can only be a single character, now is [" + qualifier + "]");
		return qualifier.charAt(0);
	}

	private static class ArrayConsumer implements ListParserConsumer {

		private Array array = new ArrayImpl();

		@Override
		public void entry(String str) {
			array.appendEL(str);
		}

		public Array getArray() {
			return array;
		}
	}
}