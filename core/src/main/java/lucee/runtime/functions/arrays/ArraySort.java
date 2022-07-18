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
 * Implements the CFML Function arraysort
 */
package lucee.runtime.functions.arrays;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lucee.runtime.PageContext;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Closure;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ArrayUtil;

public final class ArraySort extends BIF {

	private static final long serialVersionUID = -747941236369495141L;

	public static boolean call(PageContext pc, Object objArr, Object sortTypeOrClosure) throws PageException {
		return call(pc, objArr, sortTypeOrClosure, "asc", false);
	}

	public static boolean call(PageContext pc, Object objArr, Object sortTypeOrClosure, String sortorder) throws PageException {
		return call(pc, objArr, sortTypeOrClosure, sortorder, false);
	}

	public static boolean call(PageContext pc, Object objArr, Object sortTypeOrClosure, String sortorder, boolean localeSensitive) throws PageException {

		// Comparator
		Comparator comp;
		if (sortTypeOrClosure instanceof UDF) comp = new UDFComparator(pc, (UDF) sortTypeOrClosure);
		else comp = ArrayUtil.toComparator(pc, Caster.toString(sortTypeOrClosure), sortorder, localeSensitive);

		// we always need to convert the original object, because we do not return the result
		if (objArr instanceof Array) ((Array) objArr).sortIt(comp);
		else if (objArr instanceof List) Collections.sort((List) objArr, comp);
		else if (objArr instanceof Object[]) Arrays.sort((Object[]) objArr, comp);
		// else if(objArr instanceof boolean[]) Arrays.sort((boolean[])objArr);
		else if (objArr instanceof byte[]) Arrays.sort((byte[]) objArr);
		else if (objArr instanceof char[]) Arrays.sort((char[]) objArr);
		else if (objArr instanceof short[]) Arrays.sort((short[]) objArr);
		else if (objArr instanceof int[]) Arrays.sort((int[]) objArr);
		else if (objArr instanceof long[]) Arrays.sort((long[]) objArr);
		else if (objArr instanceof float[]) Arrays.sort((float[]) objArr);
		else if (objArr instanceof double[]) Arrays.sort((double[]) objArr);
		else throw new FunctionException(pc, "ArraySort", 1, "array", "cannot sort object from type [" + Caster.toTypeName(objArr) + "]");

		return true;
	}

	// used for member function
	public static boolean call(PageContext pc, Array array, Object sortTypeOrClosure) throws PageException {
		return call(pc, array, sortTypeOrClosure, "asc", false);
	}

	public static boolean call(PageContext pc, Array array, Object sortTypeOrClosure, String sortorder) throws PageException {
		return call(pc, array, sortTypeOrClosure, sortorder, false);
	}

	public static boolean call(PageContext pc, Array arr, Object sortTypeOrClosure, String sortorder, boolean localeSensitive) throws PageException {
		// Comparator
		Comparator comp;
		if (sortTypeOrClosure instanceof UDF) comp = new UDFComparator(pc, (UDF) sortTypeOrClosure);
		else comp = ArrayUtil.toComparator(pc, Caster.toString(sortTypeOrClosure), sortorder, localeSensitive);

		arr.sortIt(comp);
		return true;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toArray(args[0]), args[1]);
		else if (args.length == 3) return call(pc, Caster.toArray(args[0]), args[1], Caster.toString(args[2]));
		else if (args.length == 4) return call(pc, Caster.toArray(args[0]), args[1], Caster.toString(args[2]), Caster.toBooleanValue(args[3]));
		else throw new FunctionException(pc, "ArraySort", 2, 4, args.length);
	}
}

class UDFComparator implements Comparator<Object> {

	private UDF udf;
	private Object[] args = new Object[2];
	private PageContext pc;

	public UDFComparator(PageContext pc, UDF udf) {
		this.pc = pc;
		this.udf = udf;
	}

	@Override
	public int compare(Object oLeft, Object oRight) {
		try {
			args[0] = oLeft;
			args[1] = oRight;
			Object res = udf.call(pc, args, false);
			Integer i = Caster.toInteger(res, null);
			if (i == null) throw new FunctionException(pc, "ArraySort", 2, "function",
					"return value of the " + (udf instanceof Closure ? "closure" : "function [" + udf.getFunctionName() + "]") + " cannot be casted to an integer.",
					CasterException.createMessage(res, "integer"));
			return i.intValue();
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

}