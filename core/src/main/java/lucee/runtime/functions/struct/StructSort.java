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
 * Implements the CFML Function structsort
 */
package lucee.runtime.functions.struct;

import java.util.Arrays;
import java.util.Comparator;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.comparator.ExceptionComparator;
import lucee.runtime.type.comparator.NumberSortRegisterComparator;
import lucee.runtime.type.comparator.SortRegister;
import lucee.runtime.type.comparator.SortRegisterComparator;
import lucee.runtime.type.util.CollectionUtil;

public final class StructSort extends BIF {
	private static final long serialVersionUID = -7945612992641626477L;

	public static Array call(PageContext pc, Struct base) throws PageException {
		return call(pc, base, "text", "asc", null);
	}

	public static Array call(PageContext pc, Struct base, Object sortTypeOrSortFunc) throws PageException {
		if (Decision.isSimpleValue(sortTypeOrSortFunc)) return call(pc, base, Caster.toString(sortTypeOrSortFunc), "asc", null);
		return _call(pc, base, Caster.toFunction(sortTypeOrSortFunc));
	}

	public static Array call(PageContext pc, Struct base, Object sortType, String sortOrder) throws PageException {
		return call(pc, base, sortType, sortOrder, null);
	}

	public static Array call(PageContext pc, Struct base, Object sortType, String sortOrder, String pathToSubElement) throws PageException {
		return ((Array) _call(pc, "array", base, Caster.toString(sortType), sortOrder, pathToSubElement));
	}

	public static Object _call(PageContext pc, String type, Struct base, String sortType, String sortOrder, String pathToSubElement) throws PageException {

		boolean isAsc = true;
		PageException ee = null;
		if (sortOrder.equalsIgnoreCase("asc")) isAsc = true;
		else if (sortOrder.equalsIgnoreCase("desc")) isAsc = false;
		else throw new ExpressionException("invalid sort order type [" + sortOrder + "], sort order types are [asc and desc]");

		Collection.Key[] keys = CollectionUtil.keys(pc, base);
		SortRegister[] arr = new SortRegister[keys.length];
		boolean hasSubDef = pathToSubElement != null;
		for (int i = 0; i < keys.length; i++) {
			Object value = base.get(keys[i], null);

			if (hasSubDef) {
				value = VariableInterpreter.getVariable(pc, Caster.toCollection(value), pathToSubElement);
			}
			arr[i] = new SortRegister(i, value);
		}

		ExceptionComparator comp = null;
		if (sortType.equalsIgnoreCase("text")) comp = new SortRegisterComparator(pc, isAsc, false, false);
		else if (sortType.equalsIgnoreCase("textnocase")) comp = new SortRegisterComparator(pc, isAsc, true, false);
		else if (sortType.equalsIgnoreCase("numeric")) comp = new NumberSortRegisterComparator(isAsc);
		else {
			throw new ExpressionException("invalid sort type [" + sortType + "], sort types are [text, textNoCase, numeric]");
		}

		Arrays.sort(arr, 0, arr.length, comp);
		ee = comp.getPageException();

		if (ee != null) {
			throw ee;
		}

		Array rtn = new ArrayImpl();
		for (int i = 0; i < arr.length; i++) {
			rtn.append(keys[arr[i].getOldPosition()].getString());
		}
		return rtn;
	}

	public static Array _call(PageContext pc, Struct base, UDF sortFunc) throws PageException {
		Collection.Key[] keys = CollectionUtil.keys(pc, base);
		SortRegister[] arr = new SortRegister[keys.length];

		for (int i = 0; i < keys.length; i++) {
			arr[i] = new SortRegister(i, keys[i].toString());
		}

		Arrays.sort(arr, new StructComparator(pc, sortFunc));

		Array rtn = new ArrayImpl();
		for (int i = 0; i < arr.length; i++) {
			rtn.append(keys[arr[i].getOldPosition()].getString());
		}
		return rtn;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 4) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]));
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), args[1]);
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "StructSort", 1, 4, args.length);
	}

	public static class StructComparator implements Comparator<SortRegister> {

		private PageContext pc;
		private final UDF udf;

		public StructComparator(PageContext pc, UDF udf) {
			this.pc = pc;
			this.udf = udf;
		}

		@Override
		public int compare(SortRegister left, SortRegister right) {
			try {
				return Caster.toIntValue(udf.call(pc, new Object[] { left.getValue(), right.getValue() }, true));
			}
			catch (PageException pe) {
				throw new PageRuntimeException(pe);
			}
		}
	}
}